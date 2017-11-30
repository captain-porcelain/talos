(defproject talos "1.0.0"
  :description "Providing finite state machines to manage application state flows."

  :dependencies
  [;; core dependencies
   [org.clojure/clojure "1.9.0-alpha14"]
   [mount "0.1.11"]
   [com.taoensso/timbre "4.8.0"]]

  :plugins
  [[lein-marginalia "0.9.0"]]

  :source-paths ["src/clj" "src/cljc" "src/cljs"]

  :repl-options
  {;; If nREPL takes too long to load it may timeout,
   ;; increase this to wait longer before timing out.
   ;; Defaults to 30000 (30 seconds)
   :timeout 360000}

  ;; ensure that deployments during a release are using the regular lein deploy task.
  :lein-release
  {:deploy-via :lein-install}

  ;; enable warning when using reflection to determine types.
  :global-vars {*warn-on-reflection* false})
