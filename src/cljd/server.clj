(ns cljd.server
  (:use [net.tcp.server :only (tcp-server wrap-io start)])
  (:require [clj-json.core :as json]))

(defn wrap-json [handler & [keywords]]
  (wrap-io
   (fn [reader writer]
     (letfn [(write [data]
               (doto writer
                 (.append (json/generate-string data))
                 (.append "\n")
                 (.flush)))]
       (doseq [input (json/parsed-seq reader)]
         (handler input write))))))

(defn handler [input write]
  (write input))

(def server
  (tcp-server
   :handler (wrap-json handler)
   :host "127.0.0.1"
   :port 8000))

(defn -main []
  (start server))
