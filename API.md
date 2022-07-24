# bsless.tools.jcmd.jfr 





## `form-jcmd-opts`
``` clojure

(form-jcmd-opts s opts)
```


Select spec keys in `s` from `opts` and stingify them in `k=v` form.
<br><sub>[source](https://github.com/bsless/tools.jvm/blob/master/src/bsless/tools/jcmd/jfr.clj#L92-L100)</sub>
## `jcmd`
``` clojure

(jcmd pid command options)
```


Build process args list for JCMD for `pid`, `command` with `options`.
<br><sub>[source](https://github.com/bsless/tools.jvm/blob/master/src/bsless/tools/jcmd/jfr.clj#L109-L112)</sub>
## `jcmd-options`

Build jcmd options for JFR commands.
  See the `(clojure.spec.alpha/form s)` for:
  - `:jcmd.jfr.options/start`
  - `:jcmd.jfr.options/stop`
  - `:jcmd.jfr.options/dump`
  - `:jcmd.jfr.options/check`
  
<br><sub>[source](https://github.com/bsless/tools.jvm/blob/master/src/bsless/tools/jcmd/jfr.clj#L10-L18)</sub>
## `p`
<sub>[source](https://github.com/bsless/tools.jvm/blob/master/src/bsless/tools/jcmd/jfr.clj#L169-L169)</sub>
## `record!`
``` clojure

(record! options)
```


Record with JFR for a finite duration and save to a recording file.
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
  `:path-to-gc-roots` - record paths to gc roots.
<br><sub>[source](https://github.com/bsless/tools.jvm/blob/master/src/bsless/tools/jcmd/jfr.clj#L145-L165)</sub>
