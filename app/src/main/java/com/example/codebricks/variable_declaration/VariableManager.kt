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

    fun clear() {
        variables.clear()
        notifyListeners()
    }

    fun addListener(listener: () -> Unit) {
        listeners.add(listener)
    }

    fun assign(name: String, value: Int) {
        if (variables.containsKey(name)) {
            variables[name] = value
            notifyListeners()
        }
    }


    private fun notifyListeners() {
        listeners.forEach { it() }
    }
}
