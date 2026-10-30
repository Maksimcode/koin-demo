rootProject.name = "koin-demo"

pluginManagement {
    repositories {
        // Зеркало Maven Central от Google: на некоторых сетях repo.maven.apache.org отвечает 403
        maven("https://maven-central.storage-download.googleapis.com/maven2/")
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        // Зеркало Maven Central от Google: на некоторых сетях repo.maven.apache.org отвечает 403
        maven("https://maven-central.storage-download.googleapis.com/maven2/")
        mavenCentral()
    }
}
