plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "LiftDrop"

include("LiftDropApi")
include("LiftDropDomain")
include("LiftDropServices")
include("LiftDropPipeline")
include("LiftDropRepository")
include("LiftDropRepositoryJdbi")
include("LiftDrop")
