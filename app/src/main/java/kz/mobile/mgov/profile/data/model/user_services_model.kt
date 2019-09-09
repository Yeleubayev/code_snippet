package kz.mobile.mgov.profile.data.model

import com.google.gson.annotations.SerializedName
import kz.mobile.mgov.common.model.Title
import kz.mobile.mgov.common.utils.*
import kotlin.random.Random

data class AccommodationQueue(
    @SerializedName("id") val id: Int,
    @SerializedName("category") val category: Title? = null,
    @SerializedName("purpose") val purpose: Title? = null,
    @SerializedName("queueDate") val queueDate: Long = 0,
    @SerializedName("queueName") val queueName: Title? = null,
    @SerializedName("queuePosition") val queuePosition: String? = null,
    @SerializedName("regionCode") val regionCode: String? = null,
    @SerializedName("regionName") val regionName: Title? = null,
    @SerializedName("queueExist") val queueExist: Boolean = false
) {
    fun formattedQueueDate() = TimeUtils.formatMillisecondsToTimestamp(queueDate, DD_MM_YYYY)
}

data class Address(
    @SerializedName("id") val id: Int,
    @SerializedName("type") val type: String? = null,
    @SerializedName("countryCode") val countryCode: String? = null,
    @SerializedName("countryName") val countryName: Title? = null,
    @SerializedName("districtsName") val districtsName: Title? = null,
    @SerializedName("regionCode") val regionCode: String? = null,
    @SerializedName("regionName") val regionName: Title? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("street") val street: String? = null,
    @SerializedName("building") val building: String? = null,
    @SerializedName("corpus") val corpus: String? = null,
    @SerializedName("beginDate") val beginDate: String? = null,
    @SerializedName("endDate") val endDate: String? = null,
    @SerializedName("rka") val rka: String? = null
) {

    fun getFullAddress(): String {
        var fullAddress = "${countryName?.text}, ${districtsName?.text}, ${regionName?.text}"
        if (city != null && city.isNotEmpty()) {
            fullAddress += ", $city, "
        }
        if (street != null && street.isNotEmpty()) {
            fullAddress += "$street, "
        }
        if (building != null && building.isNotEmpty()) {
            fullAddress += building
        }
        return fullAddress
    }

    companion object {
        const val TYPE_BIRTH_PLACE = "BIRTH_PLACE"
        const val TYPE_REG_ADDRESS = "REG_ADDRESS"
    }
}

data class Auto(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("number") val number: String? = null,
    @SerializedName("model") val model: String? = null,
    @SerializedName("year") val year: String? = null,
    @SerializedName("vinCode") val vinCode: String? = null,
    @SerializedName("color") val color: String? = null,
    @SerializedName("techPassportNumber") val techPassportNumber: String? = null,
    @SerializedName("volume") val volume: String? = null,
    @SerializedName("power") val power: String? = null,
    @SerializedName("techInspection") val techInspection: TechInspection? = null
) {


    data class TechInspection(
        @SerializedName("inspectionDate") val inspectionDate: Long,
        @SerializedName("expirationDate") val expirationDate: Long,
        @SerializedName("passed") val passed: Boolean = false,
        @SerializedName("photo") val photo: String? = null
    ) {

        fun formattedInspectionDate() = TimeUtils.formatMillisecondsToTimestamp(inspectionDate, DD_MM_YYYY)

        fun formattedExpirationDate() = TimeUtils.formatMillisecondsToTimestamp(expirationDate, DD_MM_YYYY)
    }
}

data class Child(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("surname") val surname: String,
    @SerializedName("patronymic") val patronymic: String,
    @SerializedName("birthday") val birthday: Long,
    @SerializedName("birthCountryName") val birthCountryName: Title? = null,
    @SerializedName("birthDistrictName") val birthDistrictName: Title? = null,
    @SerializedName("birthRegionName") val birthRegionName: Title? = null,
    @SerializedName("birthCity") val birthCity: String? = null,
    @SerializedName("registrationDate") val registrationDate: Long,
    @SerializedName("zagsName") val zagsName: Title? = null,
    @SerializedName("type") val type: String,
    @SerializedName("iin") val iin: String,
    @SerializedName("actNumber") val actNumber: String
) {

    fun parsedBirthday() = TimeUtils.formatMillisecondsToTimestamp(birthday, DD_MMMM_YYYY)

    fun parsedRegistrationDate() = TimeUtils.formatMillisecondsToTimestamp(registrationDate, DD_MM_YYYY)
}

data class ClinicAttachment(
    @SerializedName("causeOfAttach") val causeOfAttach: String,
    @SerializedName("clinicAddress") val clinicAddress: Title? = null,
    @SerializedName("clinicName") val clinicName: String,
    @SerializedName("dateBegin") val dateBegin: Long,
    @SerializedName("doctorFullName") val doctorFullName: String,
    @SerializedName("numberTerritory") val numberTerritory: String,
    @SerializedName("patientFullName") val patientFullName: String) {

    fun getParsedAttachmentDate() = TimeUtils.formatMillisecondsToTimestamp(dateBegin, DD_MMMM_YYYY_hh_mm)
}


class DebtorRegister(
    @SerializedName("execProc") val execProc: String? = null,
    @SerializedName("execProcNum") val execProcNum: String? = null,
    @SerializedName("ipStartDate") val ipStartDate: Long = 0,
    @SerializedName("ipEndDate") val ipEndDate: Long = 0,
    @SerializedName("debtorIin") val debtorIin: String? = null,
    @SerializedName("debtorSurname") val debtorSurname: String? = null,
    @SerializedName("debtorFirstName") val debtorFirstName: String? = null,
    @SerializedName("debtorMiddleName") val debtorMiddleName: String? = null,
    @SerializedName("debtorBin") val debtorBin: String? = null,
    @SerializedName("debtorTitle") val debtorTitle: String? = null,
    @SerializedName("banStartDate") val banStartDate: Long = 0,
    @SerializedName("banEndDate") val banEndDate: Long = 0,
    @SerializedName("ilDate") val ilDate: Long = 0,
    @SerializedName("ilOrgan") val ilOrgan: Title? = null,
    @SerializedName("category") val category: Title? = null,
    @SerializedName("amount") val amount: Double = 0.toDouble(),
    @SerializedName("officerSurname") val officerSurname: String? = null,
    @SerializedName("officerFirstName") val officerFirstName: String? = null,
    @SerializedName("officerMiddleName") val officerMiddleName: String? = null,
    @SerializedName("disaName") val disaName: Title? = null,
    @SerializedName("disaDepartmentName") val disaDepartmentName: Title? = null,
    @SerializedName("disaDepartmentAddress") val disaDepartmentAddress: String? = null,
    @SerializedName("recovererType") val recovererType: Title? = null,
    @SerializedName("recovererIin") val recovererIin: String? = null,
    @SerializedName("recovererSurname") val recovererSurname: String? = null,
    @SerializedName("recovererFirstName") val recovererFirstName: String? = null,
    @SerializedName("recovererMiddleName") val recovererMiddleName: String? = null,
    @SerializedName("recovererBin") val recovererBin: String? = null,
    @SerializedName("recovererTitle") val recovererTitle: String? = null,
    @SerializedName("kbkCode") val kbkCode: String? = null,
    @SerializedName("kbkName") val kbkName: Title? = null,
    @SerializedName("knpCode") val knpCode: String? = null,
    @SerializedName("knpName") val knpName: Title? = null,
    @SerializedName("terminalNumber") val terminalNumber: String? = null,
    @SerializedName("banExist") val banExist: Boolean = false
)


data class DriverLicense(
    @SerializedName("category") val category: Any? = null,
    @SerializedName("categoryA") val categoryA: Boolean,
    @SerializedName("categoryA1") val categoryA1: Boolean,
    @SerializedName("categoryB") val categoryB: Boolean,
    @SerializedName("categoryB1") val categoryB1: Boolean,
    @SerializedName("categoryBE") val categoryBE: Boolean,
    @SerializedName("categoryC") val categoryC: Boolean,
    @SerializedName("categoryC1") val categoryC1: Boolean,
    @SerializedName("categoryC1E") val categoryC1E: Boolean,
    @SerializedName("categoryCE") val categoryCE: Boolean,
    @SerializedName("categoryD") val categoryD: Boolean,
    @SerializedName("categoryD1") val categoryD1: Boolean,
    @SerializedName("categoryD1E") val categoryD1E: Boolean,
    @SerializedName("categoryDE") val categoryDE: Boolean,
    @SerializedName("categoryE") val categoryE: Boolean,
    @SerializedName("categoryF") val categoryF: Boolean,
    @SerializedName("firstname") val firstName: String,
    @SerializedName("lastname") val lastName: String,
    @SerializedName("id") val id: Long,
    @SerializedName("iin") val iin: String,
    @SerializedName("issueDate") val issueDate: Long,
    @SerializedName("expireDate") val expireDate: Long,
    @SerializedName("number") val number: String,
    @SerializedName("owner") val owner: String,
    @SerializedName("patronymic") val patronymic: String,
    @SerializedName("serial") val serial: String
) {

    fun parsedExpireDate() = "Действ. до " + TimeUtils.formatMillisecondsToTimestamp(expireDate, MM_YYYY)

    fun parsedIssueDate() = "Выдан " + TimeUtils.formatMillisecondsToTimestamp(issueDate, MM_YYYY)


    fun getCategoriesList(): ArrayList<String> {
        val list = arrayListOf<String>()
        if (categoryA) {
            list.add("A")
        }
        if (categoryA1) {
            list.add("A1")
        }
        if (categoryB) {
            list.add("B")
        }
        if (categoryB1) {
            list.add("B1")
        }
        if (categoryBE) {
            list.add("BE")
        }
        if (categoryC) {
            list.add("C")
        }
        if (categoryC1) {
            list.add("C1")
        }
        if (categoryC1E) {
            list.add("C1E")
        }
        if (categoryCE) {
            list.add("CE")
        }
        if (categoryD) {
            list.add("D")
        }
        if (categoryD1) {
            list.add("D1")
        }
        if (categoryD1E) {
            list.add("D1E")
        }
        if (categoryDE) {
            list.add("DE")
        }
        if (categoryE) {
            list.add("E")
        }
        if (categoryF) {
            list.add("F")
        }
        return list
    }
}

data class LegalEntity(
    @SerializedName("id") val id: Long,
    @SerializedName("bin") val bin: String,
    @SerializedName("rnn") val rnn: String,
    @SerializedName("regNumber") val regNumber: String,
    @SerializedName("name") val name: Title? = null,
    @SerializedName("participantType") val participantType: String,
    @SerializedName("responseInfo") val responseInfo: Any? = null
)


data class License(
    @SerializedName("series") val series: String? = null,
    @SerializedName("number") val number: String? = null,
    @SerializedName("globalUniqueNumber") val globalUniqueNumber: String? = null,
    @SerializedName("nikad") val nikad: String? = null,
    @SerializedName("validityStartDate") val validityStartDate: Long,
    @SerializedName("activityType") val activityType: Title? = null,
    @SerializedName("licensiar") val licensiar: Title? = null,
    @SerializedName("stopSuspendDuplicateDate") val stopSuspendDuplicateDate: String? = null,
    @SerializedName("suspendingStartDate") val suspendingStartDate: String? = null,
    @SerializedName("suspendingEndDate") val suspendingEndDate: String? = null,
    @SerializedName("statusText") val staus: Title? = null
) {
    fun parsedValidityStartDate() = TimeUtils.formatMillisecondsToTimestamp(validityStartDate, DD_MM_YYYY)
}

data class Marriage(
    @SerializedName("actNumber") val actNumber: String,
    @SerializedName("id") val id: Int,
    @SerializedName("myLastnameBefore") val myLastnameBefore: String,
    @SerializedName("partnerFirstname") val partnerFirstname: String,
    @SerializedName("partnerIin") val partnerIin: String,
    @SerializedName("partnerLastname") val partnerLastname: String,
    @SerializedName("partnerLastnameAfter") val partnerLastnameAfter: String,
    @SerializedName("partnerPatronymic") val partnerPatronymic: String,
    @SerializedName("registrationDate") val registrationDate: Long,
    @SerializedName("zagsCode") val zagsCode: String,
    @SerializedName("zagsName") val zagsName: Title
) {
    fun getParsedRegistrationDate() = TimeUtils.formatMillisecondsToTimestamp(registrationDate, DD_MMMM_YYYY)
}


data class Realty(
    @SerializedName("id") val id: Int,
    @SerializedName("address") val address: Title? = null,
    @SerializedName("streetName") val streetName: Title? = null,
    @SerializedName("streetTypeCode") val streetTypeCode: String? = null,
    @SerializedName("streetTypeName") val streetTypeName: Title? = null,
    @SerializedName("buildingNumber") val buildingNumber: String? = null,
    @SerializedName("flatNumber") val flatNumber: String? = null,
    @SerializedName("postIndex") val postIndex: String? = null,
    @SerializedName("cadastralNumber") val cadastralNumber: String? = null,
    @SerializedName("corpusNumber") val corpusNumber: String? = null
) {
    fun getFullAddress(): String {
        var fullAddress = ""
        if (address != null && !address.text.isNullOrEmpty()) {
            fullAddress += "${address.text}, "
        }
        if (streetName != null && !streetName.text.isNullOrEmpty()) {
            fullAddress += "${streetName.text}, "
        }
        if (!buildingNumber.isNullOrEmpty()) {
            fullAddress += "$buildingNumber, "
        }
        if (!flatNumber.isNullOrEmpty()) {
            fullAddress += "$flatNumber"
        }
        return fullAddress
    }
}


data class RecommenderInfo(
    val id: Long = Random.nextLong(),
    @SerializedName("code") val code: String,
    @SerializedName("title") val title: Title? = null,
    @SerializedName("message") val message: Title? = null,
    @SerializedName("description") val description: Title? = null,
    @SerializedName("serviceList") val serviceList: List<ServicesInfo>
) : Comparable<RecommenderInfo> {

    override fun compareTo(other: RecommenderInfo): Int {
        return this.id.compareTo(other.id)
    }

    data class ServicesInfo(
        @SerializedName("name") val name: Title? = null,
        @SerializedName("description") val description: Title? = null,
        @SerializedName("code") val code: String,
        @SerializedName("link") val link: String,
        @SerializedName("type") val type: String,
        @SerializedName("usageCount") val usageCount: Long = 0
    )
}


data class SocialStatus(
    @SerializedName("id") val id: Long,
    @SerializedName("code") val code: String,
    @SerializedName("name") val title: Title? = null,
    @SerializedName("issueDate") val issueDate: Long,
    @SerializedName("expireDate") val expireDate: Long
) {
    fun formattedIssueDate() = TimeUtils.formatMillisecondsToTimestamp(issueDate, DD_MM_YYYY)

    fun formattedExpireDate() = TimeUtils.formatMillisecondsToTimestamp(expireDate, DD_MM_YYYY)
}

class TransportPenalty(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("violatorFullName") val violatorFullName: String? = null,
    @SerializedName("violationName") val violationName: String? = null,
    @SerializedName("violationPlace") val violationPlace: String? = null,
    @SerializedName("violationDate") val violationDate: Long = 0,
    @SerializedName("violationTimeText") val violationTimeText: String? = null,
    @SerializedName("departmentName") val departmentName: String? = null,
    @SerializedName("blankType") val blankType: String? = null,
    @SerializedName("blankSerial") val blankSerial: String? = null,
    @SerializedName("blankNumber") val blankNumber: String? = null,
    @SerializedName("penaltyCost") val penaltyCost: Float = 0f,
    @SerializedName("punishmentMain") val punishmentMain: String? = null,
    @SerializedName("punishmentAdditional") val punishmentAdditional: String? = null,
    @SerializedName("benName") val benName: String? = null,
    @SerializedName("benBank") val benBank: String? = null,
    @SerializedName("knp") val knp: String? = null,
    @SerializedName("knpName") val knpName: String? = null,
    @SerializedName("kno") val kno: String? = null,
    @SerializedName("identifier") val identifier: String? = null,
    @SerializedName("iinBin") val iinBin: String? = null,
    @SerializedName("requestNumber") val requestNumber: String? = null,
    @SerializedName("statusText") val status: String? = null
) {
    fun formattedDate() = TimeUtils.formatMillisecondsToTimestamp(violationDate, DD_MM_YYYY)
}
