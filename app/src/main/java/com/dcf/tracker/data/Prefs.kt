package com.dcf.tracker.data

import android.content.Context

class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("dcf_prefs", Context.MODE_PRIVATE)

    var userName: String?
        get() = sp.getString("user_name", null)
        set(v) = sp.edit().putString("user_name", v).apply()
}
