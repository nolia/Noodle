package com.noodle.sample

import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.androidannotations.annotations.AfterViews
import org.androidannotations.annotations.Bean
import org.androidannotations.annotations.Click
import org.androidannotations.annotations.EActivity

@EActivity(R.layout.activity_main)
open class MainActivity : AppCompatActivity() {

    @Bean
    lateinit internal var someManager: SomeManager


    @AfterViews
    fun afterView() {
        theText.text = "Hello"
    }

    @Click(R.id.buttonHey)
    internal fun onHeyClick() {
        someManager.saySomething()
    }

    @Click(R.id.buttonExit)
    internal fun onExitClick() {
        finish()
    }

}
