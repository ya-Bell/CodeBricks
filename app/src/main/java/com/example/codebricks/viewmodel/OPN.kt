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
                buildInfixExpression(a!!) + "+" + buildInfixExpression(b!!)
            }
            BlockType.MATH_SUBTRACT -> {
                val a = block.inputBlocks.getOrNull(0)
                val b = block.inputBlocks.getOrNull(1)
                buildInfixExpression(a!!) + "-" + buildInfixExpression(b!!)
            }
            BlockType.MATH_MULTIPLY -> {
                val a = block.inputBlocks.getOrNull(0)
                val b = block.inputBlocks.getOrNull(1)
                buildInfixExpression(a!!) + "*" + buildInfixExpression(b!!)
            }
            BlockType.MATH_DIVIDE -> {
                val a = block.inputBlocks.getOrNull(0)
                val b = block.inputBlocks.getOrNull(1)
                buildInfixExpression(a!!) + "/" + buildInfixExpression(b!!)
            }
            BlockType.VARIABLE_REFERENCE -> {
                val variable = block.value as? Variable
                listOf(variable?.value.toString())
            }
            else -> listOf("0")
        }
    }

    private fun toRPN(tokens: List<String>): List<String> {
        val precedence = mapOf("+" to 1, "-" to 1, "*" to 2, "/" to 2)
        val output = mutableListOf<String>()
        val stack = mutableListOf<String>()

        for (token in tokens) {
            when {
                token.toDoubleOrNull() != null -> output.add(token)
                token in precedence -> {
                    while (stack.isNotEmpty() &&
                        precedence.getOrDefault(stack.last(), 0) >= precedence[token]!!) {
                        output.add(stack.removeAt(stack.size - 1))
                    }
                    stack.add(token)
                }
            }
        }

        while (stack.isNotEmpty()) {
            output.add(stack.removeAt(stack.size - 1))
        }

        return output
    }

    private fun evaluateRPN(rpn: List<String>): Double {
        val stack = mutableListOf<Double>()
        for (token in rpn) {
            when {
                token.toDoubleOrNull() != null -> stack.add(token.toDouble())
                token in listOf("+", "-", "*", "/") -> {
                    val b = stack.removeLastOrNull() ?: 0.0
                    val a = stack.removeLastOrNull() ?: 0.0
                    val result = when (token) {
                        "+" -> a + b
                        "-" -> a - b
                        "*" -> a * b
                        "/" -> if (b != 0.0) a / b else 0.0
                        else -> 0.0
                    }
                    stack.add(result)
                }
            }
        }
        return stack.firstOrNull() ?: 0.0
    }
}
