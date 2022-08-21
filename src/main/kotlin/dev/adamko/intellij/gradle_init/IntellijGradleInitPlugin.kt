package dev.adamko.intellij.gradle_init

import java.net.URL
import java.net.URLClassLoader
import javax.inject.Inject
import org.gradle.api.Plugin
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType


abstract class IntellijGradleInitPlugin @Inject constructor(
  private val files: FileSystemOperations,
) : Plugin<Gradle> {

  private val logger: Logger = Logging.getLogger(this::class.java)


  override fun apply(target: Gradle) {

    target.allprojects {

      val ijTestLogFiles = project.files(tasks.withType<Test>()) {
        filter { file ->
          file.name.endsWith(IJ_TEST_EVENT_LOG_FILE_EXT)
        }
      }


      val ijTestEventLoggerTask = tasks.register<IJTestEventLoggerTask>(
        IJ_TEST_EVENT_LOGGER_TASK_NAME
      ) {
//        dependsOn(tasks.withType<Test>())

        logFileDirectories.from(ijTestLogFiles)
      }


      tasks.withType<Test>().configureEach {
        finalizedBy(ijTestEventLoggerTask)

        val testReportDir = temporaryDir.resolve("ijTestEvents/")

        outputs.dir(testReportDir)
          .withPropertyName("ijTestEvents")
          .optional(true)

        val ijTestEventLogger = IJTestEventLogger(testReportDir)
        addTestListener(ijTestEventLogger)
        addTestOutputListener(ijTestEventLogger)

        doFirst {
          // delete previous test results
          files.delete { delete(testReportDir) }
          testReportDir.mkdirs()
        }
      }
    }

    target.taskGraph.whenReady {
      allTasks.filterIsInstance<Test>().forEach { task ->
        enhanceGradleDaemon(task)

        // copied from Groovy, not sure why this is here
        task.testLogging.showStandardStreams = false
      }
    }
  }

  private fun enhanceGradleDaemon(task: Test) {
    try {
      task.doFirst {
        try {
          val urls = task.classpath.files.filter {
            it.name == "idea_rt.jar" || it.name.startsWith("junit")
          }.map { it.toURI().toURL() }

          val daemonMainClass = Class.forName("org.gradle.launcher.daemon.bootstrap.DaemonMain")
          logger.debug("[GradleDaemonClasspathService] got DaemonMain class $daemonMainClass")
          val classLoader = daemonMainClass.classLoader
          logger.debug("[GradleDaemonClasspathService] got DaemonMain classloader $classLoader")

          if (classLoader is URLClassLoader) {
            classLoader.addURLs(urls)
            val currentURLs = classLoader.urLs.toSet()
            if (currentURLs.containsAll(urls)) {
              val missingURLs = urls - currentURLs
              logger.error("unable to enhance gradle daemon classloader with idea_rt.jar (missingURLs: ${missingURLs}, currentURLs: $currentURLs))")
            } else {
              logger.info("enhanced gradle daemon classloader with idea_rt.jar (urls: $urls, currentURLs: $currentURLs)")
            }
          } else {
            logger.error("unable to enhance gradle daemon classloader with idea_rt.jar (unknown ClassLoader $classLoader)")
          }
        } catch (ex: RuntimeException) {
          logger.error("unable to enhance gradle daemon classloader with idea_rt.jar", ex)
        }
      }
    } catch (ex: Throwable) {
      logger.error("", ex)
    }
  }

  companion object {

    // URLClassLoader.addURL() method is protected, so here's an adapter
    private fun URLClassLoader.addURLs(urls: Collection<URL>) =
      object : URLClassLoader(urLs, parent) {
        init {
          urls.forEach { url -> addURL(url) }
        }
      }

    const val IJ_TEST_EVENT_LOGGER_TASK_NAME = "ijTestEventLogger"
    const val IJ_TEST_EVENT_LOG_FILE_EXT = ".ij_test.log"
  }
}
