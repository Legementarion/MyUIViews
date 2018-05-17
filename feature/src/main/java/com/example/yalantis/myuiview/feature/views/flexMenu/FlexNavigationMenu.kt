package com.example.yalantis.myuiview.feature.views.flexMenu

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.view.menu.MenuBuilder
import android.support.v7.view.menu.MenuItemImpl
import android.view.MenuItem
import android.view.SubMenu

@SuppressLint("RestrictedApi")
class FlexNavigationMenu(context: Context) : MenuBuilder(context) {

    companion object {
        const val MAX_ITEM_COUNT = 6
    }

    override fun addSubMenu(group: Int, id: Int, categoryOrder: Int, title: CharSequence): SubMenu {
        throw UnsupportedOperationException("FlexNavigationMenu does not support submenus")
    }

    override fun addInternal(group: Int, id: Int, categoryOrder: Int, title: CharSequence): MenuItem {
        if (size() + 1 > MAX_ITEM_COUNT) {
            throw IllegalArgumentException(
                    "Maximum number of items supported by FlexNavigationMenu is " + MAX_ITEM_COUNT
                            + ". Limit can be checked with FlexNavigationMenu#getMaxItemCount()")
        }
        stopDispatchingItemsChanged()
        val item = super.addInternal(group, id, categoryOrder, title)
        if (item is MenuItemImpl) {
            item.isExclusiveCheckable = true
        }
        startDispatchingItemsChanged()
        return item
    }
}
