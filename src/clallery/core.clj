(ns clallery.core
  (:require [me.raynes.conch :refer [programs with-programs]]
            [me.raynes.fs :as fs]
            [gaka.core :as gaka]
            [clojure.tools.cli :as cli :refer [cli]]
            [hiccup.core :as h])
  (:import [java.io File])
  (:gen-class))

;; These extensions we check.
(def file-extensions #{"jpg" "jpeg"})

;; Width of a thumbnail
(def thumb-size "400x")

;; dirname of thumbnails under output
(def thumb-dir-name "thumbs")

(def usage-msg "Clallery: generate a static gallery.")

(def cli-args
  [["-d" "--input-dir" "Directory for source pics" :default "."
    :parse-fn fs/expand-home]
   ["-o" "--output-dir" "Directory for output" :default "out"
    :parse-fn fs/expand-home]
   ["-v" "--verbose" "Verbose" :default false :flag true]
   ["-s" "--size" "Picture size [% or px]" :default "1280x"]
   ["-h" "--help" "This help" :default false :flag true]])

(defn good-extension?
  "Predicate for a good extensioned file names."
  [file-name]
  (let [file-name (.toLowerCase file-name)]
    (some (fn [ext]
            (.endsWith file-name ext))
          file-extensions)))
(defn all-good-files 
  "Get all suitable files in given directory"
  [dir]
  (fs/with-cwd dir
    (let [all (fs/glob "*")]
      (filter (fn [f] (good-extension? (.getName f)))
              all))))

(programs convert)

(defn join-paths
  "Make a proper path like Python's os.path.join"
  [& paths]
  (let [sep File/separator]
    (apply str
           (interpose sep paths))))

(defn resize-file
  "Take a file and an output dir & some other crap and call 'convert'"
  [^File in-file
   out-dir scale]
  (let [filename (.getName in-file)
        full-path (.getAbsolutePath in-file)
        output-path (join-paths out-dir filename)]
    (convert full-path "-resize" scale output-path)
    output-path))

(defn make-thumbnail
  "Make thumbnail"
  [in-file thumb-dir]
  (resize-file in-file thumb-dir thumb-size))

(defn thumb-dir
  "Determine thumbs dir by output dir"
  [out-dir]
  (join-paths out-dir thumb-dir-name))

(defn print-progress
  []
  (print (rand-nth ":._")))

(def gallery-css
  [[:#pics
    {:display :inline
     :margin 0
     :padding 0}
    [:li
     {:margin "10px"}]]
   [:#single-pic
    [:img
     {:width "100%"
      :height "auto"}]]
   [:body
    {:background-color "#333"}]])

(defn single-html
  [args prev this next]
  (let [out-dir (:output-dir args)
        ]
    (h/html
     [:html
      [:head
       [:style (apply str (map gaka/css gallery-css))]]
      [:body
       [:div#single-pic
        [:img {:src this}]]]])))

(defn gallery-html
  [args]
  (let [out-dir (:output-dir args)
        pic-files (sort (all-good-files out-dir))]
    (h/html
     [:html
      [:head
       [:style (apply str (map gaka/css gallery-css))]
       [:title out-dir]]
      [:body
       [:ul#pics
        (for [pic-file pic-files]
          [:li
           [:a {:href (str (.getName pic-file))}
            [:img {:src (join-paths thumb-dir-name
                                    (.getName pic-file))}]]])]]])))

(defmacro verbosely
  [& body]
  `(when (:verbose ~'args)
     (println ~@body)))

(defn usage
  [help]
  (println "\n" usage-msg "\n")
  (println help)
  (System/exit 0))

(defn -main
  [& args]
  (let [[args rest-args help]
        (try
          (apply cli args cli-args)
          (catch Exception e (usage "Invalid arguments.")))
        output-dir (:output-dir args)]
    (when (:help args)
      (usage help))
    (verbosely "Creating directory" output-dir)
    (fs/mkdirs (thumb-dir output-dir))
    (verbosely "Resizing pictures in parallel...")
    (doall 
     (pmap (fn [f]
             (print-progress)
             (make-thumbnail f (thumb-dir output-dir))
             (resize-file f output-dir (:size args)))
           (all-good-files (:input-dir args))))
    (verbosely "")
    (verbosely "Generating the HTML")
    (spit (join-paths output-dir "index.html")
          (gallery-html args))
    (verbosely "All done!")
    (System/exit 0)))
