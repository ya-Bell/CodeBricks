package com.example.codebricks.viewmodel

import com.example.codebricks.blocks.common.Block
import com.example.codebricks.blocks.common.BlockType

object MathExpressionEvaluator {

    fun evaluate(block: Block?): Double {
        if (block == null) return 0.0
        val infix = buildInfixExpression(block)
        val rpn = toRPN(infix)
        return evaluateRPN(rpn)
    }

    private fun buildInfixExpression(block: Block): List<String> {
        return when (block.type) {
            BlockType.MATH_ADD -> {
                val a = block.inputBlocks.getOrNull(0)
                val b = block.inputBlocks.getOrNull(1)
                if (a == null || b == null) listOf("0")
                else buildInfixExpression(a) + "+" + buildInfixExpression(b)
            }
            BlockType.MATH_SUBTRACT -> {
                val a = block.inputBlocks.getOrNull(0)
                val b = block.inputBlocks.getOrNull(1)
                if (a == null || b == null) listOf("0")
                else buildInfixExpression(a) + "-" + buildInfixExpression(b)
            }
            BlockType.MATH_MULTIPLY -> {
                val a = block.inputBlocks.getOrNull(0)
                val b = block.inputBlocks.getOrNull(1)
                if (a == null || b == null) listOf("0")
                else buildInfixExpression(a) + "*" + buildInfixExpression(b)
            }
            BlockType.MATH_DIVIDE -> {
                val a = block.inputBlocks.getOrNull(0)
                val b = block.inputBlocks.getOrNull(1)
                if (a == null || b == null) listOf("0")
                else buildInfixExpression(a) + "/" + buildInfixExpression(b)
            }
            BlockType.MATH_MODULO -> {
                val a = block.inputBlocks.getOrNull(0)
                val b = block.inputBlocks.getOrNull(1)
                if (a == null || b == null) listOf("0")
                else buildInfixExpression(a) + "%" + buildInfixExpression(b)
            }
            BlockType.COMPARISON_EQUAL -> {
                val a = block.inputBlocks.getOrNull(0)
                val b = block.inputBlocks.getOrNull(1)
                if (a == null || b == null) listOf("0")
                else buildInfixExpression(a) + "==" + buildInfixExpression(b)
            }
            BlockType.COMPARISON_GREATER -> {
                val a = block.inputBlocks.getOrNull(0)
                val b = block.inputBlocks.getOrNull(1)
                if (a == null || b == null) listOf("0")
                else buildInfixExpression(a) + ">" + buildInfixExpression(b)
            }
            BlockType.COMPARISON_LESS -> {
                val a = block.inputBlocks.getOrNull(0)
                val b = block.inputBlocks.getOrNull(1)
                if (a == null || b == null) listOf("0")
                else buildInfixExpression(a) + "<" + buildInfixExpression(b)
            }
            BlockType.LOGIC_AND -> {
                val a = block.inputBlocks.getOrNull(0)
                val b = block.inputBlocks.getOrNull(1)
                if (a == null || b == null) listOf("0")
                else buildInfixExpression(a) + "&&" + buildInfixExpression(b)
            }
            BlockType.LOGIC_OR -> {
                val a = block.inputBlocks.getOrNull(0)
                val b = block.inputBlocks.getOrNull(1)
                if (a == null || b == null) listOf("0")
                else buildInfixExpression(a) + "||" + buildInfixExpression(b)
            }
            BlockType.LOGIC_NOT -> {
                val a = block.inputBlocks.getOrNull(0)
                if (a == null) listOf("0")
                else listOf("!") + buildInfixExpression(a)
            }
            BlockType.VARIABLE_REFERENCE -> {
                val variable = block.value as? Variable
                listOf(variable?.value?.toString() ?: "0")
            }
            else -> listOf("0")
        }
    }

    private fun toRPN(tokens: List<String>): List<String> {
        val precedence = mapOf(
            "||" to 1,
            "&&" to 2,
            "==" to 3, "!=" to 3,
            "<" to 4, ">" to 4, "<=" to 4, ">=" to 4,
            "+" to 5, "-" to 5,
            "*" to 6, "/" to 6,"%" to 6,
            "!" to 7
        )
        val output = mutableListOf<String>()
        val stack = mutableListOf<String>()

        for (token in tokens) {
            when {
                token.toDoubleOrNull() != null -> output.add(token)
                token in precedence -> {
                    while (stack.isNotEmpty() && stack.lastOrNull() in precedence &&
                        (precedence[stack.last()] ?: 0) >= (precedence[token] ?: 0)) {
                        stack.removeLastOrNull()?.let { output.add(it) }
                    }
                    stack.add(token)
                }
                token == "(" -> stack.add(token)
                token == ")" -> {
                    while (stack.isNotEmpty() && stack.lastOrNull() != "(") {
                        stack.removeLastOrNull()?.let { output.add(it) }
                    }
                    if (stack.isNotEmpty() && stack.lastOrNull() == "(") {
                        stack.removeLastOrNull()
                    }
                }
            }
        }
        stack.reversed().forEach { output.add(it) }
        return output
    }

    private fun evaluateRPN(tokens: List<String>): Double {
        val stack = mutableListOf<Double>()

        for (token in tokens) {
            try {
                when {
                    token.toDoubleOrNull() != null -> stack.add(token.toDouble())
                    token == "+" -> {
                        val b = stack.removeLastOrNull() ?: 0.0
                        val a = stack.removeLastOrNull() ?: 0.0
                        stack.add(a + b)
                    }
                    token == "-" -> {
                        val b = stack.removeLastOrNull() ?: 0.0
                        val a = stack.removeLastOrNull() ?: 0.0
                        stack.add(a - b)
                    }
                    token == "*" -> {
                        val b = stack.removeLastOrNull() ?: 0.0
                        val a = stack.removeLastOrNull() ?: 0.0
                        stack.add(a * b)
                    }
                    token == "/" -> {
                        val b = stack.removeLastOrNull() ?: 1.0
                        val a = stack.removeLastOrNull() ?: 0.0
                        stack.add(if (b != 0.0) a / b else 0.0)
                    }
                    token == "%" -> {
                        val b = stack.removeLastOrNull() ?: 1.0
                        val a = stack.removeLastOrNull() ?: 0.0
                        stack.add(if (b != 0.0) a % b else 0.0)
                    }
                    token == "==" -> {
                        val b = stack.removeLastOrNull() ?: 0.0
                        val a = stack.removeLastOrNull() ?: 0.0
                        stack.add(if (a == b) 1.0 else 0.0)
                    }
                    token == "!=" -> {
                        val b = stack.removeLastOrNull() ?: 0.0
                        val a = stack.removeLastOrNull() ?: 0.0
                        stack.add(if (a != b) 1.0 else 0.0)
                    }
                    token == ">" -> {
                        val b = stack.removeLastOrNull() ?: 0.0
                        val a = stack.removeLastOrNull() ?: 0.0
                        stack.add(if (a > b) 1.0 else 0.0)
                    }
                    token == "<" -> {
                        val b = stack.removeLastOrNull() ?: 0.0
                        val a = stack.removeLastOrNull() ?: 0.0
                        stack.add(if (a < b) 1.0 else 0.0)
                    }
                    token == ">=" -> {
                        val b = stack.removeLastOrNull() ?: 0.0
                        val a = stack.removeLastOrNull() ?: 0.0
                        stack.add(if (a >= b) 1.0 else 0.0)
                    }
                    token == "<=" -> {
                        val b = stack.removeLastOrNull() ?: 0.0
                        val a = stack.removeLastOrNull() ?: 0.0
                        stack.add(if (a <= b) 1.0 else 0.0)
                    }
                    token == "&&" -> {
                        val b = stack.removeLastOrNull() ?: 0.0
                        val a = stack.removeLastOrNull() ?: 0.0
                        stack.add(if (a != 0.0 && b != 0.0) 1.0 else 0.0)
                    }
                    token == "||" -> {
                        val b = stack.removeLastOrNull() ?: 0.0
                        val a = stack.removeLastOrNull() ?: 0.0
                        stack.add(if (a != 0.0 || b != 0.0) 1.0 else 0.0)
                    }
                    token == "!" -> {
                        val a = stack.removeLastOrNull() ?: 0.0
                        stack.add(if (a == 0.0) 1.0 else 0.0)
                    }
                }
            } catch (e: Exception) {
                // В случае ошибки вычисления, возвращаем 0.0
                stack.add(0.0)
            }
        }
        return stack.lastOrNull() ?: 0.0
    }
}
