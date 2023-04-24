package cloud.pace.sdk.poikit.poi.tiles.converter

import TileQueryRequestOuterClass
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Converter

class TilesRequestBodyConverter : Converter<TileQueryRequestOuterClass.TileQueryRequest, RequestBody> {

    private val mediaType = "application/protobuf".toMediaTypeOrNull()

    override fun convert(value: TileQueryRequestOuterClass.TileQueryRequest): RequestBody {
        val content = value.toByteArray()
        return content.toRequestBody(mediaType, 0, content.size)
    }
}
