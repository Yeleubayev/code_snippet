package kz.mobile.mgov.profile.presentation.views.gov_fines

import android.content.Context
import androidx.lifecycle.MutableLiveData
import kz.mobile.mgov.egov_android.presentation.base.BaseViewModel
import kz.mobile.mgov.profile.data.model.DebtorRegister

class GovFinesViewModel(private val context: Context) : BaseViewModel() {

    val liveData by lazy {
        MutableLiveData<DebtorRegisterInfo>()
    }
}

sealed class DebtorRegisterInfo() {
    data class Result(val list: List<DebtorRegister>?) : DebtorRegisterInfo()
    data class Error(val error: String) : DebtorRegisterInfo()
    object ShowLoading : DebtorRegisterInfo()
    object HideLoading : DebtorRegisterInfo()
}