(defproject com.arohner/uri "0.1.2-SNAPSHOT"
  :description "Makes working with URLs in Clojure a little more pleasant."
  :url "http://github.com/cemerick/url"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [pathetic "0.5.0"]
                 [org.clojure/clojurescript "1.10.238" :optional true]]

  :source-paths ["src" "target/generated-src"]
  :test-paths ["test/" "target/generated-test"]
  :aliases  {"cleantest" ["do" "clean," "cljx" "once," "test,"
                          "cljsbuild" "once," "cljsbuild" "test"]}
  :profiles {:dev {:dependencies [[com.cemerick/piggieback "0.0.5"]]
                   :plugins [[lein-cljsbuild "1.1.7"]
                             [lein-doo "0.1.10"]]}}

  :deploy-repositories [["releases" :clojars]]
  
  :cljsbuild {:builds [{:id "test"
                        :source-paths ["src" "test"]
                        :compiler {:main cemerick.runner
                                   :output-to "target/cljs/testable.js"
                                   :output-dir "target/cljs/out"
                                   :pretty-print true}}]})
