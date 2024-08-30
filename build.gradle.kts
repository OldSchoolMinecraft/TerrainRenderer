plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(files("libs/lwjgl.jar"))
    implementation(files("libs/lwjgl-glfw.jar"))
    implementation(files("libs/lwjgl-opengl.jar"))
    implementation("com.google.guava:guava:33.3.0-jre")
}