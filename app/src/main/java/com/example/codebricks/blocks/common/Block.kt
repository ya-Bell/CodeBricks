package com.example.codebricks.blocks.common

data class Block(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: BlockType,
    val value: Any? = null, // значение, например, Variable, число, строка
    val children: MutableList<Block> = mutableListOf(), // вложенные блоки (например, в if или while)
    val inputBlocks: MutableList<Block> = mutableListOf(), // если блоки вставляются в параметры (print(x), set x to y)
    var nextBlockId: String? = null //  связь с другим блоком
)