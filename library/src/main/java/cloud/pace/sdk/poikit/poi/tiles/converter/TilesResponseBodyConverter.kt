package cloud.pace.sdk.poikit.poi.tiles.converter

import TileQueryResponseOuterClass
import cloud.pace.sdk.poikit.POIKit
import cloud.pace.sdk.poikit.geo.ConnectedFuelingStatus
import cloud.pace.sdk.poikit.geo.GeoAPIManagerImpl.Companion.PAYMENT_METHOD_KINDS_KEY
import cloud.pace.sdk.poikit.poi.GasStation
import cloud.pace.sdk.poikit.poi.Geometry
import cloud.pace.sdk.poikit.poi.LocationPoint
import cloud.pace.sdk.poikit.poi.tiles.TileInformation
import cloud.pace.sdk.poikit.utils.GeoMathUtils
import cloud.pace.sdk.poikit.utils.OSMKeys
import cloud.pace.sdk.utils.resume
import com.google.protobuf.InvalidProtocolBufferException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.ResponseBody
import retrofit2.Converter
import timber.log.Timber
import vector_tile.VectorTile
import java.util.Date
import kotlin.math.atan
import kotlin.math.exp

class TilesResponseBodyConverter : Converter<ResponseBody, List<GasStation>> {

    override fun convert(value: ResponseBody): List<GasStation>? {
        return try {
            val result = TileQueryResponseOuterClass.TileQueryResponse.parseFrom(value.byteStream())
            val pois = mutableListOf<GasStation>()

            result.vectorTilesList.forEach { vectorTile ->
                val tileInformation = TileInformation(result.zoom, vectorTile.geo.x, vectorTile.geo.y)
                pois.addAll(loadPois(vectorTile, tileInformation))
            }

            runBlocking {
                try {
                    val cofuGasStations = requestCofuGasStations()
                    val cofuGasStationsMap = cofuGasStations.associateBy { it.id }

                    pois.forEach {
                        it.apply {
                            updatedAt = Date()
                            cofuGasStation = cofuGasStationsMap[it.id]
                            isOnlineCoFuGasStation = cofuGasStation?.connectedFuelingStatus == ConnectedFuelingStatus.ONLINE

                            val paymentMethodKinds = cofuGasStation?.properties?.get(PAYMENT_METHOD_KINDS_KEY) as? List<*>
                            cofuPaymentMethods = paymentMethodKinds?.filterIsInstance<String>()?.toMutableList() ?: mutableListOf()
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Could not request CoFu gas stations")

                    pois.forEach {
                        it.updatedAt = Date()
                    }
                }
            }

            pois
        } catch (e: InvalidProtocolBufferException) {
            Timber.e(e, "Failed to parse protobuffer response")
            null
        }
    }

    private fun loadPois(vectorTile: TileQueryResponseOuterClass.TileQueryResponse.VectorTile, tileInformation: TileInformation): ArrayList<GasStation> {
        val tile = VectorTile.Tile.parseFrom(vectorTile.vectorTiles.toByteArray())

        // Get POI Layer
        val poiLayer = tile.layersList.filter { it.name == OSMKeys.OSM_POI }[0]
        val pois = ArrayList<GasStation>()

        for (feature in poiLayer.featuresList) {
            val values = GeoMathUtils.getValues(feature, poiLayer)
            val commands = buildGeometry(feature, tileInformation, poiLayer.extent)

            val typeString = values[OSMKeys.OSM_TYPE] ?: continue
            val id = values[OSMKeys.OSM_ID] ?: continue

            when (typeString) {
                OSMKeys.OSM_GAS_STATION -> {
                    val poi = GasStation(id, commands)
                    poi.init(values)
                    pois.add(poi)
                }
            }
        }
        return pois
    }

    private fun buildGeometry(feature: VectorTile.Tile.Feature, tileInformation: TileInformation, extent: Int): ArrayList<Geometry.CommandGeo> {
        val commands = Geometry.processGeometry(feature)
        val commandsGeo = ArrayList<Geometry.CommandGeo>()

        for (command in commands) {
            val size = extent.toDouble() * Math.pow(2.0, tileInformation.zoomLevel.toDouble())
            val castX = (tileInformation.x * extent).toDouble()
            val castY = (tileInformation.y * extent).toDouble()

            val lonDeg = (command.point.coordX + castX) * 360.0 / size - 180.0
            val latRad = 180.0 - (command.point.coordY + castY) * 360.0 / size
            val latDeg = 360.0 / Math.PI * atan(exp(latRad * Math.PI / 180.0)) - 90.0
            val locationPoint = LocationPoint(latDeg, lonDeg)

            val commandGeo = Geometry.CommandGeo(command.type, locationPoint)
            commandsGeo.add(commandGeo)
        }
        return commandsGeo
    }

    private suspend fun requestCofuGasStations() = suspendCancellableCoroutine { continuation ->
        POIKit.requestCofuGasStations {
            continuation.resume(it)
        }
    }
}
