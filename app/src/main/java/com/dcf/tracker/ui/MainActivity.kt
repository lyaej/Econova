package com.dcf.tracker.ui

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.dcf.tracker.R
import com.dcf.tracker.data.Prefs
import com.dcf.tracker.databinding.ActivityMainBinding
import com.google.android.material.chip.Chip

data class DigitalProduct(
    val name: String,
    val kgCO2perYear: Double
)

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private val selectedProducts = mutableListOf<DigitalProduct>()

    private val allProducts = listOf(
        DigitalProduct("Smartphone", 70.0),
        DigitalProduct("Laptop", 156.0),
        DigitalProduct("Tablet", 85.0),
        DigitalProduct("Desktop PC" , 175.0),
        DigitalProduct("Smart TV", 130.0),
        DigitalProduct("Gaming Console", 89.0),
        DigitalProduct("Smart Speaker",  20.0),
        DigitalProduct("Smartwatch",  15.0),
        DigitalProduct("Email (work)",  35.0),
        DigitalProduct("Social Media",  45.0),
        DigitalProduct("Music Streaming",  25.0),
        DigitalProduct("Video Streaming (mobile)",  90.0),
        DigitalProduct("Online Gaming",  65.0),
        DigitalProduct("Cloud Storage (personal)",  10.0),
        DigitalProduct("E-Book Reader",  12.0)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        val name = Prefs(this).userName ?: "User"
        b.tvGreeting.text = "Hello, $name!"

        setupProductGrid()
        setupPresets()
        updateFootprint()

        b.btnAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        b.btnClearData.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Clear Data")
                .setMessage("This will reset your name and all selections. Continue?")
                .setPositiveButton("Clear") { _, _ ->
                    Prefs(this).userName = null
                    selectedProducts.clear()
                    startActivity(Intent(this, OnboardingActivity::class.java))
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        b.btnDownloadPng.setOnClickListener { exportAsPng() }
    }

    private fun setupProductGrid() {
        b.productGrid.removeAllViews()
        allProducts.forEach { product ->
            val chip = Chip(this).apply {
                text = "${product.name}"
                isCheckable = true
                isChecked = selectedProducts.contains(product)
                setOnCheckedChangeListener { _, checked ->
                    if (checked) selectedProducts.add(product)
                    else selectedProducts.remove(product)
                    updateFootprint()
                }
            }
            b.productGrid.addView(chip)
        }
    }

    private fun setupPresets() {
        b.spinnerPreset.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                applyPreset(pos)
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
    }

    private fun applyPreset(pos: Int) {
        selectedProducts.clear()
        when (pos) {
            1 -> selectedProducts.addAll(allProducts.filter { it.name in listOf("Smartphone","Email (work)","Social Media","Music Streaming") })
            2 -> selectedProducts.addAll(allProducts.filter { it.name in listOf("Smartphone","Laptop","Smart TV","Social Media","Video Streaming (mobile)","Cloud Storage (personal)") })
            3 -> selectedProducts.addAll(allProducts)
            4 -> selectedProducts.addAll(allProducts.filter { it.name in listOf("Smart TV","Video Streaming (mobile)") })
            5 -> selectedProducts.addAll(allProducts.filter { it.name in listOf("Laptop","Smartphone","Email (work)","Cloud Storage (personal)") })
        }
        setupProductGrid()
        updateFootprint()
    }

    private fun updateFootprint() {
        val total = selectedProducts.sumOf { it.kgCO2perYear }
        b.tvFootprintValue.text = String.format("%.1f", total)
        b.tvFootprintUnit.text = "kg CO₂ per year"

        b.breakdownContainer.removeAllViews()
        selectedProducts.forEach { p ->
            val row = LayoutInflater.from(this).inflate(R.layout.item_breakdown_row, b.breakdownContainer, false)
            row.findViewById<TextView>(R.id.tvItemName).text = "${p.name}"
            row.findViewById<TextView>(R.id.tvItemValue).text = "${p.kgCO2perYear} kg"
            val bar = row.findViewById<View>(R.id.barFill)
            val maxVal = allProducts.maxOf { it.kgCO2perYear }
            bar.layoutParams = (bar.layoutParams as LinearLayout.LayoutParams).also {
                it.weight = (p.kgCO2perYear / maxVal).toFloat()
            }
            b.breakdownContainer.addView(row)
        }
    }

    private fun exportAsPng() {
        try {
            val card = b.footprintCard
            val bmp = Bitmap.createBitmap(card.width, card.height, Bitmap.Config.ARGB_8888)
            card.draw(Canvas(bmp))

            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "carbon_footprint_${System.currentTimeMillis()}.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Econova")
            }
            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            uri?.let {
                contentResolver.openOutputStream(it)?.use { out ->
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                Toast.makeText(this, "PNG saved to Gallery", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}