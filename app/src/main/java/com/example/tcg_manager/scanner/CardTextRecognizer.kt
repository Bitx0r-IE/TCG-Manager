package com.example.tcg_manager.scanner

import android.graphics.Bitmap
import com.example.tcg_manager.data.model.CardGame
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

/** What ML Kit read from a card photo. */
data class CardOcrResult(
    val fullText: String,
    /** Most likely names, best first. */
    val nameCandidates: List<String>,
    /** Game guessed from the text, null if unclear. */
    val detectedGame: CardGame?,
)

/** On-device OCR of a card photo with ML Kit (Latin script; no network needed). */
class CardTextRecognizer {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /** @param image an upright bitmap (already rotated to match the card orientation). */
    suspend fun recognize(image: Bitmap): CardOcrResult {
        val text = recognizer.process(InputImage.fromBitmap(image, 0)).await()
        val lines = text.textBlocks
            .flatMap { it.lines }
            .sortedBy { it.boundingBox?.top ?: Int.MAX_VALUE }
            .map { it.text }
        return CardOcrResult(
            fullText = text.text,
            nameCandidates = CardNameExtractor.candidates(lines),
            detectedGame = CardGameDetector.detect(text.text),
        )
    }

    fun close() = recognizer.close()
}
