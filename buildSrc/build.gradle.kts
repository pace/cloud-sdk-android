plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup:kotlinpoet:1.15.3")
    implementation("org.redundent:kotlin-xml-builder:1.9.1")
}
