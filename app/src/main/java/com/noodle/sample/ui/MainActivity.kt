package com.noodle.sample.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View

import com.noodle.sample.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setTitle(R.string.main_title)

        findViewById(R.id.addBook).setOnClickListener {
            val editBookFragment = EditBookFragment()
            editBookFragment.show(supportFragmentManager, "add_book")
        }
    }

}
