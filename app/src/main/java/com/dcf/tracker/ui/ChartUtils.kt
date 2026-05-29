package com.dcf.tracker.ui

import android.graphics.*

object ChartUtils {

    val CHART_COLORS = listOf(
        0xFF1565C0.toInt(), 0xFFAD1457.toInt(), 0xFF6A1B9A.toInt(),
        0xFF00695C.toInt(), 0xFFE65100.toInt(), 0xFF33691E.toInt(),
        0xFF880E4F.toInt(), 0xFF0D47A1.toInt(), 0xFF4A148C.toInt(),
        0xFF004D40.toInt(), 0xFFBF360C.toInt(), 0xFF1B5E20.toInt(),
        0xFF827717.toInt(), 0xFF37474F.toInt(), 0xFF4E342E.toInt()
    )

    fun drawBarChart(data: List<Pair<String, Double>>, width: Int, height: Int): Bitmap {
        val bmp = Bitmap.createBitmap(width.coerceAtLeast(10), height.coerceAtLeast(10), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.parseColor("#F5F5F5"))
        if (data.isEmpty()) return bmp

        val pL = 16f; val pR = 16f; val pT = 20f; val pB = 50f
        val cW = width - pL - pR; val cH = height - pT - pB
        val maxVal = data.maxOf { it.second }.takeIf { it > 0 } ?: 1.0

        val gridPaint = Paint().apply { color = Color.WHITE; strokeWidth = 2f }
        val textPaint = Paint().apply { color = Color.parseColor("#666666"); textSize = 22f; isAntiAlias = true }
        val axisPaint = Paint().apply { color = Color.parseColor("#BBBBBB"); strokeWidth = 2f }

        for (i in 0..4) {
            val y = pT + cH * (1f - i / 4f)
            canvas.drawLine(pL, y, pL + cW, y, gridPaint)
        }

        val barW = (cW / (data.size * 1.8f)).coerceAtLeast(10f)
        val gap = (cW - barW * data.size) / (data.size + 1)

        data.forEachIndexed { i, (label, value) ->
            val x = pL + gap + i * (barW + gap)
            val bH = (cH * value / maxVal).toFloat()
            val barPaint = Paint().apply { color = CHART_COLORS[i % CHART_COLORS.size]; isAntiAlias = true }
            if (bH > 0) canvas.drawRoundRect(x, pT + cH - bH, x + barW, pT + cH, 6f, 6f, barPaint)
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(label, x + barW / 2, pT + cH + 34f, textPaint)
        }

        canvas.drawLine(pL, pT, pL, pT + cH, axisPaint)
        canvas.drawLine(pL, pT + cH, pL + cW, pT + cH, axisPaint)
        return bmp
    }

    fun drawPieChart(data: List<Pair<String, Double>>, size: Int): Bitmap {
        val bmp = Bitmap.createBitmap(size.coerceAtLeast(10), size.coerceAtLeast(10), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.WHITE)
        val total = data.sumOf { it.second }.takeIf { it > 0 } ?: return bmp
        val paint = Paint().apply { isAntiAlias = true }
        val oval = RectF(20f, 20f, size - 20f, size - 20f)
        var startAngle = -90f
        data.forEachIndexed { i, (_, value) ->
            val sweep = (value / total * 360).toFloat()
            paint.color = CHART_COLORS[i % CHART_COLORS.size]
            canvas.drawArc(oval, startAngle, sweep, true, paint)
            startAngle += sweep
        }
        paint.color = Color.WHITE
        canvas.drawCircle(size / 2f, size / 2f, size * 0.28f, paint)
        return bmp
    }
}