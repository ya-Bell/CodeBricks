package com.example.codebricks.viewmodel.tree

import com.example.codebricks.blocks.common.Block
import com.example.codebricks.viewmodel.VariableViewModel

// Поиск блока по его ID в дереве программы
fun VariableViewModel.findBlockById(id: String): Block? {
    fun search(block: Block): Block? {
        if (block.id == id) return block
        block.inputBlocks.filterNotNull().forEach {
            val result = search(it)
            if (result != null) return result
        }
        block.children.forEach {
            val result = search(it)
            if (result != null) return result
        }
        return null
    }
    return programBlocks.firstNotNullOfOrNull { search(it) }
}

// Поиск первого блока, у которого в inputBlocks находится данный ID
fun VariableViewModel.findBlockContaining(childId: String): Block? {
    return programBlocks.find { block ->
        block.inputBlocks.any { it?.id == childId }
    }
}

// Сбор всех ID блоков внутри дерева (включая входящие и детей)
fun VariableViewModel.collectDescendantIds(block: Block): Set<String> {
    val result = mutableSetOf<String>()
    fun collect(current: Block) {
        if (result.add(current.id)) {
            current.inputBlocks.filterNotNull().forEach { collect(it) }
            current.children.forEach { collect(it) }
        }
    }
    collect(block)
    return result
}

fun VariableViewModel.findBlockRecursivelyById(targetId: String, blocks: List<Block>): Block? {
    for (block in blocks) {
        if (block.id == targetId) return block

        for (child in block.inputBlocks) {
            if (child == null) continue
            val found = findBlockRecursivelyById(targetId, listOf(child))
            if (found != null) return found
        }

        for (child in block.children) {
            val found = findBlockRecursivelyById(targetId, listOf(child))
            if (found != null) return found
        }
    }
    return null
}
