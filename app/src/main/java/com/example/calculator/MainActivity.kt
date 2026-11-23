
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
import android.widget.Toast
import java.math.BigDecimal

class MainActivity : Activity() {

    private lateinit var expressionView: TextView
    private lateinit var resultView: TextView

    private var expression = ""
    private var justCalculated = false

    private val black = Color.BLACK
    private val numberColor = Color.rgb(43, 43, 43)
    private val lightGray = Color.rgb(95, 95, 95)
    private val orange = Color.rgb(255, 145, 20)
    private val white = Color.WHITE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = black
        window.navigationBarColor = black

        createCalculator()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun createCalculator() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundColor(black)
            setPadding(dp(15), dp(12), dp(15), dp(12))
        }

        // Top three icons

        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            background = rounded(
                Color.rgb(35, 35, 35),
                60
            )
            setPadding(dp(12), dp(5), dp(12), dp(5))
        }

        val icons = listOf("◷", "＄", "▱")

        icons.forEachIndexed { index, icon ->

            val item = TextView(this).apply {
                text = icon
                textSize = 27f
                gravity = Gravity.CENTER
                setTextColor(Color.LTGRAY)
                contentDescription = when (index) {
                    0 -> "History"
                    1 -> "Currency converter"
                    else -> "Unit converter"
                }

                setOnClickListener {
                    when (index) {
                        0 -> Toast.makeText(
                            this@MainActivity,
                            "History is empty",
                            Toast.LENGTH_SHORT
                        ).show()

                        1 -> Toast.makeText(
                            this@MainActivity,
                            "Currency converter coming soon",
                            Toast.LENGTH_SHORT
                        ).show()

                        2 -> Toast.makeText(
                            this@MainActivity,
                            "Unit converter coming soon",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            toolbar.addView(
                item,
                LinearLayout.LayoutParams(
                    dp(70),
                    dp(55)
                )
            )
        }

        val toolbarContainer = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            addView(
                toolbar,
                LinearLayout.LayoutParams(
                    dp(220),
                    dp(65)
                )
            )
        }

        root.addView(
            toolbarContainer,
            LinearLayout.LayoutParams(
                -1,
                dp(75)
            )
        )

        // Display area

        val display = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.BOTTOM or Gravity.END
            setPadding(dp(8), dp(10), dp(8), dp(25))
        }

        expressionView = TextView(this).apply {
            text = ""
            textSize = 29f
            gravity = Gravity.END
            setTextColor(Color.LTGRAY)
            maxLines = 2
        }

        resultView = TextView(this).apply {
            text = ""
            textSize = 52f
            gravity = Gravity.END
            setTextColor(white)
            typeface = Typeface.DEFAULT
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
            LinearLayout.LayoutParams(
                -1,
                0,
                1.8f
            )
        )

        // Calculator keypad

        val rows = listOf(
            listOf("AC", "%", "DEL", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("√π", "0", ".", "=")
        )

        val keypad = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        rows.forEach { keys ->

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            keys.forEach { key ->

                val button = createButton(key)

                row.addView(
                    button,
                    LinearLayout.LayoutParams(
                        0,
                        -1,
                        1f
                    ).apply {
                        setMargins(
                            dp(4),
                            dp(4),
                            dp(4),
                            dp(4)
                        )
                    }
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
                2.7f
            )
        )

        setContentView(root)
    }

    private fun rounded(
        color: Int,
        radius: Int
    ): GradientDrawable {

        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }
    }

    private fun createButton(key: String): TextView {

        val isOperator = key in listOf(
            "÷", "×", "-", "+", "="
        )

        val isSpecial = key in listOf(
            "AC", "%", "DEL", "√π"
        )

        val bg = when {
            key == "=" -> orange
            isOperator -> lightGray
            isSpecial -> lightGray
            else -> numberColor
        }

        val text = when (key) {
            "DEL" -> "⌫"
            "√π" -> "√π"
            else -> key
        }

        return TextView(this).apply {

            this.text = text

            textSize = when (key) {
                "AC", "DEL" -> 25f
                "√π" -> 27f
                else -> 31f
            }

            gravity = Gravity.CENTER

            setTextColor(white)

            background = rounded(bg, 100)

            typeface = Typeface.DEFAULT

            contentDescription = when (key) {
                "DEL" -> "Delete"
                "√π" -> "Square root and pi"
                else -> key
            }

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

            "√π" -> {
                try {
                    if (expression.isEmpty()) {
                        expression = "3.141592653589793"
                    } else {
                        val value =
                            ExpressionParser(expression).parse()

                        if (value < 0) {
                            throw ArithmeticException()
                        }

                        expression = formatNumber(kotlin.math.sqrt(value))
                    }

                    justCalculated = true
                    updateDisplay()

                } catch (_: Exception) {
                    resultView.text = "Error"
                }
            }

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

                if (expression.isEmpty()) {
                    if (key == "-") expression = "-"
                } else {
                    if (expression.last() in "+-×÷") {
                        expression = expression.dropLast(1)
                    }

                    expression += key
                }

                justCalculated = false
                updateDisplay()
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
            resultView.text = ""
            return
        }

        try {
            val result = ExpressionParser(expression).parse()
            resultView.text = formatNumber(result)

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
}

// Expression parser

private class ExpressionParser(
    private val input: String
) {

    private var position = 0

    fun parse(): Double {

        val result = parseExpression()

        if (position != input.length) {
            throw IllegalArgumentException()
        }

        if (!result.isFinite()) {
            throw ArithmeticException()
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
                        throw ArithmeticException()
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

        var value: Double

        if (input[position] == '(') {

            position++
            value = parseExpression()

            if (position >= input.length ||
                input[position] != ')'
            ) {
                throw IllegalArgumentException()
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
                throw IllegalArgumentException()
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
