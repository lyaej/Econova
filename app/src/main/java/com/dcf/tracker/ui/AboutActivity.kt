package com.dcf.tracker.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.dcf.tracker.R
import com.dcf.tracker.databinding.ActivityAboutBinding
import com.google.android.material.button.MaterialButton

class AboutActivity : AppCompatActivity() {

    data class Researcher(
        val name: String,
        val facebook: String,   // full URL
        val instagram: String   // full URL
    )

    private val researchers = listOf(
        Researcher(
            "Catague, Elljah Aneeza",
            "https://facebook.com/elljahh",  // TODO: replace with actual URL
            "https://www.instagram.com/e.lljh/"  // TODO: replace with actual URL
        ),
        Researcher(
            "Custodio, Hannah Faith",
            "https://www.facebook.com/custodiohannahfaith",
            "https://www.instagram.com/my.hnnaqh_27/"
        ),
        Researcher(
            "Lugtu, Lyka Mae",
            "https://www.facebook.com/llykmae",
            "https://www.instagram.com/llykmae/"
        )
    )

    private val cardIds = listOf(R.id.researcher1, R.id.researcher2, R.id.researcher3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(b.root)
        setSupportActionBar(b.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "About Us"
        }

        researchers.forEachIndexed { i, r ->
            val card = b.root.findViewById<View>(cardIds[i])
            card.findViewById<TextView>(R.id.tvName).text = r.name
            card.findViewById<MaterialButton>(R.id.btnFacebook).setOnClickListener {
                openUrl(r.facebook)
            }
            card.findViewById<MaterialButton>(R.id.btnInstagram).setOnClickListener {
                openUrl(r.instagram)
            }

             val photoRes = resources.getIdentifier("researcher${i+1}", "drawable", packageName)
             if (photoRes != 0) card.findViewById<ImageView>(R.id.ivPhoto).setImageResource(photoRes)
        }
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
