package com.example.codebricks.blocks.common

enum class BlockType {
    // Variables
    VARIABLE_DECLARE,       // Declare x = 0
    VARIABLE_SET,           // Set x to 5
    VARIABLE_CHANGE,        // Change x by 1
    VARIABLE_REFERENCE,     // блок [x] вставляется в выражения

    // Control
    CONTROL_START,
    CONTROL_STOP,

    // Input / Output
    IO_PRINT,
    IO_WRITE,

    // Future blocks
    MATH_ADD,
    MATH_SUBTRACT,
    MATH_MULTIPLY,
    MATH_DIVIDE,
    MATH_MODULO,

    COMPARISON_EQUAL,
    COMPARISON_GREATER,
    COMPARISON_LESS,

    LOGIC_AND,
    LOGIC_OR,
    LOGIC_NOT,

    LOOP_REPEAT,
    LOOP_WHILE,
    LOOP_FOR,

    FUNCTION_DEFINE,
    FUNCTION_CALL,

    IF,
    ELSE_IF,
    ELSE,
    END_IF,
    WHILE,
    WHILE_END
}