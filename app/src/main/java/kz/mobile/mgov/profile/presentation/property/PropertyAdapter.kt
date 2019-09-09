package kz.mobile.mgov.profile.presentation.property

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import kz.mobile.mgov.R
import kz.mobile.mgov.profile.data.model.Realty


class PropertyAdapter(layoutResId: Int, data: List<Realty>) :
    BaseQuickAdapter<Realty, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder?, item: Realty?) {
        helper?.setText(R.id.cadastral_number_tv, item?.cadastralNumber)
        helper?.setText(R.id.address_tv, item?.address?.ruText)
    }
}