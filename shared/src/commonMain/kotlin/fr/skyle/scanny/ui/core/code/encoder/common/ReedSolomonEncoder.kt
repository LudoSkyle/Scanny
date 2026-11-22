/*
 * Copied from qrose, itself ported from ZXing, and corrected where marked "Fixed from qrose".
 * Copyright (c) 2023 Alexander Zhirkevich, under the MIT License: see LICENSE in the encoder folder.
 */
package fr.skyle.scanny.ui.core.code.encoder.common

internal class ReedSolomonEncoder(private val field: GenericGF) {

    private val cachedGenerators = mutableListOf<GenericGFPoly>().apply {
        add(GenericGFPoly(field, intArrayOf(1)))
    }

    private fun buildGenerator(degree: Int): GenericGFPoly {
        if (degree >= cachedGenerators.size) {
            var lastGenerator = cachedGenerators.last()
            for (d in cachedGenerators.size..degree) {
                val nextGenerator = lastGenerator.multiply(
                    GenericGFPoly(field, intArrayOf(1, field.exp(d - 1 + field.generatorBase)))
                )
                cachedGenerators.add(nextGenerator)
                lastGenerator = nextGenerator
            }
        }
        return cachedGenerators[degree]
    }

    fun encode(toEncode: IntArray, ecBytes: Int) {
        require(ecBytes > 0) { "No error correction bytes" }
        val dataBytes = toEncode.size - ecBytes
        require(dataBytes > 0) { "No data bytes provided" }

        val generator = buildGenerator(ecBytes)
        val infoCoefficients = IntArray(dataBytes)
        toEncode.copyInto(infoCoefficients, 0, 0, dataBytes)

        val info = GenericGFPoly(field, infoCoefficients)
        val shifted = info.multiplyByMonomial(ecBytes, 1)
        val remainder = shifted.divide(generator)[1]
        val coefficients = remainder.coefficients
        val numZeroCoefficients = ecBytes - coefficients.size

        for (i in 0 until numZeroCoefficients) {
            toEncode[dataBytes + i] = 0
        }
        coefficients.copyInto(toEncode, dataBytes + numZeroCoefficients, 0, coefficients.size)
    }
}
