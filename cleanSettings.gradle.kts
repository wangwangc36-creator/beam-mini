tasks.register("cleanSettings") {
    doLast {
        val f = file("app/src/main/java/montafra/beam/ui/SettingsScreen.kt")
        var content = f.readText()
        val start = content.indexOf("if (showDonateDialog) {")
        val end = content.indexOf("@Composable\ninternal fun SectionHeader(text: String) {")
        if (start != -1 && end != -1) {
            content = content.removeRange(start, end)
            f.writeText(content)
        }
    }
}
