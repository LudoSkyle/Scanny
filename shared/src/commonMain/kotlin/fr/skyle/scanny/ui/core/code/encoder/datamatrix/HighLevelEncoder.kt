/*
 * Copied from qrose, itself ported from ZXing, and corrected where marked "Fixed from qrose".
 * Copyright (c) 2023 Alexander Zhirkevich, under the MIT License: see LICENSE in the encoder folder.
 */
package fr.skyle.scanny.ui.core.code.encoder.datamatrix

import io.github.alexzhirkevich.qrose.matrix.DataMatrixShape

/**
 * The data codewords of a text, in the ASCII encodation alone, and the smallest symbol that holds
 * them. Fixed from qrose, which switches to the C40, Text, X12, EDIFACT and Base256 encodations too,
 * and gets some of them wrong: an upper case letter in Text is written as the one before it. ASCII is
 * read by every scanner, at the cost of a larger symbol for a long text in upper case only.
 */
internal object HighLevelEncoder {

    // The highest character Data Matrix holds without an ECI: the end of ISO 8859-1
    const val MAX_CHAR = 255

    private const val PAD = 129
    private const val UPPER_SHIFT = 235
    private const val DIGIT_PAIR_OFFSET = 130

    fun encodeHighLevel(msg: String, shape: DataMatrixShape): Pair<IntArray, SymbolInfo> {
        require(msg.all { c -> c.code <= MAX_CHAR }) { "Data Matrix holds no character beyond ISO 8859-1" }

        val codewords = mutableListOf<Int>()
        var pos = 0
        while (pos < msg.length) {
            val c = msg[pos]
            val next = msg.getOrNull(pos + 1)
            when {
                // Two digits in one codeword
                isDigit(c) && next != null && isDigit(next) -> {
                    codewords.add((c - '0') * 10 + (next - '0') + DIGIT_PAIR_OFFSET)
                    pos += 2
                }

                c.code <= 127 -> {
                    codewords.add(c.code + 1)
                    pos++
                }

                // The upper half of ISO 8859-1, after a shift
                else -> {
                    codewords.add(UPPER_SHIFT)
                    codewords.add(c.code - 128 + 1)
                    pos++
                }
            }
        }

        val symbolInfo = SymbolInfo.lookup(codewords.size, shape)
        val capacity = symbolInfo.dataCapacity
        if (codewords.size < capacity) {
            codewords.add(PAD)
        }
        while (codewords.size < capacity) {
            codewords.add(randomize253State(PAD, codewords.size + 1))
        }

        return codewords.toIntArray() to symbolInfo
    }

    private fun randomize253State(codewordValue: Int, codewordPosition: Int): Int {
        val pseudoRandom = ((149 * codewordPosition) % 253) + 1
        val temp = codewordValue + pseudoRandom
        return if (temp <= 254) temp else temp - 254
    }

    private fun isDigit(c: Char): Boolean = c in '0'..'9'
}
