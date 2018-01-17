(defproject com.cemerick/url "0.1.2-SNAPSHOT"
  :description "Makes working with URLs in Clojure a little more pleasant."
  :url "http://github.com/cemerick/url"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [pathetic "0.5.0"]
                 [org.clojure/clojurescript "0.0-1835" :optional true]]

  :source-paths ["src" "target/generated-src"]
  :test-paths ["test/" "target/generated-test"]
  :aliases  {"cleantest" ["do" "clean," "cljx" "once," "test,"
                          "cljsbuild" "once," "cljsbuild" "test"]}
  :profiles {:dev {:dependencies [[com.cemerick/clojurescript.test "0.0.4"]
                                  [com.cemerick/piggieback "0.0.5"]]
                   :plugins [[lein-cljsbuild "0.3.2"]]}}
  
  :cljsbuild {:builds [{:source-paths ["target/generated-src" "target/generated-test"]
                        :compiler {:output-to "target/cljs/testable.js"}
                        :optimizations :whitespace
                        :pretty-print true}]
              :test-commands {"unit-tests" ["runners/phantomjs.js" "target/cljs/testable.js"]}})
