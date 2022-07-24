# io.github.bsless/tools.jcmd.jfr

Record JFR recordings from within a JVM process or as a tool

## API docs

See [API.md](./API.md)

## Dependency

### Deps

```clojure
io.github.bsless/tools.jcmd.jfr {:mvn/version "0.0.3"}
```

### Leiningen

```clojure
[io.github.bsless/tools.jcmd.jfr "0.0.3"]
```

## As Tool

```bash
clj -Ttools install io.github.bsless/tools.jcmd.jfr '{:git/tag "v0.0.3"}' :as jfr
```

Then invoke:

```bash
clojure -Tjfr record! :pid 112848
```

## Development

Run the project's tests:

    $ clojure -T:build test

Run the project's CI pipeline and build a JAR:

    $ clojure -T:build ci

Install it locally (requires the `ci` task be run first):

    $ clojure -T:build install

Generate docs:

    $ clojure -M:quickdoc

## License

Copyright © 2022 Bsless

Distributed under the Eclipse Public License version 1.0.
