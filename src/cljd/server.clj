(ns cljd.server
  (:use [net.tcp.server :only (tcp-server wrap-io start)])
  (:require [clj-json.core :as json]))

(defmacro tap [return & forms]
  `(let [return# ~return]
     ~@forms
     return#))

(defn wrap-json [handler & [keywords]]
  (wrap-io
   (fn [reader writer]
     (let [input (ref (json/parsed-seq reader))
           read  #(dosync
                    (tap (first @input)
                         (alter input rest)))
           write #(doto writer
                    (.append (json/generate-string %))
                    (.append "\n")
                    (.flush))]
       (handler read write)))))

(defn handler [read write]
  (write (read)))

(def server
  (tcp-server
   :handler (wrap-json handler)
   :host "127.0.0.1"
   :port 8000))

(defn -main []
  (start server))
