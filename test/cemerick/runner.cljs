(ns cemerick.runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [cemerick.test-uri]))

(doo-tests 'cemerick.test-uri)

