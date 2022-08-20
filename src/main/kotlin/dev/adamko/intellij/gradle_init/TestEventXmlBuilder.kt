package dev.adamko.intellij.gradle_init

import java.io.PrintWriter
import java.io.StringWriter
import java.util.Base64
import org.gradle.api.internal.tasks.testing.TestDescriptorInternal
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestOutputEvent
import org.gradle.api.tasks.testing.TestResult
import org.redundent.kotlin.xml.Node
import org.redundent.kotlin.xml.PrintOptions
import org.redundent.kotlin.xml.xml

class TestEventXmlBuilder {

  private val logger: Logger = Logging.getLogger(this::class.java)

  fun build(
    testEventType: TestEventType,
    testDescriptor: TestDescriptor,
    testEvent: TestOutputEvent?,
    testResult: TestResult?,
  ): String {

    return xml("event") {
      attributes {
        "type" += testEventType.value
      }

      "test" {
        attributes {
          "id" += testDescriptor.id ?: ""
          "parentId" += testDescriptor.parent?.id ?: ""
        }

        "descriptor" {
          attributes {
            "name" += testDescriptor.name
            "displayName" += testDescriptor.displayName
            "className" += testDescriptor.className ?: ""
          }

          if (testEvent != null) {
            "event" {
              attributes {
                "destination" += testEvent.destination.name
              }
              cdataEncoded(testEvent.message)
            }
          }
        }

        if (testResult != null) {

          "result" {
            attributes {
              "resultType" += testResult.resultType.name
              "startTime" += testResult.startTime.toString()
              "endTime" += testResult.endTime.toString()
            }

            val exception = testResult.exception

            if (exception == null) {
              "failureType" { -"error" }
            } else {

              "errorMsg" {
                cdataEncoded(exception.message ?: "")
              }
              "stackTrace" {
                cdataEncoded(exception.getPrintedStackTrace())
              }

              val testExceptionData = TestExceptionData.create(exception)
                ?: TestExceptionData.create(exception.cause)

              logger.lifecycle("exception ${exception.javaClass.name} was mapped to $testExceptionData. toString:$exception")

              when (testExceptionData) {

                TestExceptionData.Assertion         -> {
                  "failureType" { -"assertionFailed" }
                }

                is TestExceptionData.Comparison     -> {
                  "failureType" { -"comparison" }
                  "expected" { cdataEncoded(testExceptionData.expected) }
                  "actual" { cdataEncoded(testExceptionData.actual) }
                }

                is TestExceptionData.FileComparison -> {
                  "failureType" { -"comparison" }
                  "expected" { cdataEncoded(testExceptionData.expected) }
                  "actual" { cdataEncoded(testExceptionData.actual) }
                  "filePath" { cdataEncoded(testExceptionData.filePath) }
                  if (testExceptionData.actualFilePath != null) {
                    "actualFilePath" { cdataEncoded(testExceptionData.actualFilePath) }
                  }
                }

                null                                -> {
                  "failureType" { -"error" }
                }
              }
            }
          }
        }
      }
    }.toString(xmlPrintOptions)
  }

  companion object {

    private fun Throwable.getPrintedStackTrace(): String {
      return StringWriter().use { sw ->
        this.printStackTrace(PrintWriter(sw))
        sw.toString()
      }
    }

    private inline val TestDescriptor.id: String?
      get() = (this as? TestDescriptorInternal)?.id?.toString()


    private class NodeAttributeAdder(private val node: Node) {
      operator fun String.plusAssign(value: String) {
        node.attribute(this, value)
      }
    }

    private fun Node.attributes(build: NodeAttributeAdder.() -> Unit) {
      NodeAttributeAdder(this).build()
    }

    private val xmlPrintOptions = PrintOptions(
      pretty = false,
      singleLineTextElements = true,
      useSelfClosingTags = true
    )

    private fun Node.cdataEncoded(text: String) {
      val encoded = Base64.getEncoder().encodeToString(text.toByteArray())
      cdata(encoded)
    }

  }
}
