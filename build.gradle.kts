plugins {
  `java-gradle-plugin`
  kotlin("jvm") version embeddedKotlinVersion
  `kotlin-dsl`
}


group = "dev.adamko"
version = "0.0.1-SNAPSHOT"


dependencies {
  implementation(kotlin("stdlib"))
}


tasks.wrapper {
  gradleVersion = "7.5.1"
  distributionType = Wrapper.DistributionType.ALL
}
