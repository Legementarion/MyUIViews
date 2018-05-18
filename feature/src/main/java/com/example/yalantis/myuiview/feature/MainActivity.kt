package com.example.yalantis.myuiview.feature

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.example.yalantis.myuiview.feature.views.flexMenu.FlexNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        private val CHECKED_STATE_SET = intArrayOf(android.R.attr.state_checked)
        private val DISABLED_STATE_SET = intArrayOf(-android.R.attr.state_enabled)
        val EMPTY_STATE_SET = intArrayOf()
    }

    private val mOnNavigationItemSelectedListener = object : FlexNavigationView.OnNavigationItemSelectedListener {
        override fun onNavigationItemSelected(item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.navigation_home -> {
                    message.setText(R.string.title_home)
                    return true
                }
                R.id.navigation_message -> {
                    return true
                }
                R.id.navigation_store -> {
                    return true
                }
                R.id.navigation_university -> {
                    return true
                }
                R.id.navigation_notification -> {
                    return true
                }
                R.id.navigation_profile -> {
                    return true
                }
                else -> {
                    message.setText(R.string.title_notifications)
                    return true
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        snakeView.setCount(3, 1)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)


        navigation.setItemIconTintList(getTintList())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            navigation.getMenu().getItem(5).icon = baseContext.getDrawable(R.drawable.tiger)
        }
    }

    private fun getTintList(): List<ColorStateList> {
        val colorTintList = mutableListOf<ColorStateList>()
        val states = arrayOf(CHECKED_STATE_SET,
                DISABLED_STATE_SET, EMPTY_STATE_SET)

        var colors = intArrayOf(ContextCompat.getColor(baseContext, R.color.menu_home),
                ContextCompat.getColor(baseContext, R.color.menu_unselected),
                ContextCompat.getColor(baseContext, R.color.menu_unselected))
        colorTintList.add(ColorStateList(states, colors))

        colors = intArrayOf(ContextCompat.getColor(baseContext, R.color.menu_message),
                ContextCompat.getColor(baseContext, R.color.menu_unselected),
                ContextCompat.getColor(baseContext, R.color.menu_unselected))
        colorTintList.add(ColorStateList(states, colors))

        colors = intArrayOf(ContextCompat.getColor(baseContext, R.color.menu_store),
                ContextCompat.getColor(baseContext, R.color.menu_unselected),
                ContextCompat.getColor(baseContext, R.color.menu_unselected))
        colorTintList.add(ColorStateList(states, colors))

        colors = intArrayOf(ContextCompat.getColor(baseContext, R.color.menu_university),
                ContextCompat.getColor(baseContext, R.color.menu_unselected),
                ContextCompat.getColor(baseContext, R.color.menu_unselected))
        colorTintList.add(ColorStateList(states, colors))

        colors = intArrayOf(ContextCompat.getColor(baseContext, R.color.menu_notification),
                ContextCompat.getColor(baseContext, R.color.menu_unselected),
                ContextCompat.getColor(baseContext, R.color.menu_unselected))
        colorTintList.add(ColorStateList(states, colors))

        colors = intArrayOf(-1, -1, -1)
        colorTintList.add(ColorStateList(states, colors))

        return colorTintList
    }
}
