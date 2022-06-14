package cloud.pace.sdk.idkit.userinfo

import com.google.gson.annotations.SerializedName

data class UserInfoResponse(
    @SerializedName("sub")
    val subject: String? = null,
    @SerializedName("zoneinfo")
    val zoneInfo: String? = null,
    @SerializedName("email_verified")
    val emailVerified: Boolean? = null,
    @SerializedName("created_at")
    val createdAt: Long? = null,
    val locale: String? = null,
    val email: String? = null
)
