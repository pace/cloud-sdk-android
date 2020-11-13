package cloud.pace.sdk.poikit.poi

import android.util.Log
import cloud.pace.sdk.poikit.poi.download.VectorTile

class Geometry {

    companion object {

        private val TAG = "Geometry"

        private fun getParamCount(ct: CommandType?): Int {
            return 2 // Both for MOVETO AND LINETO
        }

        private fun makeCommand(type: Int): CommandType? {
            return when (type) {
                1 -> CommandType.MOVETO
                2 -> CommandType.LINETO
                else -> null
            }
        }

        fun processGeometryWithList(geometry: List<Int>, featureGeomType: VectorTile.Tile.GeomType): ArrayList<Command> {
            val commands = ArrayList<Command>()
            var index = 0
            while (index < geometry.size) {
                // Read command type and parameter count:
                val cac = extractCommandTypeAndCount(geometry[index])

                // Special case for point features
                if (featureGeomType == VectorTile.Tile.GeomType.POINT && cac.commandType == CommandType.MOVETO && cac.count == 0) {
                    cac.count = 2
                }

                index++
                var index2 = 0
                val parameters = ArrayList<Int>()
                while (index2 < cac.count && index < geometry.size) {
                    val value = geometry[index]
                    val param = value shr 1 xor -(value and 1)
                    parameters.add(param)
                    index2++
                    index++
                }
                if (parameters.size == cac.count && cac.commandType != null) { // Only parse valid commands understood by app
                    commands.addAll(convertToCommands(cac.commandType, parameters))
                } else {
                    Log.d(TAG, "malformed command in geometry") // This should not happen in unit test!
                }
            }
            return convertPoints(commands)
        }

        fun processGeometry(feature: VectorTile.Tile.Feature): ArrayList<Command> {
            return processGeometryWithList(feature.geometryList, feature.type)
        }

        private fun extractCommandTypeAndCount(value: Int): CommandTypeAndCount {
            val commandInteger = value and 0x7
            val count = value shr 3
            val type = makeCommand(commandInteger)
            return CommandTypeAndCount(type, count * getParamCount(type))
        }

        private fun convertToCommands(commandType: CommandType?, params: ArrayList<Int>): ArrayList<Command> {
            val points = ArrayList<Point>()
            var index = 0
            while (index < params.size) {
                val cmdX = params[index]
                index++
                if (index < params.size) {
                    val cmdY = params[index]
                    points.add(Point(cmdX.toDouble(), cmdY.toDouble()))
                    index++
                }
            }
            val commands = ArrayList<Command>(params.size)
            for (point in points) {
                commands.add(Command(commandType, point))
            }
            return commands
        }

        private fun convertPoints(commands: ArrayList<Command>): ArrayList<Command> {
            val newCommands = ArrayList<Command>()
            for (command in commands) {
                if (newCommands.size > 0) {
                    val previous = newCommands[newCommands.size - 1]
                    val newPoint = Point(
                        command.point.coordX + previous.point.coordX,
                        command.point.coordY + previous.point.coordY
                    )
                    newCommands.add(Command(command.type, newPoint))
                } else {
                    newCommands.add(command)
                }
            }
            return newCommands
        }
    }

    internal data class CommandTypeAndCount(var commandType: CommandType?, var count: Int)

    data class Command(var type: CommandType?, var point: Point)

    data class CommandGeo(
        var commandType: CommandType?,
        var locationPoint: LocationPoint
    ) {
        override fun toString(): String {
            return "$commandType: $locationPoint"
        }

        override fun hashCode(): Int {
            return locationPoint.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            if (other is CommandGeo) {
                return other.locationPoint.lat == locationPoint.lat && other.locationPoint.lon == locationPoint.lon
            }
            return false
        }
    }

    enum class CommandType {
        MOVETO,
        LINETO,
        OTHER;
    }
}
