package com.example.codebricks.viewmodel.conversion

import com.example.codebricks.blocks.common.BlockType
import com.example.codebricks.screens.workscreen.tracker.BlockPositionTracker.redrawTrigger
import com.example.codebricks.viewmodel.Variable
import com.example.codebricks.viewmodel.VariableViewModel

fun VariableViewModel.convertVariableType(variableName: String, newType: String) {
    _variables.value = _variables.value.map { variable ->
        if (variable.name == variableName) {
            val newValue = when {
                variable.type == newType -> variable.value

                newType == "double" -> when {
                    variable.value is Int -> (variable.value as Int).toDouble()
                    variable.value is Double -> variable.value
                    //variable.value is Boolean -> if (variable.value) 1.0 else 0.0
                    variable.value is String -> (variable.value as String).toDoubleOrNull()
                        ?: 0.0

                    else -> (variable.value as? Number)?.toDouble() ?: 0.0
                }

                newType == "int" -> when {
                    variable.value is Double -> (variable.value as Double).toInt()
                    variable.value is Int -> variable.value
                    //variable.value is Boolean -> if (variable.value) 1 else 0
                    variable.value is String -> (variable.value as String).toIntOrNull() ?: 0
                    else -> (variable.value as? Number)?.toInt() ?: 0
                }

                newType == "bool" -> when {
                    variable.value is Number -> (variable.value as Number).toInt() != 0
                    variable.value is Boolean -> variable.value
                    variable.value is String -> (variable.value as String).toBooleanStrictOrNull()
                        ?: false

                    else -> false
                }

                newType == "string" -> variable.value.toString()

                else -> variable.value
            }

            variable.copy(type = newType, value = newValue)
        } else {
            variable
        }
    }

    _programBlocks.value = _programBlocks.value.map { block ->
        when (block.type) {
            BlockType.VARIABLE_DECLARE -> {
                val varValue = block.value as? Variable
                if (varValue?.name == variableName) {
                    _variables.value.find { it.name == variableName }?.let { newVar ->
                        block.copy(value = newVar)
                    } ?: block
                } else {
                    block
                }
            }

            else -> block
        }
    }

    redrawTrigger.intValue++
}

fun formatValueForOutput(value: Any, type: String): String {
    return when (type) {
        "double" -> {
            val doubleValue = when (value) {
                is Number -> value.toDouble()
                else -> value.toString().toDoubleOrNull() ?: 0.0
            }
            if (doubleValue % 1 == 0.0) "%.1f".format(doubleValue).replace(',', '.')
            else doubleValue.toString().replace(',', '.')
        }

        "string" -> "\"$value\""
        else -> value.toString()
    }
}