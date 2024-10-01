package com.example.aplicacion.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.aplicacion.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        Toast.makeText(this, "MainActivity", Toast.LENGTH_SHORT).show()


        Handler(Looper.getMainLooper()).postDelayed({

            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()
        }, 2000)
    }
}
