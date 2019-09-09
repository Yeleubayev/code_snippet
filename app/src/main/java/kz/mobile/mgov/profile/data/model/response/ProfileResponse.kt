package kz.mobile.mgov.profile.data.model.response

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import kz.mobile.mgov.common.model.BaseResponse

data class ProfileResponse(
    @SerializedName("profile") val profile: JsonObject
): BaseResponse()

