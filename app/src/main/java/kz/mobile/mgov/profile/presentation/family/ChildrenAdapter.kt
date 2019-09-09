package kz.mobile.mgov.profile.presentation.family

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import kz.mobile.mgov.R
import kz.mobile.mgov.profile.data.model.Child

class ChildrenAdapter(layoutResId: Int, data: List<Child>) :
    BaseQuickAdapter<Child, BaseViewHolder>(layoutResId, data) {
    private val nameFormat: String = "%s %s %s"
    override fun convert(helper: BaseViewHolder?, item: Child?) {
        helper?.setText(R.id.child_name, String.format(nameFormat, item?.surname, item?.name, item?.patronymic))
        helper?.setText(R.id.child_iin_tv, item?.iin)
        helper?.setText(R.id.child_birth_place_tv, getChildBirthPlace(item))
        helper?.setText(R.id.child_birth_date_tv, item?.parsedBirthday())
        helper?.setText(R.id.child_register_place_tv, item?.zagsName?.ruText)
    }


    private fun getChildBirthPlace(child: Child?): String {
        val birthPlace = StringBuilder()
        if (child?.birthRegionName?.ruText != null) {
            birthPlace.append(child.birthRegionName.ruText)
        }
        if (child?.birthCity != null) {
            if (birthPlace.isEmpty()) {
                birthPlace.append(child.birthCity)
            } else {
                birthPlace.append(", ")
                birthPlace.append(child.birthCity)
            }
        }
        if (child?.birthDistrictName?.ruText != null) {
            if (birthPlace.isEmpty()) {
                birthPlace.append(child.birthDistrictName.ruText)
            } else {
                birthPlace.append(", ")
                birthPlace.append(child.birthDistrictName.ruText)
            }
        }

        if (child?.birthCountryName?.ruText != null) {
            if (birthPlace.isEmpty()) {
                birthPlace.append(child.birthCountryName.ruText)
            } else {
                birthPlace.append(", ")
                birthPlace.append(child.birthCountryName.ruText)
            }
        }

        return birthPlace.toString()
    }
}