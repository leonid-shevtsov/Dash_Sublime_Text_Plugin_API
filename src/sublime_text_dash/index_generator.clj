(ns sublime-text-dash.index-generator
  (:require [clojure.java.jdbc :as jdbc :refer [db-do-commands create-table-ddl]])
  (:import (java.io File)))

(def db
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "Sublime_Text_Plugin_API.docset/Contents/Resources/docSet.dsidx"})


(defn create-db []
  (.delete (File. (:subname db)))
  (try (db-do-commands db
                       (create-table-ddl :searchIndex
                                         [:id :integer :primaryKey]
                                         [:name :text]
                                         [:type :text]
                                         [:path :text])
                       "CREATE UNIQUE INDEX anchor ON searchIndex (name, type, path)")

       (catch Exception e (println e))))

(defn insert! [data] (jdbc/insert! db :searchIndex data))

(defn write-index [docs]
  (create-db)
  (insert! {:name "Sublime Text 2 API"
            :type "Guide"
            :path "2_api_reference.html"})

  (insert! {:name "Sublime Text 3 API"
            :type "Guide"
            :path "3_api_reference.html"})

  (insert! {:name "Plugin Porting Guide"
            :type "Guide"
            :path "porting_guide.html"})

  (doseq [klass (:classes docs)]
    (insert! {:name (:name klass)
              :type (:type klass)
              :path (str (:name klass) ".html")})

    (doseq [method (:methods klass)]
      (insert! {:name (:short-name method)
                :type (:type method)
                :path (str (:name klass) ".html#//apple_ref/cpp/" (:type method) "/" (:short-name method))}))))

