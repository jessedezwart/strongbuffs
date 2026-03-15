Build and run the Strong Buffs RuneLite dev client.

Run the Gradle `run` task which launches the dev client with `--developer-mode --debug`:
```
./gradlew run
```

If it fails to compile, show the full error output and identify which file and line has the issue.
If it fails to start (runtime error), show the stack trace and last 50 lines of output.
Do not attempt to fix errors automatically — report them clearly to the user.
