(ns sublime-text-dash.html-generator
  (:require [net.cgrand.enlive-html :as html]
            [clojure.string :as s])
  )

(def base-path "Sublime_Text_Plugin_API.docset/Contents/Resources/Documents/")

(def class-template (html/html-resource "templates/class.html"))

(def STATIC-DOCS
  {"2_api_reference.html" "http://www.sublimetext.com/docs/2/api_reference.html"
   "3_api_reference.html" "http://www.sublimetext.com/docs/2/api_reference.html"
   "porting_guide.html" "http://www.sublimetext.com/docs/3/porting_guide.html"
   "sublime_docs.css" "http://www.sublimetext.com/docs/3/sublime_docs.css"
   })

(defn version-label-class [{:keys [:versions :version-data]}]
  (cond
    (and (= #{:st2 :st3} versions) (some #(not= (:versions %) versions) version-data)) "label label-warning"
    (= #{:st2 :st3} versions) "label label-success"
    (= #{:st2} versions) "label label-default"
    (= #{:st3} versions) "label label-primary"))

(defn version-label-text [{:keys [:versions :version-data]}]
  (cond
    (and (= #{:st2 :st3} versions) (some #(not= (:versions %) versions) version-data)) "ST2≠3"
    (= #{:st2 :st3} versions) "ST2&3"
    (= #{:st2} versions) "ST2"
    (= #{:st3} versions) "ST3"))

(defn version-label [versioned]
  (html/do->
    (html/set-attr :class (version-label-class versioned))
    (html/content (version-label-text versioned)))
  )


(defn generate-class-doc [class-doc]
  (let [filename (str base-path (:name class-doc) ".html")
        html (-> (html/at class-template
                    [:h1#name] (html/content (:name class-doc))
                    [:.class-version] (html/clone-for [class-version (:version-data class-doc)]
                                        [:.class-version-label] (version-label class-version)
                                        [:.class-doc] (html/html-content (:doc-block class-version)))
                    [:#doc-block] (html/html-content (:doc-block class-doc))
                    [:.method] (html/clone-for [method-doc (:methods class-doc)]
                                [:.dashAnchor] (html/set-attr :name (str "//apple_ref/cpp/" (:type method-doc) "/" (:short-name method-doc)))
                                [:.method-name] (html/html-content (:short-name method-doc))
                                [:.versions-label] (version-label method-doc)
                                [:.version] (html/clone-for [version (:version-data method-doc)]
                                              [:.version-label] (version-label version)
                                              [:.method-syntax] (html/html-content (:name version))
                                              [:.returns] (html/html-content (:returns version))
                                              [:.doc] (html/html-content (:doc version)))
                                )
                    [:.generation-date] (html/content (str (java.util.Date.))))
                 html/emit*
                 s/join)]
    (spit filename html)
  ))

(defn generate-html [docs]
  (doseq [class (:classes docs)]
    (doseq [[filename url] STATIC-DOCS]
      (spit (str base-path filename) (slurp url)))
    (generate-class-doc class)))
