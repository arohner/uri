(ns cemerick.uri
  #?(:clj (:import (java.net URLEncoder URLDecoder)))
  #?(:cljs (:require-macros [clojure.core :refer [some-> some->>]]))
  (:require [pathetic.core :as pathetic]
            [clojure.string :as string]
            #?(:cljs [goog.Uri])))

#?(:clj
   (defn uri-encode
     [string]
     (some-> string str (URLEncoder/encode "UTF-8") (.replace "+" "%20"))))

#?(:cljs
   (defn uri-encode
     [string]
     (some-> string str (js/encodeURIComponent) (.replace "+" "%20"))))

(defn- query-map->query-seq [m]
  (mapcat (fn [[k v]]
            (cond
              (map? v) (map #(cons (name k) %) (query-map->query-seq v))
              :else [[(name k) (if (keyword? v) (name v) (str v))]]))
          m))

(defn- query-seq->simple-query-map [s]
  (map (fn [s]
         [(str (first s)
               (clojure.string/join (map #(str "[" % "]")
                                         (rest (butlast s)))))
          (last s)])
       s))

#?(:clj
   (defn uri-decode
     ([string] (uri-decode string "UTF-8"))
     ([string encoding]
      (some-> string str (URLDecoder/decode encoding)))))

#?(:cljs
   (defn uri-decode
     [string]
     (some-> string str (js/decodeURIComponent))))

(defn map->query
  [m]
  (some->> (seq m)
           (query-map->query-seq)
           (query-seq->simple-query-map)
           sort                     ; sorting makes testing a lot easier :-)
           (map (fn [[k v]]
                  [(uri-encode k)
                   "="
                   (uri-encode v)]))
           (interpose "&")
           flatten
           (apply str)))

(defn split-param [param]
  (->
   (string/split param #"=")
   (concat (repeat ""))
   (->>
    (take 2))))

(defn- unnest-query-map [m]
  (reduce-kv (fn [m k v]
               (let [[_ f r] (re-find #"^([^\[]+)((\[[^\]]+\])*)$" k)]
                 (assoc-in m
                           (cons f (map second (re-seq #"\[([^\]]+)\]" r)))
                           v)))
             {}
             m))

(defn query->map
  [qstr]
  (when (not (string/blank? qstr))
    (some->> (string/split qstr #"&")
      seq
      (mapcat split-param)
      (map uri-decode)
      (apply hash-map)
      (unnest-query-map))))

(defn- port-str
  [protocol port]
  (when (and (not= nil port)
             (not= -1 port)
             (not (and (== port 80) (= protocol "http")))
             (not (and (== port 443) (= protocol "https"))))
    (str ":" port)))

(defn- uri-creds
  [username password]
  (when username
    (str username ":" password)))

(defrecord URI
  [protocol username password host port path query anchor]
  Object
  (toString [this]
    (let [creds (uri-creds username password)]
      (str protocol "://"
           creds
           (when creds \@)
           host
           (port-str protocol port)
           path
           (when (seq query) (str \? (if (string? query)
                                       query
                                       (map->query query))))
           (when anchor (str \# anchor))))))

#?(:clj
   (defn- uri*
     [uri]
     (let [uri (java.net.URI. uri)
           [user pass] (string/split (or (.getUserInfo uri) "") #":" 2)]
       (URI. (.toLowerCase (.getScheme uri))
             (and (seq user) user)
             (and (seq pass) pass)
             (.getHost uri)
             (.getPort uri)
             (pathetic/normalize (.getPath uri))
             (query->map (.getQuery uri))
             (.getFragment uri)))))

#?(:cljs
   (defn translate-default
     [s old-default new-default]
     (if (= s old-default)
       new-default
       s)))

#?(:cljs
   (defn- uri*
     [uri]
     (let [uri (goog.Uri. uri)
           [user pass] (string/split (or (.getUserInfo uri) "") #":" 2)]
       (URI. (.getScheme uri)
             (and (seq user) user)
             (and (seq pass) pass)
             (.getDomain uri)
             (translate-default (.getPort uri) nil -1)
             (pathetic/normalize (.getPath uri))
             (query->map (translate-default (.getQuery uri) "" nil))
             (translate-default (.getFragment uri) "" nil)))))

(defn uri
  "Returns a new URI record for the given uri string(s).

   The first argument must be a base uri — either a complete uri string, or
   a pre-existing URI record instance that will serve as the basis for the new
   URI.  Any additional arguments must be strings, which are interpreted as
   relative paths that are successively resolved against the base uri's path
   to construct the final :path in the returned URI record. 

   This function does not perform any uri-encoding.  Use `uri-encode` to encode
   URI path segments as desired before passing them into this fn."
  ([uri]
    (if (instance? URI uri)
      uri
      (uri* uri)))
  ([base-uri & path-segments]
    (let [base-uri (if (instance? URI base-uri) base-uri (uri base-uri))]
      (assoc base-uri :path (pathetic/normalize (reduce pathetic/resolve
                                                        (:path base-uri)
                                                        path-segments))))))

