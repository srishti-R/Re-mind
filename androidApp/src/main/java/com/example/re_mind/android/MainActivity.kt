package com.example.re_mind.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ReminderMainScreen()
                }
            }
        }
    }
}

@Composable
fun ReminderMainScreen() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    )  {
        Text(
            text = "Create New Reminder",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        SimpleTextField("What?", "Remind me to call mom", Icons.Filled.Info)
        SimpleTextField("When?", "at 7 PM", Icons.Filled.DateRange)
        SimpleTextField("Where?", "place", Icons.Filled.LocationOn)
        SimpleTextField("How?", "by mobile", Icons.Filled.Create)
    }
}

@Composable
fun SimpleTextField(label: String, placeholder: String, leadingIcon: ImageVector? = null) {
    var text by remember { mutableStateOf("") }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = {
            if (leadingIcon != null) {
                Icon(imageVector = leadingIcon, contentDescription = null)
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        ReminderMainScreen()
    }
}
