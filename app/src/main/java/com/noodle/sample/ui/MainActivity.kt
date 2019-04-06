package com.noodle.sample.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.noodle.sample.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setTitle(R.string.main_title)

        findViewById<View>(R.id.addBook).setOnClickListener {
            val editBookFragment = EditBookFragment()
            editBookFragment.show(supportFragmentManager, "add_book")
        }
    }

}
