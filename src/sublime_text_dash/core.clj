(ns sublime-text-dash.core
  (:require [net.cgrand.enlive-html :as html] [clojure.string :as s] [clojure.data.json :as j]
            [sublime-text-dash.index-generator :refer [write-index]]
            [sublime-text-dash.html-generator :refer [generate-html]])
  (:gen-class)
  (:import (java.net URL)))

(def st2-api-url "http://www.sublimetext.com/docs/2/api_reference.html")
(def st3-api-url "http://www.sublimetext.com/docs/3/api_reference.html")

(defn fetch-url [url]
  (html/html-resource (URL. url)))

(defn select-one [doc query]
  (first (html/select doc query)))

(defn is-class-def? [[section-name _section-contents]]
  (and section-name ((some-fn #(s/starts-with? % "Class")
                              #(s/starts-with? % "Module")
                              #(s/ends-with? % "Class")
                              #(s/ends-with? % "Module")) section-name)))

(defn print-section [[section-name section-contents]]
  (s/join (-> (concat
                (and section-name (html/html [:h2 section-name]))
                section-contents)
              html/emit*
              s/join
              s/trim)))

(defn parse-method-tr [method-tr]
  (let [clean-td #(-> % first :content html/emit* s/join s/trim)
        name (clean-td (html/select method-tr [:td.mth]))
        returns (clean-td (html/select method-tr [:td.rtn]))
        doc (clean-td (html/select method-tr [:td.dsc]))
        short-name (first (s/split name #"\("))]
    {:short-name short-name, :name name, :returns returns, :doc doc}))

(defn parse-method-table [method-table]
  (let [type (get {"Constructors" "Constructor" "Methods" "Method" "Properties" "Property"} (html/text (first (html/select method-table [:th]))))
        method-trs (html/select method-table [[:tr (html/has [:td])]])]
    (map #(assoc (parse-method-tr %) :type type) method-trs)))

(defn parse-class-docs [class-doc]
  (let [method-tables (set (html/select class-doc [:table.functions]))
        class-doc-block (-> (html/at class-doc [:table.functions] nil) html/emit* s/join s/trim)]
    {:doc-block class-doc-block
     :methods (remove #(= (:name %) "(no methods)") (mapcat parse-method-table method-tables))}))

(defn parse-st2-class-def [[section-name section-contents]]
  (let [[[_ type name]] (re-seq #"^(Class|Module) (.+)$" section-name)]
    (assoc (parse-class-docs section-contents) :name name :type type)))

(defn parse-st3-class-def [[section-name section-contents]]
  (let [[[_ name type]] (re-seq #"^(.+) (Class|Module)$" section-name)]
    (assoc (parse-class-docs section-contents) :name name :type type)))

(defn partition-by-h2 [tag]
  (partition-by #(= (:tag %) :h2) (:content tag)))

(defn parse-st2-docs [doc]
  (let [partitions (partition-by-h2 (select-one doc [:body]))
        sections (cons [nil (first partitions)] (map (fn [[[header] contents]] [(first (:content header)) contents]) (partition 2 (rest partitions))))
        [non-class-defs class-defs] (split-with (complement is-class-def?) sections)]
    {:doc-block (map print-section non-class-defs)
     :classes (map parse-st2-class-def class-defs)}))

(defn parse-st3-section [section]
  (let [[_before-h2 [h2] & contents] (partition-by-h2 section)]
    [(html/text h2) (apply concat contents)]))

(defn parse-st3-docs [doc]
  (let [doc-sections (drop 2 (html/select doc [:section])) ; drop header and contents table
        sections (map parse-st3-section doc-sections)
        [non-class-defs class-defs] (split-with (complement is-class-def?) sections)]
    {:doc-block (map print-section non-class-defs)
     :classes (map parse-st3-class-def class-defs)}))


(defn versionize-class [version class-doc]
  (assoc class-doc
    :version version
    :methods (map #(assoc % :version version) (:methods class-doc))))

(defn versionize-doc [version doc]
  (assoc doc
    :version version
    :classes (map (partial versionize-class version) (:classes doc))))

(defn merge-method-def [method-defs]
  (assoc
    (first method-defs)
    :versions (into #{} (map :version method-defs))
    :version-data (map
                   #(assoc (first %) :versions (into #{} (map :version %)))
                   (vals (group-by #(str (:name %) (:returns %) (:doc %)) method-defs)))))

(defn merge-class-def [class-defs]
  (assoc
    (first class-defs)
    :versions (into #{} (map :version class-defs))
    :version-data (map
                    #(assoc (first %) :versions (into #{} (map :version %)))
                    (vals (group-by :doc-block class-defs)))
    :methods (sort-by :short-name (map merge-method-def (vals (group-by :short-name (mapcat :methods class-defs)))))))

(defn merge-classes [& docs]
  (assoc
    (first docs)
    :classes (map merge-class-def (vals (group-by :name (mapcat :classes docs))))))

(defn -main
  [& args]
  (let [[st2-docs st3-docs] (map versionize-doc [:st2 :st3] (map #(-> %1 fetch-url %2) [st2-api-url st3-api-url] [parse-st2-docs parse-st3-docs]))
        merged-docs (merge-classes st2-docs st3-docs)]
    (generate-html merged-docs)
    (write-index merged-docs)))

