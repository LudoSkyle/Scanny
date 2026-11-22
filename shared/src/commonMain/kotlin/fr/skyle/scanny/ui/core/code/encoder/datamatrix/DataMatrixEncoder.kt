/*
 * Copied from qrose, itself ported from ZXing, and corrected where marked "Fixed from qrose".
 * Copyright (c) 2023 Alexander Zhirkevich, under the MIT License: see LICENSE in the encoder folder.
 */
package fr.skyle.scanny.ui.core.code.encoder.datamatrix

import io.github.alexzhirkevich.qrose.matrix.DataMatrixShape
import io.github.alexzhirkevich.qrose.matrix.Matrix2D
import io.github.alexzhirkevich.qrose.matrix.MatrixCodeEncoder
import fr.skyle.scanny.ui.core.code.encoder.common.GenericGF
import fr.skyle.scanny.ui.core.code.encoder.common.ReedSolomonEncoder

internal class DataMatrixEncoder(
    private val shape: DataMatrixShape = DataMatrixShape.Auto
) : MatrixCodeEncoder {

    public override fun encode(
        data: String,
    ): Matrix2D {
        val (dataCodewords, symbolInfo) = HighLevelEncoder.encodeHighLevel(data, shape)
        val allCodewords = calculateErrorCorrection(dataCodewords, symbolInfo)

        val placement = DataMatrixPlacement(
            codewords = allCodewords,
            numcols = symbolInfo.symbolDataWidth,
            numrows = symbolInfo.symbolDataHeight
        )
        placement.place()

        return assembleMatrix(placement, symbolInfo)
    }

    private fun calculateErrorCorrection(
        dataCodewords: IntArray,
        symbolInfo: SymbolInfo
    ): IntArray {
        val blockCount = symbolInfo.rsBlocks
        val rsEncoder = ReedSolomonEncoder(GenericGF.DATA_MATRIX_FIELD_256)

        if (blockCount == 1) {
            val full = IntArray(symbolInfo.totalCodewords)
            dataCodewords.copyInto(full, 0, 0, dataCodewords.size)
            rsEncoder.encode(full, symbolInfo.errorCodewords)
            return full
        }

        val totalData = symbolInfo.dataCapacity
        val totalEc = symbolInfo.errorCodewords
        val ecPerBlock = totalEc / blockCount

        val dataBlocks = Array(blockCount) { b ->
            val len = symbolInfo.getDataLengthForInterleavedBlock(b + 1)
            IntArray(len + ecPerBlock)
        }

        // Distribute data codewords interleaved into blocks
        for (i in 0 until totalData) {
            val b = i % blockCount
            val idx = i / blockCount
            dataBlocks[b][idx] = dataCodewords[i]
        }

        // Calculate RS for each block
        for (b in 0 until blockCount) {
            rsEncoder.encode(dataBlocks[b], ecPerBlock)
        }

        // Interleave output: first data codewords, then error correction codewords
        val result = IntArray(symbolInfo.totalCodewords)
        var outIdx = 0

        for (i in 0 until totalData) {
            result[outIdx++] = dataCodewords[i]
        }

        // Fixed from qrose: the codewords are interleaved over the whole stream, a codeword going to the
        // block of its position. The error correction of the 144 × 144 symbol, whose last two blocks hold
        // one data codeword less, starts at its ninth block, as ZXing reads it
        val firstBlock = totalData % blockCount
        for (k in 0 until ecPerBlock * blockCount) {
            val b = (firstBlock + k) % blockCount
            val dataLen = symbolInfo.getDataLengthForInterleavedBlock(b + 1)
            result[outIdx++] = dataBlocks[b][dataLen + k / blockCount]
        }

        return result
    }

    private fun assembleMatrix(
        placement: DataMatrixPlacement,
        symbolInfo: SymbolInfo
    ): Matrix2D {
        val matrix = Matrix2D(symbolInfo.matrixWidth, symbolInfo.matrixHeight)
        val regW = symbolInfo.dataRegionWidth
        val regH = symbolInfo.dataRegionHeight

        for (ry in 0 until symbolInfo.verticalDataRegions) {
            for (rx in 0 until symbolInfo.horizontalDataRegions) {
                val startX = rx * (regW + 2)
                val startY = ry * (regH + 2)

                // Left border: solid dark
                for (dy in 0 until regH + 2) {
                    matrix[startX, startY + dy] = true
                }

                // Bottom border: solid dark
                for (dx in 0 until regW + 2) {
                    matrix[startX + dx, startY + regH + 1] = true
                }

                // Top border: alternating clock track (dx % 2 == 0 is dark)
                for (dx in 0 until regW + 2) {
                    matrix[startX + dx, startY] = (dx % 2 == 0)
                }

                // Right border: alternating clock track (dy % 2 == 1 is dark)
                for (dy in 0 until regH + 2) {
                    matrix[startX + regW + 1, startY + dy] = (dy % 2 == 1)
                }

                // Internal data modules
                for (dy in 1..regH) {
                    for (dx in 1..regW) {
                        val dataX = rx * regW + (dx - 1)
                        val dataY = ry * regH + (dy - 1)
                        matrix[startX + dx, startY + dy] = placement.getBit(dataX, dataY)
                    }
                }
            }
        }

        return matrix
    }
}
