package com.example.codebricks.variable_declaration

object VariableManager {
    private val variables = mutableMapOf<String, Int>()
    private val listeners = mutableListOf<() -> Unit>()

    fun declareVariables(names: List<String>) {
        var changed = false
        names.forEach { name ->
            if (!variables.containsKey(name)) {
                variables[name] = 0
                changed = true
            }
        }
        if (changed) notifyListeners()
    }

    fun getAllVariables(): Map<String, Int> = variables.toMap()


    fun addListener(listener: () -> Unit) {
        listeners.add(listener)
    }

    private fun notifyListeners() {
        listeners.forEach { it() }
    }
}
