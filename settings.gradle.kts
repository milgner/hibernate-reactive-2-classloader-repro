rootProject.name = "hr2cl"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include("api")
include("app")
include("plugins")
for (dir in File("${rootDir.absolutePath}/plugins").list { dir, name ->
    File("${dir.absolutePath}/$name").isDirectory
}) {
    include("plugins:$dir")
}
