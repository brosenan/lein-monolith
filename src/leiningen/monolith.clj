(ns leiningen.monolith
  (:require
    [clojure.java.io :as jio]
    [clojure.pprint :refer [pprint]])
  (:import
    (java.io
      File
      PushbackReader)))


(def config-name "monolith.clj")


(defn- read-clj
  "Read the first data structure in a clojure file."
  [file]
  (-> (jio/file file)
      (jio/reader)
      (PushbackReader.)
      (read)))


(defn- find-config
  "Searches upward from the project root until it finds a configuration file.
  Returns the `File` object if found, or nil if no matching file could be
  located in the parent directories."
  [dir]
  (when dir
    (let [dir (jio/file dir)
          file (jio/file dir config-name)]
      (if (.exists file)
        file
        (recur (.getParent dir))))))


(defn- load-config!
  "Reads the monolith configuration file and returns the contained data
  structure."
  ([]
   (load-config! (System/getProperty "user.dir")))
  ([dir]
   (let [file (find-config dir)]
     (when-not file
       (println "Could not find configuration file" config-name "in any parent directory of" dir)
       (System/exit 1))
     (-> (read-clj file)
         (assoc :config-path (str file))))))


(defn- mono-root
  "Returns the path to the monorepo's root."
  [config]
  (.getParent (jio/file (:config-path config))))


(defn- read-project-coord
  "Reads a leiningen project definition from the given directory and returns a
  vector of the project's name symbol and version. Returns nil if the project
  file does not exist or is invalid."
  [dir]
  (let [project-file (jio/file dir "project.clj")]
    (when-let [project (and (.exists project-file) (read-clj project-file))]
      (if (and (list? project) (= 'defproject (first project)))
        [(nth project 1) (nth project 2)]
        (println "WARN:" (str project-file) "does not appear to be a valid leiningen project definition!")))))


(defn- find-internal-projects
  "Returns a sequence of vectors containing the project name and the path to
  the project's directory."
  [config]
  (when-let [root (mono-root config)]
    (->>
      (:project-dirs config)
      (mapcat
        (fn list-projects
          [path]
          (let [projects-dir (jio/file root path)]
            (->> (.listFiles projects-dir)
                 (map #(vector (read-project-coord %) %))
                 (filter first))))))))


(defn- merged-profile
  "Constructs a profile map containing merged `:src-paths` and `:test-paths` entries."
  [version options]
  {:src-paths ['...]
   :test-paths ['...]
   :dependencies ['...]})



;; ## Command Implementations

(defn- print-help
  []
  (println "NYI"))


(defn- print-stats
  [project args]
  (let [config (load-config!)]
    (println "Config path:" (::config-path (meta config)))
    (println "Configuration:")
    (pprint config)))


(defn- link-checkouts!
  [project args]
  ; ...
  (println "NYI"))


(defn- check-dependencies
  [project args]
  ; ...
  (println "NYI"))


(defn- apply-with-all
  [project args]
  ; ...
  (println "NYI"))



;; ## Plugin Entry

(defn monolith
  "..."
  [project & [command & args]]
  (case command
    (nil "stats")
      (print-stats project args)

    "checkouts"
      (link-checkouts! project args)

    "deps"
      (check-dependencies project args)

    "with-all"
      (apply-with-all project args)

    "help"
      (print-help)

    (do
      (println (pr-str command) "is not a valid monolith command!")
      (System/exit 1))))
