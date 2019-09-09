package kz.mobile.mgov.profile.presentation.business

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import kz.mobile.mgov.R
import kz.mobile.mgov.profile.data.model.LegalEntity


class LegalEntitiesAdapter(layoutResId: Int, data: List<LegalEntity>) :
    BaseQuickAdapter<LegalEntity, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder?, item: LegalEntity?) {
        helper?.setText(R.id.item_name_tv, item?.name?.ruText)
    }
}