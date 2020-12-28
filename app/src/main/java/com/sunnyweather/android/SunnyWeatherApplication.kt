package com.sunnyweather.android

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class SunnyWeatherApplication:Application() {
    companion object {

        const val TOKEN = "89f3efbd4aa183d495af2f94e8d9ef10" // 填入你申请到的令牌值

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}