(ns overseer.middleware
  (:require [overseer.session :as session]
            [overseer.layout :refer [*servlet-context*]]
            [taoensso.timbre :as timbre]
            [environ.core :refer [env]]
            [overseer.db.db :as db]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [jdbc-ring-session.core :refer [jdbc-store]]
            [selmer.middleware :refer [wrap-error-page]]
            [prone.middleware :refer [wrap-exceptions]]
            [ring.util.response :refer [redirect]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [ring.middleware.session-timeout :refer [wrap-idle-session-timeout]]
            [ring.middleware.session.memory :refer [memory-store]]
            [ring.middleware.format :refer [wrap-restful-format]]

            [ring.middleware.json :refer [wrap-json-response wrap-json-body wrap-json-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.session :refer [wrap-session]]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])
            ))

(def users {"admin" {:username "admin"
                     :password (creds/hash-bcrypt (env :admin))
                     :roles #{::admin ::user}}
            "super" {:username "super"
                     :password (creds/hash-bcrypt (env :admin))
                     :roles #{::admin ::user ::super}}
            "user" {:username "user"
                    :password (creds/hash-bcrypt (env :userpass))
                    :roles #{::user}}})

(defn wrap-servlet-context [handler]
  (fn [request]
    (binding [*servlet-context*
              (if-let [context (:servlet-context request)]
                ;; If we're not inside a serlvet environment
                ;; (for example when using mock requests), then
                ;; .getContextPath might not exist
                (try (.getContextPath context)
                     (catch IllegalArgumentException _ context)))]
      (handler request))))

(defn wrap-internal-error [handler]
  (fn [req]
    (try
      (handler req)
      (catch Throwable t
        (timbre/error t)
        {:status 500
         :headers {"Content-Type" "text/html"}
         :body (-> "templates/error.html" io/resource slurp)}))))

(defn wrap-dev [handler]
  (if (env :dev)
    (-> handler
        wrap-error-page
        wrap-exceptions)
    handler))

(defn wrap-csrf [handler]
  (wrap-anti-forgery handler))

(defn wrap-formats [handler]
  (wrap-restful-format handler :formats [:json-kw :transit-json :transit-msgpack]))

(defn wrap-base [handler]
  (println "====================")
  (db/init-pg)
  (-> handler
      (friend/authenticate {:credential-fn (partial creds/bcrypt-credential-fn users)
                            :allow-anon? true
                            :login-uri "/login"
                            :default-landing-uri "/"
                            :workflows [(workflows/interactive-form)]})
      wrap-dev
      (wrap-idle-session-timeout
        {:timeout (* 3 365 24 3600)
         :timeout-response (redirect "/")})
      wrap-formats
      (wrap-defaults
        (-> site-defaults
            (assoc-in [:security :anti-forgery] false)
            (assoc-in  [:session :store]
                       (jdbc-store @db/pgdb)
                       #_(memory-store session/mem))))
      wrap-servlet-context
      wrap-internal-error
      wrap-keyword-params
      wrap-json-body
      wrap-json-params
      wrap-json-response
      ))
