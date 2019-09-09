package kz.mobile.mgov.profile.data.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Completable
import io.reactivex.Single
import kz.mobile.mgov.common.model.SubscriptionCode
import kz.mobile.mgov.common.model.SubscriptionInfo
import kz.mobile.mgov.common.network.NetworkConstants
import kz.mobile.mgov.common.repository.user.UserApi
import kz.mobile.mgov.common.security.SecurityUtils
import kz.mobile.mgov.common.storage.AppFileUtils
import kz.mobile.mgov.common.storage.PrefUtils
import kz.mobile.mgov.common.utils.DD_MMMM_YYYY_hh_mm
import kz.mobile.mgov.common.utils.DD_MMM_YYYY
import kz.mobile.mgov.common.utils.TimeUtils
import kz.mobile.mgov.document.data.*
import kz.mobile.mgov.profile.data.api.ProfileApi
import kz.mobile.mgov.profile.data.model.*
import javax.inject.Inject

interface UserServiceRepository {
    fun getAddresses(): Single<List<Address>>
    fun getDocuments(): Single<List<DocumentInfo>>
    fun getBusinessmanInfo(): Single<IndividualEntrepreneur?>
    fun getCarInfo(): Single<List<Auto>>
    fun getCarFines(): Single<List<TransportPenalty>>
    fun getChildrenInfo(): Single<List<Child>>
    fun getDigitalSignatureInfo(): Single<String>
    fun getGovFinesList(): Single<List<DebtorRegister>>
    fun getGovServicesInfo(): Single<String>
    fun getClinicInfo(): Single<Pair<String?, ClinicAttachment?>?>
    fun getHandleDocuments(): Single<List<DocumentInfo>>
    fun getInterestingServicesList(): Single<Pair<String, List<RecommenderInfo>>>
    fun getLicenseInfo(): Single<List<License>>
    fun getAccommodationQueuesList(): Single<List<AccommodationQueue>>
    fun getMarriageInfo(): Single<Pair<String, Marriage?>>
    fun getMyDocumentsList(): Single<List<DocumentRecord>>
    fun getLegalEntityList(): Single<List<LegalEntity>?>
    fun getRealtyList(): Single<List<Realty>>
    fun getSocialStatusList(): Single<List<SocialStatus>>
    fun getTimelineList(): Single<List<Timeline>>
    fun getDriverLicenses(): Single<List<DriverLicense>>
    fun getServiceSubscriptionStates(): Completable
    fun getServiceSubscriptionById(code: SubscriptionCode): Single<SubscriptionInfo>
    fun subscribeToSection(section: SubscriptionCode): Single<String>
    fun unsubscribeFromSection(section: SubscriptionCode): Single<String>
}

class UserServiceRepositoryImpl @Inject constructor(
    private val profileApi: UserApi,
    private val prefUtils: PrefUtils,
    private val appFileUtils: AppFileUtils,
    private val gson: Gson
) : UserServiceRepository {

    private val recommenderInfoType = object : TypeToken<List<RecommenderInfo>>() {}.type
    private val realtyType = object : TypeToken<List<Realty>>() {}.type
    private var subscriptionInfoType = object : TypeToken<List<SubscriptionInfo>>() {}.type

    override fun getAddresses(): Single<List<Address>> {
        return profileApi.getAddresses(
            ticket = prefUtils.getDataString(PrefUtils.TICKET),
            code = SecurityUtils.encryptIin(prefUtils.getDataString(PrefUtils.IIN))
        ).map { response ->
            if (response.responseInfo.isSuccessful()) {
                response.addressList
            } else {
                emptyList()
            }
        }
    }

    override fun getDocuments(): Single<List<DocumentInfo>> {
        return profileApi.getDocuments(
            ticket = prefUtils.getDataString(PrefUtils.TICKET),
            code = SecurityUtils.encryptIin(prefUtils.getDataString(PrefUtils.IIN))
        ).map { response ->
            if (response.isSuccessful) {
                response.body()?.list
            } else {
                emptyList()
            }
        }
    }

    override fun getBusinessmanInfo(): Single<IndividualEntrepreneur?> {
        return profileApi.getIndividualEntrepreneurInfo(
            ticket = prefUtils.getDataString(PrefUtils.TICKET),
            code = SecurityUtils.encryptIin(prefUtils.getDataString(PrefUtils.IIN))
        ).map { response ->
            if (response.isSuccessful) {
                if (response.body()?.responseInfo?.code == NetworkConstants.SUCCESS_CODE) {
                    response.body()?.individualEntrepreneur
                } else {
                    throw Throwable(response.body()?.responseInfo?.message)
                }
            } else {
                throw Throwable("error response") //TODO translate
            }
        }
    }

    override fun getCarInfo(): Single<List<Auto>> {
        return profileApi.getTransport(
            ticket = prefUtils.getDataString(PrefUtils.TICKET),
            code = SecurityUtils.encryptIin(prefUtils.getDataString(PrefUtils.IIN))
        ).map { response ->
            if (response.isSuccessful) {
                if (response.body()?.responseInfo?.isSuccessful() == true) {
                    response.body()?.list ?: emptyList()
                } else {
                    //emptyList()
                    throw Throwable(response.body()?.responseInfo?.message)
                }
            } else {
                throw Throwable("error response") //TODO translate
            }
        }
    }

    override fun getCarFines(): Single<List<TransportPenalty>> {
        return profileApi.getTransportFines(
            ticket = prefUtils.getDataString(PrefUtils.TICKET),
            code = SecurityUtils.encryptIin(prefUtils.getDataString(PrefUtils.IIN))
        ).map { response ->
            if (response.isSuccessful) {
                if (response.body()?.responseInfo?.isSuccessful() == true) {
                    response.body()?.penaltyList ?: emptyList()
                } else {
                    throw Throwable(response.body()?.responseInfo?.message)
                }
            } else {
                throw Throwable("response error") //TODO translate
            }
        }
    }

    override fun getChildrenInfo(): Single<List<Child>> {
        return profileApi.getChildInfo(
            ticket = prefUtils.getDataString(PrefUtils.TICKET),
            code = SecurityUtils.encryptIin(prefUtils.getDataString(PrefUtils.IIN))
        ).map { response ->
            if (response.isSuccessful) {
                if (response.body()?.responseInfo?.code == NetworkConstants.SUCCESS_CODE) {
                    response.body()?.children?.list ?: emptyList()
                } else {
                    throw Throwable(response.body()?.responseInfo?.message)
                }
            } else {
                throw Throwable("response error") //TODO translate
            }
        }
    }

    override fun getDigitalSignatureInfo(): Single<String> {
        return profileApi.getMSignInfo(
            ticket = prefUtils.getDataString(PrefUtils.TICKET),
            code = SecurityUtils.encryptIin(prefUtils.getDataString(PrefUtils.IIN))
        ).doAfterSuccess { response -> Log.d("MSign_info", response.body()?.toString()) }
            .map { it.body()?.toString() }
    }

    override fun getGovFinesList(): Single<List<DebtorRegister>> {
        return profileApi.getGovFinesInfo(
            ticket = prefUtils.getDataString(PrefUtils.TICKET),
            code = SecurityUtils.encryptIin(prefUtils.getDataString(PrefUtils.IIN))
        )
            .map { response ->
                if (response.isSuccessful) {
                    if (response.body()?.responseInfo?.isSuccessful() == true) {
                        response.body()?.debtorRegisters ?: emptyList()
                    } else {
                        throw Throwable(response.body()?.responseInfo?.message)
                    }
                } else {
                    throw Throwable("response error") //TODO translate
                }
            }
    }

    override fun getGovServicesInfo(): Single<String> {
        return profileApi.getGovServicesInfo(
            ticket = prefUtils.getDataString(PrefUtils.TICKET),
            code = SecurityUtils.encryptIin(prefUtils.getDataString(PrefUtils.IIN))
        ).map { it.body()?.toString() }
    }

    override fun getClinicInfo(): Single<Pair<String?, ClinicAttachment?>?> {
        return profileApi.getClinicAttachments(
            ticket = prefUtils.getDataString(PrefUtils.TICKET),
            code = SecurityUtils.encryptIin(prefUtils.getDataString(PrefUtils.IIN))
        )
            .map { response ->
                if (response.isSuccessful) {
                    if (response.body()?.responseInfo?.code == NetworkConstants.SUCCESS_CODE) {
                        val data = response.body()?.clinicAttachment
                        val date = response.body()?.updateInfo?.lastUpdateDate ?: 0
                        val lastUpdateDate = TimeUtils.formatMillisecondsToTimestamp(date, DD_MMMM_YYYY_hh_mm)
                        val pair: Pair<String?, ClinicAttachment?>? = Pair(lastUpdateDate, data)
                        pair
                    } else {
                        throw Throwable(response.body()?.responseInfo?.message ?: "")
                    }
                } else {
                    throw Throwable(response.errorBody()?.string() ?: "")
                }
            }
    }

    override fun getHandleDocuments(): Single<List<DocumentInfo>> {
        return profileApi
            .getDocuments(
                ticket = prefUtils.getDataString(PrefUtils.TICKET),
                code = SecurityUtils.encryptIin(prefUtils.getDataString(PrefUtils.IIN))
            )
            .map { response ->
                if (response.isSuccessful) {
                    if (response.body()?.responseInfo?.code == NetworkConstants.SUCCESS_CODE) {
                        response.body()?.list ?: emptyList()
                    } else {
                        throw Throwable(response.body()?.responseInfo?.message ?: "")
                    }
                } else {
                    throw Throwable(response.errorBody()?.string() ?: "")
                }
            }
    }

    override fun getInterestingServicesList(): Single<Pair<String, List<RecommenderInfo>>> {
        return profileApi
            .getInterestingServices(
                ticket = prefUtils.getDataString(PrefUtils.TICKET),
                code = SecurityUtils.encryptIin(prefUtils.getDataString(PrefUtils.IIN))
            )
            .map { result ->
                if (result.isSuccessful) {
                    val responseInfo = result.body()?.getAsJsonObject("responseInfo")
                    if (responseInfo?.get("code")?.asInt == NetworkConstants.SUCCESS_CODE) {
                        val updateTime = result.body()
                            ?.getAsJsonObject("updateInfo")
                            ?.get("lastUpdateDate")?.asLong ?: 0
                        val recommenderList = result.body()
                            ?.getAsJsonObject("recommenders")
                            ?.getAsJsonArray("recommenderList")
                        val list = gson.fromJson<List<RecommenderInfo>>(recommenderList, recommenderInfoType)
                        Pair(TimeUtils.formatMillisecondsToTimestamp(updateTime), list)
                    } else {
                        throw Throwable("response list is null") // TODO translate
                    }
                } else {
                    throw Throwable("response is null") // TODO translate
                }
            }
    }

    override fun getLicenseInfo(): Single<List<License>> {
        return profileApi.getLicenses(
            ticket = prefUtils.getDataString(PrefUtils.TICKET),
            code = SecurityUtils.encryptIin(prefUtils.getDataString(PrefUtils.IIN))
        )
            .map { response ->
                Log.d("Licenses_data", response.body().toString())
                if (response.isSuccessful) {
                    if (response.body()?.responseInfo?.code == NetworkConstants.SUCCESS_CODE) {
                        response.body()?.list ?: emptyList()
                    } else {
                        throw Throwable(response.body()?.responseInfo?.message ?: "")
                    }
                } else {
                    throw Throwable(response.errorBody()?.string() ?: "")
                }
            }
    }

    override fun getAccommodationQueuesList(): Single<List<AccommodationQueue>> {
        return profileApi.getAccommodationQueue(
            ticket = prefUtils.getDataString(PrefUtils.TICKET),
            code = SecurityUtils.encryptIin(prefUtils.getDataString(PrefUtils.IIN))
        )
            .map { response ->
                if (response.isSuccessful) {
                    if (response.body()?.responseInfo?.isSuccessful() == true) {
                        response.body()?.data?.list ?: emptyList()
                    } else {
                        throw Throwable(response.body()?.responseInfo?.message ?: "")
                    }
                } else {
                    throw Throwable(response.errorBody()?.string() ?: "")
                }
            }
    }

    override fun getMarriageInfo(): Single<Pair<String, Marriage?>> {
        return profileApi.getMarriage(
            ticket = prefUtils.getDataString(PrefUtils.TICKET),
            code = SecurityUtils.encryptIin(prefUtils.getDataString(PrefUtils.IIN))
        )
            .map { response ->
                Log.d("Marriage_response", response.body()?.toString())
                if (response.isSuccessful) {
                    if (response.body()?.responseInfo?.code == NetworkConstants.SUCCESS_CODE) {
                        val updateDate = TimeUtils.formatMillisecondsToTimestamp(
                            timestamp = response.body()?.updateInfo?.lastUpdateDate ?: 0,
                            regex = DD_MMM_YYYY
                        )
                        val marriage = response.body()?.marriage
                        Pair(updateDate, marriage)
                    } else {
                        throw Throwable(response.body()?.responseInfo?.message ?: "")
                    }
                } else {
                    throw Throwable(response.errorBody()?.string() ?: "")
                }
            }
    }

    override fun getMyDocumentsList(): Single<List<DocumentRecord>> {
        val map = HashMap<String, String>().apply {
            put("ticket", prefUtils.getDataString(PrefUtils.TICKET))
        }
        return profileApi.getElectronicDocuments(
            code = SecurityUtils.encryptIin(prefUtils.getDataString(PrefUtils.IIN)),
            map = map
        )
            .map { response ->
                if (response.isSuccessful) {
                    if (response.body()?.responseInfo?.code == NetworkConstants.SUCCESS_CODE) {
                        response.body()?.myDocument?.list ?: emptyList()
                    } else {
                        throw Throwable(response.body()?.responseInfo?.message ?: "")
                    }
                } else {
                    throw Throwable(response.errorBody()?.string() ?: "")
                }
            }
    }

    override fun getLegalEntityList(): Single<List<LegalEntity>?> {
        return profileApi.getLegalEntityParticipant(
            ticket = prefUtils.getDataString(PrefUtils.TICKET),
            code = SecurityUtils.encryptIin(prefUtils.getDataString(PrefUtils.IIN))
        )
            .map { response ->
                Log.d("legal_participant", response.body()?.toString())
                if (response.isSuccessful) {
                    if (response.body()?.responseInfo?.code == NetworkConstants.SUCCESS_CODE) {
                        response.body()?.list ?: emptyList()
                    } else {
                        throw Throwable(response.body()?.responseInfo?.message ?: "")
                    }
                } else {
                    throw Throwable(response.errorBody()?.string() ?: "")
                }
            }
    }

    override fun getRealtyList(): Single<List<Realty>> {
        return profileApi.getProperty(
            ticket = prefUtils.getDataString(PrefUtils.TICKET),
            code = SecurityUtils.encryptIin(prefUtils.getDataString(PrefUtils.IIN))
        )
            .map { response ->
                if (response.isSuccessful) {
                    val responseInfo = response.body()?.getAsJsonObject("responseInfo")
                    if (responseInfo?.get("code")?.asInt == NetworkConstants.SUCCESS_CODE) {
                        val jsonArray = response.body()?.getAsJsonArray("realtyList")
                        val list = gson.fromJson<List<Realty>>(jsonArray, realtyType)
                        if (list.isNullOrEmpty()) {
                            emptyList<Realty>()
                        } else {
                            list
                        }
                    } else {
                        throw Throwable("response error") // TODO translate
                    }
                } else {
                    throw Throwable("response error") // TODO translate
                }
            }
    }

    override fun getSocialStatusList(): Single<List<SocialStatus>> {
        return profileApi.getSocialStatus(
            ticket = prefUtils.getDataString(PrefUtils.TICKET),
            code = SecurityUtils.encryptIin(prefUtils.getDataString(PrefUtils.IIN))
        )
            .map { response ->
                Log.d("social_status_data", response.body().toString())
                if (response.isSuccessful) {
                    if (response.body()?.responseInfo?.isSuccessful() == true) {
                        response.body()?.list ?: emptyList()
                    } else {
                        throw Throwable(response.body()?.responseInfo?.message ?: "")
                    }
                } else {
                    throw Throwable(response.errorBody()?.string() ?: "")
                }
            }
    }

    override fun getTimelineList(): Single<List<Timeline>> {
        return profileApi.getTimeline(
            ticket = prefUtils.getDataString(PrefUtils.TICKET),
            code = SecurityUtils.encryptIin(prefUtils.getDataString(PrefUtils.IIN))
        )
            .map { response ->
                Log.d("Timeline_result", response.body()?.toString())
                if (response.isSuccessful) {
                    if (response.body()?.responseInfo?.code == NetworkConstants.SUCCESS_CODE) {
                        response.body()?.timelineInfo ?: emptyList()
                    } else {
                        throw Throwable(response.body()?.responseInfo?.message ?: "")
                    }
                } else {
                    throw Throwable(response.errorBody()?.string() ?: "")
                }
            }
    }

    override fun getDriverLicenses(): Single<List<DriverLicense>> {
        return profileApi.getDriverLicense(
            ticket = prefUtils.getDataString(PrefUtils.TICKET),
            code = SecurityUtils.encryptIin(prefUtils.getDataString(PrefUtils.IIN))
        )
            .map { response ->
                Log.d("Auto_id_data", response.body()?.toString())
                if (response.isSuccessful) {
                    if (response.body()?.responseInfo?.code == NetworkConstants.SUCCESS_CODE) {
                        response.body()?.list ?: emptyList()
                    } else {
                        throw Throwable(response.body()?.responseInfo?.message ?: "")
                    }
                } else {
                    throw Throwable(response.errorBody()?.string() ?: "")
                }
            }
    }

    override fun getServiceSubscriptionStates(): Completable {
        return profileApi.userServicesSectionsInfo(
            ticket = prefUtils.getDataString(PrefUtils.TICKET),
            code = SecurityUtils.encryptIin(prefUtils.getDataString(PrefUtils.IIN))
        ).map { response ->
            val json = gson.toJson(response.body()?.list)
            appFileUtils.saveStringToFile(
                SUBSCRIPTIONS_INFO,
                SUBSCRIPTION_INFO_LIST_ITEM, json
            )
        }.ignoreElement()
    }

    override fun getServiceSubscriptionById(code: SubscriptionCode): Single<SubscriptionInfo> {
        return Single.fromCallable {
            val json = appFileUtils.getStringFileDataByName(
                SUBSCRIPTIONS_INFO,
                SUBSCRIPTION_INFO_LIST_ITEM
            )
            gson.fromJson<List<SubscriptionInfo>>(json, subscriptionInfoType)
        }.map { list -> list.find { item -> item.code == code } }
    }

    override fun subscribeToSection(section: SubscriptionCode): Single<String> {
        return profileApi.subscribeToSection(
            code = SecurityUtils.encryptIin(prefUtils.getDataString(PrefUtils.IIN)),
            ticket = prefUtils.getDataString(PrefUtils.TICKET),
            section = section.name
        ).map { it.toString() }
    }

    override fun unsubscribeFromSection(section: SubscriptionCode): Single<String> {
        return profileApi.unsubscribeFromSection(
            code = SecurityUtils.encryptIin(prefUtils.getDataString(PrefUtils.IIN)),
            ticket = prefUtils.getDataString(PrefUtils.TICKET),
            section = section.name
        ).map { it.toString() }
    }


    companion object {
        private const val SUBSCRIPTIONS_INFO = "subscriptions_info"
        private const val SUBSCRIPTION_INFO_LIST_ITEM = "SUBSCRIPTION_INFO_LIST_ITEM"
    }
}