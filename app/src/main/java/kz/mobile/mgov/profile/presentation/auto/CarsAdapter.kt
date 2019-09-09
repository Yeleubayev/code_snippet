package kz.mobile.mgov.profile.presentation.auto

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import kz.mobile.mgov.R
import kz.mobile.mgov.profile.data.model.Auto


class CarsAdapter(layoutResId: Int, data: List<Auto>) :
    BaseQuickAdapter<Auto, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder?, item: Auto?) {
        helper?.setText(R.id.item_name_tv, item?.model + "-" + item?.number)
    }
}