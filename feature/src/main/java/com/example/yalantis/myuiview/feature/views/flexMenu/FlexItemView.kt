package com.example.yalantis.myuiview.feature.views.flexMenu

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.view.PointerIconCompat
import android.support.v4.view.ViewCompat
import android.support.v7.view.menu.MenuItemImpl
import android.support.v7.view.menu.MenuView
import android.support.v7.widget.TooltipCompat
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.example.yalantis.myuiview.feature.R

@SuppressLint("RestrictedApi, PrivateResource")
class FlexItemView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr), MenuView.ItemView {

    companion object {
        const val INVALID_ITEM_POSITION = -1
        private val CHECKED_STATE_SET = intArrayOf(android.R.attr.state_checked)
    }

    private var defaultMargin: Int
    private var mShiftAmount: Int
    private var scaleUpFactor: Float
    private var scaleDownFactor: Float
    private val padding = 40f
    var isCircleItem: Boolean = false

    private var icon: ImageView
    private var smallLabel: TextView
    private var largeLabel: TextView
    private var itemPosition = INVALID_ITEM_POSITION
    private var labelHidden = false

    private var itemData: MenuItemImpl? = null

    private var iconTint: ColorStateList? = null

    init {
        val res = resources
        val inactiveLabelSize = res.getDimensionPixelSize(R.dimen.design_bottom_navigation_text_size)
        val activeLabelSize = res.getDimensionPixelSize(
                R.dimen.design_bottom_navigation_active_text_size)
        defaultMargin = res.getDimensionPixelSize(R.dimen.design_bottom_navigation_margin)
        mShiftAmount = inactiveLabelSize - activeLabelSize
        scaleUpFactor = 1f * activeLabelSize / inactiveLabelSize
        scaleDownFactor = 1f * inactiveLabelSize / activeLabelSize

        LayoutInflater.from(context).inflate(R.layout.navigation_item, this, true)
        setBackgroundResource(R.drawable.design_bottom_navigation_item_background)
        icon = findViewById(R.id.icon)
        smallLabel = findViewById(R.id.smallLabel)
        largeLabel = findViewById(R.id.largeLabel)
    }

    override fun initialize(itemData: MenuItemImpl, menuType: Int) {
        this.itemData = itemData
        setCheckable(itemData.isCheckable)
        setChecked(itemData.isChecked)
        isEnabled = itemData.isEnabled
        setIcon(itemData.icon)
        setTitle(itemData.title)
        id = itemData.itemId
        contentDescription = itemData.contentDescription
        TooltipCompat.setTooltipText(this, itemData.tooltipText)
    }

    fun setItemPosition(position: Int) {
        itemPosition = position
    }

    fun getItemPosition(): Int {
        return itemPosition
    }

    override fun getItemData(): MenuItemImpl? {
        return itemData
    }

    override fun setTitle(title: CharSequence) {
        smallLabel.text = title
        largeLabel.text = title
    }

    fun hideTitle() {
        smallLabel.visibility = View.GONE
        largeLabel.visibility = View.GONE
        labelHidden = true
    }

    fun showTitle() {
        smallLabel.visibility = View.VISIBLE
        largeLabel.visibility = View.VISIBLE
        labelHidden = false
    }

    override fun setCheckable(checkable: Boolean) {
        refreshDrawableState()
    }

    override fun setChecked(checked: Boolean) {
        largeLabel.pivotX = (largeLabel.width / 2).toFloat()
        largeLabel.pivotY = largeLabel.baseline.toFloat()
        smallLabel.pivotX = (smallLabel.width / 2).toFloat()
        smallLabel.pivotY = smallLabel.baseline.toFloat()

        if (checked) {
            val iconParams = icon.layoutParams as FrameLayout.LayoutParams
            iconParams.gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
            iconParams.topMargin = defaultMargin + mShiftAmount
            icon.layoutParams = iconParams
            if (!labelHidden) {
                largeLabel.visibility = View.VISIBLE
                smallLabel.visibility = View.INVISIBLE
            }
            largeLabel.scaleX = 1f
            largeLabel.scaleY = 1f
            smallLabel.scaleX = scaleUpFactor
            smallLabel.scaleY = scaleUpFactor
        } else {
            val iconParams = icon.layoutParams as FrameLayout.LayoutParams
            iconParams.gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
            iconParams.topMargin = defaultMargin
            icon.layoutParams = iconParams
            if (!labelHidden) {
                largeLabel.visibility = View.INVISIBLE
                smallLabel.visibility = View.VISIBLE
            }
            largeLabel.scaleX = scaleDownFactor
            largeLabel.scaleY = scaleDownFactor
            smallLabel.scaleX = 1f
            smallLabel.scaleY = 1f
        }

        refreshDrawableState()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        smallLabel.isEnabled = enabled
        largeLabel.isEnabled = enabled
        icon.isEnabled = enabled

        if (enabled) {
            ViewCompat.setPointerIcon(this,
                    PointerIconCompat.getSystemIcon(context, PointerIconCompat.TYPE_HAND))
        } else {
            ViewCompat.setPointerIcon(this, null)
        }

    }

    public override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 1)
        itemData?.let {
            if (it.isCheckable && it.isChecked) {
                View.mergeDrawableStates(drawableState, CHECKED_STATE_SET)
            }
        }

        return drawableState
    }

    override fun setShortcut(showShortcut: Boolean, shortcutKey: Char) {}

    override fun setIcon(icon: Drawable?) {
        var newIcon = icon
        if (icon != null) {
            val state = icon.constantState
            newIcon = DrawableCompat.wrap(if (state == null) icon else state.newDrawable()).mutate()
            if (iconTint?.defaultColor != -1) {
                DrawableCompat.setTintList(icon, iconTint)
//                this.icon.setImageDrawable(newIcon)
            }
            else {
                this.icon.background = makeSelector()
//                if (isCircleItem)
//                    this.icon.setImageBitmap(getCroppedBitmap(convertToBitmap(newIcon, icon.minimumHeight, icon.minimumHeight)))
//                else
//                    this.icon.setImageDrawable(newIcon)

            }
            this.icon.setImageDrawable(newIcon)

        }
    }

    private fun makeSelector(): StateListDrawable {
        val res = StateListDrawable()
        res.setExitFadeDuration(400)
        res.addState(intArrayOf(android.R.attr.state_checked), ContextCompat.getDrawable(context, R.drawable.bg_tab_selected_focused))
        res.addState(intArrayOf(), ColorDrawable(Color.TRANSPARENT))
        return res
    }

    private fun convertToBitmap(drawable: Drawable, widthPixels: Int, heightPixels: Int): Bitmap {
        val mutableBitmap = Bitmap.createBitmap(widthPixels, heightPixels, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mutableBitmap)
        drawable.setBounds(0, 0, widthPixels, heightPixels)
        drawable.draw(canvas)

        return mutableBitmap
    }

    private fun getCroppedBitmap(bitmap: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width,
                bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val color = -0xbdbdbe
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle((bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat(),
                (bitmap.width / 2).toFloat() - padding, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }

    override fun prefersCondensedTitle(): Boolean {
        return false
    }

    override fun showsIcon(): Boolean {
        return true
    }

    fun setIconTintList(tint: ColorStateList) {
        iconTint = tint
        itemData?.let {
            // Update the icon so that the tint takes effect
            setIcon(it.icon)
        }
    }

    fun setTextColor(color: ColorStateList) {
        smallLabel.setTextColor(color)
        largeLabel.setTextColor(color)
    }

    fun setItemBackground(background: Int) {
        val backgroundDrawable = if (background == 0)
            null
        else
            ContextCompat.getDrawable(context, background)
        ViewCompat.setBackground(this, backgroundDrawable)
    }
}