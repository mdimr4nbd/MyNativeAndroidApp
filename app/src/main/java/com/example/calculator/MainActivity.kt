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
    private var justCalculated = false

    private lateinit var root: LinearLayout
    private lateinit var expressionView: TextView
    private lateinit var resultView: TextView

    private val history = mutableListOf<String>()

    private val red = Color.rgb(235, 45, 53)
    private val bg get() = if (dark) Color.BLACK else Color.rgb(247,247,249)
    private val panel get() = if (dark) Color.rgb(12,12,14) else Color.WHITE
    private val button get() = if (dark) Color.rgb(20,20,22) else Color.WHITE
    private val txt get() = if (dark) Color.WHITE else Color.rgb(25,25,28)
    private val muted get() = if (dark) Color.LTGRAY else Color.GRAY
    private val border get() = if (dark) Color.rgb(45,45,48) else Color.rgb(235,235,238)

    private fun dp(v: Int) =
        (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dark = getPreferences(MODE_PRIVATE)
            .getBoolean("dark", true)

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
            setPadding(dp(15), dp(5), dp(15), dp(8))
        }

        // HEADER

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val historyButton = iconButton("◷", 36f) {
            showHistory()
        }.apply {
            translationY = dp(5).toFloat()
            contentDescription = "History"
        }

        val title = TextView(this).apply {
            text = "Calculator"
            textSize = 21f
            gravity = Gravity.CENTER
            setTextColor(txt)
            typeface = Typeface.create(
                "sans-serif-medium", Typeface.NORMAL
            )
        }

        val themeButton = iconButton(
            if (dark) "☀" else "☾", 30f
        ) {
            dark = !dark
            getPreferences(MODE_PRIVATE).edit()
                .putBoolean("dark", dark).apply()
            createUI()
        }.apply {
            contentDescription = "Change theme"
        }

        header.addView(historyButton,
            LinearLayout.LayoutParams(dp(55), dp(58)))
        header.addView(title,
            LinearLayout.LayoutParams(0, dp(58), 1f))
        header.addView(themeButton,
            LinearLayout.LayoutParams(dp(55), dp(58)))

        root.addView(header,
            LinearLayout.LayoutParams(-1, dp(60)))

        // DISPLAY

        val display = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.BOTTOM or Gravity.END
            setPadding(dp(8), dp(10), dp(8), dp(28))
        }

        expressionView = TextView(this).apply {
            textSize = 23f
            gravity = Gravity.END
            setTextColor(muted)
            maxLines = 2
        }

        resultView = TextView(this).apply {
            text = "0"
            textSize = 54f
            gravity = Gravity.END
            setTextColor(txt)
            typeface = Typeface.create(
                "sans-serif-light", Typeface.NORMAL
            )
            maxLines = 1
        }

        display.addView(expressionView,
            LinearLayout.LayoutParams(-1, -2))
        display.addView(resultView,
            LinearLayout.LayoutParams(-1, -2))

        root.addView(display,
            LinearLayout.LayoutParams(-1, 0, 1.7f))

        // KEYPAD PANEL

        val keypad = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                setColor(panel)
                cornerRadii = floatArrayOf(
                    dp(38).toFloat(), dp(38).toFloat(),
                    dp(38).toFloat(), dp(38).toFloat(),
                    0f, 0f, 0f, 0f
                )
            }
            setPadding(dp(9), dp(12), dp(9), dp(8))
        }

        val handle = View(this).apply {
            background = GradientDrawable().apply {
                setColor(if (dark) Color.DKGRAY else
                    Color.rgb(215,215,220))
                cornerRadius = dp(20).toFloat()
            }
        }

        val handleBox = FrameLayout(this)
        handleBox.addView(handle,
            FrameLayout.LayoutParams(dp(76), dp(5), Gravity.CENTER))

        keypad.addView(handleBox,
            LinearLayout.LayoutParams(-1, dp(30)))

        val rows = listOf(
            listOf("AC", "C", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "−"),
            listOf("1", "2", "3", "+"),
            listOf("00", "0", ".", "=")
        )

        rows.forEach { rowKeys ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }

            rowKeys.forEach { key ->
                val cell = FrameLayout(this)
                val buttonView = makeButton(key)

                cell.addView(buttonView,
                    FrameLayout.LayoutParams(-1, -1).apply {
                        setMargins(dp(4), dp(5), dp(4), dp(5))
                    })

                row.addView(cell,
                    LinearLayout.LayoutParams(0, -1, 1f))
            }

            keypad.addView(row,
                LinearLayout.LayoutParams(-1, 0, 1f))
        }

        root.addView(keypad,
            LinearLayout.LayoutParams(-1, 0, 2.8f))

        setContentView(root)
        updateDisplay()
    }

    // HEADER ICON

    private fun iconButton(
        symbol: String,
        size: Float,
        action: () -> Unit
    ): TextView {
        return TextView(this).apply {
            text = symbol
            textSize = size
            gravity = Gravity.CENTER
            setTextColor(txt)
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }
    }

    // BUTTON DESIGN

    private fun makeButton(key: String): TextView {

        val isRed = key == "AC" || key == "="
        val isOperator = key in listOf("C", "%", "÷", "×", "−", "+")

        return TextView(this).apply {
            text = key
            gravity = Gravity.CENTER

            textSize = if (key == "AC") 23f else 28f

            setTextColor(
                when {
                    isRed -> Color.WHITE
                    isOperator -> red
                    else -> txt
                }
            )

            typeface = Typeface.create(
                "sans-serif-medium", Typeface.NORMAL
            )

            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(if (isRed) red else button)
                setStroke(dp(1), border)
            }

            isClickable = true
            isFocusable = true

            setOnClickListener { press(key) }
        }
    }

    // BUTTON ACTIONS

    private fun press(key: String) {
        when (key) {
            "AC" -> {
                expression = ""
                justCalculated = false
            }

            "C" -> clearEntry()

            "=" -> {
                calculate()
                return
            }

            "÷", "×", "−", "+" -> {
                val op = if (key == "−") "-" else key

                if (expression.isEmpty()) {
                    if (op == "-") expression = "-"
                } else {
                    if (expression.last() in "+-×÷") {
                        expression = expression.dropLast(1)
                    }
                    expression += op
                }
                justCalculated = false
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

                val current = expression.substringAfterLast(
                    Regex("[+\\-×÷]").toString()
                )

                if (!current.contains(".")) {
                    expression += if (
                        expression.isEmpty() ||
                        expression.last() in "+-×÷"
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

    private fun clearEntry() {
        if (expression.isEmpty()) return

        var i = expression.length - 1
        while (i >= 0 &&
            (expression[i].isDigit() || expression[i] == '.')) {
            i--
        }

        expression = expression.substring(0, i + 1)
        justCalculated = false
    }

    // CALCULATION

    private fun calculate() {
        if (expression.isEmpty()) return

        try {
            val answer = Parser(expression).parse()
            val formatted = format(answer)

            history.add("${expression.replace("-", "−")} = $formatted")

            expressionView.text = ""
            expression = formatted
            resultView.text = formatted
            justCalculated = true

        } catch (_: Exception) {
            resultView.text = "Error"
            justCalculated = true
        }
    }

    private fun format(value: Double): String {
        if (!value.isFinite()) return "Error"
        return BigDecimal.valueOf(value)
            .stripTrailingZeros().toPlainString()
    }

    private fun updateDisplay() {
        expressionView.text = expression.replace("-", "−")

        if (expression.isEmpty()) {
            resultView.text = "0"
            return
        }

        try {
            resultView.text = format(Parser(expression).parse())
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
            .setItems(history.reversed().toTypedArray()) { _, index ->
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

// EXPRESSION PARSER

private class Parser(private val input: String) {

    private var pos = 0

    fun parse(): Double {
        val result = expression()
        if (pos != input.length || !result.isFinite()) {
            throw IllegalArgumentException("Invalid")
        }
        return result
    }

    private fun expression(): Double {
        var value = term()

        while (pos < input.length) {
            when (input[pos]) {
                '+' -> {
                    pos++
                    value += term()
                }
                '-' -> {
                    pos++
                    value -= term()
                }
                else -> return value
            }
        }
        return value
    }

    private fun term(): Double {
        var value = factor()

        while (pos < input.length) {
            when (input[pos]) {
                '×' -> {
                    pos++
                    value *= factor()
                }
                '÷' -> {
                    pos++
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
        if (pos >= input.length) throw IllegalArgumentException()

        if (input[pos] == '-') {
            pos++
            return -factor()
        }

        if (input[pos] == '+') {
            pos++
            return factor()
        }

        val start = pos
        var decimal = false

        while (pos < input.length) {
            val c = input[pos]

            if (c.isDigit()) {
                pos++
            } else if (c == '.' && !decimal) {
                decimal = true
                pos++
            } else {
                break
            }
        }

        if (start == pos) throw IllegalArgumentException()

        var value = input.substring(start, pos).toDouble()

        while (pos < input.length && input[pos] == '%') {
            value /= 100.0
            pos++
        }

        return value
    }
}
