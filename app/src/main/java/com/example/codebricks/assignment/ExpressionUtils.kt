package com.example.codebricks.assignment

object ExpressionEvaluator {
    private lateinit var expr: String
    private var pos = 0
    private lateinit var vars: Map<String, Int>

    fun evaluate(expression: String, variables: Map<String, Int>): Int {
        expr = expression.replace(" ", "")
        pos = 0
        vars = variables
        return parseExpr()
    }

    private fun parseExpr(): Int {
        var value = parseTerm()
        while (pos < expr.length) {
            when (expr[pos]) {
                '+' -> {
                    pos++
                    value += parseTerm()
                }
                '-' -> {
                    pos++
                    value -= parseTerm()
                }
                else -> return value
            }
        }
        return value
    }

    private fun parseTerm(): Int {
        var value = parseFactor()
        while (pos < expr.length) {
            when (expr[pos]) {
                '*' -> {
                    pos++
                    value *= parseFactor()
                }
                '/' -> {
                    pos++
                    val divisor = parseFactor()
                    value = if (divisor != 0) value / divisor else 0
                }
                '%' -> {
                    pos++
                    val divisor = parseFactor()
                    value = if (divisor != 0) value % divisor else 0
                }
                else -> return value
            }
        }
        return value
    }

    private fun parseFactor(): Int {
        if (pos < expr.length && expr[pos] == '(') {
            pos++
            val value = parseExpr()
            if (pos < expr.length && expr[pos] == ')') pos++
            return value
        }
        val start = pos
        while (pos < expr.length && (expr[pos].isLetterOrDigit() || expr[pos] == '_')) pos++
        val token = expr.substring(start, pos)
        return token.toIntOrNull() ?: vars[token] ?: 0
    }
}
