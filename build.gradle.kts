plugins {
    kotlin("jvm") version "2.3.0"
    application
}

dependencies {
    // Исследуемая версия библиотеки
    implementation("io.insert-koin:koin-core:4.2.2")
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("demo.MainKt")
}
