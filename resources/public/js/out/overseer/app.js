// Compiled by ClojureScript 0.0-3291 {}
goog.provide('overseer.app');
goog.require('cljs.core');
goog.require('overseer.core');
goog.require('figwheel.client');
goog.require('weasel.repl');
cljs.core.enable_console_print_BANG_.call(null);
figwheel.client.watch_and_reload.call(null,new cljs.core.Keyword(null,"websocket-url","websocket-url",-490444938),"ws://localhost:3449/figwheel-ws",new cljs.core.Keyword(null,"jsload-callback","jsload-callback",-1949628369),overseer.core.mount_components);
weasel.repl.connect.call(null,"ws://localhost:9001",new cljs.core.Keyword(null,"verbose","verbose",1694226060),true);
overseer.core.init_BANG_.call(null);

//# sourceMappingURL=app.js.map