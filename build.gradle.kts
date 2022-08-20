plugins {
  `java-gradle-plugin`
  kotlin("jvm") version embeddedKotlinVersion
  `kotlin-dsl`
  `maven-publish`
}


group = "dev.adamko.intellij.gradle_init"
version = "0.0.1-SNAPSHOT"



//val fatJar by configurations.creating<Configuration> {
////  extendsFrom(configurations.implementation.get())
//  isTransitive = false
//}

dependencies {
  implementation(kotlin("stdlib"))

  implementation("org.redundent:kotlin-xml-builder:1.8.0")
  implementation("org.opentest4j:opentest4j:1.2.0")

//  compileOnly("junit:junit:4.13.2")
//  fatJar("junit:junit:4.13.2")
//  implementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
//  implementation("org.junit.jupiter:junit-jupiter-engine:5.9.0")
//  implementation("org.junit.jupiter:junit-jupiter-api:5.9.0")

//  implementation("org.jetbrains.intellij.plugins:gradle-intellij-plugin:1.8.0")

//  val ijPlatformVersion = "222.3739.57"
//  compileOnly("com.jetbrains.intellij.platform:test-framework:$ijPlatformVersion")
//  fatJar("com.jetbrains.intellij.platform:test-framework:$ijPlatformVersion")

//  implementation("com.jetbrains.intellij.platform:test-framework-core:$ijPlatformVersion")
//  implementation("com.jetbrains.intellij.platform:test-framework-rt:$ijPlatformVersion")
//  implementation("com.jetbrains.intellij.platform:test-framework-rt:$ijPlatformVersion")
//  implementation("com.jetbrains.intellij.platform:external-system-rt:$ijPlatformVersion")
//  implementation("com.jetbrains.intellij.platform:util-rt:$ijPlatformVersion")

}



gradlePlugin {
  plugins {
    create("dev.adamko.IntelliJGradleInitPlugin") {
      id = "dev.adamko.IntelliJGradleInitPlugin"
      implementationClass = "dev.adamko.intellij.gradle_init.IntellijGradleInitPlugin"
    }
  }
}

//publishing {
//  publications.create<MavenPublication>("mavenJava") {
//    from(components["java"])
//  }
//}


tasks.wrapper {
  gradleVersion = "7.5.1"
  distributionType = Wrapper.DistributionType.ALL
}


//
//
//tasks.jar {
//
////  from(sourceSets.main.map { it.output })
//
//  dependsOn(configurations.runtimeClasspath)
//
//
////  from({
////    configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
////  })
//
//  from({
//    fatJar
//      .filter { it.name.endsWith("jar") }
//      .map { jar ->
//        zipTree(jar).matching {
//          include("**/*.class")
//          exclude("/kotlin/**")
//          }
////          .filter { file ->
////            file.name.endsWith("class") && !file.path.startsWith("/kotlin")
////          }
//      }
//  })
//
//  duplicatesStrategy = DuplicatesStrategy.WARN
////  isZip64 = true
//}
