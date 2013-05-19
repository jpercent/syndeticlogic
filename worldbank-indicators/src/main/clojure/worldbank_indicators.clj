;; Copyright 2013 James Percent
;;
;;   Licensed under the Apache License, Version 2.0 (the "License");
;;   you may not use this file except in compliance with the License.
;;   You may obtain a copy of the License at
;;
;;      http://www.apache.org/licenses/LICENSE-2.0
;;
;;   Unless required by applicable law or agreed to in writing, software
;;   distributed under the License is distributed on an "AS IS" BASIS,
;;   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;;   See the License for the specific language governing permissions and
;;   limitations under the License.
(ns worldbank-indicators
  (:require [clj-http.client :as client])
  (:require [cheshire.core :refer :all :as json])
  (:require [clojure.repl :as repl])
  (:gen-class :name syndeticlogic.wb  :methods 
              [#^{:static true} [get_indicator_ids [] clojure.lang.PersistentVector]]))
;;  (:import [java.io StringReader BufferedReader]))

(def base-uri "http://api.worldbank.org/")
(def use-json "?format=json")

(defn throw-catch [f] 
  [(try (f)
     (catch ArithmeticException e "No dividing by zero!")
     (catch Exception e (.printStackTrace e) (str (.getMessage e)))
     (finally))])

(defn not-last-element [raw]
  (not (= (first raw) (last raw))))

(defn add-slash [path raw]
  (str path (first raw) "/"))

(defn make-path [raw path]
  (if (not-last-element raw)
    (recur (rest raw) (add-slash path raw))
    (str path (first raw))))

(defn construct-request [raw]
  (str base-uri (make-path raw "") use-json))
  
(defn make-request [raw]
  (client/get (construct-request raw)))

(defn get-catalog-sources []
  (make-request ["sources"]))

(defn get-countries []
  (make-request ["countries"]))

(defn get-country [country]
  (make-request ["countries" country]))

(defn get-body [get-content-fn] 
 ; (println "get content == " `(get-content-fn))
  (.get (get-content-fn) :body))

(defn get-content [get-content-fn] 
  (nth (json/parse-string (get-body get-content-fn)) 1))

(defn get-indicators []
  (get-content #(make-request ["indicators"])))

(defn map-id-to-name [{id "id" name "name"}]
  {id name})

(defn add-to-hash [[indicator & more] thehash]
  (conj thehash (map-id-to-name indicator)))
  
(defn build-hash [indicators thehash]
  (if (not-last-element indicators)
    (recur (rest indicators) (add-to-hash indicators thehash)) 
    thehash))

(defn get-indicator-ids []
  (throw-catch #(build-hash (get-indicators) {})))

(defn -get_indicator_ids []
  (get-indicator-ids))
  
(defn get-income-levels []
  (make-request ["incomeLevels"]))

(defn get-countries-with-incomelevel [income-level]
  (make-request ["incomeLevels" income-level "countries"]))
