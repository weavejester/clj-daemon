(ns cljd.server
  (:use [net.tcp.server :only (tcp-server wrap-io start)]))

(defn handler [reader writer]
  (.append writer "Hello World"))

(def server
  (tcp-server
   :handler (wrap-io handler)
   :host "127.0.0.1"
   :port 8000))

(defn -main []
  (start server))
