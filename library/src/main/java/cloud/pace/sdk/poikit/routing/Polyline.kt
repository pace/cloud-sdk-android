package cloud.pace.sdk.poikit.routing

import cloud.pace.sdk.poikit.poi.LocationPoint

class Polyline() {
    var coordinates: List<LocationPoint>? = null
    var levels: Int? = null

    constructor(encodedPolyline: String, precision: Int = 5) : this() {
        coordinates = decodePolyline(encodedPolyline, precision)
    }

    constructor(points: List<LocationPoint>?) : this() {
        coordinates = points
    }

    /**
     * Decode Polyline encoded by Google's algorithm.
     * Source: https://gist.github.com/ghiermann/ed692322088bb39166a669a8ed3a6d14
     */
    private fun decodePolyline(polyline: String, precision: Int = 5): List<LocationPoint>? {
        val coordinateChunks: MutableList<MutableList<Int>> = mutableListOf()
        coordinateChunks.add(mutableListOf())

        for (char in polyline.toCharArray()) {
            // convert each character to decimal from ascii
            var value = char.toInt() - 63

            // values that have a chunk following have an extra 1 on the left
            val isLastOfChunk = (value and 0x20) == 0
            value = value and (0x1F)

            coordinateChunks.last().add(value)

            if (isLastOfChunk)
                coordinateChunks.add(mutableListOf())
        }

        coordinateChunks.removeAt(coordinateChunks.lastIndex)

        val coordinates: MutableList<Double> = mutableListOf()

        for (coordinateChunk in coordinateChunks) {
            var coordinate = coordinateChunk.mapIndexed { i, chunk -> chunk shl (i * 5) }.reduce { i, j -> i or j }

            // there is a 1 on the right if the coordinate is negative
            if (coordinate and 0x1 > 0)
                coordinate = (coordinate).inv()

            coordinate = coordinate shr 1
            coordinates.add((coordinate).toDouble() / 100000.0)
        }

        val points: MutableList<LocationPoint> = mutableListOf()
        var previousX = 0.0
        var previousY = 0.0

        for (i in 0..coordinates.size - 1 step 2) {
            if (coordinates[i] == 0.0 && coordinates[i + 1] == 0.0)
                continue

            previousX += coordinates[i + 1]
            previousY += coordinates[i]

            points.add(LocationPoint(roundWithPrecision(previousY, precision), roundWithPrecision(previousX, precision)))
        }
        return points
    }

    private fun roundWithPrecision(value: Double, precision: Int) =
        (value * Math.pow(10.0, precision.toDouble())).toInt().toDouble() / Math.pow(10.0, precision.toDouble())

    fun encode(): String {
        val result: MutableList<String> = mutableListOf()

        var prevLat = 0
        var prevLong = 0

        coordinates?.let {
            for (coord in it) {
                val lat = coord.lat
                val long = coord.lon
                val iLat = (lat * 1e5).toInt()
                val iLong = (long * 1e5).toInt()

                val deltaLat = encodeValue(iLat - prevLat)
                val deltaLong = encodeValue(iLong - prevLong)

                prevLat = iLat
                prevLong = iLong

                result.add(deltaLat)
                result.add(deltaLong)
            }
        }
        return result.joinToString("")
    }

    private fun encodeValue(value: Int): String {
        // Step 2 & 4
        var actualValue = if (value < 0) (value shl 1).inv() else (value shl 1)

        // Step 5-8
        val chunks: List<Int> = splitIntoChunks(actualValue)

        // Step 9-10
        return chunks.map { (it + 63).toChar() }.joinToString("")
    }

    private fun splitIntoChunks(toEncode: Int): List<Int> {
        // Step 5-8
        val chunks = mutableListOf<Int>()
        var value = toEncode
        while (value >= 32) {
            chunks.add((value and 31) or (0x20))
            value = value shr 5
        }
        chunks.add(value)
        return chunks
    }
}
