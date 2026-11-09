import org.gradle.api.JavaVersion

object Config {
    const val COMPILE_SDK = 37
    const val TARGET_SDK = 37
    const val MIN_SDK = 24
    val javaVersion = JavaVersion.VERSION_21
}
