package com.dcf.tracker.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dcf.tracker.data.Prefs
import com.dcf.tracker.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.btnContinue.setOnClickListener {
            val name = b.etName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Prefs(this).userName = name
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
