(ns clj-daemon.classloader
  "Classloader functions with code borrowed from Classlojure snapshot."
  (:require [clojure.java.io :as io])
  (:import [java.net URL URLClassLoader]))

(defn url-classloader [& urls]
  (URLClassLoader.
   (into-array URL (map io/as-url urls))
   (.getParent (.getClassLoader clojure.lang.RT))))

(defmacro with-classloader [cl & body]
  `(binding [*use-context-classloader* true]
     (let [cl# (.getContextClassLoader (Thread/currentThread))]
       (try (.setContextClassLoader (Thread/currentThread) ~cl)
            ~@body
            (finally
             (.setContextClassLoader (Thread/currentThread) cl#))))))

(defn- invoke-in [cl class-method signature & params]
  (let [class     (.loadClass cl (namespace class-method))
        signature (into-array Class (or signature []))
        method    (.getDeclaredMethod class (name class-method) signature)]
    (.invoke method class (into-array Object params))))

(defn eval-string
  "Eval the given string in a separate classloader."
  [cl string]
  (with-classloader cl
    (let [form   (invoke-in cl 'clojure.lang.RT/readString [String] string)
          result (invoke-in cl 'clojure.lang.Compiler/eval [Object] form)]
      (invoke-in cl 'clojure.lang.RT/printString [Object] result))))
