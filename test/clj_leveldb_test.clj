(ns clj-leveldb-test
  (:require
    [clojure.test :refer :all]
    [clj-leveldb :as l]
    [byte-streams :as bs]
    [clojure.edn :as edn])
  (:import
    [java.util
     UUID]
    [java.io
     File]))

(defn nil-pun
  "Convert nil values to string literal 'nil'.
  Workaround for bs/to-char-sequence not handling nil values."
  [x]
  (or x "nil"))

(def db
  (l/create-db
    (doto (File. (str "/tmp/" (UUID/randomUUID)))
      .deleteOnExit)
    {:key-encoder name
     :key-decoder (comp keyword bs/to-string)
     :val-decoder (comp edn/read-string
                        bs/to-char-sequence
                        nil-pun)
     :val-encoder pr-str}))

(deftest test-basic-operations
  (l/put db :a :b)
  (is (= :b
        (l/get db :a)
        (l/get db :a ::foo)))
  (is (= [[:a :b]]
        (l/iterator db)
        (l/iterator db :a)
        (l/iterator db :a :a)
        (l/iterator db :a :c)))
  (is (= nil
        (l/iterator db :b)
        (l/iterator db :b :d)))
  (l/delete db :a)
  (is (= nil (l/get db :a)))
  (is (= ::foo (l/get db :a ::foo)))

  (l/put db :a :b :z :y)
  (is (= :b (l/get db :a)))
  (is (= :y (l/get db :z)))

  (is (= [[:a :b] [:z :y]]
        (l/iterator db)))
  (is (= [[:a :b]]
        (l/iterator db :a :x)
        (l/iterator db nil :x)))
  (is (= [[:z :y]]
        (l/iterator db :b)
        (l/iterator db :b :z)))

  (is (= [:a :z] (l/bounds db)))

  (l/compact db)

  (with-open [snapshot (l/snapshot db)]
    (l/delete db :a :z)
    (is (= nil (l/get db :a)))
    (is (= :b (l/get snapshot :a))))

  (l/compact db)

  (l/delete db :a :b :z :y)

  (l/put db :j :k :l :m)
  (is (= :k (l/get db :j)))
  (is (= :m (l/get db :l)))

  (l/batch db)
  (is (= :k (l/get db :j)))
  (is (= :m (l/get db :l)))
  (l/batch db {:put [:r :s :t :u]})
  (is (= :s (l/get db :r)))
  (is (= :u (l/get db :t)))
  (l/batch db {:delete [:r :t]})
  (is (= nil (l/get db :r)))
  (is (= nil (l/get db :t)))

  (l/batch db {:put [:a :b :c :d]
               :delete [:j :l]})
  (is (= :b (l/get db :a)))
  (is (= :d (l/get db :c)))
  (is (= nil (l/get db :j)))
  (is (= nil (l/get db :l)))
  (is (thrown? AssertionError (l/batch db {:put [:a]}))))
