rootProject.name = "intellij-gradle-init-plugin"

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
  pluginManagement {
    repositories {
      gradlePluginPortal()
      mavenCentral()
    }
  }

  repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
}
