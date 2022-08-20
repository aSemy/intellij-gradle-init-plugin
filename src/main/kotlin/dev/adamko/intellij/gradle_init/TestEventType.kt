package dev.adamko.intellij.gradle_init


// https://github.com/JetBrains/intellij-community/blob/6faed1607b9d86aa29c1d8e02e1f2c3dfac27eea/plugins/gradle/java/src/execution/test/runner/events/TestEventType.java
enum class TestEventType(val value: String) {

  CONFIGURATION_ERROR("configurationError"),
  REPORT_LOCATION("reportLocation"),
  BEFORE_TEST("beforeTest"),
  ON_OUTPUT("onOutput"),
  AFTER_TEST("afterTest"),
  BEFORE_SUITE("beforeSuite"),
  AFTER_SUITE("afterSuite"),
  UNKNOWN_EVENT("unknown");


  companion object {
    val entries: Set<TestEventType> = values().toSet()

    fun fromValue(v: String): TestEventType =
      entries.firstOrNull { it.value == v } ?: UNKNOWN_EVENT
  }
}
