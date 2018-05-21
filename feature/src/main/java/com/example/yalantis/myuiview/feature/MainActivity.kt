package com.example.yalantis.myuiview.feature

import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.example.yalantis.myuiview.feature.views.flexMenu.FlexNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


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

        val colors = intArrayOf(R.color.menu_home, R.color.menu_message, R.color.menu_store, R.color.menu_university, R.color.menu_notification, -1)
        navigation.setItemIconTintList(baseContext, colors, R.color.menu_unselected)
        navigation.customCircleIcons = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            navigation.getMenu().getItem(5).icon = baseContext.getDrawable(R.drawable.tiger)
        }
    }

}
