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
