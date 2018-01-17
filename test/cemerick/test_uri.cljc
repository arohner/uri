(ns cemerick.test-uri
  #?(:clj (:import java.net.URI))
  #?(:clj (:use cemerick.uri
               clojure.test))
  #?(:cljs (:require-macros [cemerick.cljs.test :refer (are is deftest with-test run-tests testing)]))
  #?(:cljs (:use [cemerick.uri :only [uri map->query query->map map->URI]]))
  #?(:cljs (:require [cemerick.cljs.test :as t])))

(def uri-str (comp str uri))

(deftest test-map-to-query-str
  (are [x y] (= x (map->query y))
       "a=1&b=2&c=3" {:a 1 :b 2 :c 3}
       "a=1&b=2&c=3" {:a "1"  :b "2" :c "3"}
       "a=1&b=2" {"a" "1" "b" "2"}
       "a=" {"a" ""}))

(deftest uri-roundtripping
  (let [auri (uri "https://username:password@some.host.com/database?query=string")]
    (is (= "https://username:password@some.host.com/database?query=string" (str auri)))
    (is (== -1 (:port auri)))
    (is (= "username" (:username auri)))
    (is (= "password" (:password auri)))
    (is (= "https://username:password@some.host.com" (str (assoc auri :path nil :query nil))))))

(deftest uri-segments
  (is (= "http://localhost:5984/a/b" (uri-str "http://localhost:5984" "a" "b")))
  (is (= "http://localhost:5984/a/b/c" (uri-str "http://localhost:5984" "a" "b" "c")))
  (is (= "http://localhost:5984/a/b/c" (uri-str (uri "http://localhost:5984" "a") "b" "c"))))

(deftest port-normalization
  #?(:clj (is (== -1 (-> "https://foo" uri-str URI. .getPort))))
  (is (= "http://localhost" (uri-str "http://localhost")))
  (is (= "http://localhost" (uri-str "http://localhost:80")))
  (is (= "http://localhost:8080" (uri-str "http://localhost:8080")))
  (is (= "https://localhost" (uri-str "https://localhost")))
  (is (= "https://localhost" (uri-str "https://localhost:443")))
  (is (= "https://localhost:8443" (uri-str "https://localhost:8443")))
  (is (= "http://localhost" (str (map->URI {:host "localhost" :protocol "http"})))))

(deftest query-params
  (are [query map] (is (= map (query->map query)))
    "a=b" {"a" "b"}
    "a=1&b=2&c=3" {"a" "1" "b" "2" "c" "3"}
    "a=" {"a" ""}
    "a" {"a" ""}
    nil nil
    "" nil))

(deftest user-info-edgecases
  (are [user-info uri-string] (= user-info ((juxt :username :password) (uri uri-string)))
    ["a" nil] "http://a@foo"
    ["a" nil] "http://a:@foo"
    ["a" "b:c"] "http://a:b:c@foo"))

(deftest path-normalization
  (is (= "http://a/" (uri-str "http://a/b/c/../..")))
  
  (is (= "http://a/b/c" (uri-str "http://a/b/" "c")))
  (is (= "http://a/b/c" (uri-str "http://a/b/.." "b" "c")))
  (is (= "http://a/b/c" (str (uri "http://a/b/..////./" "b" "c" "../././.." "b" "c"))))
  (is (= "http://a/" (str (uri "http://a/b/..////./" "b" "c" "../././.." "b" "c" "/"))))
  
  (is (= "http://a/x" (str (uri "http://a/b/c" "/x"))))
  (is (= "http://a/" (str (uri "http://a/b/c" "/"))))
  (is (= "http://a/" (str (uri "http://a/b/c" "../.."))))
  (is (= "http://a/x" (str (uri "http://a/b/c" "../.." "." "./x")))))

(deftest anchors
  (is (= "http://a#x" (uri-str "http://a#x")))
  (is (= "http://a?b=c#x" (uri-str "http://a?b=c#x")))
  (is (= "http://a?b=c#x" (-> "http://a#x" uri (assoc :query {:b "c"}) str))))

(deftest no-bare-?
  (is (= "http://a" (-> "http://a?b=c" uri (update-in [:query] dissoc "b") str))))

