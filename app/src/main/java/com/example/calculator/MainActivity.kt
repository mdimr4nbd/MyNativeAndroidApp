
package com.example.calculator

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import java.math.BigDecimal

class MainActivity : Activity() {

    private lateinit var root: LinearLayout
    private lateinit var expressionView: TextView
    private lateinit var resultView: TextView

    private var expression = ""
    private var dark = true
    private var justCalculated = false

    private val history = mutableListOf<String>()

    private val keys = listOf(
        listOf("AC", "C", "%", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "−"),
        listOf("1", "2", "3", "+"),
        listOf("00", "0", ".", "=")
    )

    private fun dp(v: Int) =
        (v * resources.displayMetrics.density).toInt()

    private fun color(hex: Long) = Color.rgb(
        ((hex shr 16) and 255).toInt(),
        ((hex shr 8) and 255).toInt(),
        (hex and 255).toInt()
    )

    private val bg: Int
        get() = if (dark) Color.BLACK else color(0xEAF6FF)

    private val buttonBg: Int
        get() = if (dark) color(0x101010) else color(0xD9EDF9)

    private val functionBg: Int
        get() = if (dark) color(0x080808) else color(0xF5FBFF)

    private val primary: Int
        get() = if (dark) Color.WHITE else color(0x253D50)

    private val secondary: Int
        get() = if (dark) color(0xB0B0B0) else color(0x52748A)

    private val blue: Int
        get() = color(0x168ACB)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK

        createUI()
    }

    private fun rounded(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(5).toFloat()
        }
    }

    private fun createUI() {

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(15), dp(10), dp(15), dp(12))
            setBackgroundColor(bg)
        }

        // Header

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
            setTextColor(primary)
            typeface = Typeface.DEFAULT_BOLD
        }

        val themeButton = headerIcon(
            if (dark) "☼" else "☾"
        ) {
            dark = !dark
            createUI()
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
            themeButton,
            LinearLayout.LayoutParams(dp(48), dp(48))
        )

        root.addView(
            header,
            LinearLayout.LayoutParams(-1, dp(52))
        )

        // Display

        val display = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.BOTTOM or Gravity.END
            setPadding(dp(8), dp(20), dp(8), dp(25))
        }

        expressionView = TextView(this).apply {
            textSize = 23f
            gravity = Gravity.END
            setTextColor(secondary)
            maxLines = 2
        }

        resultView = TextView(this).apply {
            textSize = 48f
            gravity = Gravity.END
            setTextColor(primary)
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
            LinearLayout.LayoutParams(-1, 0, 1.8f)
        )

        // Keypad

        val keypad = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        keys.forEach { rowKeys ->

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            rowKeys.forEach { key ->

                val button = makeButton(key)

                row.addView(
                    button,
                    LinearLayout.LayoutParams(
                        0, -1, 1f
                    ).apply {
                        setMargins(
                            dp(4), dp(5), dp(4), dp(5)
                        )
                    }
                )
            }

            keypad.addView(
                row,
                LinearLayout.LayoutParams(-1, 0, 1f)
            )
        }

        root.addView(
            keypad,
            LinearLayout.LayoutParams(-1, 0, 2.7f)
        )

        setContentView(root)

        updateDisplay()
    }

    private fun headerIcon(
        symbol: String,
        action: () -> Unit
    ): TextView {

        return TextView(this).apply {
            text = symbol
            textSize = 27f
            gravity = Gravity.CENTER
            setTextColor(secondary)
            setOnClickListener { action() }
        }
    }

    private fun makeButton(key: String): TextView {

        val isOperator = key in listOf(
            "÷", "×", "−", "+", "%"
        )

        val isFunction = key in listOf("AC", "C")

        val buttonColor = when {
            isOperator -> functionBg
            isFunction -> functionBg
            else -> buttonBg
        }

        val textColor = when {
            isOperator -> blue
            isFunction -> blue
            else -> primary
        }

        return TextView(this).apply {

            text = key
            gravity = Gravity.CENTER

            textSize = when (key) {
                "AC", "C" -> 23f
                else -> 29f
            }

            setTextColor(textColor)
            typeface = Typeface.DEFAULT_BOLD

            background = rounded(buttonColor)

            isClickable = true
            isFocusable = true

            setOnClickListener {
                handleKey(key)
            }
        }
    }

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

                val op = when (key) {
                    "÷" -> "÷"
                    "×" -> "×"
                    "−" -> "-"
                    else -> "+"
                }

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
            (expression[i].isDigit() ||
             expression[i] == '.')
        ) {
            i--
        }

        expression = expression.substring(0, i + 1)
        justCalculated = false
    }

    private fun updateDisplay() {

        expressionView.text = expression.replace("-", "−")

        if (expression.isEmpty()) {
            resultView.text = ""
            return
        }

        try {
            val value = ExpressionParser(expression).parse()
            resultView.text = formatNumber(value)
        } catch (_: Exception) {
            resultView.text = ""
        }
    }

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

    private fun formatNumber(value: Double): String {
        if (!value.isFinite()) return "Error"

        return BigDecimal.valueOf(value)
            .stripTrailingZeros()
            .toPlainString()
    }
}

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
            val c = input[position]

            if (c.isDigit()) {
                position++
            } else if (c == '.' && !decimal) {
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
