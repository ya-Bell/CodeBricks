package com.example.codebricks.logicblocks

import androidx.compose.ui.graphics.Color
import com.example.codebricks.variable_declaration.VariableManager

object ConditionEvaluator {
    data class Result(val text: String, val color: Color)

    fun evaluate(
        input: String,
        errorNoOperator: String,
        errorInvalidFormat: String,
        errorVarNotFound: String,
        conditionTrue: String,
        conditionFalse: String
    ): Result {
        val ops = listOf("==", "!=", ">=", "<=", ">", "<")
        val operator = ops.find { input.contains(it) }

        if (operator == null) return Result(errorNoOperator, Color.Red)

        val parts = input.split(operator).map { it.trim() }
        if (parts.size != 2) return Result (errorInvalidFormat, Color.Red)

        val left = resolveValue(parts[0])
        val right = resolveValue(parts[1])

        if (left == null || right == null) return Result (errorVarNotFound, Color.Red)

        val comparison = "$left $operator $right"

        val isTrue = when (operator) {
            ">" -> left > right
            "<" -> left < right
            "==" -> left == right
            "!=" -> left != right
            ">=" -> left >= right
            "<=" -> left <= right
            else -> false
        }

        return if (isTrue) {
            Result("$conditionTrue ($comparison)", Color.Green)
        } else {
            Result("$conditionFalse ($comparison)", Color.Red)
        }
    }

    private fun resolveValue(raw: String): Int? {
        return raw.toIntOrNull() ?: VariableManager.all()[raw]
    }
}