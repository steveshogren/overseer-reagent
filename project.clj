(defproject overseer "0.1.0-SNAPSHOT"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.7.0-RC1"]
                 [selmer "0.8.2"]
                 [com.taoensso/timbre "3.4.0"]
                 [com.taoensso/tower "3.0.2"]
                 [markdown-clj "0.9.66"]
                 [environ "1.0.0"]
                 [compojure "1.3.4"]
                 [org.clojure/core.memoize "0.5.6"]
                 [ring/ring-defaults "0.1.5"]
                 [ring/ring-session-timeout "0.1.0"]
                 [metosin/ring-middleware-format "0.6.0"]
                 [metosin/ring-http-response "0.6.2"]
                 [bouncer "0.3.2"]
                 [prone "0.8.2"]
                 [org.clojure/tools.nrepl "0.2.10"]
                 [ring-server "0.4.0"]
                 [ragtime "0.3.8"]
                 [org.clojure/java.jdbc "0.3.7"]
                 [instaparse "1.4.0"]
                 [yesql "0.5.0-rc2"]
                 [org.postgresql/postgresql "9.3-1102-jdbc41"]
                 [org.clojure/clojurescript "0.0-3291" :scope "provided"]
                 [org.clojure/tools.reader "0.9.2"]
                 [reagent "0.5.0"]
                 [cljsjs/react "0.13.3-0"]
                 [reagent-forms "0.5.1"]
                 [reagent-utils "0.1.4"]
                 [secretary "1.2.3"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [cljs-ajax "0.3.11"]
                 [org.clojure/data.json "0.2.5"]
                 [com.cemerick/friend "0.2.1" :exclusions [xerces/xercesImpl]]
                 [sonian/carica "1.1.0" :exclusions [[cheshire]]]
                 [ring/ring-json "0.3.1"]
                 [jdbc-ring-session "0.2"]
                 [clj-time "0.8.0"]
                 [com.cemerick/url "0.1.1"]
                 [org.clojure/tools.trace "0.7.8"]
                 [heroku-database-url-to-jdbc "0.2.2"]
                 [prismatic/schema "0.4.2"]
                 ]

  :min-lein-version "2.0.0"
  :uberjar-name "overseer.jar"
  :jvm-opts ["-server"]

;;enable to start the nREPL server when the application launches
;:env {:repl-port 7001}

  :main overseer.core

  :plugins [[lein-ring "0.9.1"]
            [lein-environ "1.0.0"]
            [lein-ancient "0.6.5"]
            [ragtime/ragtime.lein "0.3.8"]
            [lein-cljsbuild "1.0.6"]]
  

  
  :ring {:handler overseer.handler/app
         :init    overseer.handler/init
         :destroy overseer.handler/destroy
         :uberwar-name "overseer.war"}

  
  :ragtime
  {:migrations ragtime.sql.files/migrations
   :database
   "jdbc:postgresql://localhost/overseer?user=db_user_name_here&password=db_user_password_here"}
  
  
  :clean-targets ^{:protect false} ["resources/public/js"]
  
  :cljsbuild
  {:builds
   {:app
    {:source-paths ["src-cljs"]
     :compiler
     {:pretty-print true
      :output-dir "resources/public/js/out"
      :output-to "resources/public/js/app.js"
      :externs ["react/externs/react.js"]
      :optimizations :none}}}}
  
  
  :profiles
  {:uberjar {:omit-source true
             :env {:production true}
              :cljsbuild
              {:builds
               {:app
                {:source-paths ["env/prod/cljs"]
                 :compiler {:pretty-print false :optimizations :advanced}}}
               :jar true}
              :hooks [leiningen.cljsbuild] 
             
             :aot :all}
   :dev {:dependencies [[ring-mock "0.1.5"]
                        [ring/ring-devel "1.3.2"]
                        [pjstadig/humane-test-output "0.7.0"]
                        [weasel "0.6.0"]
                        [lein-figwheel "0.3.3"]
                        [org.clojure/tools.nrepl "0.2.10"]
                        [com.cemerick/piggieback "0.2.1"]]
         :source-paths ["env/dev/clj"]
         :plugins [[lein-figwheel "0.3.3"]]
          :cljsbuild
          {:builds
           {:app
            {:source-paths ["env/dev/cljs"] :compiler {:source-map true}}}} 
         
         :figwheel
         {:http-server-root "public"
          :server-port 3449
          :css-dirs ["resources/public/css"]
          :ring-handler overseer.handler/app}
         
         :repl-options {:init-ns overseer.core}
         :injections [(require 'pjstadig.humane-test-output)
                      (pjstadig.humane-test-output/activate!)]
         :env {:dev true}}})
