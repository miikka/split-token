(ns split-token.core-test
  (:require [clojure.test :refer [deftest is]]
            [split-token.core :as split-token]))

(deftest end-to-end-test
  (let [{:keys [token selector verifier-hash]} (split-token/generate)]
    (is (= selector (split-token/get-selector token)))
    (is (split-token/valid? token verifier-hash))))
