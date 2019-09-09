package kz.mobile.mgov.profile.presentation.views.hospital

import android.content.Context
import androidx.lifecycle.MutableLiveData
import kz.mobile.mgov.egov_android.presentation.base.BaseViewModel
import kz.mobile.mgov.profile.data.model.ClinicAttachment

class HospitalViewModel(private val context: Context) : BaseViewModel() {

    val liveData by lazy {
        MutableLiveData<ClinicInfo>()
    }
}

sealed class ClinicInfo() {
    object ShowLoading : ClinicInfo()
    object HideLoading : ClinicInfo()
    data class Result(
        val lastUpdateDate: String?,
        val clinicAttachment: ClinicAttachment?
    ) : ClinicInfo()

    data class Error(val error: String) : ClinicInfo()
}

