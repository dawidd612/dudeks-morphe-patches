package app.template.patches.hogwartsmystery

import app.morphe.patcher.patch.RawResourcePatchBuilder
import app.morphe.patcher.patch.rawResourcePatch
import app.morphe.patcher.patch.stringOption

private fun hogwartsPatch(name: String, description: String, block: RawResourcePatchBuilder.() -> Unit) =
    rawResourcePatch(name = name, description = description, default = false) {
        compatibleWith(hogwartsCompatibility)
        dependsOn(verifyHogwartsBuild)
        block()
    }

@Suppress("unused")
val fasterHogwartsDialogue = hogwartsPatch(
    name = "Faster dialogue text",
    description = "Reveals dialogue text at 100 characters per second. Dialogue choices and manual advancement remain available.",
) {
    execute {
        // HPTypewriterLabel::applyDefaults has separate configured and fallback rate paths.
        editPotterLibraries(
            arm64 = listOf(
                PotterEdit(0x030663c8, "0000221e", "0059a852"),
                PotterEdit(0x030663d0, "605209bd", "605209b9"),
                PotterEdit(0x03066408, "0000221e", "0059a852"),
                PotterEdit(0x03066410, "605209bd", "605209b9"),
            ),
            x64 = listOf(
                PotterEdit(0x030152bf, "f30f2ac0f30f118370090000", "c783700900000000c8426690"),
                PotterEdit(0x030152fe, "f30f2ac0f30f118370090000", "c783700900000000c8426690"),
            ),
        )
    }
}

@Suppress("unused")
val fasterHogwartsActionFeedback = hogwartsPatch(
    name = "Faster action feedback",
    description = "Shortens the action button's local progress feedback from 0.3 to 0.125 seconds.",
) {
    execute {
        // InteractionActionButton::updateStats shares this duration with its scheduled callbacks.
        // The branch that uses a zero duration still does so.
        editPotterLibraries(
            arm64 = listOf(PotterEdit(0x0318bb18, "0101271e", "0110281e")),
            x64 = listOf(PotterEdit(0x0314330e, "f30f1005f2fc5efe", "f30f10057afc5efe")),
        )
    }
}

@Suppress("unused")
val fasterHogwartsInteractionFades = hogwartsPatch(
    name = "Faster interaction fades",
    description = "Speeds up the brief pause and fade in the action-button completion animation.",
) {
    val speed = stringOption(
        key = "speed",
        default = "fast",
        values = mapOf("Normal" to "normal", "Fast" to "fast", "Very Fast" to "very-fast"),
        title = "Interaction fade speed",
        description = "Each stage takes 0.5 seconds on Normal, 0.25 on Fast, or 0.125 on Very Fast.",
        required = true,
    )

    execute {
        // InteractionActionButton::onAdvanceProgress schedules DelayTime, FadeOut and CallFunc.
        // Edit only the two duration inputs to keep the sequence and callback intact.
        val mode = speed.value
        if (mode != "normal") {
            val veryFast = mode == "very-fast"
            val armDuration = if (veryFast) "0010281e" else "00102a1e"
            editPotterLibraries(
                arm64 = listOf(
                    PotterEdit(0x0318f508, "00102c1e", armDuration),
                    PotterEdit(0x0318f510, "00102c1e", armDuration),
                ),
                x64 = listOf(
                    PotterEdit(0x03146fc1, "f30f1005c3bc5efe", if (veryFast) "f30f1005c7bf5efe" else "f30f1005b7fd4ffe"),
                    PotterEdit(0x03146fd1, "f30f1005b3bc5efe", if (veryFast) "f30f1005b7bf5efe" else "f30f1005a7fd4ffe"),
                ),
            )
        }
    }
}

@Suppress("unused")
val quickerHogwartsRewardReveals = hogwartsPatch(
    name = "Quicker reward reveals",
    description = "Shortens three pauses in the reward reveal screen while keeping the original reward and collection flow.",
) {
    execute {
        // CollectRewardsVC::continueReveal passes 0.5 seconds to these three DelayTime calls.
        editPotterLibraries(
            arm64 = listOf(
                PotterEdit(0x024ec510, "00102c1e", "0010281e"),
                PotterEdit(0x024ec5bc, "00102c1e", "0010281e"),
                PotterEdit(0x024ec650, "00102c1e", "0010281e"),
            ),
            x64 = listOf(
                PotterEdit(0x023e2b7e, "f30f1005060135ff", "f30f10050a0435ff"),
                PotterEdit(0x023e2c39, "f30f10054b0035ff", "f30f10054f0335ff"),
                PotterEdit(0x023e2ce6, "f30f10059eff34ff", "f30f1005a20235ff"),
            ),
        )
    }
}

@Suppress("unused")
val hideHogwartsPaidGreeting = hogwartsPatch(
    name = "Hide automatic paid greeting",
    description = "Hides the automatic paid greeting pop-up. The shop and manually opened offers remain available.",
) {
    execute {
        // The void wrapper only queues the paid greeting. Callers do not wait for a result.
        editPotterLibraries(
            arm64 = listOf(PotterEdit(0x02ab2cdc, "e20301aa", "c0035fd6")),
            x64 = listOf(PotterEdit(0x02a1eaa0, "4889f2", "c36690")),
        )
    }
}

@Suppress("unused")
val quickerHogwartsCosmeticPreview = hogwartsPatch(
    name = "Quicker outfit previews",
    description = "Shortens avatar preview camera transitions when changing clothing categories or slots.",
) {
    execute {
        // CharacterViewerNode::updateConstraints passes the duration to AvatarBuilderSlotTransition.
        editPotterLibraries(
            arm64 = listOf(PotterEdit(0x02417c50, "88c9a772", "88b9a772")),
            x64 = listOf(PotterEdit(0x022fec1a, "f30f1005224143ff", "f30f10059e4343ff")),
        )
    }
}

@Suppress("unused")
val fasterHogwartsEnergyRegeneration = hogwartsPatch(
    name = "Faster energy regeneration",
    description = "Experimental. Regenerates normal energy about eight times faster below the cap. Actions still spend energy.",
) {
    execute {
        // EnergyPool::refreshSettings scales the configured interval after booster calculation.
        // ARM64 converts fixed-point seconds directly. x86_64 rounds up to a whole second
        // before conversion so a valid positive interval cannot become zero.
        editPotterLibraries(
            arm64 = listOf(PotterEdit(0x0285f7a0, "0800221e", "08f4021e")),
            x64 = listOf(
                PotterEdit(0x0279a0c0, "0f57c0f30f2ac0f30f1145d4", "f7d8c1f803f7d88945d46690"),
                PotterEdit(0x0279a0d1, "f30f1055d4", "f30f2a55d4"),
            ),
        )
    }
}
