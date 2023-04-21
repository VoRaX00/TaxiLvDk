package com.example.lvdk

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class GeneralActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // requestedOrientation =  ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_general)
    }

    fun onClickDriver(view: View)
    {
        val intentDriver = Intent(this,DriverActivity::class.java)
        startActivity(intentDriver)
    }
}