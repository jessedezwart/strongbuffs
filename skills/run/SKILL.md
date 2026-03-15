---
name: run
description: Start the Strong Buffs RuneLite dev client. Use this when the user wants to run, launch, or test the plugin in-game. Do not use for building a JAR or running unit tests.
---

Run the Gradle `run` task to launch the RuneLite dev client with the plugin loaded:

```
./gradlew run
```

This automatically passes `--developer-mode` and `--debug` to the client.

On success: the client window opens — no further action needed.

On compile failure: show the full error output and identify the file and line number.

On runtime failure: show the last 50 lines of output and the stack trace.

Do not attempt to fix errors automatically — report them clearly to the user.
