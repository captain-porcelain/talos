# Talos

> Talos [...] was a giant automaton made of bronze to protect Europa in Crete from pirates and invaders.
>
> -- <cite><a href="https://en.wikipedia.org/wiki/Talos">from Wikipedia</a></cite>

In this case pirates and invaders are state and state transitions.

The sidenotes documentation for talos can be found [here](https://captain-porcelain.github.io/talos/toc.html)

Talos is small library that implements a finite state machine that can be used to manage an applications state
and the transitions from one to another.

A talos fsm is defined by a list of states and a map of transitions between them:

```clojure
(def states
  (list
    (talos/state :a)
    (talos/state :b)
    (talos/state :c state-callback)))

(def transitions
  (list
    (talos/transition :a-to-b :a :b)
    (talos/transition :b-to-c :b :c)
    (talos/transition :all-to-a :* :a)))

(def fsm (talos/fsm states transitions))
```

A state is identified by a keyword and may have an optional callback that is executed when the fsm enters that state.

Transitions are triggered by an event like :a-to-b and must give the state in which the event is accepted as well as the state
that is to entered next. The special state :* can be used to indicate that an event is accepted in any state.

A transition is triggered when the fsm processes an event, which can have optional data associated.

```clojure
(talos/process! fsm {:event :a-to-b})

(talos/process! fsm {:event :a-to-b :something :value})
```

The event is handed to callback functions with the fsm itself:

```clojure
(defn state-callback
  [source-fsm source-event]
  (println (:something source-event)))
```

The fsm also wraps an atom that contains a map and which can be accessed like this:

```clojure
(talos/data! fsm #(assoc % :my-key :my-value))

(talos/data fsm :my-key)
```

Transitions additionally may have a condition function that is evaluated upon the event and the state progression is performed
only if the condition is evaluated as true:

```clojure
(def transitions
	(list
		(talos/transition :a-to-b :a :b (fn [event data] (= :value (:something event))))
		(talos/transition :a-to-b :a :c)))
```

The condition function is handed the event and the contents data atom of the fsm for the evaluation.

