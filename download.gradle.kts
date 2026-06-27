tasks.register("downloadRepo") {
    doLast {
        val zipFile = file("repo.zip")
        ant.invokeMethod("get", mapOf("src" to "https://github.com/montafra/beam/archive/refs/heads/master.zip", "dest" to zipFile))
        copy {
            from(zipTree(zipFile))
            into("beam_full")
        }
    }
}
