package com.example.codebricks.variable_declaration

object VariableManager {
    private val vars = mutableMapOf<String, Int>()
    private val listeners = mutableListOf<() -> Unit>()

    fun declare(names: List<String>) {
        var changed = false
        for (n in names) if (vars.putIfAbsent(n, 0) == null) changed = true
        if (changed) listeners.forEach { it() }
    }

    fun assign(name: String, value: Int) {
        if (vars.containsKey(name)) {
            vars[name] = value
            listeners.forEach { it() }
        }
    }

    fun clear() { vars.clear(); listeners.forEach { it() } }
    fun all() = vars.toMap()
    fun addListener(l: () -> Unit) { listeners.add(l) }
}
