rootProject.name = "intellij-gradle-init-plugin"

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  repositories {
    mavenCentral()
    mavenLocal()
    gradlePluginPortal()
    maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
    maven("https://www.jetbrains.com/intellij-repository/releases")
    maven("https://cache-redirector.jetbrains.com/intellij-dependencies")
    maven("https://cache-redirector.jetbrains.com/www.jetbrains.com/intellij-repository")
    maven("https://cache-redirector.jetbrains.com/plugins.jetbrains.com/maven")
    maven("https://cache-redirector.jetbrains.com/intellij-jbr")
    maven("https://cache-redirector.jetbrains.com/packages.jetbrains.team/maven/p/intellij-plugin-verifier/intellij-plugin-verifier")

    // platform/build-scripts/groovy/org/jetbrains/intellij/build/CommunityLibraryLicenses.kt
    maven("https://packages.jetbrains.team/maven/p/grazi/grazie-platform-public")
    maven("https://cache-redirector.jetbrains.com/download-pgp-verifier")
  }
  pluginManagement {
    repositories {
      gradlePluginPortal()
      mavenCentral()
    }
  }

  repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
}
