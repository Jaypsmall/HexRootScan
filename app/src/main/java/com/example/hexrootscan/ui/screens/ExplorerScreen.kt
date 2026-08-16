package com.example.hexrootscan.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.hexrootscan.logic.ScannerViewModel
import com.example.hexrootscan.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExplorerScreen(viewModel: ScannerViewModel) {
    val isDarkMode = viewModel.isDarkMode
    val currentAccent = if (isDarkMode) HexAccent else Color(0xFF0066FF)
    val currentPanel = if (isDarkMode) HexPanel else Color.White
    val currentText = if (isDarkMode) HexText else Color(0xFF333333)

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("ROOT EXPORTS EXPLORER", color = currentAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { viewModel.refreshExplorer() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = currentAccent)
            }
        }
        
        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(containerColor = currentPanel),
            border = BorderStroke(1.dp, currentAccent.copy(0.3f))
        ) {
            if (viewModel.explorerFiles.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("NO EXPORTED FILES FOUND", color = currentText.copy(0.5f), fontFamily = FontFamily.Monospace)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    items(viewModel.explorerFiles) { file ->
                        ListItem(
                            modifier = Modifier.fillMaxWidth().clickable { viewModel.openFile(file) },
                            headlineContent = { Text(file.name, color = currentText, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) },
                            supportingContent = { 
                                val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(file.lastModified()))
                                Text("${file.length() / 1024} KB | $date", color = currentText.copy(0.6f), fontSize = 10.sp) 
                            },
                            leadingContent = { Icon(Icons.Default.Description, contentDescription = null, tint = currentAccent) },
                            trailingContent = {
                                IconButton(onClick = { 
                                    file.delete()
                                    viewModel.refreshExplorer()
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(0.6f))
                                }
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                        HorizontalDivider(color = currentAccent.copy(0.1f))
                    }
                }
            }
        }

        // --- Visor de Archivos ---
        if (viewModel.selectedFileContent != null) {
            AlertDialog(
                onDismissRequest = { viewModel.selectedFileContent = null },
                modifier = Modifier.fillMaxSize().padding(16.dp),
                properties = DialogProperties(usePlatformDefaultWidth = false),
                containerColor = currentPanel,
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("REPORT VIEWER", color = currentAccent, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { viewModel.selectedFileContent = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = currentAccent)
                        }
                    }
                },
                text = {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.2f), RoundedCornerShape(8.dp)).border(1.dp, currentAccent.copy(0.2f), RoundedCornerShape(8.dp)).padding(8.dp)) {
                        SelectionContainer {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                item {
                                    Text(
                                        text = viewModel.selectedFileContent ?: "",
                                        color = if (isDarkMode) Color.White else Color.Black,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.selectedFileContent = null }) {
                        Text("CLOSE", color = currentAccent, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}
