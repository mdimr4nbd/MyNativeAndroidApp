
package com.example.calculator

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import java.math.BigDecimal

class MainActivity : Activity() {

    private lateinit var root: LinearLayout
    private lateinit var expressionView: TextView
    private lateinit var resultView: TextView

    private var dark = true
    private var expression = ""
    private var justCalculated = false

    private val history = mutableListOf<String>()

    private val keys = listOf(
        listOf("AC", "C", "%", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "−"),
        listOf("1", "2", "3", "+"),
        listOf("00", "0", ".", "=")
    )

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun color(hex: Long): Int =
        Color.rgb(
            ((hex shr 16) and 255).toInt(),
            ((hex shr 8) and 255).toInt(),
            (hex and 255).toInt()
        )

    // Theme colors

    private val backgroundColor: Int
        get() = if (dark) Color.BLACK else color(0xF7F7F7)

    private val panelColor: Int
        get() = if (dark) color(0x101010) else Color.WHITE

    private val numberColor: Int
        get() = if (dark) Color.BLACK else color(0xFFFFFF)

    private val functionColor: Int
        get() = if (dark) color(0x080808) else color(0xFFFFFF)

    private val textColor: Int
        get() = if (dark) Color.WHITE else color(0x222222)

    private val secondaryColor: Int
        get() = if (dark) color(0xAAAAAA) else color(0x777777)

    private val redColor: Int
        get() = color(0xE52D36)

    private val borderColor: Int
        get() = if (dark) color(0x202020) else color(0xEEEEEE)

    private val operatorTextColor: Int
        get() = if (dark) color(0xFF5058) else color(0xD72D35)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dark = getPreferences(MODE_PRIVATE)
            .getBoolean("dark_theme", true)

        createUI()
    }

    private fun createUI() {

        window.statusBarColor = backgroundColor
        window.navigationBarColor = backgroundColor

        if (dark) {
            window.decorView.systemUiVisibility = 0
        } else {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(backgroundColor)
            setPadding(dp(15), dp(5), dp(15), dp(8))
        }

        // HEADER

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val historyButton = headerIcon("◷") {
            showHistory()
        }

        val title = TextView(this).apply {
            text = "Calculator"
            textSize = 21f
            gravity = Gravity.CENTER
            setTextColor(textColor)
            typeface = Typeface.create(
                "sans-serif-medium",
                Typeface.NORMAL
            )
        }

        val settingsButton = headerIcon("☷") {
            showSettings()
        }

        header.addView(
            historyButton,
            LinearLayout.LayoutParams(dp(48), dp(48))
        )

        header.addView(
            title,
            LinearLayout.LayoutParams(0, dp(48), 1f)
        )

        header.addView(
            settingsButton,
            LinearLayout.LayoutParams(dp(48), dp(48))
        )

        root.addView(
            header,
            LinearLayout.LayoutParams(-1, dp(52))
        )

        // DISPLAY AREA

        val display = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.BOTTOM or Gravity.END
            setPadding(dp(8), dp(10), dp(8), dp(28))
        }

        expressionView = TextView(this).apply {
            textSize = 23f
            gravity = Gravity.END
            setTextColor(secondaryColor)
            maxLines = 2
        }

        resultView = TextView(this).apply {
            text = "0"
            textSize = 53f
            gravity = Gravity.END
            setTextColor(textColor)
            typeface = Typeface.create(
                "sans-serif-light",
                Typeface.NORMAL
            )
            maxLines = 1
        }

        display.addView(
            expressionView,
            LinearLayout.LayoutParams(-1, -2)
        )

        display.addView(
            resultView,
            LinearLayout.LayoutParams(-1, -2)
        )

        root.addView(
            display,
            LinearLayout.LayoutParams(-1, 0, 1.7f)
        )

        // CALCULATOR BOTTOM PANEL

        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL

            background = topRoundedBackground(panelColor)

            setPadding(
                dp(10),
                dp(12),
                dp(10),
                dp(8)
            )
        }

        // Small handle

        val handle = View(this).apply {
            background = GradientDrawable().apply {
                setColor(
                    if (dark) color(0x454545)
                    else color(0xDADADA)
                )
                cornerRadius = dp(20).toFloat()
            }
        }

        val handleContainer = FrameLayout(this)

        handleContainer.addView(
            handle,
            FrameLayout.LayoutParams(
                dp(76),
                dp(5),
                Gravity.CENTER
            )
        )

        panel.addView(
            handleContainer,
            LinearLayout.LayoutParams(-1, dp(30))
        )

        // BUTTON ROWS

        keys.forEach { rowKeys ->

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }

            rowKeys.forEach { key ->

                val cell = FrameLayout(this)

                val button = createButton(key)

                cell.addView(
                    button,
                    FrameLayout.LayoutParams(
                        -1,
                        -1
                    ).apply {
                        setMargins(dp(4), dp(4), dp(4), dp(4))
                    }
                )

                row.addView(
                    cell,
                    LinearLayout.LayoutParams(
                        0,
                        -1,
                        1f
                    )
                )
            }

            panel.addView(
                row,
                LinearLayout.LayoutParams(-1, 0, 1f)
            )
        }

        root.addView(
            panel,
            LinearLayout.LayoutParams(-1, 0, 2.8f)
        )

        setContentView(root)

        updateDisplay()
    }

    // HEADER ICONS

    private fun headerIcon(
        symbol: String,
        action: () -> Unit
    ): TextView {

        return TextView(this).apply {
            text = symbol
            textSize = 27f
            gravity = Gravity.CENTER
            setTextColor(secondaryColor)
            isClickable = true
            isFocusable = true

            setOnClickListener {
                action()
            }
        }
    }

    // ROUNDED TOP PANEL

    private fun topRoundedBackground(
        color: Int
    ): GradientDrawable {

        return GradientDrawable().apply {
            setColor(color)

            cornerRadii = floatArrayOf(
                dp(38).toFloat(), dp(38).toFloat(),
                dp(38).toFloat(), dp(38).toFloat(),
                0f, 0f,
                0f, 0f
            )
        }
    }

    // CIRCULAR BUTTON BACKGROUND

    private fun buttonBackground(
        key: String
    ): GradientDrawable {

        val fill = when (key) {
            "AC", "=" -> redColor

            "C", "%", "÷", "×", "−", "+" ->
                functionColor

            else -> numberColor
        }

        return GradientDrawable().apply {

            shape = GradientDrawable.OVAL
            setColor(fill)

            setStroke(
                dp(1),
                when (key) {
                    "AC", "=" -> color(0xF05A60)
                    else -> borderColor
                }
            )
        }
    }

    // CREATE BUTTON

    private fun createButton(key: String): TextView {

        val isRed = key == "AC" || key == "="

        val isOperator = key in listOf(
            "C", "%", "÷", "×", "−", "+"
        )

        return TextView(this).apply {

            text = key
            gravity = Gravity.CENTER

            textSize = when (key) {
                "AC" -> 23f
                "C" -> 24f
                "00" -> 25f
                else -> 29f
            }

            setTextColor(
                when {
                    isRed -> Color.WHITE
                    isOperator -> operatorTextColor
                    else -> textColor
                }
            )

            typeface = Typeface.create(
                "sans-serif-medium",
                Typeface.NORMAL
            )

            background = buttonBackground(key)

            isClickable = true
            isFocusable = true

            contentDescription = when (key) {
                "AC" -> "All Clear"
                "C" -> "Clear Entry"
                "=" -> "Calculate"
                "÷" -> "Divide"
                "×" -> "Multiply"
                else -> key
            }

            setOnClickListener {
                handleKey(key)
            }
        }
    }

    // BUTTON ACTIONS

    private fun handleKey(key: String) {

        when (key) {

            "AC" -> {
                expression = ""
                justCalculated = false
            }

            "C" -> {
                clearEntry()
            }

            "=" -> {
                calculate()
                return
            }

            "%" -> {
                if (expression.isNotEmpty() &&
                    (expression.last().isDigit() ||
                     expression.last() == ')')
                ) {
                    expression += "%"
                }

                justCalculated = false
            }

            "÷", "×", "−", "+" -> {

                val operator = when (key) {
                    "÷" -> "÷"
                    "×" -> "×"
                    "−" -> "-"
                    else -> "+"
                }

                if (expression.isEmpty()) {

                    if (operator == "-") {
                        expression = "-"
                    }

                } else {

                    if (expression.last() in "+-×÷") {
                        expression = expression.dropLast(1)
                    }

                    expression += operator
                }

                justCalculated = false
            }

            "." -> {

                if (justCalculated) {
                    expression = ""
                    justCalculated = false
                }

                val current = expression
                    .substringAfterLast("+")
                    .substringAfterLast("-")
                    .substringAfterLast("×")
                    .substringAfterLast("÷")

                if (!current.contains(".")) {

                    expression += if (
                        expression.isEmpty() ||
                        expression.last() in "+-×÷"
                    ) {
                        "0."
                    } else {
                        "."
                    }
                }
            }

            else -> {

                if (justCalculated) {
                    expression = ""
                    justCalculated = false
                }

                expression += key
            }
        }

        updateDisplay()
    }

    // CLEAR CURRENT NUMBER

    private fun clearEntry() {

        if (expression.isEmpty()) return

        var index = expression.length - 1

        while (index >= 0 &&
            (expression[index].isDigit() ||
             expression[index] == '.')
        ) {
            index--
        }

        expression = expression.substring(0, index + 1)

        justCalculated = false
    }

    // UPDATE DISPLAY

    private fun updateDisplay() {

        expressionView.text = expression.replace("-", "−")

        if (expression.isEmpty()) {
            resultView.text = "0"
            return
        }

        try {
            val value = ExpressionParser(expression).parse()
            resultView.text = formatNumber(value)
        } catch (_: Exception) {
            resultView.text = ""
        }
    }

    // CALCULATE RESULT

    private fun calculate() {

        if (expression.isEmpty()) return

        try {

            val original = expression.replace("-", "−")

            val value = ExpressionParser(expression).parse()

            val answer = formatNumber(value)

            history.add("$original = $answer")

            expression = answer

            expressionView.text = ""
            resultView.text = answer

            justCalculated = true

        } catch (_: Exception) {

            resultView.text = "Error"
            justCalculated = true
        }
    }

    // FORMAT NUMBER

    private fun formatNumber(value: Double): String {

        if (!value.isFinite()) return "Error"

        return BigDecimal.valueOf(value)
            .stripTrailingZeros()
            .toPlainString()
    }

    // HISTORY

    private fun showHistory() {

        if (history.isEmpty()) {

            AlertDialog.Builder(this)
                .setTitle("History")
                .setMessage("No calculations yet.")
                .setPositiveButton("OK", null)
                .show()

            return
        }

        AlertDialog.Builder(this)
            .setTitle("Calculation History")
            .setItems(
                history.reversed().toTypedArray()
            ) { _, index ->

                expression = history.reversed()[index]
                    .substringAfter(" = ")

                justCalculated = true

                updateDisplay()
            }
            .setNeutralButton("Clear History") { _, _ ->
                history.clear()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    // SETTINGS

    private fun showSettings() {

        val options = arrayOf(
            if (dark) "Switch to Light Theme"
            else "Switch to AMOLED Dark Theme",

            "Calculation History"
        )

        AlertDialog.Builder(this)
            .setTitle("Calculator Settings")
            .setItems(options) { _, index ->

                when (index) {

                    0 -> {
                        dark = !dark

                        getPreferences(MODE_PRIVATE)
                            .edit()
                            .putBoolean("dark_theme", dark)
                            .apply()

                        createUI()
                    }

                    1 -> showHistory()
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }
}

// MATHEMATICAL EXPRESSION PARSER

private class ExpressionParser(
    private val input: String
) {

    private var position = 0

    fun parse(): Double {

        val result = parseExpression()

        if (position != input.length || !result.isFinite()) {
            throw IllegalArgumentException("Invalid expression")
        }

        return result
    }

    private fun parseExpression(): Double {

        var value = parseTerm()

        while (position < input.length) {

            when (input[position]) {

                '+' -> {
                    position++
                    value += parseTerm()
                }

                '-' -> {
                    position++
                    value -= parseTerm()
                }

                else -> return value
            }
        }

        return value
    }

    private fun parseTerm(): Double {

        var value = parseFactor()

        while (position < input.length) {

            when (input[position]) {

                '×' -> {
                    position++
                    value *= parseFactor()
                }

                '÷' -> {

                    position++

                    val divisor = parseFactor()

                    if (divisor == 0.0) {
                        throw ArithmeticException("Division by zero")
                    }

                    value /= divisor
                }

                else -> return value
            }
        }

        return value
    }

    private fun parseFactor(): Double {

        if (position >= input.length) {
            throw IllegalArgumentException()
        }

        if (input[position] == '-') {
            position++
            return -parseFactor()
        }

        if (input[position] == '+') {
            position++
            return parseFactor()
        }

        val start = position
        var decimal = false

        while (position < input.length) {

            val character = input[position]

            if (character.isDigit()) {
                position++
            } else if (character == '.' && !decimal) {
                decimal = true
                position++
            } else {
                break
            }
        }

        if (start == position) {
            throw IllegalArgumentException()
        }

        var value = input.substring(start, position).toDouble()

        while (position < input.length &&
            input[position] == '%'
        ) {
            value /= 100.0
            position++
        }

        return value
    }
}
