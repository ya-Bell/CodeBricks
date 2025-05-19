package com.example.codebricks.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codebricks.R

@Composable
fun WorkScreen() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFB7EFFF))
            .padding(16.dp)
    ) {

        Header()

        Spacer(modifier = Modifier.height(8.dp))

        ConsoleSection()

        Spacer(modifier = Modifier.height(8.dp))

    }
}

@Composable
fun Header() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = stringResource(id = R.string.code_bricks_logo),
                modifier = Modifier.size(30.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Row {
            Button(
                onClick = {  },
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, Color.Gray, RoundedCornerShape(12.dp))
                    .height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFFFFF),
                    contentColor = Color.Black
                ),
                interactionSource = remember { MutableInteractionSource() },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(stringResource(id = R.string.help_button), color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {},
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, Color.Gray, RoundedCornerShape(12.dp))
                    .height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFFFFF),
                    contentColor = Color.Black
                ),
                interactionSource = remember { MutableInteractionSource() },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(stringResource(id = R.string.back_button), color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun ConsoleSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(Color(0xFFD9D9D9))
                .border(1.dp, Color.Gray, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .padding(4.dp)
        ) {
            Text(
                text = stringResource(id = R.string.console_label),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .align(Alignment.CenterStart)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                .background(Color.White)
                .border(1.dp, Color.Gray, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
        ) {
        }
    }
}

