package com.example.yalantis.myuiview.feature.views.flexMenu

import android.content.Context
import android.os.Parcelable
import android.support.v7.view.menu.*
import android.view.ViewGroup
import kotlinx.android.parcel.Parcelize

class FlexNavigationPresenter : MenuPresenter {

    private lateinit var menu: MenuBuilder
    private var menuView: FlexNavigationMenuView? = null
    private var updateSuspended = false
    private var id: Int = 0

    fun setBottomNavigationMenuView(menuView: FlexNavigationMenuView) {
        this.menuView = menuView
    }

    override fun initForMenu(context: Context, menu: MenuBuilder) {
        menuView?.initialize(menu)
        this.menu = menu
    }

    override fun getMenuView(root: ViewGroup): MenuView? {
        return menuView
    }

    override fun updateMenuView(cleared: Boolean) {
        if (updateSuspended) return
        if (cleared) {
            menuView?.buildMenuView()
        } else {
            menuView?.updateMenuView()
        }
    }

    override fun setCallback(cb: MenuPresenter.Callback) {}

    override fun onSubMenuSelected(subMenu: SubMenuBuilder): Boolean {
        return false
    }

    override fun onCloseMenu(menu: MenuBuilder, allMenusAreClosing: Boolean) {}

    override fun flagActionItems(): Boolean {
        return false
    }

    override fun expandItemActionView(menu: MenuBuilder, item: MenuItemImpl): Boolean {
        return false
    }

    override fun collapseItemActionView(menu: MenuBuilder, item: MenuItemImpl): Boolean {
        return false
    }

    fun setId(id: Int) {
        this.id = id
    }

    override fun getId(): Int {
        return id
    }

    override fun onSaveInstanceState(): Parcelable {
        val savedState = SavedState()
        savedState.selectedItemId = menuView?.getSelectedItemId() ?: 0
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is SavedState) {
            menuView?.tryRestoreSelectedItemId(state.selectedItemId)
        }
    }

    fun setUpdateSuspended(updateSuspended: Boolean) {
        this.updateSuspended = updateSuspended
    }

    @Parcelize
    internal data class SavedState(var selectedItemId: Int = 0) : Parcelable

}