package com.dcf.tracker.ui

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.dcf.tracker.R
import com.dcf.tracker.data.Prefs
import com.dcf.tracker.databinding.ActivityHistoryBinding
import com.dcf.tracker.model.DailyLog
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var b: ActivityHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(b.root)
        setSupportActionBar(b.toolbar)
        supportActionBar?.apply { setDisplayHomeAsUpEnabled(true); title = "History" }

        val logs = Prefs(this).getDailyLogs().sortedByDescending { it.date }
        updateChart(logs, "daily")
        showLogList(logs)

        b.filterGroup.setOnCheckedChangeListener { _, id ->
            val filter = when (id) {
                R.id.rbWeekly -> "weekly"
                R.id.rbMonthly -> "monthly"
                R.id.rbYearly -> "yearly"
                else -> "daily"
            }
            updateChart(logs, filter)
        }
    }

    private fun updateChart(logs: List<DailyLog>, filter: String) {
        val data = when (filter) {
            "weekly" -> getWeeklyData(logs)
            "monthly" -> getMonthlyData(logs)
            "yearly" -> getYearlyData(logs)
            else -> getDailyData(logs)
        }
        b.tvChartTitle.text = when (filter) {
            "weekly" -> "Last 4 Weeks (kg CO₂)"
            "monthly" -> "By Month (kg CO₂)"
            "yearly" -> "By Year (kg CO₂)"
            else -> "Last 7 Days (kg CO₂)"
        }
        b.ivHistoryChart.post {
            val w = b.ivHistoryChart.width.coerceAtLeast(200)
            val h = b.ivHistoryChart.height.coerceAtLeast(150)
            b.ivHistoryChart.setImageBitmap(ChartUtils.drawBarChart(data, w, h))
        }
    }

    private fun getDailyData(logs: List<DailyLog>): List<Pair<String, Double>> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val lbl = SimpleDateFormat("MM/dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        return (6 downTo 0).map { i ->
            cal.time = Date(); cal.add(Calendar.DAY_OF_YEAR, -i)
            val dateStr = sdf.format(cal.time)
            val co2 = logs.filter { it.date == dateStr }.sumOf { it.dailyCO2 }
            Pair(lbl.format(cal.time), co2)
        }
    }

    private fun getWeeklyData(logs: List<DailyLog>): List<Pair<String, Double>> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return (3 downTo 0).map { w ->
            val end = Calendar.getInstance().also { it.add(Calendar.WEEK_OF_YEAR, -w) }
            val start = Calendar.getInstance().also { it.add(Calendar.WEEK_OF_YEAR, -w); it.add(Calendar.DAY_OF_YEAR, -6) }
            val co2 = logs.filter {
                try { val d = sdf.parse(it.date)!!; d >= start.time && d <= end.time } catch (e: Exception) { false }
            }.sumOf { it.dailyCO2 }
            Pair("W${4 - w}", co2)
        }
    }

    private fun getMonthlyData(logs: List<DailyLog>): List<Pair<String, Double>> {
        val months = arrayOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
        val map = mutableMapOf<Int, Double>()
        logs.forEach { log ->
            try {
                val cal = Calendar.getInstance()
                cal.time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(log.date)!!
                val m = cal.get(Calendar.MONTH)
                map[m] = (map[m] ?: 0.0) + log.dailyCO2
            } catch (e: Exception) {}
        }
        return months.mapIndexed { i, name -> Pair(name, map[i] ?: 0.0) }
    }

    private fun getYearlyData(logs: List<DailyLog>): List<Pair<String, Double>> {
        val map = mutableMapOf<Int, Double>()
        logs.forEach { log ->
            try { val y = log.date.substring(0, 4).toInt(); map[y] = (map[y] ?: 0.0) + log.dailyCO2 }
            catch (e: Exception) {}
        }
        return if (map.isEmpty()) listOf(Pair("No data", 0.0))
        else map.entries.sortedBy { it.key }.map { Pair(it.key.toString(), it.value) }
    }

    private fun showLogList(logs: List<DailyLog>) {
        b.logListContainer.removeAllViews()
        if (logs.isEmpty()) {
            val tv = TextView(this).apply {
                text = "No logs yet. Use the calculator and tap Save Today's Log."
                textSize = 13f; setTextColor(Color.parseColor("#888888"))
            }
            b.logListContainer.addView(tv); return
        }
        logs.take(30).forEach { log ->
            val view = layoutInflater.inflate(R.layout.item_log_entry, b.logListContainer, false)
            view.findViewById<TextView>(R.id.tvLogDate).text = log.date
            view.findViewById<TextView>(R.id.tvLogCO2).text = "${String.format("%.4f", log.dailyCO2)} kg CO₂"
            view.findViewById<TextView>(R.id.tvLogDetail).text = log.entries.entries.joinToString(", ") { "${it.key}: ${it.value}h" }
            b.logListContainer.addView(view)
        }
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }
}