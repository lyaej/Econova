package com.dcf.tracker.data

import android.content.Context
import com.dcf.tracker.model.DailyLog
import org.json.JSONArray
import org.json.JSONObject

class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("dcf_prefs", Context.MODE_PRIVATE)

    var userName: String?
        get() = sp.getString("user_name", null)
        set(v) = sp.edit().putString("user_name", v).apply()

    fun clearAll() = sp.edit().clear().apply()

    fun saveDailyLog(log: DailyLog) {
        val logs = getDailyLogs().filter { it.date != log.date }.toMutableList()
        logs.add(log)
        val arr = JSONArray()
        logs.forEach { l ->
            val obj = JSONObject()
            obj.put("date", l.date); obj.put("co2", l.dailyCO2)
            val ent = JSONObject()
            l.entries.forEach { (k, v) -> ent.put(k, v) }
            obj.put("entries", ent); arr.put(obj)
        }
        sp.edit().putString("logs", arr.toString()).apply()
    }

    fun getDailyLogs(): List<DailyLog> {
        return try {
            val arr = JSONArray(sp.getString("logs", "[]") ?: "[]")
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                val ent = obj.getJSONObject("entries")
                val entries = mutableMapOf<String, Int>()
                ent.keys().forEach { k -> entries[k] = ent.getInt(k) }
                DailyLog(obj.getString("date"), entries, obj.getDouble("co2"))
            }
        } catch (e: Exception) { emptyList() }
    }
}