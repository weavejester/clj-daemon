(ns clj-daemon.main
  (:use [clj-daemon.server :only (server)]
        [net.tcp.server :only (start running?)])
  (:require [clj-json.core :as json])
  (:gen-class))

(def config (atom {}))

(defn load-config! [file]
  (reset! config
          (json/parse-string (slurp file) true)))

(defn -main [config-file]
  (load-config! config-file)
  (start server)
  (while (running? server)
    (Thread/sleep 1000)))
