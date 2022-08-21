package dev.adamko.intellij.gradle_init

import dev.adamko.intellij.gradle_init.IntellijGradleInitPlugin.Companion.IJ_TEST_EVENT_LOG_FILE_EXT
import java.io.File
import java.security.MessageDigest
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.LocalState
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault


@DisableCachingByDefault(because = "state managed internally")
abstract class IJTestEventLoggerTask : DefaultTask() {

  @get:InputFiles
  abstract val logFileDirectories: ConfigurableFileCollection


  /**
   * Track whether the files [logFileDirectories] have changed since last execution. Used to
   * determine if this task should run.
   */
  @get:LocalState
  protected val stateFile: File = temporaryDir.resolve("state.md5")


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

    val currentState = computeMd5(files)

    stateFile.createNewFile() // create it, just in case it doesn't already exist
    val storedState = stateFile.readText().trim()

    logger.lifecycle("[IJTestEventLoggerTask] currentState: $currentState, storedState: $storedState")

    if (currentState == storedState) {
      logger.lifecycle("[IJTestEventLoggerTask] printing stored logs from $files")
      // We are go to launch! The input files haven't changed, therefore the Test tasks didn't run.
      // Since they didn't run, they didn't directly print the IJ Test XML to stdout.
      printStoredLogs(files)
    } else {
      logger.lifecycle("[IJTestEventLoggerTask] skipping printing - test tasks run")
      // Abort! The input files have changed, therefore the Test tasks ran, and they directly
      // logged the IJ Test XML to stdout
    }

    // finally, update the state file
    stateFile.writeText(currentState)
  }


  private fun printStoredLogs(files: Collection<File>) {
    files
      .sorted() // the files are named by timestamp
      .forEach { file ->
        file.bufferedReader().useLines { lines ->
          lines.forEach { line ->
            // IntelliJ will listen on stdout for each message
            println(line)
          }
        }
      }
  }


  companion object {

    private fun computeMd5(files: Collection<File>): String {
      val md5 = MessageDigest.getInstance("MD5")
      files.forEach { md5.update(it.readBytes()) }

      return md5.digest().toHexString()
    }

    private fun ByteArray.toHexString(): String =
      joinToString(separator = "") { byte -> "%02x".format(byte) }
  }
}
