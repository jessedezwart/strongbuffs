---
name: build
description: Build the Strong Buffs plugin JAR. Use this when the user wants to compile the plugin, produce a distributable JAR, or check for build errors. Do not use for running the dev client or unit tests.
---

Build the fat shadow JAR:

```
./gradlew shadowJar
```

On success: report the output JAR path (typically `build/libs/strongbuffs-<version>-all.jar`).

On failure: show the full error output and identify the root cause (compile error, missing dependency, etc.).

Do not attempt to fix errors automatically — report them clearly to the user.
