SHELL := /bin/bash
VERSION=$(shell cat version)

build: repl

clean:
		rm -rf pom.xml
		rm -rf target/

package:
		clojure -A:cambada -m cambada.jar --app-version $(VERSION)

tests:
		clojure -A:test -m kaocha.runner

test-watch:
		clojure -A:test -m kaocha.runner --watch

repl:
		clojure -A:repl -m repl 7878

outdated:
		clojure -A:outdated

install:
		clojure -A:publish install target/talos-$(VERSION).jar

publish:
		echo ${CLOJARS_USERNAME}
		clojure -A:publish deploy target/talos-$(VERSION).jar

sidenotes:
		clojure -A:sidenotes

