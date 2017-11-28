(ns talos.core
  (:require
    [taoensso.timbre :as log]))

;; Create one state for use in the FSM.
;; The optional callback is called if the state is entered.
;; It will receive the tranisition that triggered it as a parameter.
(defrecord State [name callback])
(defn state
  ([name] (State. name nil))
  ([name callback] (State. name callback)))

;; Create one transition for use in the FSM.
;; The event can be anything that should trigger this transition.
;; The from and to are the names of the involved states.
;; An optional condition can be given that is tested before the state is changed.
(defrecord Transition [event from to condition])
(defn transition
  ([event from to] (Transition. event from to nil))
  ([event from to condition] (Transition. event from to condition)))

;; Create an FSM with states and transitions.
;; The optional state is used as the initial state.
;; If it is not given the first of the states is used instead.
(defrecord Fsm [states transitions state initial data])
(defn fsm
  ([states transitions]
    (Fsm. states transitions (atom (first states)) (first states) (atom {})))
  ([states transitions state]
    (Fsm. states transitions (atom state) state (atom {}))))

(defn matches?
  "Check that the second transitions event matches the firsts and the condition is met."
  [t1 event data state]
  (and
    (or
      (= state (:from t1))
      (= :* (:from t1)))
    (= (:event t1) (:event event))
    (if-not (nil? (:condition t1))
      ((:condition t1) event data)
      true)))

(defn get-transition
  "From the list of transitions find the one where the event and state match the given transition."
  [transitions state event data]
  (first (filter #(matches? % event data state) transitions)))

(defn get-state
  "Get a state by name."
  [name states]
  (first (filter #(= name (:name %)) states)))

(defn verify-state
  "Verify that the from-state of a transition matches the state of the fsm."
  [my-fsm transition]
  (or
    (= (:name @(:state my-fsm)) (:from transition))
    (= :* (:from transition))))

(defn handle-callback
  "Ensure that a callback is executed if one is associated with a state."
  [my-fsm state event]
  (when-not (nil? (:callback state)) ((:callback state) my-fsm event)))

(defn data
  "Get the data associated with the given fsm."
  ([my-fsm]
   @(:data my-fsm))
  ([my-fsm item]
   (item @(:data my-fsm))))

(defn data!
  "Alter the data associated with the given fsm."
  [my-fsm alter-fn]
  (swap! (:data my-fsm) alter-fn))

(defn process!
  "Process an event on an fsm and update the internal state."
  [my-fsm event]
  (let [transition (get-transition (:transitions my-fsm) (:name @(:state my-fsm)) event (data my-fsm))]
    (when (verify-state my-fsm transition)
      (let [to (get-state (:to transition) (:states my-fsm))]
        (reset! (:state my-fsm) to)
        (handle-callback my-fsm to event)))))

(defn reinit!
  "Reset the state of the FSM."
  [my-fsm]
  (reset! (:data my-fsm)  {})
  (reset! (:state my-fsm) (:initial my-fsm)))
