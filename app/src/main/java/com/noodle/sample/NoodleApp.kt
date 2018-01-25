package com.noodle.sample

import android.app.Application

import com.noodle.sample.data.BookManager

class NoodleApp : Application() {

    lateinit var bookManager: BookManager
        private set

    override fun onCreate() {
        super.onCreate()

        bookManager = BookManager(this)
    }
}
