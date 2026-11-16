package com.example.tcg_manager.scanner

import com.example.tcg_manager.data.model.CardGame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CardScannerLogicTest {

    @Test
    fun detectsMagicCard() {
        val text = "Lightning Bolt\nInstant\nLightning Bolt deals 3 damage to any target.\n™ & © Wizards of the Coast"
        assertEquals(CardGame.MAGIC, CardGameDetector.detect(text))
    }

    @Test
    fun detectsPokemonCard() {
        val text = "BASIC\nPikachu 60 HP\nThundershock 20\nweakness resistance retreat cost\n©Nintendo"
        assertEquals(CardGame.POKEMON, CardGameDetector.detect(text))
    }

    @Test
    fun unclearTextGivesNoGame() {
        assertNull(CardGameDetector.detect("hello world"))
    }

    @Test
    fun extractsNameAndSkipsNoise() {
        val lines = listOf("BASIC", "Pikachu 60 HP", "Thundershock", "Weakness")
        assertEquals(listOf("Pikachu", "Thundershock", "Weakness"), CardNameExtractor.candidates(lines))
    }

    @Test
    fun stripsManaSymbolsReadAsDigits() {
        val lines = listOf("Serra Angel 3 @@", "Creature — Angel")
        assertEquals("Serra Angel", CardNameExtractor.candidates(lines).first())
    }
}
