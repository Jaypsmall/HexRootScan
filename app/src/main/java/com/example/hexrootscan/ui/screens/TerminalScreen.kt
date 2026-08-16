package com.example.hexrootscan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hexrootscan.logic.ScannerViewModel
import com.example.hexrootscan.ui.components.StatusLed
import com.example.hexrootscan.ui.theme.*

@Composable
fun TerminalScreen(viewModel: ScannerViewModel) {
    val isDarkMode = viewModel.isDarkMode
    val currentAccent = if (isDarkMode) HexAccent else Color(0xFF0066FF)
    val currentAccentLow = if (isDarkMode) HexAccentLow else Color(0xFFD0E0FF)
    val terminalBg = if (isDarkMode) Color.Black else Color(0xFFE9EDF0)
    val currentOk = if (isDarkMode) HexOk else Color(0xFF008800)
    
    val listState = rememberLazyListState()
    
    LaunchedEffect(viewModel.terminalLogs.size) {
        if (viewModel.terminalLogs.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.terminalLogs.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            Text("SYSTEM ROOT CONSOLE", color = currentAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            StatusLed(viewModel.status)
        }
        
        Box(modifier = Modifier.weight(1f).fillMaxWidth().background(terminalBg, RoundedCornerShape(8.dp)).border(1.dp, currentAccentLow, RoundedCornerShape(8.dp)).padding(8.dp)) {
            SelectionContainer {
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    items(viewModel.terminalLogs) { log ->
                        Text(
                            text = log,
                            color = when {
                                log.startsWith("root@hex:#") -> currentAccent
                                log.contains("![ERR]") || log.contains("![CRITICAL]") -> Color.Red
                                else -> currentOk
                            },
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().background(currentAccentLow.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).border(1.dp, currentAccentLow.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp)) {
            Text(">", color = currentAccent, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            TextField(
                value = viewModel.terminalInput,
                onValueChange = { viewModel.terminalInput = it },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = currentAccent,
                    focusedTextColor = if (isDarkMode) Color.White else Color.Black
                ),
                textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp),
                placeholder = { Text("Enter command...", color = Color.Gray, fontSize = 12.sp) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { viewModel.runTerminalCommand() }),
                singleLine = true
            )
        }
    }
}
