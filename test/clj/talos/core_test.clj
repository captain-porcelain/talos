(ns talos.core-test
  (:require
	[talos.core :as talos]
    [mount.core :as mount])
  (:use clojure.test))


(def test-state (atom false))

(defn test-callback
  [source-fsm source-event]
  (reset! test-state source-event))

(def states (list (talos/state :a)
                  (talos/state :b)
                  (talos/state :c test-callback)))
(def transitions (list (talos/transition :a-to-b :a :b)
                       (talos/transition :b-to-c :b :c)
                       (talos/transition :all-to-a :* :a)))

(deftest find-appropriate-transition
  (is (=
       (talos/transition :a-to-b :a :b)
       (talos/get-transition transitions :a {:event :a-to-b} {}))))

(deftest simple-transitioning
  (def fsm (talos/fsm states transitions))
  (talos/process! fsm {:event :a-to-b})
  (is (= :b (:name @(:state fsm))))
  (talos/process! fsm {:event :all-to-a})
  (is (= :a (:name @(:state fsm)))))

(deftest reinit-transitioning
  (def fsm (talos/fsm states transitions))
  (talos/process! fsm {:event :a-to-b})
  (is (= :b (:name @(:state fsm))))
  (talos/reinit! fsm)
  (is (= :a (:name @(:state fsm)))))

(deftest transitioning-with-callback
  (def fsm (talos/fsm states transitions))
  (talos/process! fsm {:event :a-to-b})
  (talos/process! fsm {:event :b-to-c :payload true})
  (is (= :c (:name @(:state fsm))))
  (is (= true (:payload @test-state))))

(deftest transitioning-with-condition
  (testing "condition is true"
    (def transitions2 (list (talos/transition :a-to-b :a :b (fn [event data] true))
                            (talos/transition :b-to-c :b :c)))
    (def fsm (talos/fsm states transitions2))
    (talos/process! fsm {:event :a-to-b})
    (is (= :b (:name @(:state fsm)))))
  (testing "condition is false"
    (def transitions2 (list (talos/transition :a-to-b :a :b (fn [event data] false))
                            (talos/transition :b-to-c :b :c)))
    (def fsm (talos/fsm states transitions2))
    (talos/process! fsm {:event :a-to-b})
    (is (= :a (:name @(:state fsm))))))


(deftest multiple-transitions-with-one-name
  (def states2 (list (talos/state :a)
                     (talos/state :b)
                     (talos/state :c)))
  (def transitions2 (list (talos/transition :multi :a :b)
                          (talos/transition :multi :b :c)))
  (is (true? (talos/matches? (nth transitions2 0) {:event :multi} {} :a)))
  (def fsm (talos/fsm states2 transitions2))
  (talos/process! fsm {:event :multi})
  (is (= :b (:name @(:state fsm))))
  (talos/process! fsm {:event :multi})
  (is (= :c (:name @(:state fsm)))))
