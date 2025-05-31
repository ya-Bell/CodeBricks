package com.example.codebricks.screens.workscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.codebricks.R
import com.example.codebricks.ui.theme.BlockClear
import com.example.codebricks.ui.theme.BlockControl
import com.example.codebricks.ui.theme.BlockDebug
import com.example.codebricks.ui.theme.BlockIO
import com.example.codebricks.ui.theme.BlockLogic
import com.example.codebricks.ui.theme.BlockLoops
import com.example.codebricks.ui.theme.BlockMath
import com.example.codebricks.ui.theme.BlockRun
import com.example.codebricks.ui.theme.BlockVariables
import com.example.codebricks.ui.theme.ButtonError
import com.example.codebricks.ui.theme.TextBlack
import com.example.codebricks.ui.theme.TextWhite

@Composable
fun HelpDialog(onDismiss: () -> Unit) {
    var currentPage by remember { mutableStateOf(0) }
    val totalPages = 3

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = TextWhite
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.help_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    when (currentPage) {
                        0 -> Page1()
                        1 -> Page2()
                        2 -> Page3()
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { if (currentPage > 0) currentPage-- },
                        enabled = currentPage > 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BlockControl,
                            contentColor = TextWhite
                        )
                    ) {
                        Text(stringResource(id = R.string.help_previous))
                    }

                    Text(
                        text = "${currentPage + 1}/$totalPages",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Button(
                        onClick = { if (currentPage < totalPages - 1) currentPage++ },
                        enabled = currentPage < totalPages - 1,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BlockControl,
                            contentColor = TextWhite
                        )
                    ) {
                        Text(stringResource(id = R.string.help_next))
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ButtonError,
                        contentColor = TextWhite
                    )
                ) {
                    Text(stringResource(id = R.string.help_close))
                }
            }
        }
    }
}

@Composable
private fun Page1() {
    Column(
        modifier = Modifier.padding(8.dp)
    ) {
        HelpItem(
            text = stringResource(id = R.string.help_control),
            title = stringResource(id = R.string.help_section_control),
            color = BlockControl
        )
        HelpItem(
            text = stringResource(id = R.string.help_variables),
            title = stringResource(id = R.string.help_section_variables),
            color = BlockVariables
        )
        HelpItem(
            text = stringResource(id = R.string.help_math),
            title = stringResource(id = R.string.help_section_math),
            color = BlockMath
        )
    }

}

@Composable
private fun Page2() {
    Column(
        modifier = Modifier.padding(8.dp)
    ) {
        HelpItem(
            text = stringResource(id = R.string.help_io),
            title = stringResource(id = R.string.help_section_io),
            color = BlockIO
        )
        HelpItem(
            text = stringResource(id = R.string.help_logic),
            title = stringResource(id = R.string.help_section_logic),
            color = BlockLogic
        )
        HelpItem(
            text = stringResource(id = R.string.help_while),
            title = stringResource(id = R.string.help_section_loops),
            color = BlockLoops
        )
    }
}

@Composable
private fun Page3() {
    Column(
        modifier = Modifier.padding(8.dp)
    ) {
        HelpItem(
            text = stringResource(id = R.string.help_debug),
            title = stringResource(id = R.string.help_section_debug),
            color = BlockDebug
        )
        HelpItem(
            text = stringResource(id = R.string.help_run),
            title = stringResource(id = R.string.help_section_run),
            color = BlockRun
        )
        HelpItem(
            text = stringResource(id = R.string.help_clear),
            title = stringResource(id = R.string.help_section_clear),
            color = BlockClear
        )
    }
}

@Composable
private fun HelpItem(text: String, title: String, color: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = color
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                style = MaterialTheme.typography.bodyMedium.copy(
                    shadow = Shadow(
                        color = TextBlack.copy(alpha = 0.5f),
                        offset = Offset(1f, 1f),
                        blurRadius = 2f
                    )
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = text,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Justify,
                color = TextWhite
            )
        }
    }
}