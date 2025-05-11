package com.example.codebricks.logicblocks

import com.example.codebricks.variable_declaration.VariableManager

object ConditionEvaluator {
    fun evaluate(
        input: String,
        errorNoOperator: String,
        errorInvalidFormat: String,
        errorVarNotFound: String,
        conditionTrue: String,
        conditionFalse: String
    ): String {
        val ops = listOf("==", "!=", ">=", "<=", ">", "<")
        val operator = ops.find { input.contains(it) }

        if (operator == null) return errorNoOperator

        val parts = input.split(operator).map { it.trim() }
        if (parts.size != 2) return errorInvalidFormat

        val left = resolveValue(parts[0])
        val right = resolveValue(parts[1])

        if (left == null || right == null) return errorVarNotFound

        val isTrue = when (operator) {
            ">" -> left > right
            "<" -> left < right
            "==" -> left == right
            "!=" -> left != right
            ">=" -> left >= right
            "<=" -> left <= right
            else -> false
        }

        return if (isTrue) conditionTrue else conditionFalse
    }

    private fun resolveValue(raw: String): Int? {
        return raw.toIntOrNull() ?: VariableManager.all()[raw]
    }
}