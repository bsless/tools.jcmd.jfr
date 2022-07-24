(ns bsless.tools.jcmd.jfr-test
  (:require
   [clojure.test :as t]
   [bsless.tools.jcmd.jfr :as jfr]))

(t/deftest form-jcmd-opts
  (t/testing "start"
    (t/is
     (= ["name=foo" "delay=3s"]
        (jfr/form-jcmd-opts :jcmd.jfr.options/start {:name "foo" :delay 3 :foreign 2}))))
  (t/testing "stop"
    (t/is
     (= ["name=foo"]
        (jfr/form-jcmd-opts :jcmd.jfr.options/stop {:name "foo" :foreign 2})))
    (t/testing "missing required key"
      (t/is
       (thrown?
        clojure.lang.ExceptionInfo
        (jfr/form-jcmd-opts :jcmd.jfr.options/stop {:foreign 2})))))
  (t/testing "check"
    (t/is
     (= ["name=foo"]
        (jfr/form-jcmd-opts :jcmd.jfr.options/check {:name "foo" :delay 3 :foreign 2}))))
  (t/testing "dump"
    (t/is
     (= ["name=foo" "filename=foo.bar"]
        (jfr/form-jcmd-opts :jcmd.jfr.options/dump {:name "foo" :filename "foo.bar" :foreign 2})))))

(t/deftest jcmd-options
  (t/testing "start"
    (t/is
     (= ["name=foo" "delay=3s"]
        (jfr/jcmd-options "JFR.start" {:name "foo" :delay 3 :foreign 2}))))
  (t/testing "stop"
    (t/is
     (= ["name=foo"]
        (jfr/jcmd-options "JFR.stop" {:name "foo" :foreign 2})))
    (t/testing "missing required key"
      (t/is
       (thrown?
        clojure.lang.ExceptionInfo
        (jfr/jcmd-options "JFR.stop" {:foreign 2})))))
  (t/testing "check"
    (t/is
     (= ["name=foo"]
        (jfr/jcmd-options "JFR.check" {:name "foo" :delay 3 :foreign 2}))))
  (t/testing "dump"
    (t/is
     (= ["name=foo" "filename=foo.bar"]
        (jfr/jcmd-options "JFR.dump" {:name "foo" :filename "foo.bar" :foreign 2})))))

(t/deftest jcmd
  (t/is
   (= ["jcmd" "3" "JFR.start" "name=foo" "delay=3s"]
      (jfr/jcmd 3 "JFR.start" {:name "foo" :delay 3 :foreign 2}))))
