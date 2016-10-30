(defproject sublime-text-dash "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [enlive "1.1.5"]
                 [org.clojure/data.json "0.2.5"]
                 [org.clojure/java.jdbc "0.6.2-alpha3"]
                 [org.xerial/sqlite-jdbc "3.14.2.1"]]
  :main ^:skip-aot sublime-text-dash.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
