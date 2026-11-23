package com.example.calculator

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*
import java.math.BigDecimal

class MainActivity : Activity() {

    private var dark = true
    private var expression = ""
    private var result = "0"
    private var justCalculated = false

    private val history = mutableListOf<String>()

    private lateinit var root: LinearLayout
    private lateinit var resultView: TextView
    private lateinit var expressionView: TextView

    private val red = Color.rgb(239, 38, 53)

    private val bg: Int
        get() = if (dark) Color.BLACK else Color.rgb(250, 250, 250)

    private val panel: Int
        get() = if (dark) Color.rgb(12, 12, 14) else Color.WHITE

    private val buttonBg: Int
        get() = if (dark) Color.rgb(22, 22, 25) else Color.rgb(248, 248, 250)

    private val textColor: Int
        get() = if (dark) Color.WHITE else Color.rgb(25, 25, 28)

    private val borderColor: Int
        get() = if (dark) Color.rgb(48, 48, 52) else Color.rgb(228, 228, 232)

    private val secondaryText: Int
        get() = if (dark) Color.rgb(190, 190, 195) else Color.GRAY

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dark = getPreferences(MODE_PRIVATE)
            .getBoolean("dark_theme", true)

        createUI()
    }

    private fun createUI() {

        window.statusBarColor = bg
        window.navigationBarColor = bg

        window.decorView.systemUiVisibility =
            if (dark) 0 else
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            setPadding(dp(14), dp(3), dp(14), 0)
        }

        // HEADER

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val historyButton = headerIcon("◷", 27f) {
            showHistory()
        }

        historyButton.translationY = dp(4).toFloat()

        val title = TextView(this).apply {
            text = "Calculator"
            textSize = 23f
            gravity = Gravity.CENTER
            setTextColor(textColor)
            typeface = Typeface.create(
                "sans-serif-medium",
                Typeface.NORMAL
            )
        }

        val themeButton = headerIcon(
            if (dark) "☀" else "☾",
            28f
        ) {
            dark = !dark

            getPreferences(MODE_PRIVATE)
                .edit()
                .putBoolean("dark_theme", dark)
                .apply()

            createUI()
        }

        header.addView(
            historyButton,
            LinearLayout.LayoutParams(dp(55), dp(58))
        )

        header.addView(
            title,
            LinearLayout.LayoutParams(0, dp(58), 1f)
        )

        header.addView(
            themeButton,
            LinearLayout.LayoutParams(dp(55), dp(58))
        )

        root.addView(
            header,
            LinearLayout.LayoutParams(-1, dp(60))
        )

        // DISPLAY AREA

        val display = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.BOTTOM or Gravity.END
            setPadding(dp(8), dp(10), dp(8), dp(32))
        }

        expressionView = TextView(this).apply {
            textSize = 25f
            gravity = Gravity.END
            setTextColor(secondaryText)
            maxLines = 2
            typeface = Typeface.create(
                "sans-serif-light",
                Typeface.NORMAL
            )
        }

        resultView = TextView(this).apply {
            text = "0"
            textSize = 58f
            gravity = Gravity.END
            setTextColor(textColor)
            maxLines = 1
            typeface = Typeface.create(
                "sans-serif-light",
                Typeface.NORMAL
            )
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
            LinearLayout.LayoutParams(-1, 0, 1.65f)
        )

        // KEYPAD PANEL

        val keypad = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = roundedPanel()
            setPadding(dp(9), dp(12), dp(9), dp(8))
        }

        // HANDLE

        val handleContainer = FrameLayout(this)

        val handle = View(this).apply {
            background = GradientDrawable().apply {
                setColor(
                    if (dark) Color.rgb(75, 75, 78)
                    else Color.rgb(205, 205, 210)
                )
                cornerRadius = dp(20).toFloat()
            }
        }

        handleContainer.addView(
            handle,
            FrameLayout.LayoutParams(
                dp(76),
                dp(5),
                Gravity.CENTER
            )
        )

        keypad.addView(
            handleContainer,
            LinearLayout.LayoutParams(-1, dp(30))
        )

        // BUTTON ROWS

        val rows = listOf(
            listOf("AC", "C", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "−"),
            listOf("1", "2", "3", "+"),
            listOf("00", "0", ".", "=")
        )

        rows.forEach { keys ->

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }

            keys.forEach { key ->

                val cell = FrameLayout(this)

                val button = makeButton(key)

                cell.addView(
                    button,
                    FrameLayout.LayoutParams(
                        -1,
                        -1
                    ).apply {
                        setMargins(
                            dp(5),
                            dp(5),
                            dp(5),
                            dp(5)
                        )
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

            keypad.addView(
                row,
                LinearLayout.LayoutParams(
                    -1,
                    0,
                    1f
                )
            )
        }

        root.addView(
            keypad,
            LinearLayout.LayoutParams(
                -1,
                0,
                2.8f
            )
        )

        setContentView(root)

        updateDisplay()
    }

    // HEADER ICON

    private fun headerIcon(
        symbol: String,
        size: Float,
        action: () -> Unit
    ): TextView {

        return TextView(this).apply {
            text = symbol
            textSize = size
            gravity = Gravity.CENTER
            setTextColor(textColor)
            isClickable = true
            isFocusable = true
            setOnClickListener {
                action()
            }
        }
    }

    // ROUNDED KEYPAD PANEL

    private fun roundedPanel(): GradientDrawable {

        return GradientDrawable().apply {
            setColor(panel)

            cornerRadii = floatArrayOf(
                dp(42).toFloat(), dp(42).toFloat(),
                dp(42).toFloat(), dp(42).toFloat(),
                dp(0).toFloat(), dp(0).toFloat(),
                dp(0).toFloat(), dp(0).toFloat()
            )
        }
    }

    // PROFESSIONAL BUTTONS

    private fun makeButton(key: String): TextView {

        val redButton = key == "AC" || key == "="

        val operator = key in listOf(
            "C", "%", "÷", "×", "−", "+"
        )

        return TextView(this).apply {

            text = key
            gravity = Gravity.CENTER

            textSize = if (key == "AC") 23f else 29f

            typeface = Typeface.create(
                "sans-serif-medium",
                Typeface.NORMAL
            )

            setTextColor(
                when {
                    redButton -> Color.WHITE
                    operator -> red
                    else -> textColor
                }
            )

            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL

                setColor(
                    if (redButton) red else buttonBg
                )

                setStroke(
                    dp(1),
                    if (redButton) red else borderColor
                )
            }

            isClickable = true
            isFocusable = true

            setOnClickListener {
                press(key)
            }
        }
    }

    // BUTTON ACTIONS

    private fun press(key: String) {

        when (key) {

            "AC" -> {
                expression = ""
                result = "0"
                justCalculated = false
            }

            "C" -> {
                if (expression.isNotEmpty()) {
                    expression = expression.dropLast(1)
                }
                justCalculated = false
            }

            "=" -> {
                calculate()
                return
            }

            "+", "−", "×", "÷" -> {

                val op = key

                if (justCalculated) {
                    justCalculated = false
                }

                if (expression.isEmpty()) {
                    if (op == "−") expression = "−"
                } else if (expression.last() in listOf('+', '−', '×', '÷')) {
                    expression = expression.dropLast(1) + op
                } else {
                    expression += op
                }
            }

            "%" -> {
                if (expression.isNotEmpty() &&
                    expression.last().isDigit()) {
                    expression += "%"
                }
                justCalculated = false
            }

            "." -> {

                if (justCalculated) {
                    expression = ""
                    justCalculated = false
                }

                val currentNumber = expression
                    .substringAfterLast('+')
                    .substringAfterLast('−')
                    .substringAfterLast('×')
                    .substringAfterLast('÷')

                if (!currentNumber.contains(".")) {
                    expression += if (
                        currentNumber.isEmpty()
                    ) "0." else "."
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

    // CALCULATOR ENGINE

    private fun calculate() {

        if (expression.isEmpty()) return

        try {

            val answer = CalculatorParser(expression).parse()

            result = formatNumber(answer)

            history.add(
                "$expression = $result"
            )

            expressionView.text = expression
            resultView.text = result

            expression = result
            justCalculated = true

        } catch (_: Exception) {
            resultView.text = "Error"
            justCalculated = true
        }
    }

    private fun formatNumber(value: Double): String {

        if (!value.isFinite()) return "Error"

        return BigDecimal.valueOf(value)
            .stripTrailingZeros()
            .toPlainString()
    }

    // UPDATE DISPLAY

    private fun updateDisplay() {

        expressionView.text = expression

        if (expression.isEmpty()) {
            resultView.text = "0"
            return
        }

        try {
            val answer = CalculatorParser(expression).parse()
            resultView.text = formatNumber(answer)
        } catch (_: Exception) {
            resultView.text = ""
        }
    }

    // HISTORY

    private fun showHistory() {

        if (history.isEmpty()) {

            AlertDialog.Builder(this)
                .setTitle("History")
                .setMessage("No calculations yet")
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
}

// MATHEMATICAL EXPRESSION PARSER

private class CalculatorParser(
    private val input: String
) {

    private var position = 0

    fun parse(): Double {

        val value = expression()

        if (position != input.length || !value.isFinite()) {
            throw IllegalArgumentException("Invalid expression")
        }

        return value
    }

    private fun expression(): Double {

        var value = term()

        while (position < input.length) {

            when (input[position]) {

                '+' -> {
                    position++
                    value += term()
                }

                '−' -> {
                    position++
                    value -= term()
                }

                else -> return value
            }
        }

        return value
    }

    private fun term(): Double {

        var value = factor()

        while (position < input.length) {

            when (input[position]) {

                '×' -> {
                    position++
                    value *= factor()
                }

                '÷' -> {
                    position++

                    val divisor = factor()

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

    private fun factor(): Double {

        if (position >= input.length) {
            throw IllegalArgumentException("Invalid number")
        }

        if (input[position] == '−') {
            position++
            return -factor()
        }

        if (input[position] == '+') {
            position++
            return factor()
        }

        val start = position
        var decimalFound = false

        while (position < input.length) {

            val character = input[position]

            when {
                character.isDigit() -> position++

                character == '.' && !decimalFound -> {
                    decimalFound = true
                    position++
                }

                else -> break
            }
        }

        if (start == position) {
            throw IllegalArgumentException("Invalid number")
        }

        var value = input.substring(start, position).toDouble()

        while (position < input.length && input[position] == '%') {
            value /= 100.0
            position++
        }

        return value
    }
}
