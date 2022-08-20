package dev.adamko.intellij.gradle_init

import dev.adamko.intellij.gradle_init.DynamicCastDelegate.Companion.dynamicCast
import org.opentest4j.AssertionFailedError


/** Adapter for external exceptions. */
internal sealed class TestExceptionData {


  object Assertion : TestExceptionData() {
    override fun toString(): String = this::class.java.name
  }


  data class Comparison(
    val expected: String,
    val actual: String,
  ) : TestExceptionData() {
    constructor(ex: ComparisonFailureAdapter) : this(
      expected = ex.getExpected(),
      actual = ex.getActual(),
    )
  }


  data class FileComparison(
    val expected: String,
    val actual: String,

    val filePath: String,
    val actualFilePath: String?,
  ) : TestExceptionData() {

    constructor(ex: FileComparisonFailureAdapter) : this(
      expected = ex.getExpected(),
      actual = ex.getActual(),
      filePath = ex.getFilePath(),
      actualFilePath = ex.getActualFilePath(),
    )
  }


  companion object {
    fun create(ex: Throwable?): TestExceptionData? {
      ex ?: return null

      return when (ex.javaClass.name) {
        "com.intellij.rt.execution.junit.FileComparisonFailure" -> {
          val e by dynamicCast<FileComparisonFailureAdapter> { ex }
          FileComparison(e)
        }

        "org.junit.ComparisonFailure",
        "junit.framework.ComparisonFailure"                     -> {
          val e by dynamicCast<ComparisonFailureAdapter> { ex }
          Comparison(e)
        }

        "junit.framework.AssertionFailedError"                  -> {
          Assertion
        }

        else                                                    -> when (ex) {
          is AssertionFailedError -> Comparison(
            expected = ex.expected?.stringRepresentation ?: "",
            actual = ex.actual?.stringRepresentation ?: "",
          )

          is AssertionError       -> {
            Assertion
          }

          else                    -> null
        }
      }
    }
  }
}


internal interface FileComparisonFailureAdapter {
  fun getFilePath(): String
  fun getActualFilePath(): String?
  fun getExpected(): String
  fun getActual(): String
}


internal interface ComparisonFailureAdapter {
  fun getExpected(): String
  fun getActual(): String
}
