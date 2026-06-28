pluginManagement {
    plugins {
        kotlin("jvm") version "2.1.21"
    }
}
rootProject.name = "GraphleManager"

include(
    "common",
    "model",
    "tag",
    "connection",
    "autocomplete",
    "file",
    "application",
    "dsl",
)
