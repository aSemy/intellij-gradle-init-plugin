# intellij-gradle-init-plugin

Proposal for improving IntelliJ compatibility with Gradle incremental tasks

https://youtrack.jetbrains.com/issue/IDEA-240111

### What's wrong?

IntelliJ breaks a Gradle feature for avoiding re-running tasks.

Fixing this is really important. It would have a huge impact on users, as their workflow would
become faster.

Additionally, the power requirements of repeatedly re-running the same tests would be avoided,
helping reduce energy usage worldwide.

### Where is it wrong?

When running Gradle using IntelliJ, IntelliJ injects a `init.gradle` init
script, [`testFilterInit.gradle`](https://github.com/JetBrains/intellij-community/blob/bfcb5f072de24ff83060b1b11ce5f9064e58fc6d/plugins/gradle/tooling-extension-impl/src/org/jetbrains/plugins/gradle/tooling/internal/init/testFilterInit.gradle#L16)
. This file does many things, including adding this code:

```groovy
taskGraph.allTasks.each { Task task ->
  if (task instanceof Test || (abstractTestTaskClass != null && abstractTestTaskClass.isAssignableFrom(task.class))) {

    task.outputs.upToDateWhen { false } // this breaks 

  }
}
```

This breaks
[Gradle up-to-date checks](https://docs.gradle.org/current/userguide/more_about_tasks.html#sec:up_to_date_checks)
for *all* test tasks! This forces *all* tests tasks to run, even if
nothing has changed. This is very wasteful
([Gradle recommends against it](https://blog.gradle.org/stop-rerunning-tests))
and can hugely increase the amount of time spent running tests.

### What's the proposal?

1. Migrate from a Groovy init script, to
   an [Init Script Plugin](https://docs.gradle.org/current/userguide/init_scripts.html#sec:init_script_plugins)
   .

   This makes the change much more modular, easier to test and debug, and can be written in Kotlin.

   (Extra bonus: the init script plugin can be updated independently of an IntelliJ release -
   meaning bug fixes can be released faster.)

2. When the test tasks run, they'll log the IJLog XML to stdout as normal. Additionally, they'll
   print the XML messages to file, and register that file as a task output.

3. The init script plugin will register an additional
   task, [`IJTestEventLoggerTask`](https://github.com/aSemy/intellij-gradle-init-plugin/blob/main/src/main/kotlin/dev/adamko/intellij/gradle_init/IJTestEventLoggerTask.kt)
   , that will
   collect all of those files.

4. [`IJTestEventLoggerTask`](https://github.com/aSemy/intellij-gradle-init-plugin/blob/main/src/main/kotlin/dev/adamko/intellij/gradle_init/IJTestEventLoggerTask.kt)
   will determine if the test tasks have already run, and if they
   haven't (because
   they're [up-to-date](https://docs.gradle.org/current/userguide/more_about_tasks.html#sec:task_outcomes))
   ,
   will print them to stdout.
