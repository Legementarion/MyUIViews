package com.example.yalantis.myuiview.feature.views.flexMenu

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.DrawableRes
import android.support.annotation.IdRes
import android.support.v4.content.ContextCompat
import android.support.v4.view.AbsSavedState
import android.support.v4.view.ViewCompat
import android.support.v7.content.res.AppCompatResources
import android.support.v7.view.SupportMenuInflater
import android.support.v7.view.menu.MenuBuilder
import android.support.v7.widget.TintTypedArray
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.widget.FrameLayout
import com.example.yalantis.myuiview.feature.R

@SuppressLint("RestrictedApi,PrivateResource")
class FlexNavigationView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private val CHECKED_STATE_SET = intArrayOf(android.R.attr.state_checked)
        private val DISABLED_STATE_SET = intArrayOf(-android.R.attr.state_enabled)
        private const val MENU_PRESENTER_ID = 1
    }

    private var menu: MenuBuilder = FlexNavigationMenu(context)
    private var menuView: FlexNavigationMenuView = FlexNavigationMenuView(context)
    private val presenter = FlexNavigationPresenter()
    private var menuInflater: MenuInflater? = null

    private var selectedListener: OnNavigationItemSelectedListener? = null
    private var reselectedListener: OnNavigationItemReselectedListener? = null
    var customCircleIcons: Boolean = false
        set(value) {
            field = value
            menuView.customCircleIcons = value
        }

    init {
//        ThemeUtils.checkAppCompatTheme(context)

        // Create the menu
        val params = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.gravity = Gravity.CENTER
        menuView.layoutParams = params

        presenter.setBottomNavigationMenuView(menuView)
        presenter.id = MENU_PRESENTER_ID
        menuView.setPresenter(presenter)
        menu.addMenuPresenter(presenter)
        presenter.initForMenu(getContext(), menu)

        // Custom attributes
        val a = TintTypedArray.obtainStyledAttributes(context, attrs,
                R.styleable.NavigationView, defStyleAttr,
                R.style.Widget_Design_BottomNavigationView)

        if (a.hasValue(R.styleable.NavigationView_itemIconTint)) {
            menuView.setIconTintList(
                    a.getColorStateList(R.styleable.NavigationView_itemIconTint))
        } else {
            createDefaultColorStateList(android.R.attr.textColorSecondary)?.let { menuView.setIconTintList(it) }
        }
        if (a.hasValue(R.styleable.NavigationView_itemTextColor)) {
            menuView.setItemTextColor(a.getColorStateList(R.styleable.NavigationView_itemTextColor))
        } else {
            createDefaultColorStateList(android.R.attr.textColorSecondary)?.let { menuView.setItemTextColor(it) }
        }
        if (a.hasValue(R.styleable.NavigationView_elevation)) {
            ViewCompat.setElevation(this, a.getDimensionPixelSize(
                    R.styleable.NavigationView_elevation, 0).toFloat())
        }

        val itemBackground = a.getResourceId(R.styleable.NavigationView_itemBackground, 0)
        menuView.setBackgroundResource(itemBackground)

        if (a.hasValue(R.styleable.NavigationView_menu)) {
            inflateMenu(a.getResourceId(R.styleable.NavigationView_menu, 0))
        }
        a.recycle()

        addView(menuView, params)
        if (Build.VERSION.SDK_INT < 21) {
            addCompatibilityTopDivider(context)
        }

        menu.setCallback(object : MenuBuilder.Callback {
            override fun onMenuItemSelected(menu: MenuBuilder, item: MenuItem): Boolean {
                if (reselectedListener != null && item.itemId == getSelectedItemId()) {
                    reselectedListener?.onNavigationItemReselected(item)
                    return true // item is already selected
                }
                selectedListener?.let {
                    return !it.onNavigationItemSelected(item)
                }
                return false
            }

            override fun onMenuModeChange(menu: MenuBuilder) {}
        })
    }

    /**
     * Set a listener that will be notified when a bottom navigation item is selected. This listener
     * will also be notified when the currently selected item is reselected, unless an
     * [OnNavigationItemReselectedListener] has also been set.
     *
     * @param listener The listener to notify
     *
     * @see .setOnNavigationItemReselectedListener
     */
    fun setOnNavigationItemSelectedListener(listener: OnNavigationItemSelectedListener) {
        selectedListener = listener
    }

    /**
     * Set a listener that will be notified when the currently selected bottom navigation item is
     * reselected. This does not require an [OnNavigationItemSelectedListener] to be set.
     *
     * @param listener The listener to notify
     *
     * @see .setOnNavigationItemSelectedListener
     */
    fun setOnNavigationItemReselectedListener(listener: OnNavigationItemReselectedListener?) {
        reselectedListener = listener
    }

    /**
     * Returns the [Menu] instance associated with this bottom navigation bar.
     */
    fun getMenu(): Menu {
        return menu
    }

    /**
     * Inflate a menu resource into this navigation view.
     *
     * Existing items in the menu will not be modified or removed.
     *
     * @param resId ID of a menu resource to inflate
     */
    private fun inflateMenu(resId: Int) {
        presenter.setUpdateSuspended(true)
        getMenuInflater().inflate(resId, menu)
        presenter.setUpdateSuspended(false)
        presenter.updateMenuView(true)
    }

    /**
     * @return The maximum number of items that can be shown in BottomNavigationView.
     */
    fun getMaxItemCount(): Int {
        return FlexNavigationMenu.MAX_ITEM_COUNT
    }

    /**
     * Returns the tint which is applied to our menu items' icons.
     *
     * @see .setItemIconTintList
     * @attr ref R.styleable#BottomNavigationView_itemIconTint
     */
    fun getItemIconTintList(): ColorStateList? {
        return menuView.getIconTintList()
    }

    /**
     * Set the tint which is applied to our menu items' icons.
     *
     * @param tint the tint to apply.
     *
     * @attr ref R.styleable#BottomNavigationView_itemIconTint
     */
    fun setItemIconTintList(tint: ColorStateList) {
        menuView.setIconTintList(tint)
    }

    /**
     * Set the tint which is applied to each menu items' icons.
     *
     * @param tintList the list of tint to apply.
     *
     * @attr ref R.styleable#BottomNavigationView_itemIconTint
     */
    fun setItemIconTintList(context: Context, colors: IntArray, unselectedColor: Int) {
        if (colors.size != menu.size())
            throw IllegalArgumentException("FlexNavigation count of tint list of ColorStateList doesn't match to menu items count")

        menuView.setIconTintList(getTintList(context, colors, unselectedColor))
    }

    private fun getTintList(context: Context, selectedColor: IntArray, unselectedColor: Int): List<ColorStateList> {
        val colorTintList = mutableListOf<ColorStateList>()
        val states = arrayOf(CHECKED_STATE_SET,
                DISABLED_STATE_SET, EMPTY_STATE_SET)

        selectedColor.forEach {
            if (it == -1) {
                colorTintList.add(ColorStateList(states, intArrayOf(-1, -1, -1)))
            } else
                colorTintList.add(ColorStateList(states, generateColorState(context, it, unselectedColor)))
        }


        return colorTintList
    }

    private fun generateColorState(baseContext: Context, selectColor: Int, unselectedColor: Int): IntArray {
        return intArrayOf(ContextCompat.getColor(baseContext, selectColor),
                ContextCompat.getColor(baseContext, unselectedColor),
                ContextCompat.getColor(baseContext, unselectedColor))
    }

    /**
     * Returns colors used for the different states (normal, selected, focused, etc.) of the menu
     * item text.
     *
     * @see .setItemTextColor
     * @return the ColorStateList of colors used for the different states of the menu items text.
     *
     * @attr ref R.styleable#BottomNavigationView_itemTextColor
     */
    fun getItemTextColor(): ColorStateList? {
        return menuView.getItemTextColor()
    }

    /**
     * Set the colors to use for the different states (normal, selected, focused, etc.) of the menu
     * item text.
     *
     * @see .getItemTextColor
     * @attr ref R.styleable#BottomNavigationView_itemTextColor
     */
    fun setItemTextColor(textColor: ColorStateList) {
        menuView.setItemTextColor(textColor)
    }

    /**
     * Returns the background resource of the menu items.
     *
     * @see .setItemBackgroundResource
     * @attr ref R.styleable#BottomNavigationView_itemBackground
     */
    @DrawableRes
    fun getItemBackgroundResource(): Int {
        return menuView.getItemBackgroundRes()
    }

    /**
     * Set the background of our menu items to the given resource.
     *
     * @param resId The identifier of the resource.
     *
     * @attr ref R.styleable#BottomNavigationView_itemBackground
     */
    fun setItemBackgroundResource(@DrawableRes resId: Int) {
        menuView.setItemBackgroundRes(resId)
    }

    /**
     * Returns the currently selected menu item ID, or zero if there is no menu.
     *
     * @see .setSelectedItemId
     */
    @IdRes
    fun getSelectedItemId(): Int {
        return menuView.getSelectedItemId()
    }

    /**
     * Set the selected menu item ID. This behaves the same as tapping on an item.
     *
     * @param itemId The menu item ID. If no item has this ID, the current selection is unchanged.
     *
     * @see .getSelectedItemId
     */
    fun setSelectedItemId(@IdRes itemId: Int) {
        val item = menu.findItem(itemId)
        item?.let {
            if (!menu.performItemAction(it, presenter, 0)) {
                it.isChecked = true
            }
        }
    }

    /**
     * Listener for handling selection events on bottom navigation items.
     */
    interface OnNavigationItemSelectedListener {

        /**
         * Called when an item in the bottom navigation menu is selected.
         *
         * @param item The selected item
         *
         * @return true to display the item as the selected item and false if the item should not
         * be selected. Consider setting non-selectable items as disabled preemptively to
         * make them appear non-interactive.
         */
        fun onNavigationItemSelected(item: MenuItem): Boolean
    }

    /**
     * Listener for handling reselection events on bottom navigation items.
     */
    interface OnNavigationItemReselectedListener {

        /**
         * Called when the currently selected item in the bottom navigation menu is selected again.
         *
         * @param item The selected item
         */
        fun onNavigationItemReselected(item: MenuItem)
    }

    private fun addCompatibilityTopDivider(context: Context) {
        val divider = View(context)
        divider.setBackgroundColor(
                ContextCompat.getColor(context, R.color.design_bottom_navigation_shadow_color))
        val dividerParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                resources.getDimensionPixelSize(
                        R.dimen.design_bottom_navigation_shadow_height))
        divider.layoutParams = dividerParams
        addView(divider)
    }

    private fun getMenuInflater(): MenuInflater {
        if (menuInflater == null) {
            menuInflater = SupportMenuInflater(context)
        }
        return menuInflater as MenuInflater
    }

    private fun createDefaultColorStateList(baseColorThemeAttr: Int): ColorStateList? {
        val value = TypedValue()
        if (!context.theme.resolveAttribute(baseColorThemeAttr, value, true)) {
            return null
        }
        val baseColor = AppCompatResources.getColorStateList(
                context, value.resourceId)
        if (!context.theme.resolveAttribute(
                        android.support.v7.appcompat.R.attr.colorPrimary, value, true)) {
            return null
        }
        val colorPrimary = value.data
        val defaultColor = baseColor.defaultColor
        return ColorStateList(arrayOf(DISABLED_STATE_SET, CHECKED_STATE_SET, View.EMPTY_STATE_SET),
                intArrayOf(baseColor.getColorForState(DISABLED_STATE_SET, defaultColor), colorPrimary, defaultColor))
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        savedState.menuPresenterState = Bundle()
        menu.savePresenterStates(savedState.menuPresenterState)
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superState)
        menu.restorePresenterStates(state.menuPresenterState)
    }

    internal class SavedState : AbsSavedState {
        lateinit var menuPresenterState: Bundle

        constructor(superState: Parcelable) : super(superState)

        constructor(source: Parcel, loader: ClassLoader?) : super(source, loader) {
            readFromParcel(source, loader)
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeBundle(menuPresenterState)
        }

        private fun readFromParcel(`in`: Parcel, loader: ClassLoader?) {
            menuPresenterState = `in`.readBundle(loader)
        }

        companion object {

            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.ClassLoaderCreator<SavedState> {
                override fun createFromParcel(`in`: Parcel, loader: ClassLoader): SavedState {
                    return SavedState(`in`, loader)
                }

                override fun createFromParcel(`in`: Parcel): SavedState {
                    return SavedState(`in`, null)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

}