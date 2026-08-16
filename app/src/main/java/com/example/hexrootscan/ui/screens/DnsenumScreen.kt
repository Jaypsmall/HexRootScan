package com.example.hexrootscan.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hexrootscan.logic.ScannerViewModel
import com.example.hexrootscan.logic.Screen
import com.example.hexrootscan.ui.components.HexInput
import com.example.hexrootscan.ui.components.StatusLed
import com.example.hexrootscan.ui.theme.HexAccent
import com.example.hexrootscan.ui.theme.HexPanel
import com.example.hexrootscan.ui.theme.HexText

@Composable
fun DnsenumScreen(viewModel: ScannerViewModel) {
    val isDarkMode = viewModel.isDarkMode
    val currentAccent = if (isDarkMode) HexAccent else Color(0xFF0066FF)
    val currentPanel = if (isDarkMode) HexPanel else Color.White
    val currentText = if (isDarkMode) HexText else Color(0xFF333333)

    val options = listOf(
        "0 : DNS Records (A,AAA,MX,NS,CNAME,SOA)" to "",
        "1 : Autonomous System (AS)" to "--as",
        "2 : Subdomains Enumeration" to "--enum",
        "3 : DNS Zone Transfer Lookup" to "--zonetransfer",
        "4 : Shared DNS Servers" to "--shared",
        "5 : Reverse DNS Lookup" to "--reverse",
        "6 : Sender Policy Framework (SPF)" to "--spf",
        "7 : Domain Keys Identified Mail (DKIM)" to "--dkim",
        "8 : DNS Certification Authority Authorization (CAA)" to "--caa",
        "9 : Domain Name System Security Extensions (DNSSEC)" to "--dnssec",
        "10 : Domain Message Authentication Reporting and Conformance (DMARC)" to "--dmarc"
    )

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("DNS ENUMERATION MODULE", color = currentAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            StatusLed(viewModel.status)
        }
        
        Spacer(Modifier.height(8.dp))
        HexInput(viewModel.target, { viewModel.target = it }, "DOMAIN TARGET", Icons.Default.Language, currentAccent)
        Spacer(Modifier.height(12.dp))
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(options) { (label, flag) ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                        val t = viewModel.target.trim()
                        if (t.isNotEmpty()) {
                            viewModel.runRootCommand("perl /data/data/com.termux/files/home/dnsenum/dnsenum.pl $flag $t")
                            viewModel.currentScreen = Screen.SCANNER
                        } else {
                            viewModel.runRootCommand("echo '![ERR] NO TARGET SPECIFIED'")
                        }
                    },
                    colors = CardDefaults.cardColors(containerColor = currentPanel),
                    border = BorderStroke(1.dp, currentAccent.copy(0.2f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Dns, contentDescription = null, tint = currentAccent, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(label, color = currentText, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
