(ns bsless.tools.jcmd.jfr
  (:require
   [bsless.tools.jvm :as jvm]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s])
  (:import
   (java.io InputStream StringWriter)
   (java.lang ProcessBuilder ProcessBuilder$Redirect)))

(defmulti jcmd-options
  "Build jcmd options for JFR commands.
  See the `(clojure.spec.alpha/form s)` for:
  - `:jcmd.jfr.options/start`
  - `:jcmd.jfr.options/stop`
  - `:jcmd.jfr.options/dump`
  - `:jcmd.jfr.options/check`
  "
  (fn [command _options] (str command)))

(s/def :jcmd/time (s/and number? (s/conformer #(str % "s"))))
(s/def :jcmd.jfr/name             (s/and (s/conformer str) string?))
(s/def :jcmd.jfr/settings         string?)
(s/def :jcmd.jfr/defaultrecording boolean?)
(s/def :jcmd.jfr/delay            :jcmd/time)
(s/def :jcmd.jfr/duration         :jcmd/time)
(s/def :jcmd.jfr/filename         string?)
(s/def :jcmd.jfr/compress         boolean?)
(s/def :jcmd.jfr/maxage           :jcmd/time)
(s/def :jcmd.jfr/maxsize          int?)
(s/def :jcmd.jfr/disk             boolean?)
(s/def :jcmd.jfr/flush-interval   :jcmd/time)
(s/def :jcmd.jfr/dumponexit       boolean?)
(s/def :jcmd.jfr/path-to-gc-roots boolean?)
(s/def :jcmd.jfr/verbose boolean?)

(s/def :jcmd.jfr.options/start
  (s/keys
   :opt-un
   [:jcmd.jfr/name
    :jcmd.jfr/settings
    :jcmd.jfr/defaultrecording
    :jcmd.jfr/delay
    :jcmd.jfr/duration
    :jcmd.jfr/filename
    :jcmd.jfr/compress
    :jcmd.jfr/maxage
    :jcmd.jfr/maxsize
    :jcmd.jfr/disk
    :jcmd.jfr/flush-interval
    :jcmd.jfr/dumponexit
    :jcmd.jfr/path-to-gc-roots]))

(s/def :jcmd.jfr.options/stop
  (s/keys
   :req-un [:jcmd.jfr/name]
   :opt-un [:jcmd.jfr/filename]))

(comment (s/valid? :jcmd.jfr.options/stop {:foreign 2}))

(s/def :jcmd.jfr.options/check
  (s/keys :opt-un [:jcmd.jfr/name :jcmd.jfr/verbose]))

(s/def :jcmd.jfr.options/dump
  (s/keys
   :opt-un
   [:jcmd.jfr/name
    :jcmd.jfr/filename
    :jcmd.jfr/maxage
    :jcmd.jfr/maxsize
    ;; :jcmd.jfr/begin ;; TODO
    ;; :jcmd.jfr/end
    :jcmd.jfr/path-to-gc-roots]))

(defn- -map-spec-keys
  [s]
  (let [{:keys [req-un opt-un req opt]}
        (->> s s/form rest (apply hash-map))
        req (into (set req) (map (comp keyword name)) req-un)
        opt (into (set opt) (map (comp keyword name)) opt-un)]
    (into opt req)))

(def ^:private map-spec-keys (memoize -map-spec-keys))

(defmacro ^:private with-checked-asserts
  [& body]
  `(let [v# (s/check-asserts?)]
     (s/check-asserts true)
     (let [ret# (do ~@body)]
       (s/check-asserts v#)
       ret#)))

(defn form-jcmd-opts
  "Select spec keys in `s` from `opts` and stingify them in `k=v` form."
  [s opts]
  (with-checked-asserts
    (s/assert s opts))
  (->> s
       map-spec-keys
       (select-keys (s/conform s opts))
       (map (fn [[k v]] (str (name k) "=" v)))))

(comment (form-jcmd-opts :jcmd.jfr.options/start {:delay 2 :other 2}))

(defmethod jcmd-options "JFR.start" [_ opts] (form-jcmd-opts :jcmd.jfr.options/start opts))
(defmethod jcmd-options "JFR.stop" [_ opts] (form-jcmd-opts :jcmd.jfr.options/stop opts))
(defmethod jcmd-options "JFR.dump" [_ opts] (form-jcmd-opts :jcmd.jfr.options/dump opts))
(defmethod jcmd-options "JFR.check" [_ opts] (form-jcmd-opts :jcmd.jfr.options/check opts))

(defn jcmd
  "Build process args list for JCMD for `pid`, `command` with `options`."
  [pid command options]
  (into ["jcmd" (str pid) command] (jcmd-options command options)))

(defn- copy-stream
  [^InputStream input-stream]
  (let [writer (StringWriter.)]
    (io/copy input-stream writer)
    (let [s (.toString writer)]
      (when-not (zero? (.length s))
        s))))

(defn- process
  [args]
  (let [pb (ProcessBuilder. ^java.util.List args)]
    (.redirectOutput pb ProcessBuilder$Redirect/PIPE)
    (.redirectError pb ProcessBuilder$Redirect/PIPE)
    (let [proc (.start pb)
          exit (.waitFor proc)
          out-str (copy-stream (.getInputStream proc))
          err-str (copy-stream (.getErrorStream proc))]
      (cond-> {:exit exit}
        out-str (assoc :out out-str)
        err-str (assoc :err err-str)))))

(comment
  (def p (process (jcmd 1399098 "JFR.start" {:name "foo"})))
  (process (jcmd 1399098 "JFR.stop" {:name "7"})))

(defn- default-options
  []
  {:filename "myrecording.jfr"
   :pid (jvm/pid)
   :duration 60})

(defn record!
  "Record with JFR for a finite duration and save to a recording file.
  By default records current process for 60 seconds to myrecording.jfr.
  Available options:
  `:pid` - Process ID to record. Default to current JVM.
  `:name` - recording name, can be anything
  `:settings` - JFR settings, string.
  `:defaultrecording` - boolean
  `:delay` - seconds before starting. number.
  `:duration` - recording duration. number. default 60 seconds.
  `:filename` - output file name. Default myrecording.jfr. String.
  `:compress` - Compress output file? boolean.
  `:maxage` - number.
  `:maxsize` - integer.
  `:disk` - record disk events. boolean.
  `:flush-interval` - number.
  `:dumponexit` - dump on VM exit. boolean.
  `:path-to-gc-roots` - record paths to gc roots."
  [options]
  (let [{:keys [pid] :as options} (merge (default-options) options)]
    (process (jcmd pid "JFR.start" options))))

(comment
  (jcmd 1 "JFR.start" (merge default-options {:name "fizz"}))
  (record! {:filename "spam" :duration 2 :delay 3}))
