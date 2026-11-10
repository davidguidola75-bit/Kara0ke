package com.example.data.model

data class LyricLine(
    val timestampMs: Long,
    val text: String
)

object LrcParser {
    private val LRC_REGEX = Regex("""\[(\d{1,2}):(\d{2})(?:\.(\d{1,3}))?](.*)""")

    fun parse(lrcContent: String): List<LyricLine> {
        val lines = mutableListOf<LyricLine>()
        lrcContent.lineSequence().forEach { line ->
            val match = LRC_REGEX.find(line.trim())
            if (match != null) {
                val min = match.groupValues[1].toLongOrNull() ?: 0L
                val sec = match.groupValues[2].toLongOrNull() ?: 0L
                val rawMillis = match.groupValues[3]
                val millis = when (rawMillis.length) {
                    1 -> rawMillis.toLong() * 100
                    2 -> rawMillis.toLong() * 10
                    3 -> rawMillis.toLong()
                    else -> 0L
                }
                val totalMs = min * 60_000L + sec * 1_000L + millis
                val text = match.groupValues[4].trim()
                if (text.isNotEmpty() || lines.isNotEmpty()) {
                    lines.add(LyricLine(totalMs, text))
                }
            } else {
                // If it's a plain line without timestamp, ignore metadata tags like [ti:...]
                val trimmed = line.trim()
                if (trimmed.isNotEmpty() && !trimmed.startsWith("[") && !trimmed.endsWith("]")) {
                    // Fallback line
                    lines.add(LyricLine(0L, trimmed))
                }
            }
        }
        return lines.sortedBy { it.timestampMs }
    }
}
