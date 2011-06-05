(ns clj-daemon.server
  (:use [net.tcp.server :only (tcp-server wrap-io start)])
  (:require [clj-json.core :as json]))

(defmacro tap [return & forms]
  `(let [return# ~return]
     ~@forms
     return#))

(defn wrap-json [handler & [keywords?]]
  (wrap-io
   (fn [reader writer]
     (let [input (ref (json/parsed-seq reader keywords?))
           read  #(dosync
                    (tap (first @input)
                         (alter input rest)))
           write #(doto writer
                    (.append (json/generate-string %))
                    (.append "\n")
                    (.flush))]
       (handler read write)))))

(defn handler [read write]
  (try
    (let [setup  (read)
          source (-> setup :source read-string)
          return (eval source)]
      (write {:return (pr-str return)}))
    (catch Exception e
      (write {:exception (.getName (class e))
              :message   (.getMessage e)}))))

(def server
  (tcp-server
   :handler (wrap-json handler true)
   :host "127.0.0.1"
   :port 8000))

(defn -main []
  (start server))
