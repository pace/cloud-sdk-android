package cloud.pace.sdk.poikit.poi.download

import TileQueryRequestOuterClass
import TileQueryResponseOuterClass
import cloud.pace.sdk.api.utils.InterceptorUtils
import cloud.pace.sdk.poikit.POIKit
import cloud.pace.sdk.poikit.geo.ConnectedFuelingStatus
import cloud.pace.sdk.poikit.poi.GasStation
import cloud.pace.sdk.poikit.poi.Geometry
import cloud.pace.sdk.poikit.poi.LocationPoint
import cloud.pace.sdk.poikit.utils.ApiException
import cloud.pace.sdk.poikit.utils.GeoMathUtils
import cloud.pace.sdk.poikit.utils.OSMKeys
import cloud.pace.sdk.poikit.utils.OSMKeys.OSM_GAS_STATION
import cloud.pace.sdk.poikit.utils.OSMKeys.OSM_ID
import cloud.pace.sdk.poikit.utils.OSMKeys.OSM_TYPE
import cloud.pace.sdk.poikit.utils.POIKitConfig
import cloud.pace.sdk.utils.Environment
import cloud.pace.sdk.utils.Success
import cloud.pace.sdk.utils.requestId
import com.google.protobuf.InvalidProtocolBufferException
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import vector_tile.VectorTile
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.math.atan
import kotlin.math.exp

class TileDownloader(environment: Environment) {
    private val client =
        OkHttpClient.Builder()
            .addInterceptor {
                it.proceed(
                    it.request()
                        .newBuilder()
                        .header(InterceptorUtils.USER_AGENT_HEADER, InterceptorUtils.getUserAgent())
                        .header(InterceptorUtils.UBER_TRACE_ID_HEADER, InterceptorUtils.getUberTraceId())
                        .build()
                )
            }
            .connectTimeout(POIKitConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(POIKitConfig.READ_TIMEOUT, TimeUnit.SECONDS)
            .build()

    private var poiTileBaseUrl = "${environment.apiUrl}/poi/v1/tiles/query"
    private val mediaType = "application/protobuf".toMediaTypeOrNull()

    fun load(job: TileQueryRequestOuterClass.TileQueryRequest, handler: (Result<List<GasStation>>) -> Unit): Call {
        val content = job.toByteArray()
        val request = Request.Builder()
            .url(poiTileBaseUrl)
            .method("POST", content.toRequestBody(mediaType, 0, content.size))
            .build()

        val call = client.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Timber.e(e, "Request failed for URL: ${call.request().url}")
                handler(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Timber.e(
                        ApiException(response.code, response.message, response.requestId),
                        "Request unsuccessful for URL: ${call.request().url}"
                    )
                    handler(Result.failure(Exception("Request failed with code: ${response.code}")))
                    return
                }

                val body = response.body
                if (body == null) {
                    Timber.e(
                        ApiException(response.code, response.message, response.requestId),
                        "Missing response body for URL: ${call.request().url}"
                    )
                    handler(Result.failure(Exception("Missing response body")))
                    return
                }

                try {
                    val result = TileQueryResponseOuterClass.TileQueryResponse.parseFrom(body.byteStream())
                    val pois = mutableListOf<GasStation>()

                    result.vectorTilesList.forEach { vectorTile ->
                        val tileInformation = TileInformation(result.zoom, vectorTile.geo.x, vectorTile.geo.y)
                        pois.addAll(this@TileDownloader.loadPois(vectorTile, tileInformation))
                    }
                    POIKit.requestCofuGasStations {
                        when (it) {
                            is Success -> {
                                val cofuStationMap = it.result.map { it.id to it }.toMap()
                                pois.forEach { station ->
                                    station.updatedAt = Date()
                                    station.isOnlineCoFuGasStation = cofuStationMap[station.id]?.let { it.connectedFuelingStatus == ConnectedFuelingStatus.ONLINE }
                                }
                                handler(Result.success(pois))
                            }
                            else -> {
                                pois.forEach { station ->
                                    station.updatedAt = Date()
                                }
                                handler(Result.success(pois))
                            }
                        }
                    }
                } catch (e: InvalidProtocolBufferException) {
                    Timber.e(
                        ApiException(response.code, response.message, response.requestId),
                        "Failed to parse protobuffer response for URL: ${call.request().url}"
                    )
                    handler(Result.failure(e))
                }
            }
        })

        return call
    }

    private fun loadPois(vectorTile: TileQueryResponseOuterClass.TileQueryResponse.VectorTile, tileInformation: TileInformation): ArrayList<GasStation> {
        val tile = VectorTile.Tile.parseFrom(vectorTile.vectorTiles.toByteArray())

        // Get POI Layer
        val poiLayer = tile.layersList.filter { it.name == OSMKeys.OSM_POI }[0]
        val pois = ArrayList<GasStation>()

        for (feature in poiLayer.featuresList) {
            val values = GeoMathUtils.getValues(feature, poiLayer)
            val commands = buildGeometry(feature, tileInformation, poiLayer.extent)

            val typeString = values[OSM_TYPE] ?: continue
            val id = values[OSM_ID] ?: continue

            when (typeString) {
                OSM_GAS_STATION -> {
                    val poi = GasStation(id, commands)
                    poi.init(values)
                    pois.add(poi)
                }
            }
        }
        return pois
    }

    private fun buildGeometry(
        feature: VectorTile.Tile.Feature,
        tileInformation: TileInformation,
        extent: Int
    ): ArrayList<Geometry.CommandGeo> {
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
}
