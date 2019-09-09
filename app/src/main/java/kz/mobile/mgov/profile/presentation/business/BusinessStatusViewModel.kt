package kz.mobile.mgov.profile.presentation.business

import androidx.lifecycle.MutableLiveData
import kz.mobile.mgov.common.di.modules.RxSchedulers
import kz.mobile.mgov.common.model.SubscriptionCode
import kz.mobile.mgov.egov_android.presentation.base.BaseViewModel
import kz.mobile.mgov.profile.data.model.IndividualEntrepreneur
import kz.mobile.mgov.profile.data.model.LegalEntity
import kz.mobile.mgov.profile.data.repository.UserServiceRepository
import javax.inject.Inject

class BusinessStatusViewModel @Inject constructor(
    private val userServiceRepository: UserServiceRepository,
    private val schedulers: RxSchedulers
) : BaseViewModel() {

    val entrepreneurStatusLiveData by lazy {
        MutableLiveData<BusinessmanResult>()
    }

    val legalEntitiesLiveData by lazy {
        MutableLiveData<LegalEntityResult>()
    }

    fun getBusinessmanInfo() {
        addDisposable(userServiceRepository.getBusinessmanInfo()
            .compose(schedulers.applySchedulersSingle())
            .doOnSubscribe { entrepreneurStatusLiveData.value = BusinessmanResult.ShowLoading }
            .doFinally {
                entrepreneurStatusLiveData.value = BusinessmanResult.HideLoading
            }
            .subscribe(
                { result -> entrepreneurStatusLiveData.value = BusinessmanResult.Result(result) },
                { error -> entrepreneurStatusLiveData.value = BusinessmanResult.Error(error.localizedMessage) }
            )
        )
    }

    fun getLegalEntityParticipant() {
        addDisposable(
            userServiceRepository.getLegalEntityList()
                .compose(schedulers.applySchedulersSingle())
                .doOnSubscribe { legalEntitiesLiveData.value = LegalEntityResult.ShowLoading }
                .doFinally { legalEntitiesLiveData.value = LegalEntityResult.HideLoading }
                .subscribe(
                    { result -> legalEntitiesLiveData.value = LegalEntityResult.Result(result) },
                    { error -> legalEntitiesLiveData.value = LegalEntityResult.Error(error.localizedMessage) }
                )
        )
    }

    fun getServiceStatus() = addDisposable(
        userServiceRepository.getServiceSubscriptionById(code = SubscriptionCode.PP_INDIVIDUAL_ENTREPRENEUR)
            .flatMap { childResult ->
                userServiceRepository.getServiceSubscriptionById(code = SubscriptionCode.PP_LEGAL_PARTICIPANT)
                    .map { marriageResult -> Pair(childResult, marriageResult) }
            }
            .compose(schedulers.applySchedulersSingle())
            .subscribe(
                { result ->
                    entrepreneurStatusLiveData.value = BusinessmanResult.Status(result.first.isEnable())
                    legalEntitiesLiveData.value = LegalEntityResult.Status(result.second.isEnable())
                },
                { error -> error.printStackTrace() }
            )
    )

    fun subscribeUnsubscribeSection(isSubscribe: Boolean, section: SubscriptionCode) {
        val single = if (isSubscribe) {
            userServiceRepository.unsubscribeFromSection(section = section)
        } else {
            userServiceRepository.subscribeToSection(section = section)
        }
        addDisposable(
            single
                .compose(schedulers.applySchedulersSingle())
                .doOnSubscribe { entrepreneurStatusLiveData.value = BusinessmanResult.ShowLoading }
                .doFinally { entrepreneurStatusLiveData.value = BusinessmanResult.HideLoading }
                .subscribe(
                    { result ->
                        when (section) {
                            SubscriptionCode.PP_INDIVIDUAL_ENTREPRENEUR -> {
                                getBusinessmanInfo()
                            }
                            SubscriptionCode.PP_LEGAL_PARTICIPANT -> {
                                getLegalEntityParticipant()
                            }
                            else -> {
                            }
                        }
                    },
                    { error -> error.printStackTrace() }
                )
        )
    }

    sealed class BusinessmanResult() {
        data class Result(val individualEntrepreneur: IndividualEntrepreneur?) : BusinessmanResult()
        data class Error(val error: String) : BusinessmanResult()
        data class Status(val status: Boolean) : BusinessmanResult()
        object ShowLoading : BusinessmanResult()
        object HideLoading : BusinessmanResult()
    }


    sealed class LegalEntityResult() {
        data class Result(val list: List<LegalEntity>?) : LegalEntityResult()
        data class Error(val error: String) : LegalEntityResult()
        data class Status(val status: Boolean) : LegalEntityResult()
        object ShowLoading : LegalEntityResult()
        object HideLoading : LegalEntityResult()
    }


}



