package com.example.domain.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import java.security.MessageDigest
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

object NativeMakerCanvasEngine {

    fun renderBratCanvas(
        text: String,
        theme: String,
        blurMode: String
    ): Bitmap {
        val bgColor = when {
            theme.contains("Gold", ignoreCase = true) -> Color.parseColor("#E5B842")
            theme.contains("White", ignoreCase = true) -> Color.parseColor("#FFFFFF")
            theme.contains("Pink", ignoreCase = true) -> Color.parseColor("#FF2A85")
            else -> Color.parseColor("#8ACE00") // Iconic Brat Acid Lime
        }
        val textColor = Color.parseColor("#080808")

        // For authentic lo-fi Brat blur, render at a lower resolution and upscale with bilinear filtering
        val renderSize = when {
            blurMode.contains("Heavy", ignoreCase = true) -> 260
            blurMode.contains("Crisp", ignoreCase = true) -> 800
            else -> 380 // Authentic Lo-Fi
        }

        val lowRes = Bitmap.createBitmap(renderSize, renderSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(lowRes)
        canvas.drawColor(bgColor)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
            textScaleX = 0.92f
            textAlign = Paint.Align.CENTER
        }

        val cleanText = text.ifBlank { "julian omnicore v9" }.lowercase(Locale.ROOT)
        val words = cleanText.split(Regex("\\s+"))
        val lines = mutableListOf<String>()
        var currentLine = ""
        for (word in words) {
            val candidate = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (candidate.length > 14 && currentLine.isNotEmpty()) {
                lines.add(currentLine)
                currentLine = word
            } else {
                currentLine = candidate
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine)

        val fontSize = (renderSize / (lines.size.coerceAtLeast(2) + 2.2f)).coerceIn(22f, renderSize * 0.22f)
        paint.textSize = fontSize

        val lineHeight = fontSize * 1.18f
        val totalHeight = lines.size * lineHeight
        var startY = (renderSize - totalHeight) / 2f + fontSize * 0.82f

        for (line in lines) {
            canvas.drawText(line, renderSize / 2f, startY, paint)
            startY += lineHeight
        }

        return if (renderSize != 800) {
            Bitmap.createScaledBitmap(lowRes, 800, 800, true)
        } else {
            lowRes
        }
    }

    fun renderStrukCanvas(
        merchant: String,
        itemsRaw: String,
        cashier: String
    ): Bitmap {
        val width = 760
        val height = 1040
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Deep background around the receipt paper
        canvas.drawColor(Color.parseColor("#07090E"))

        val paperRect = RectF(50f, 40f, width - 50f, height - 40f)
        val paperPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FAF8F2")
        }
        canvas.drawRoundRect(paperRect, 12f, 12f, paperPaint)

        val monoBold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#111827")
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textSize = 28f
            textAlign = Paint.Align.CENTER
        }
        val monoReg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1F2937")
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            textSize = 21f
        }
        val dashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#6B7280")
            style = Paint.Style.STROKE
            strokeWidth = 2f
            pathEffect = DashPathEffect(floatArrayOf(10f, 8f), 0f)
        }

        var y = 105f
        canvas.drawText(merchant.ifBlank { "JULIAN CYBER-CAFE" }.uppercase(Locale.ROOT), width / 2f, y, monoBold)
        y += 34f
        monoReg.textAlign = Paint.Align.CENTER
        canvas.drawText("OMNI-CORE POS TERMINAL // VERIFIED", width / 2f, y, monoReg)
        y += 28f
        canvas.drawText("CASHIER: ${cashier.ifBlank { "OMNI-09" }}  |  2026-10-17 14:10", width / 2f, y, monoReg)
        y += 28f
        canvas.drawLine(85f, y, width - 85f, y, dashPaint)
        y += 42f

        val parsedItems = itemsRaw.split(",")
            .mapNotNull { entry ->
                val parts = entry.trim().split(":")
                if (parts.isNotEmpty() && parts[0].isNotBlank()) {
                    val name = parts[0].trim()
                    val price = parts.getOrNull(1)?.trim()?.filter { it.isDigit() }?.toLongOrNull() ?: 35000L
                    name to price
                } else null
            }
            .ifEmpty {
                listOf(
                    "Quantum Espresso" to 45000L,
                    "Croissant Gold" to 38000L,
                    "Omni API Pass" to 120000L
                )
            }

        val nf = NumberFormat.getNumberInstance(Locale.US)
        var subtotal = 0L

        for ((name, price) in parsedItems.take(8)) {
            subtotal += price
            monoReg.textAlign = Paint.Align.LEFT
            canvas.drawText(name.take(24), 90f, y, monoReg)
            monoReg.textAlign = Paint.Align.RIGHT
            canvas.drawText("IDR ${nf.format(price)}", width - 90f, y, monoReg)
            y += 38f
        }

        y += 10f
        canvas.drawLine(85f, y, width - 85f, y, dashPaint)
        y += 40f

        val tax = (subtotal * 0.11).toLong()
        val grandTotal = subtotal + tax

        monoReg.textAlign = Paint.Align.LEFT
        canvas.drawText("SUBTOTAL", 90f, y, monoReg)
        monoReg.textAlign = Paint.Align.RIGHT
        canvas.drawText("IDR ${nf.format(subtotal)}", width - 90f, y, monoReg)
        y += 34f

        monoReg.textAlign = Paint.Align.LEFT
        canvas.drawText("TAX (PPN 11%)", 90f, y, monoReg)
        monoReg.textAlign = Paint.Align.RIGHT
        canvas.drawText("IDR ${nf.format(tax)}", width - 90f, y, monoReg)
        y += 42f

        monoBold.textSize = 26f
        monoBold.textAlign = Paint.Align.LEFT
        canvas.drawText("GRAND TOTAL", 90f, y, monoBold)
        monoBold.textAlign = Paint.Align.RIGHT
        canvas.drawText("IDR ${nf.format(grandTotal)}", width - 90f, y, monoBold)
        y += 30f

        canvas.drawLine(85f, y, width - 85f, y, dashPaint)
        y += 50f

        // Draw deterministic 1D barcode
        val barPaint = Paint().apply {
            color = Color.parseColor("#111827")
            style = Paint.Style.FILL
        }
        var bx = 130f
        val seed = (merchant + itemsRaw).hashCode()
        var idx = 0
        while (bx < width - 130f) {
            val barW = if (((seed shr (idx % 16)) and 1) == 1) 6f else 3f
            canvas.drawRect(bx, y, bx + barW, y + 76f, barPaint)
            bx += barW + if (idx % 3 == 0) 6f else 4f
            idx++
        }
        y += 106f
        monoReg.textAlign = Paint.Align.CENTER
        monoReg.textSize = 18f
        canvas.drawText("AUTH HASH: JLN-${abs(seed).toString(16).uppercase(Locale.ROOT)}-OK", width / 2f, y, monoReg)

        return bitmap
    }

    fun renderKalenderCanvas(
        monthYear: String,
        highlightDayStr: String,
        eventTitle: String
    ): Bitmap {
        val width = 800
        val height = 860
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Obsidian Glass Background
        canvas.drawColor(Color.parseColor("#0B0E17"))

        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#141A29")
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E5B842")
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        val cardRect = RectF(40f, 40f, width - 40f, height - 40f)
        canvas.drawRoundRect(cardRect, 28f, 28f, cardPaint)
        canvas.drawRoundRect(cardRect, 28f, 28f, borderPaint)

        // Header Banner
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                40f, 40f, width - 40f, 170f,
                Color.parseColor("#362A0D"),
                Color.parseColor("#06343B"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(RectF(44f, 44f, width - 44f, 180f), 24f, 24f, headerPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E5B842")
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 36f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(monthYear.ifBlank { "OCTOBER 2026" }.uppercase(Locale.ROOT), width / 2f, 115f, titlePaint)

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#00F0FF")
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textSize = 18f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("JULIAN // QUANTUM EPHEMERIS GRID", width / 2f, 152f, subPaint)

        val daysOfWeek = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
        val colWidth = (width - 140f) / 7f
        val startX = 70f + colWidth / 2f

        val dowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textSize = 20f
            textAlign = Paint.Align.CENTER
        }
        daysOfWeek.forEachIndexed { i, day ->
            canvas.drawText(day, startX + i * colWidth, 240f, dowPaint)
        }

        val highlightDay = highlightDayStr.trim().toIntOrNull()?.coerceIn(1, 31) ?: 17
        val dayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F3F6FC")
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 26f
            textAlign = Paint.Align.CENTER
        }
        val highlightCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E5B842")
            style = Paint.Style.FILL
        }

        val offset = 4 // Starts on Thursday
        for (day in 1..31) {
            val cellIndex = (day - 1) + offset
            val col = cellIndex % 7
            val row = cellIndex / 7
            val cx = startX + col * colWidth
            val cy = 315f + row * 82f

            if (day == highlightDay) {
                canvas.drawCircle(cx, cy - 9f, 30f, highlightCirclePaint)
                dayPaint.color = Color.parseColor("#07090E")
                canvas.drawText(day.toString(), cx, cy, dayPaint)
                dayPaint.color = Color.parseColor("#F3F6FC")
            } else {
                canvas.drawText(day.toString(), cx, cy, dayPaint)
            }
        }

        // Milestone Footer Box
        val footerRect = RectF(70f, height - 145f, width - 70f, height - 70f)
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
        }
        canvas.drawRoundRect(footerRect, 16f, 16f, footerPaint)
        subPaint.color = Color.parseColor("#E5B842")
        canvas.drawText(
            "DAY $highlightDay // ${eventTitle.ifBlank { "MILESTONE EVENT" }.uppercase(Locale.ROOT)}",
            width / 2f,
            height - 98f,
            subPaint
        )

        return bitmap
    }

    fun renderQrCodeCanvas(
        payload: String,
        accent: String
    ): Bitmap {
        val size = 800
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val fgColor = when {
            accent.contains("Cyan", ignoreCase = true) -> Color.parseColor("#00F0FF")
            accent.contains("Mono", ignoreCase = true) -> Color.parseColor("#F8FAFC")
            accent.contains("Emerald", ignoreCase = true) -> Color.parseColor("#10B981")
            else -> Color.parseColor("#E5B842")
        }

        canvas.drawColor(Color.parseColor("#07090E"))

        val gridN = 25
        val margin = 90f
        val cell = (size - margin * 2f) / gridN

        val modulePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = fgColor
            style = Paint.Style.FILL
        }

        val digest = MessageDigest.getInstance("SHA-256").digest(payload.ifBlank { "julian" }.toByteArray())

        fun isFinderPattern(r: Int, c: Int): Boolean {
            val inTopLeft = r in 0..6 && c in 0..6
            val inTopRight = r in 0..6 && c in (gridN - 7) until gridN
            val inBottomLeft = r in (gridN - 7) until gridN && c in 0..6
            return inTopLeft || inTopRight || inBottomLeft
        }

        fun drawFinder(startR: Int, startC: Int) {
            for (dr in 0..6) {
                for (dc in 0..6) {
                    val isBorder = dr == 0 || dr == 6 || dc == 0 || dc == 6
                    val isInner = dr in 2..4 && dc in 2..4
                    if (isBorder || isInner) {
                        val left = margin + (startC + dc) * cell
                        val top = margin + (startR + dr) * cell
                        canvas.drawRoundRect(
                            RectF(left + 1.5f, top + 1.5f, left + cell - 1.5f, top + cell - 1.5f),
                            5f, 5f, modulePaint
                        )
                    }
                }
            }
        }

        drawFinder(0, 0)
        drawFinder(0, gridN - 7)
        drawFinder(gridN - 7, 0)

        for (r in 0 until gridN) {
            for (c in 0 until gridN) {
                if (isFinderPattern(r, c)) continue
                // Quiet separator around finders
                if ((r == 7 && (c <= 7 || c >= gridN - 8)) ||
                    (r == gridN - 8 && c <= 7) ||
                    (c == 7 && (r <= 7 || r >= gridN - 8)) ||
                    (c == gridN - 8 && r <= 7)
                ) continue

                // Timing pattern
                val active = if (r == 6 || c == 6) {
                    (r + c) % 2 == 0
                } else {
                    val bitIdx = (r * gridN + c) % (digest.size * 8)
                    val byteVal = digest[bitIdx / 8].toInt()
                    ((byteVal shr (bitIdx % 8)) and 1) == 1
                }

                if (active) {
                    val left = margin + c * cell
                    val top = margin + r * cell
                    canvas.drawRoundRect(
                        RectF(left + 1.5f, top + 1.5f, left + cell - 1.5f, top + cell - 1.5f),
                        5f, 5f, modulePaint
                    )
                }
            }
        }

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = fgColor
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textSize = 20f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("JULIAN // QR-MATRIX // ${payload.take(28)}", size / 2f, size - 32f, labelPaint)

        return bitmap
    }

    fun renderCarbonCanvas(
        title: String,
        code: String
    ): Bitmap {
        val width = 920
        val height = 620
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Outer gradient backdrop
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                Color.parseColor("#1E1B4B"),
                Color.parseColor("#0F172A"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Inner macOS Carbon Window
        val winRect = RectF(48f, 48f, width - 48f, height - 48f)
        val winPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#090D16")
        }
        val winStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E5B842")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(winRect, 22f, 22f, winPaint)
        canvas.drawRoundRect(winRect, 22f, 22f, winStroke)

        // Window controls
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        dotPaint.color = Color.parseColor("#FF5F56")
        canvas.drawCircle(90f, 90f, 10f, dotPaint)
        dotPaint.color = Color.parseColor("#FFBD2E")
        canvas.drawCircle(122f, 90f, 10f, dotPaint)
        dotPaint.color = Color.parseColor("#27C93F")
        canvas.drawCircle(154f, 90f, 10f, dotPaint)

        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textSize = 20f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(title.ifBlank { "JulianOmniCore.kt" }, width / 2f, 97f, headerPaint)

        val codePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            textSize = 22f
        }
        val lineNumPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#475569")
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            textSize = 20f
        }

        val lines = code.ifBlank { "val julian = OmniCore(version = 9.0)" }.lines().take(11)
        var y = 165f
        lines.forEachIndexed { idx, rawLine ->
            canvas.drawText((idx + 1).toString().padStart(2, '0'), 78f, y, lineNumPaint)
            codePaint.color = when {
                rawLine.trim().startsWith("//") -> Color.parseColor("#64748B")
                rawLine.contains("suspend") || rawLine.contains("val ") || rawLine.contains("fun ") -> Color.parseColor("#E5B842")
                rawLine.contains("\"") -> Color.parseColor("#00F0FF")
                else -> Color.parseColor("#F8FAFC")
            }
            canvas.drawText(rawLine.take(44), 130f, y, codePaint)
            y += 36f
        }

        return bitmap
    }

    fun renderBadgeCanvas(
        toolId: String,
        sender: String,
        message: String,
        accent: String
    ): Bitmap {
        val width = 880
        val height = 500
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val accentColor = when {
            accent.contains("Cyan", ignoreCase = true) -> Color.parseColor("#00F0FF")
            accent.contains("Emerald", ignoreCase = true) -> Color.parseColor("#10B981")
            else -> Color.parseColor("#E5B842")
        }

        canvas.drawColor(Color.parseColor("#07090E"))

        val cardRect = RectF(36f, 36f, width - 36f, height - 36f)
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#121826")
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawRoundRect(cardRect, 28f, 28f, cardPaint)
        canvas.drawRoundRect(cardRect, 28f, 28f, borderPaint)

        // Avatar / Crest Circle
        val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
        }
        canvas.drawCircle(115f, 125f, 40f, circlePaint)

        val monogramPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#07090E")
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 34f
            textAlign = Paint.Align.CENTER
        }
        val initial = sender.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "J"
        canvas.drawText(initial, 115f, 137f, monogramPaint)

        val senderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 28f
        }
        canvas.drawText(sender.ifBlank { "JULIAN OMNI-CORE" }.take(32), 180f, 118f, senderPaint)

        val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            textSize = 18f
        }
        canvas.drawText("MODULE // ${toolId.uppercase(Locale.ROOT)} // VERIFIED", 180f, 150f, metaPaint)

        val msgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F8FAFC")
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 28f
        }

        val words = message.ifBlank { "Julian v9.0 Studio active." }.split(" ")
        var line = ""
        var y = 240f
        for (word in words) {
            val candidate = if (line.isEmpty()) word else "$line $word"
            if (candidate.length > 36) {
                canvas.drawText(line, 80f, y, msgPaint)
                line = word
                y += 40f
            } else {
                line = candidate
            }
        }
        if (line.isNotEmpty()) {
            canvas.drawText(line, 80f, y, msgPaint)
        }

        canvas.drawText("TIMESTAMP: 2026-10-17T14:10:00Z  •  SHA256-SIGNED", 80f, height - 70f, metaPaint)
        return bitmap
    }
}
