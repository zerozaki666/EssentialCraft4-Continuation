
import java.util.jar.JarFile

plugins {
    id("com.gtnewhorizons.gtnhconvention")
}

// EC4 predates Git-based semantic versioning. Keep the Gradle artifact version
// aligned with the version declared by EssentialCraftCore.
extra["modVersion"] = "4.6.1710.70"

val requiredClientModels = listOf(
    "ChargingChamber.obj",
    "ColdDistillator.obj",
    "CrystalController.obj",
    "CrystalExtractor.obj",
    "CrystalFormer.obj",
    "Cube.obj",
    "DarknessObelisk.obj",
    "EnderGenerator.obj",
    "FlowerBurner.obj",
    "HeatGenerator.obj",
    "MIM.obj",
    "MIN_Ejector.obj",
    "MIN_Injector.obj",
    "MagicalEnchenter.obj",
    "MagicalJukebox.obj",
    "MagicalRepairer.obj",
    "MagicianTable.obj",
    "MagmaticSmeltery.obj",
    "MatrixAbsorber.obj",
    "MithrilineCrystal.obj",
    "MithrilineFurnace.obj",
    "MonsterHarvester.obj",
    "PotionSpreader.obj",
    "RadiatingChamber.obj",
    "SunrayCollector.obj",
    "assembler/Assembler.obj",
    "assembler/Mirror.obj",
    "board.obj",
    "divideSphere.obj",
    "mruCoilHardener.obj",
    "mruReactor_btm.obj",
    "sphere.obj",
).map { "assets/essentialcraft/textures/special/models/$it" }

val reobfJar = tasks.named("reobfJar")

val verifyClientModelResources by tasks.registering {
    group = "verification"
    description = "Verifies that the production jar contains every required EC4 OBJ model."
    dependsOn(reobfJar)

    doLast {
        val missingSources = requiredClientModels.filterNot {
            layout.projectDirectory.file("src/main/resources/$it").asFile.isFile
        }
        check(missingSources.isEmpty()) {
            "Required client model sources are missing:\n${missingSources.joinToString("\n")}"
        }

        val productionJars = reobfJar.get().outputs.files.files.filter {
            it.isFile && it.extension.equals("jar", ignoreCase = true)
        }
        check(productionJars.size == 1) {
            "Expected exactly one production jar from reobfJar, found: $productionJars"
        }

        JarFile(productionJars.single()).use { jar ->
            val missingPackagedModels = requiredClientModels.filter { jar.getEntry(it) == null }
            check(missingPackagedModels.isEmpty()) {
                "Production jar is missing required client models:\n${missingPackagedModels.joinToString("\n")}"
            }
            check(jar.manifest?.mainAttributes?.getValue("FMLCorePlugin") == null) {
                "Production jar unexpectedly registers the inactive legacy FML coremod."
            }
        }

        logger.lifecycle(
            "Verified ${requiredClientModels.size} required OBJ models in ${productionJars.single().name}",
        )
    }
}

tasks.named("check") {
    dependsOn(verifyClientModelResources)
}
