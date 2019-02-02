(defproject factual/clj-leveldb "0.1.2"
  :description "an idiomatic wrapper for LevelDB"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.fusesource.leveldbjni/leveldbjni-all "1.8"]
                 [org.iq80.leveldb/leveldb-api "0.10"]
                 [byte-streams "0.2.4"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.10.0"]
                                  [criterium "0.4.4"]]}})
