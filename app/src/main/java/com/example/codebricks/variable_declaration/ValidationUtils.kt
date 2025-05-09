package com.example.codebricks.variable_declaration

fun validateVariableBlock(block: VariableDeclarationBlock): VariableDeclarationBlock {
    val names = block.variableNames.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    val errorCode = when {
        names.isEmpty() -> "min_one_var"
        names.any { !it.matches(Regex("^[\\p{L}_][\\p{L}0-9_]*$")) } -> "invalid_chars"
        names.groupBy { it }.any { it.value.size > 1 } -> "duplicates"
        else -> null
    }
    return block.copy(error = errorCode)
}
