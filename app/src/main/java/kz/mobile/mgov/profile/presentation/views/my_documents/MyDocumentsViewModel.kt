package kz.mobile.mgov.profile.presentation.views.my_documents

import android.content.Context
import androidx.lifecycle.MutableLiveData
import kz.mobile.mgov.document.data.DocumentRecord
import kz.mobile.mgov.egov_android.presentation.base.BaseViewModel

class MyDocumentsViewModel(val context: Context) : BaseViewModel() {

    val liveData by lazy {
        MutableLiveData<ElectronicDocumentInfo>()
    }
}

sealed class ElectronicDocumentInfo() {
    object ShowLoading : ElectronicDocumentInfo()
    object HideLoading : ElectronicDocumentInfo()
    data class Result(val list: List<DocumentRecord>?) : ElectronicDocumentInfo()
    data class Error(val error: String) : ElectronicDocumentInfo()
}

