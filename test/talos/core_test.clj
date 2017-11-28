(ns talos.core-test
  (:require
	[talos.core :as fsm]
    [mount.core :as mount])
  (:use clojure.test))


(def test-state (atom false))

(defn test-callback
  [source-fsm source-event]
  (reset! test-state source-event))

(def states (list (fsm/state :a)
                  (fsm/state :b)
                  (fsm/state :c test-callback)))
(def transitions (list (fsm/transition :a-to-b :a :b)
                       (fsm/transition :b-to-c :b :c)
                       (fsm/transition :all-to-a :* :a)))

(deftest find-appropriate-transition
  (is (=
       (fsm/transition :a-to-b :a :b)
       (fsm/get-transition transitions :a {:event :a-to-b} {}))))

(deftest simple-transitioning
  (def my-fsm (fsm/fsm states transitions))
  (fsm/process! my-fsm {:event :a-to-b})
  (is (= :b (:name @(:state my-fsm))))
  (fsm/process! my-fsm {:event :all-to-a})
  (is (= :a (:name @(:state my-fsm)))))

(deftest reinit-transitioning
  (def my-fsm (fsm/fsm states transitions))
  (fsm/process! my-fsm {:event :a-to-b})
  (is (= :b (:name @(:state my-fsm))))
  (fsm/reinit! my-fsm)
  (is (= :a (:name @(:state my-fsm)))))

(deftest transitioning-with-callback
  (def my-fsm (fsm/fsm states transitions))
  (fsm/process! my-fsm {:event :a-to-b})
  (fsm/process! my-fsm {:event :b-to-c :payload true})
  (is (= :c (:name @(:state my-fsm))))
  (is (= true (:payload @test-state))))

(deftest transitioning-with-condition
  (testing "condition is true"
    (def transitions2 (list (fsm/transition :a-to-b :a :b (fn [event data] true))
                            (fsm/transition :b-to-c :b :c)))
    (def my-fsm (fsm/fsm states transitions2))
    (fsm/process! my-fsm {:event :a-to-b})
    (is (= :b (:name @(:state my-fsm)))))
  (testing "condition is false"
    (def transitions2 (list (fsm/transition :a-to-b :a :b (fn [event data] false))
                            (fsm/transition :b-to-c :b :c)))
    (def my-fsm (fsm/fsm states transitions2))
    (fsm/process! my-fsm {:event :a-to-b})
    (is (= :a (:name @(:state my-fsm))))))


(deftest multiple-transitions-with-one-name
  (def states2 (list (fsm/state :a)
                     (fsm/state :b)
                     (fsm/state :c)))
  (def transitions2 (list (fsm/transition :multi :a :b)
                          (fsm/transition :multi :b :c)))
  (is (true? (fsm/matches? (nth transitions2 0) {:event :multi} {} :a)))
  (def my-fsm (fsm/fsm states2 transitions2))
  (fsm/process! my-fsm {:event :multi})
  (is (= :b (:name @(:state my-fsm))))
  (fsm/process! my-fsm {:event :multi})
  (is (= :c (:name @(:state my-fsm)))))
