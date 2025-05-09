package com.example.codebricks.variable_declaration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.codebricks.R

@Composable
fun VariablePanel(vars: Map<String, Int>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .padding(8.dp)
            .width(150.dp)
    ) {
        Text(stringResource(R.string.variables_title), style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(8.dp))
        if (vars.isEmpty()) {
            Text(stringResource(R.string.no_variables), style = MaterialTheme.typography.bodySmall)
        } else {
            vars.forEach { (n, v) ->
                Text("$n = $v", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
