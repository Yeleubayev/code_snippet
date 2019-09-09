package kz.mobile.mgov.profile.presentation.views.license

import android.content.Context
import androidx.lifecycle.MutableLiveData
import kz.mobile.mgov.egov_android.presentation.base.BaseViewModel
import kz.mobile.mgov.profile.data.model.License

class LicencesViewModel(private val context: Context) : BaseViewModel() {

    val liveData by lazy {
        MutableLiveData<LicenseInfo>()
    }
}

sealed class LicenseInfo() {
    object ShowLoading : LicenseInfo()
    object HideLoading : LicenseInfo()
    data class Result(val list: List<License>?) : LicenseInfo()
    data class Error(val error: String) : LicenseInfo()
}