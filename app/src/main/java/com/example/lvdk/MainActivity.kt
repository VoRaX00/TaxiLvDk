package com.example.lvdk

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val handler = Handler()
        handler.postDelayed({
            // Код, который нужно выполнить через 2 секунды
            val intentReg = Intent(this, RegActivity::class.java)
            startActivity(intentReg)
        }, 1500)
    }
}