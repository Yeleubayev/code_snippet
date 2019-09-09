package kz.mobile.mgov.profile.data.model.response

import android.telephony.SubscriptionInfo
import com.google.gson.annotations.SerializedName
import kz.mobile.mgov.common.model.BaseResponse
import kz.mobile.mgov.document.data.MyDocument
import kz.mobile.mgov.profile.data.model.*

data class ClinicResponse(
    @SerializedName("clinicAttachment") val clinicAttachment: ClinicAttachment
) : BaseResponse()

data class ElectronicDocumentsListResponse(
    @SerializedName("myDocuments") val myDocuments: Any
) : BaseResponse()

data class TimelineResponse(
    @SerializedName("timelineInfo") val timelineInfo: List<Timeline>
) : BaseResponse()

data class MarriageResponse(
    @SerializedName("marriage") val marriage: Marriage
) : BaseResponse()

data class DriverLicenseResponse(
    @SerializedName("driverLicenseList") val list: List<DriverLicense>
) : BaseResponse()

data class LicensesResponse(
    @SerializedName("licenses") val list: List<License>,
    @SerializedName("licenseCount") val licenseCount: Int
) : BaseResponse()

data class ChildrenResponse(
    @SerializedName("children") val children: ChildrenWrapper
) : BaseResponse() {

    data class ChildrenWrapper(
        @SerializedName(
            "childList"
        ) val list: List<Child>
    )
}

data class LegalEntityResponse(
    @SerializedName(
        "legalEntityParticipants"
    ) val list: List<LegalEntity>
) : BaseResponse()

data class IndividualEntrepreneurResponse(
    @SerializedName("individualEntrepreneur") val individualEntrepreneur: IndividualEntrepreneur
) : BaseResponse()

data class MyDocumentsResponse(
    @SerializedName("myDocuments") val myDocument: MyDocument
) : BaseResponse()

data class SocialStatusResponse(
    @SerializedName("lastModifiedDate") val lastModifiedDate: Long,
    @SerializedName("id") val id: Long,
    @SerializedName("isSubscriptionExist") val isSubscriptionExist: Boolean,
    @SerializedName("socialStatusList") val list: List<SocialStatus>
) : BaseResponse()

data class AccommodationQueueResponse(
    @SerializedName("accommodationQueues") val data: AccommodationQueueList
) : BaseResponse() {

    data class AccommodationQueueList(
        @SerializedName("accommodationQueueList") val list: List<AccommodationQueue>?
    )
}

data class AutoResponse(
    @SerializedName("transportList") val list: List<Auto>?
) : BaseResponse()

data class TransportPenaltyResponse(
    @SerializedName("penaltyList") var penaltyList: List<TransportPenalty>?
) : BaseResponse()

data class DebtorRegisterResponse(
    @SerializedName("debtorRegisters") val debtorRegisters: List<DebtorRegister>
) : BaseResponse()

data class AddressResponse(
    @SerializedName("addressList") val addressList: List<Address>
) : BaseResponse()

data class SubscriptionInfoResponse(
    @SerializedName("subscriptionInfoList") val list: List<SubscriptionInfo>
) : BaseResponse()