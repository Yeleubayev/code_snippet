package kz.mobile.mgov.profile.data.model

import com.google.gson.annotations.SerializedName
import kz.mobile.mgov.common.model.Title
import kz.mobile.mgov.common.utils.DD_MMM_YYYY
import kz.mobile.mgov.common.utils.TimeUtils

data class Timeline(@SerializedName("birthDateStr") val birthDateStr: String? = null,
                    @SerializedName("birthPlace") val birthPlace: Title? = null,
                    @SerializedName("clinicName") val clinicName: String? = null,
                    @SerializedName("doctorFullName") val doctorFullName: String? = null,
                    @SerializedName("documentType") val documentType: Title? = null,
                    @SerializedName("isBeginDate") val isBeginDate: Boolean,
                    @SerializedName("issueOrgName") val issueOrgName: Title? = null,
                    @SerializedName("number") val number: String? = null,
                    @SerializedName("date") val date: Long,
                    @SerializedName("strDate") val strDate: String,
                    @SerializedName("type") val type: String) {

    fun getParsedDate() = TimeUtils.formatMillisecondsToTimestamp(date, DD_MMM_YYYY)

    fun getTimelineType() = TimelineType.valueOf(type.toUpperCase())
}

enum class TimelineType {
    USER_INFO,
    USER_CHILDREN,
    USER_DOCUMENT,
    USER_CLINIC,
    USER_PP_CREATED,
    USER_DRIVER_LICENSE,
    USER_MBC_CREATED,
    USER_PEP_CREATED,
    USER_TECH_INSPECTION,
    USER_INDIVIDUAL_ENTREPRENEUR,
    USER_MARRIAGE,
    USER_ACCOMMODATION_QUEUE
}