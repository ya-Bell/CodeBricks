package com.example.codebricks.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

data class Variable(val name: String, val value: Any, val type: String)

class VariableViewModel : ViewModel() {

    private val _variables = mutableStateOf<List<Variable>>(emptyList())
    val variables: List<Variable> get() = _variables.value

    private val _blocks = mutableStateOf<List<Variable>>(emptyList())
    val blocks: List<Variable> get() = _blocks.value

    fun declareVariable(name: String, value: Any, type: String) {
        val newVariable = Variable(name, value, type)
        _variables.value = _variables.value + newVariable
    }


    fun declareControlBlock(type: String) {
        val newControlBlock = Variable(name = type, value = 0, type = "Control")
        _blocks.value = _blocks.value + newControlBlock
    }

    fun declarePrintBlock(variable: Variable) {
        val printBlock = Variable(name = "Print", value = variable, type = "Print")
        _blocks.value = _blocks.value + printBlock
    }


}

