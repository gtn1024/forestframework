include("app")
include("forest")
include("forest-kotlin")
include("forest-jsr311")
include("forest-jsr303")

rootProject.children.forEach { project ->
    project.buildFileName = "${project.name}.gradle.kts"
}