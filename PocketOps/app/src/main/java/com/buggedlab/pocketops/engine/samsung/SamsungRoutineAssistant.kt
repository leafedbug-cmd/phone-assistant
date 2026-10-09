package com.buggedlab.pocketops.engine.samsung

import android.content.Context
import android.content.Intent
import com.buggedlab.pocketops.engine.capability.DeviceCapabilityDetector

data class SamsungRoutineBlueprint(
    val title: String,
    val description: String,
    val samsungIfConditions: List<String>,
    val samsungThenActions: List<String>,
    val guidanceSteps: List<String>
)

class SamsungRoutineAssistant(private val context: Context) {
    private val detector = DeviceCapabilityDetector(context)

    fun getRecommendedBlueprints(): List<SamsungRoutineBlueprint> {
        return listOf(
            SamsungRoutineBlueprint(
                title = "Galaxy Deep Work Mode",
                description = "Maximizes focus during working hours using One UI's native system hooks.",
                samsungIfConditions = listOf("Time period: 08:00 - 17:00 (Mon, Tue, Wed, Thu, Fri)"),
                samsungThenActions = listOf(
                    "Do Not Disturb: Turn on",
                    "Grayscale / Dark mode: Turn on",
                    "Restrict app usage: Social & Games"
                ),
                guidanceSteps = listOf(
                    "1. Tap 'Open Samsung Routines' below.",
                    "2. Tap the '+' button in Modes and Routines to create a new routine.",
                    "3. In 'If', add 'Time period' (08:00 to 17:00, Mon-Fri).",
                    "4. In 'Then', add 'Do Not Disturb' and 'Display: Grayscale'.",
                    "5. Tap 'Save' to activate natively in One UI."
                )
            ),
            SamsungRoutineBlueprint(
                title = "Galaxy Bedtime Battery & Calm",
                description = "Night routine to protect battery health and silence overnight notifications.",
                samsungIfConditions = listOf(
                    "Time: 22:30 - 06:30",
                    "Charging state: Charging"
                ),
                samsungThenActions = listOf(
                    "Eye comfort shield: Turn on",
                    "Protect battery (limit charge to 80%): Turn on",
                    "Always On Display: Turn off"
                ),
                guidanceSteps = listOf(
                    "1. Open Modes and Routines on your Galaxy S24 Ultra.",
                    "2. Add 'If Charging' and 'Time 22:30 - 06:30'.",
                    "3. Add 'Then Protect Battery' (Battery care).",
                    "4. Add 'Eye Comfort Shield: Enhanced comfort'.",
                    "5. Save."
                )
            ),
            SamsungRoutineBlueprint(
                title = "Galaxy Commute / Driving Focus",
                description = "Automatically silences unnecessary alerts when connected to your car Bluetooth.",
                samsungIfConditions = listOf("Bluetooth device: Connected to Car BT"),
                samsungThenActions = listOf(
                    "Read notifications aloud: Only VIP calls",
                    "Mute promotional and social notifications"
                ),
                guidanceSteps = listOf(
                    "1. Open Modes and Routines.",
                    "2. In 'If', select 'Bluetooth device' and pick your vehicle.",
                    "3. In 'Then', select 'Do Not Disturb' -> allow calls from starred contacts.",
                    "4. Save."
                )
            )
        )
    }

    fun openSamsungRoutinesApp(): Intent? {
        return detector.getSamsungModesAndRoutinesIntent()
    }
}
