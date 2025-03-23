plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "lift-drop"

include("LiftDropApi")
include("LiftDropDomain")
include("LiftDropServices")
