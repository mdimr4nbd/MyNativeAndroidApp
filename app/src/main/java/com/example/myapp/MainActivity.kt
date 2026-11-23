
package com.example.myapp

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val text = TextView(this).apply {
            text = "Hello Android 16!\nMy First Kotlin App"
            textSize = 22f
            gravity = Gravity.CENTER
        }

        setContentView(text)
    }
}
