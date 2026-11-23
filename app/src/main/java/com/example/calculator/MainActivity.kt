package com.example.calculator

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import java.math.BigDecimal
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var expressionView: TextView
    private lateinit var resultView: TextView

    private var expression = ""
    private var justCalculated = false

    private val bgColor = Color.rgb(10, 12, 12)
    private val buttonColor = Color.rgb(29, 34, 33)
    private val green = Color.rgb(0, 220, 140)
    private val textColor = Color.WHITE
    private val mutedColor = Color.rgb(145, 155, 150)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = bgColor
        window.navigationBarColor = bgColor
        window.decorView.systemUiVisibility = 0

        createCalculator()
    }

    private fun createCalculator() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bgColor)
            setPadding(dp(18), dp(20), dp(18), dp(18))
        }

        val heading = TextView(this).apply {
            text = "CALCULATOR"
            textSize = 15f
            letterSpacing = 0.16f
            setTextColor(mutedColor)
            typeface = Typeface.DEFAULT_BOLD
        }

        root.addView(
            heading,
            LinearLayout.LayoutParams(
                -1,
                dp(45)
            )
        )

        val display = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.BOTTOM or Gravity.END
            setPadding(dp(5), dp(20), dp(5), dp(25))
        }

        expressionView = TextView(this).apply {
            text = ""
            textSize = 30f
            setTextColor(mutedColor)
            gravity = Gravity.END
            maxLines = 2
        }

        resultView = TextView(this).apply {
            text = "0"
            textSize = 48f
            setTextColor(textColor)
            gravity = Gravity.END
            typeface = Typeface.DEFAULT_BOLD
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
            LinearLayout.LayoutParams( -1, 0, 1f)
        )

        val keys = listOf(
            listOf("AC", "DEL", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "=", "")
        )

        val keypad = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        keys.forEach { rowKeys ->

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            rowKeys.forEach { key ->

                if (key.isEmpty()) {
                    row.addView(
                        View(this),
                        LinearLayout.LayoutParams(
                            0,
                            dp(76),
                            1f
                        )
                    )
                } else {
                    val button = createButton(key)

                    row.addView(
                        button,
                        LinearLayout.LayoutParams(
                            0,
                            dp(76),
                            1f
                        ).apply {
                            setMargins(
                                dp(5),
                                dp(5),
                                dp(5),
                                dp(5)
                            )
                        }
                    )
                }
            }

            keypad.addView(
                row,
                LinearLayout.LayoutParams(-1, 0, 1f)
            )
        }

        root.addView(
            keypad,
            LinearLayout.LayoutParams(-1, 0, 2.5f)
        )

        setContentView(root)
    }

    private fun createButton(key: String): TextView {

        val isOperator = key in listOf(
            "÷", "×", "-", "+", "%", "="
        )

        val isAction = key in listOf("AC", "DEL")

        val button = TextView(this).apply {
            text = key
            textSize = if (key.length > 2) 18f else 25f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD

            setTextColor(
                when {
                    key == "=" -> Color.BLACK
                    isOperator -> green
                    isAction -> green
                    else -> textColor
                }
            )

            background = roundedBackground(
                when {
                    key == "=" -> green
                    isOperator -> Color.rgb(25, 48, 40)
                    isAction -> Color.rgb(35, 43, 40)
                    else -> buttonColor
                }
            )

            isClickable = true
            isFocusable = true

            setOnClickListener {
                handleKey(key)
            }
        }

        return button
    }

    private fun roundedBackground(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(24).toFloat()
        }
    }

    private fun handleKey(key: String) {

        when (key) {

            "AC" -> {
                expression = ""
                justCalculated = false
                updateDisplay()
            }

            "DEL" -> {
                if (expression.isNotEmpty()) {
                    expression = expression.dropLast(1)
                }
                justCalculated = false
                updateDisplay()
            }

            "=" -> calculateResult()

            "%" -> {
                if (expression.isNotEmpty() &&
                    (expression.last().isDigit() ||
                     expression.last() == ')')
                ) {
                    expression += "%"
                }
                justCalculated = false
                updateDisplay()
            }

            "+", "-", "×", "÷" -> {
                val operator = key

                if (expression.isEmpty()) {
                    if (operator == "-") expression = "-"
                } else {
                    if (expression.last() in "+-×÷") {
                        expression = expression.dropLast(1)
                    }

                    expression += operator
                }

                justCalculated = false
                updateDisplay()
            }

            "." -> {
                if (justCalculated) {
                    expression = ""
                    justCalculated = false
                }

                val currentNumber = expression
                    .substringAfterLast("+")
                    .substringAfterLast("-")
                    .substringAfterLast("×")
                    .substringAfterLast("÷")

                if (!currentNumber.contains(".")) {
                    expression += if (
                        expression.isEmpty() ||
                        expression.last() in "+-×÷"
                    ) "0." else "."
                }

                updateDisplay()
            }

            else -> {
                if (justCalculated) {
                    expression = ""
                    justCalculated = false
                }

                expression += key
                updateDisplay()
            }
        }
    }

    private fun updateDisplay() {
        expressionView.text = expression

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

    private fun calculateResult() {
        if (expression.isEmpty()) return

        try {
            val value = ExpressionParser(expression).parse()
            val answer = formatNumber(value)

            expression = answer
            expressionView.text = ""
            resultView.text = answer
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

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}

/* Mathematical expression parser */

private class ExpressionParser(
    private val input: String
) {

    private var position = 0

    fun parse(): Double {
        val result = parseExpression()

        if (position != input.length) {
            throw IllegalArgumentException("Invalid expression")
        }

        if (!result.isFinite()) {
            throw ArithmeticException("Invalid result")
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
            throw IllegalArgumentException("Missing number")
        }

        if (input[position] == '-') {
            position++
            return -parseFactor()
        }

        if (input[position] == '+') {
            position++
            return parseFactor()
        }

        var value: Double

        if (input[position] == '(') {
            position++
            value = parseExpression()

            if (position >= input.length ||
                input[position] != ')'
            ) {
                throw IllegalArgumentException("Missing bracket")
            }

            position++

        } else {
            val start = position
            var hasDecimal = false

            while (position < input.length) {
                val char = input[position]

                if (char.isDigit()) {
                    position++
                } else if (char == '.' && !hasDecimal) {
                    hasDecimal = true
                    position++
                } else {
                    break
                }
            }

            if (start == position) {
                throw IllegalArgumentException("Invalid number")
            }

            value = input.substring(start, position).toDouble()
        }

        while (position < input.length &&
            input[position] == '%'
        ) {
            value /= 100.0
            position++
        }

        return value
    }
}
