package kz.mobile.mgov.profile.data.model

import com.google.gson.annotations.SerializedName
import kz.mobile.mgov.common.model.Title
import kz.mobile.mgov.common.utils.DD_MM_YYYY
import kz.mobile.mgov.common.utils.TimeUtils

data class IndividualEntrepreneur(
    @SerializedName("individualEntrepreneurActivityList") val activityList: List<EntrepreneurActivity>,
    @SerializedName("registrationDate") val registrationDate: Long,
    @SerializedName("name") val name: Title? = null,
    @SerializedName("address") val address: String,
    @SerializedName("blankSeries") val blankSeries: String,
    @SerializedName("blankNumber") val blankNumber: String,
    @SerializedName("deliveryDate") val deliveryDate: Long,
    @SerializedName("suspendStartDate") val suspendStartDate: Long? = null,
    @SerializedName("suspendEndDate") val suspendEndDate: Long? = null,
    @SerializedName("inactivityStartDate") val inactivityStartDate: Long? = null,
    @SerializedName("inactivityEndDate") val inactivityEndDate: Long? = null,
    @SerializedName("pseudoCompanyStartDate") val pseudoCompanyStartDate: Long? = null,
    @SerializedName("pseudoCompanyEndDate") val pseudoCompanyEndDate: Long? = null) {


    fun parsedDeliverDate() = TimeUtils.formatMillisecondsToTimestamp(deliveryDate, DD_MM_YYYY)

    fun parsedRegistrationDate() = TimeUtils.formatMillisecondsToTimestamp(registrationDate, DD_MM_YYYY)
}

data class EntrepreneurActivity(@SerializedName("activityCode") val activityCode: String,
                                @SerializedName("activityName") val activityName: Title? = null)