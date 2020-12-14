package cloud.pace.sdk.idkit

import com.squareup.moshi.Json

data class UserInfoResponse(
    val id: String?,
    @Json(name = "first_name")
    val firstName: String?,
    @Json(name = "last_name")
    val lastName: String?,
    @Json(name = "is_email_verified")
    val isEmailVerified: Boolean?,
    val email: String?
)
