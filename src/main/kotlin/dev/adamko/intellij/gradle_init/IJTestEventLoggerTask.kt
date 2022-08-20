package dev.adamko.intellij.gradle_init

import dev.adamko.intellij.gradle_init.IntellijGradleInitPlugin.Companion.IJ_TEST_EVENT_LOG_FILE_EXT
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction


abstract class IJTestEventLoggerTask : DefaultTask() {

  @get:InputFiles
  abstract val logFileDirectories: ConfigurableFileCollection


  init {
    super.getOutputs().upToDateWhen { false } // always run this task
  }


  // try and use Worker API, hopefully IO doesn't block Gradle
  @TaskAction
  fun exec() {

    // find all log files
    val files = logFileDirectories.files.flatMap { dir ->
      dir.walk()
        .filter { it.isFile && it.name.endsWith(IJ_TEST_EVENT_LOG_FILE_EXT) }
    }

    logger.lifecycle("[IJTestEventLoggerTask] checking ${files.size} files")

    files
      .sorted()
      .forEach { file ->
        file.bufferedReader().useLines { lines ->
          lines.forEach { line ->
//          logger.lifecycle(line)
            println(line) // IntelliJ will listen on StdOut for each message
          }
        }
      }
  }
}
