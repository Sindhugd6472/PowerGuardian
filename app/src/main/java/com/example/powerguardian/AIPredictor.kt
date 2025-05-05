package com.example.powerguardian

object AIPredictor {
    fun predictUserState(): String {
        val state = (1..3).random()
        return when (state) {
            1 -> "Idle"
            2 -> "Moderate Use"
            else -> "Heavy Use"
        }
    }
}
