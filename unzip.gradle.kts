tasks.register("unzipRepo") {
    doLast {
        copy {
            from(zipTree(file("repo.zip")))
            into("beam_full")
        }
    }
}
