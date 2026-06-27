plugins {
    id("com.android.application") version "9.0.0" apply false
    id("com.android.library") version "9.0.0" apply false
    id("org.jetbrains.kotlin.android") version "2.2.10" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10" apply false
}

tasks.register("cleanStatus") {
    doLast {
        val f = file("app/src/main/java/montafra/beam/StatusService.kt")
        var content = f.readText()
        val start = content.indexOf("    private fun checkAlarms")
        if (start != -1) {
            val prefix = content.substring(0, start)
            f.writeText(prefix + "}\n")
        }
    }
}

