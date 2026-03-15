---
name: test
description: Run the Strong Buffs unit tests. Use this when the user wants to verify correctness, run tests, or check for regressions. Do not use for launching the dev client or building a JAR.
---

Run all unit tests:

```
./gradlew test
```

Report:
- Number of tests passed / failed / skipped
- For each failing test: the test name, failure message, and full stack trace
- Any build errors that prevented tests from running

Do not attempt to fix failing tests automatically — report results clearly to the user.
