(ns cljd.server
  (:use [net.tcp.server :only (tcp-server wrap-io start)])
  (:import java.io.PushbackReader))

(defn wrap-clojure [handler]
  (wrap-io
   (fn [reader writer]
     (binding [*in* (PushbackReader. reader), *out* writer]
       (handler)))))

(defn handler []
  (prn (read)))

(def server
  (tcp-server
   :handler (wrap-clojure handler)
   :host "127.0.0.1"
   :port 8000))

(defn -main []
  (start server))
