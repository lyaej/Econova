package com.dcf.tracker.ui

import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.dcf.tracker.R
import com.dcf.tracker.data.Prefs
import com.dcf.tracker.databinding.ActivityMainBinding
import com.dcf.tracker.model.DailyLog
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.*

data class DigitalProduct(val name: String, val kgCO2perYear: Double)

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private val selectedProducts = mutableListOf<DigitalProduct>()
    private val hoursMap = mutableMapOf<String, Int>()
    private var selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private val allProducts = listOf(
        DigitalProduct("Smartphone", 70.0),
        DigitalProduct("Laptop", 156.0),
        DigitalProduct("Tablet", 85.0),
        DigitalProduct("Desktop PC", 175.0),
        DigitalProduct("Smart TV", 130.0),
        DigitalProduct("Gaming Console", 89.0),
        DigitalProduct("Smart Speaker", 20.0),
        DigitalProduct("Smartwatch", 15.0),
        DigitalProduct("Email", 35.0),
        DigitalProduct("Social Media", 45.0),
        DigitalProduct("Music Streaming", 25.0),
        DigitalProduct("Video Streaming", 90.0),
        DigitalProduct("Online Gaming", 65.0),
        DigitalProduct("Cloud Storage", 10.0),
        DigitalProduct("E-Book Reader", 12.0)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.tvGreeting.text = "Hello, ${Prefs(this).userName ?: "User"}!"
        b.tvSelectedDate.text = selectedDate

        setupProductGrid()
        setupPresets()
        updateFootprint()

        b.btnPickDate.setOnClickListener { pickDate() }
        b.btnAbout.setOnClickListener { startActivity(Intent(this, AboutActivity::class.java)) }
        b.btnHistory.setOnClickListener { startActivity(Intent(this, HistoryActivity::class.java)) }
        b.btnClearData.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Clear Data")
                .setMessage("This will delete your name and all history records. Continue?")
                .setPositiveButton("Clear") { _, _ ->
                    Prefs(this).clearAll()
                    startActivity(Intent(this, OnboardingActivity::class.java))
                    finish()
                }.setNegativeButton("Cancel", null).show()
        }
        b.btnSaveLog.setOnClickListener { saveLog() }
        b.btnDownloadPng.setOnClickListener { exportAsPng() }
    }

    private fun pickDate() {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            selectedDate = String.format("%04d-%02d-%02d", y, m + 1, d)
            b.tvSelectedDate.text = selectedDate
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun setupProductGrid() {
        b.productGrid.removeAllViews()
        allProducts.forEach { product ->
            val chip = Chip(this).apply {
                text = product.name
                isCheckable = true
                isChecked = selectedProducts.any { it.name == product.name }
                setOnCheckedChangeListener { _, checked ->
                    if (checked) selectedProducts.add(product)
                    else { selectedProducts.removeAll { it.name == product.name }; hoursMap.remove(product.name) }
                    updateHoursSection(); updateFootprint()
                }
            }
            b.productGrid.addView(chip)
        }
    }

    private fun updateHoursSection() {
        b.hoursSection.visibility = if (selectedProducts.isEmpty()) View.GONE else View.VISIBLE
        b.hoursContainer.removeAllViews()
        selectedProducts.forEach { product ->
            val row = LayoutInflater.from(this).inflate(R.layout.item_hours_row, b.hoursContainer, false)
            row.findViewById<TextView>(R.id.tvProductName).text = product.name
            val et = row.findViewById<EditText>(R.id.etHours)
            et.setText((hoursMap[product.name] ?: 1).toString())
            et.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    hoursMap[product.name] = s?.toString()?.toIntOrNull()?.coerceIn(0, 24) ?: 0
                    updateFootprint()
                }
                override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
                override fun onTextChanged(s: CharSequence?, st: Int, b2: Int, c: Int) {}
            })
            b.hoursContainer.addView(row)
        }
    }

    private fun setupPresets() {
        b.spinnerPreset.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) { applyPreset(pos) }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
    }

    private fun applyPreset(pos: Int) {
        selectedProducts.clear(); hoursMap.clear()
        val names = when (pos) {
            1 -> listOf("Smartphone", "Email", "Social Media", "Music Streaming")
            2 -> listOf("Smartphone", "Laptop", "Smart TV", "Social Media", "Video Streaming", "Cloud Storage")
            3 -> allProducts.map { it.name }
            4 -> listOf("Smart TV", "Video Streaming")
            5 -> listOf("Laptop", "Smartphone", "Email", "Cloud Storage")
            else -> emptyList()
        }
        selectedProducts.addAll(allProducts.filter { it.name in names })
        setupProductGrid(); updateHoursSection(); updateFootprint()
    }

    private fun hours(p: DigitalProduct) = (hoursMap[p.name] ?: 1).toDouble()

    private fun updateFootprint() {
        val daily = selectedProducts.sumOf { p -> (p.kgCO2perYear / 365.0) * (hours(p) / 24.0) }
        val yearly = daily * 365.0
        b.tvFootprintValue.text = String.format("%.1f", yearly)
        b.tvFootprintUnit.text = "kg CO2/year"
        b.tvDailyValue.text = "Daily estimate: ${String.format("%.4f", daily)} kg CO2"

        val pieData = selectedProducts.map { p -> Pair(p.name, p.kgCO2perYear * (hours(p) / 24.0)) }
        val barData = selectedProducts.map { p -> Pair(p.name.take(6), p.kgCO2perYear * (hours(p) / 24.0)) }

        b.ivPieChart.post {
            val s = b.ivPieChart.width.coerceAtLeast(200)
            b.ivPieChart.setImageBitmap(ChartUtils.drawPieChart(pieData, s))
        }
        b.ivBarChart.post {
            val w = b.ivBarChart.width.coerceAtLeast(200)
            val h = b.ivBarChart.height.coerceAtLeast(150)
            b.ivBarChart.setImageBitmap(ChartUtils.drawBarChart(barData, w, h))
        }
        updateLegend(pieData)
    }

    private fun updateLegend(data: List<Pair<String, Double>>) {
        b.legendContainer.removeAllViews()
        data.forEachIndexed { i, (name, value) ->
            val tv = TextView(this).apply {
                text = "  $name: ${String.format("%.2f", value)} kg"
                textSize = 11f
                setTextColor(ChartUtils.CHART_COLORS[i % ChartUtils.CHART_COLORS.size])
            }
            b.legendContainer.addView(tv)
        }
    }

    private fun saveLog() {
        if (selectedProducts.isEmpty()) {
            Toast.makeText(this, "Select at least one product", Toast.LENGTH_SHORT).show(); return
        }
        val daily = selectedProducts.sumOf { p -> (p.kgCO2perYear / 365.0) * (hours(p) / 24.0) }
        val entries = selectedProducts.associate { p -> p.name to (hoursMap[p.name] ?: 1) }
        Prefs(this).saveDailyLog(DailyLog(selectedDate, entries, daily))
        Toast.makeText(this, "Log saved for $selectedDate", Toast.LENGTH_SHORT).show()
    }

    private fun exportAsPng() {
        try {
            val card = b.footprintCard
            val bmp = Bitmap.createBitmap(card.width, card.height, Bitmap.Config.ARGB_8888)
            card.draw(Canvas(bmp))
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "econova_${System.currentTimeMillis()}.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/EcoNova")
            }
            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            uri?.let { contentResolver.openOutputStream(it)?.use { out -> bmp.compress(Bitmap.CompressFormat.PNG, 100, out) } }
            Toast.makeText(this, "PNG saved to Gallery", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}