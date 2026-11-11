import org.gradle.api.JavaVersion

object Config {
    const val COMPILE_SDK: Int = 37
    const val TARGET_SDK: Int = 37
    const val MIN_SDK: Int = 24
    val javaVersion: JavaVersion = JavaVersion.VERSION_21
}
