(ns clj-daemon.server
  (:use [net.tcp.server :only (tcp-server wrap-io start running?)]
        [clj-daemon.classloader :only (url-classloader eval-string)])
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

(defmacro catch-exceptions [write & body]
  `(try ~@body
        (catch Exception e#
          (~write {:exception (.getName (class e#))
                   :message   (.getMessage e#)}))))

(def create-classloader
  (memoize
   (fn [paths]
     (apply url-classloader paths))))

(defn handler [read-data write]
  (catch-exceptions write
    (let [loader (create-classloader (:classpath (read-data)))]
      (loop [data (read-data)]
        (catch-exceptions write
          (let [source (:source data)]
            (write {:return (eval-string loader source)})
            (recur (read-data))))))))

(def server
  (tcp-server
   :handler (wrap-json handler true)
   :host "127.0.0.1"
   :port 8000))

(defn -main []
  (start server)
  (while (running? server)
    (Thread/sleep 1000)))
