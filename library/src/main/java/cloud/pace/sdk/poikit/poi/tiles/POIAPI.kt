package cloud.pace.sdk.poikit.poi.tiles

import TileQueryRequestOuterClass
import cloud.pace.sdk.api.API
import cloud.pace.sdk.api.poi.POIAPI.gasStations
import cloud.pace.sdk.api.poi.POIAPI.tiles
import cloud.pace.sdk.api.poi.generated.request.gasStations.GetGasStationAPI.getGasStation
import cloud.pace.sdk.api.poi.generated.request.tiles.GetTilesAPI.getTiles
import cloud.pace.sdk.poikit.poi.GasStation
import retrofit2.Response
import retrofit2.await
import retrofit2.awaitResponse

interface POIAPI {

    suspend fun getTiles(
        body: TileQueryRequestOuterClass.TileQueryRequest,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ): List<GasStation>

    suspend fun getGasStation(
        id: String,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ): Response<cloud.pace.sdk.api.poi.generated.model.GasStation>
}

class POIAPIImpl : POIAPI {

    override suspend fun getTiles(
        body: TileQueryRequestOuterClass.TileQueryRequest,
        readTimeout: Long?,
        additionalHeaders: Map<String, String>?,
        additionalParameters: Map<String, String>?
    ): List<GasStation> {
        return API.tiles.getTiles(body, readTimeout, additionalHeaders, additionalParameters).await()
    }

    override suspend fun getGasStation(
        id: String,
        readTimeout: Long?,
        additionalHeaders: Map<String, String>?,
        additionalParameters: Map<String, String>?
    ): Response<cloud.pace.sdk.api.poi.generated.model.GasStation> {
        return API.gasStations.getGasStation(id, readTimeout, additionalHeaders, additionalParameters).awaitResponse()
    }
}
