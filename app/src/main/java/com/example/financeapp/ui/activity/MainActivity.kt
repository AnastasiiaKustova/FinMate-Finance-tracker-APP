package com.example.financeapp.ui.activity

import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.financeapp.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.financeapp.ui.fragments.Home
import com.example.financeapp.ui.fragments.MoreScreen
import com.example.financeapp.ui.fragments.allList.AllPlannings
import com.example.financeapp.ui.fragments.statistic.Statistic
import com.example.financeapp.ui.fragments.add.AddTransaction
import com.example.financeapp.ui.common.CurrencyManager
import com.example.financeapp.ui.common.LanguageManager
import com.example.financeapp.ui.fragments.allList.AllTransaction
import com.google.android.material.navigation.NavigationBarView
import com.jakewharton.threetenabp.AndroidThreeTen

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private var navListener: NavigationBarView.OnItemSelectedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        LanguageManager.init(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_screen)

        CurrencyManager.init(this)
        AndroidThreeTen.init(this)

        bottomNav = findViewById(R.id.bottomNavigationView)
        bottomNav.selectedItemId = R.id.home
        navigateTo(R.id.home)

        navListener = NavigationBarView.OnItemSelectedListener { item ->
            navigateTo(item.itemId)
            true
        }
        bottomNav.setOnItemSelectedListener(navListener)
    }

    fun navigateTo(itemId: Int) {
        val fragment = when (itemId) {
            R.id.home -> Home()
            R.id.statistic -> Statistic()
            R.id.add -> AddTransaction()
            R.id.planing -> AllPlannings()
            R.id.more -> MoreScreen()
            else -> null
        } ?: return

        replaceFragment(fragment)

        bottomNav.setOnItemSelectedListener(null)
        bottomNav.selectedItemId = itemId
        bottomNav.setOnItemSelectedListener(navListener)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.popBackStack(
            null,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )

        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()

    }

    fun openHomeThenAllTransactions() {
        navigateTo(R.id.home)
        supportFragmentManager.executePendingTransactions()
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, AllTransaction())
            .addToBackStack(null)
            .commit()
    }
}

