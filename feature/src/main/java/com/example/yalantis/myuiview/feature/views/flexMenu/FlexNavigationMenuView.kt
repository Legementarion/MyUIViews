package com.example.yalantis.myuiview.feature.views.flexMenu

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.support.design.R
import android.support.design.internal.TextScale
import android.support.transition.AutoTransition
import android.support.transition.TransitionManager
import android.support.transition.TransitionSet
import android.support.v4.util.Pools
import android.support.v4.view.ViewCompat
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.view.menu.MenuBuilder
import android.support.v7.view.menu.MenuItemImpl
import android.support.v7.view.menu.MenuView
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

@SuppressLint("RestrictedApi, PrivateResource")
class FlexNavigationMenuView(context: Context, attrs: AttributeSet? = null) : ViewGroup(context, attrs), MenuView {

    companion object {
        private const val ACTIVE_ANIMATION_DURATION_MS = 115L
    }

    private var transitionSet: TransitionSet
    private var inactiveItemMaxWidth: Int = 0
    private var inactiveItemMinWidth: Int = 0
    private var activeItemMaxWidth: Int = 0
    private var itemHeight: Int = 0
    private var mOnClickListener: View.OnClickListener
    private val itemPool = Pools.SynchronizedPool<FlexItemView>(FlexNavigationMenu.MAX_ITEM_COUNT)

    private var buttons: Array<FlexItemView?>? = arrayOf()
    private var selectedItemId = 0
    private var selectedItemPosition = 0
    private var itemIconTintList: List<ColorStateList> = mutableListOf()
    private var itemIconTint: ColorStateList? = null
    private var itemTextColor: ColorStateList? = null
    private var itemBackgroundRes: Int = 0
    private var tempChildWidths: IntArray

    private var presenter: FlexNavigationPresenter? = null
    private var menu: MenuBuilder? = null

    init {
        val res = resources
        inactiveItemMaxWidth = res.getDimensionPixelSize(
                R.dimen.design_bottom_navigation_item_max_width)
        inactiveItemMinWidth = res.getDimensionPixelSize(
                R.dimen.design_bottom_navigation_item_min_width)
        activeItemMaxWidth = res.getDimensionPixelSize(
                R.dimen.design_bottom_navigation_active_item_max_width)
        itemHeight = res.getDimensionPixelSize(R.dimen.design_bottom_navigation_height)

        transitionSet = AutoTransition()
        transitionSet.ordering = TransitionSet.ORDERING_TOGETHER
        transitionSet.duration = ACTIVE_ANIMATION_DURATION_MS
        transitionSet.interpolator = FastOutSlowInInterpolator()
        transitionSet.addTransition(TextScale())

        mOnClickListener = View.OnClickListener { v ->
            val itemView = v as FlexItemView
            val item = itemView.itemData
            menu?.let {
                if (!it.performItemAction(item, presenter, 0)) {
                    item?.isChecked = true
                }
            }

        }
        tempChildWidths = IntArray(FlexNavigationMenu.MAX_ITEM_COUNT)
    }

    override fun initialize(menu: MenuBuilder) {
        this.menu = menu
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = View.MeasureSpec.getSize(widthMeasureSpec)
        val count = childCount

        val heightSpec = View.MeasureSpec.makeMeasureSpec(itemHeight, View.MeasureSpec.EXACTLY)

        val maxAvailable = width / if (count == 0) 1 else count
        val childWidth = Math.min(maxAvailable, activeItemMaxWidth)
        var extra = width - childWidth * count
        for (i in 0 until count) {
            tempChildWidths[i] = childWidth
            if (extra > 0) {
                tempChildWidths[i]++
                extra--
            }
        }

        var totalWidth = 0
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility == View.GONE) {
                continue
            }
            child.measure(View.MeasureSpec.makeMeasureSpec(tempChildWidths[i], View.MeasureSpec.EXACTLY),
                    heightSpec)
            val params = child.layoutParams
            params.width = child.measuredWidth
            totalWidth += child.measuredWidth
        }
        setMeasuredDimension(
                View.resolveSizeAndState(totalWidth,
                        View.MeasureSpec.makeMeasureSpec(totalWidth, View.MeasureSpec.EXACTLY), 0),
                View.resolveSizeAndState(itemHeight, heightSpec, 0))
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val count = childCount
        val width = right - left
        val height = bottom - top
        var used = 0
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility == View.GONE) {
                continue
            }
            if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                child.layout(width - used - child.measuredWidth, 0, width - used, height)
            } else {
                child.layout(used, 0, child.measuredWidth + used, height)
            }
            used += child.measuredWidth
        }
    }

    override fun getWindowAnimations(): Int {
        return 0
    }

    /**
     * Sets the tint which is applied to the menu items' icons.
     *
     * @param tint the tint to apply
     */
    fun setIconTintList(tint: ColorStateList) {
        itemIconTint = tint
        if (buttons == null) return

        buttons?.let {
            for (button in it) {
                button?.setIconTintList(tint)
            }
        }
    }

    /**
     * Sets the tint which is applied to the menu items' icons.
     *
     * @param tintList the list of tint to apply
     */
    fun setIconTintList(tintList: List<ColorStateList>) {
        itemIconTintList = tintList
        if (buttons == null) return

        buttons?.let {
            for (i in 0 until it.size) {
                it[i]?.setIconTintList(tintList[i])
            }
        }
    }

    /**
     * Returns the tint which is applied to menu items' icons.
     *
     * @return the ColorStateList that is used to tint menu items' icons
     */
    fun getIconTintList(): ColorStateList? {
        return itemIconTint
    }

    /**
     * Sets the text color to be used on menu items.
     *
     * @param color the ColorStateList used for menu items' text.
     */
    fun setItemTextColor(color: ColorStateList) {
        itemTextColor = color
        if (buttons == null) return

        buttons?.let {
            for (button in it) {
                button?.setTextColor(color)
            }
        }

    }

    /**
     * Returns the text color used on menu items.
     *
     * @return the ColorStateList used for menu items' text
     */
    fun getItemTextColor(): ColorStateList? {
        return itemTextColor
    }

    /**
     * Sets the resource ID to be used for item background.
     *
     * @param background the resource ID of the background
     */
    fun setItemBackgroundRes(background: Int) {
        itemBackgroundRes = background
        if (buttons == null) return
        buttons?.let {
            for (button in it) {
                button?.setItemBackground(background)
            }
        }
    }

    /**
     * Returns the resource ID for the background of the menu items.
     *
     * @return the resource ID for the background
     */
    fun getItemBackgroundRes(): Int {
        return itemBackgroundRes
    }

    fun setPresenter(presenter: FlexNavigationPresenter) {
        this.presenter = presenter
    }

    fun buildMenuView() {
        removeAllViews()
        buttons?.let {
            for (button in it) {
                button?.let { item -> itemPool.release(item) }
            }
        }
        val menuSize = menu?.size() ?: 0
        if (menuSize == 0) {
            selectedItemId = 0
            selectedItemPosition = 0
            buttons = null
            return
        }
        buttons = arrayOfNulls(menuSize)

        for (i in 0 until menuSize) {
            presenter?.setUpdateSuspended(true)
            menu?.getItem(i)?.isCheckable = true
            presenter?.setUpdateSuspended(false)
            val child = getNewItem()
            buttons?.let { it[i] = child }
            itemIconTint?.let { child.setIconTintList(it) }
            itemTextColor?.let { child.setTextColor(it) }
            child.hideTitle()
            child.setItemBackground(itemBackgroundRes)
            child.initialize(menu?.getItem(i) as MenuItemImpl, 0)
            child.setItemPosition(i)
            child.setOnClickListener(mOnClickListener)
            addView(child)
        }
        selectedItemPosition = Math.min(menuSize - 1, selectedItemPosition)
        menu?.getItem(selectedItemPosition)?.isChecked = true
    }

    fun updateMenuView() {
        val menuSize = menu?.size() ?: 0
        if (menuSize != buttons?.size) {
            // The size has changed. Rebuild menu view from scratch.
            buildMenuView()
            return
        }
        val previousSelectedId = selectedItemId

        for (i in 0 until menuSize) {
            val item = menu?.getItem(i)
            item?.let {
                if (it.isChecked) {
                    selectedItemId = item.itemId
                    selectedItemPosition = i
                }
            }
        }
        if (previousSelectedId != selectedItemId) {
            // Note: this has to be called before BottomNavigationItemView#initialize().
            TransitionManager.beginDelayedTransition(this, transitionSet)
        }

        for (i in 0 until menuSize) {
            presenter?.setUpdateSuspended(true)
            buttons?.let { it[i]?.initialize(menu?.getItem(i) as MenuItemImpl, 0) }
            presenter?.setUpdateSuspended(false)
        }

    }

    private fun getNewItem(): FlexItemView {
        var item: FlexItemView? = itemPool.acquire()
        if (item == null) {
            item = FlexItemView(context)
        }
        return item
    }

    fun getSelectedItemId(): Int {
        return selectedItemId
    }

    internal fun tryRestoreSelectedItemId(itemId: Int) {
        val size = menu?.size()
        size?.let {
            for (i in 0 until it) {
                val item = menu?.getItem(i)
                if (itemId == item?.itemId) {
                    selectedItemId = itemId
                    selectedItemPosition = i
                    item.isChecked = true
                    break
                }
            }
        }
    }


}