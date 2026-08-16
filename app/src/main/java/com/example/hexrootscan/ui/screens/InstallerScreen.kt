package com.example.hexrootscan.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hexrootscan.logic.ScannerViewModel
import com.example.hexrootscan.ui.theme.HexAccent
import com.example.hexrootscan.ui.theme.HexPanel
import com.example.hexrootscan.ui.theme.HexText

@Composable
fun InstallerScreen(viewModel: ScannerViewModel) {
    val isDarkMode = viewModel.isDarkMode
    val currentAccent = if (isDarkMode) HexAccent else Color(0xFF0066FF)
    val currentPanel = if (isDarkMode) HexPanel else Color.White
    val currentText = if (isDarkMode) HexText else Color(0xFF333333)

    val sections = listOf(
        "CORE REPAIRS" to listOf(
            "Fix Termux Permissions" to "su -c 'chmod -R 755 /data/data/com.termux/files/usr/bin && chmod -R 755 /data/data/com.termux/files/home'",
            "DNSENUM Fix (Net::IP)" to "pkg install perl-net-ip -y || cpan install Net::IP"
        ),
        "SYSTEM ENGINES" to listOf(
            "Install Perl, Ruby & Python" to "pkg update -y && pkg install perl ruby python git -y",
            "Install Nmap Scanner" to "pkg install nmap -y",
            "Install Dnsutils & Whois" to "pkg install dnsutils whois -y"
        ),
        "GITHUB AUDIT TOOLS" to listOf(
            "Clone Nikto" to "git clone https://github.com/sullo/nikto /data/data/com.termux/files/home/nikto",
            "Clone WhatWeb" to "git clone https://github.com/urbanadventurer/WhatWeb /data/data/com.termux/files/home/WhatWeb",
            "Clone Dnsenum" to "git clone https://github.com/fwaeytens/dnsenum /data/data/com.termux/files/home/dnsenum"
        )
    )

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Text("TOOL INSTALLATION & REPAIR CENTER", color = currentAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            sections.forEach { (sectionTitle, tools) ->
                item {
                    Text(
                        sectionTitle, 
                        color = currentAccent.copy(alpha = 0.7f), 
                        fontSize = 11.sp, 
                        fontWeight = FontWeight.Black, 
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                    )
                }
                items(tools) { (name, cmd) ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = currentPanel),
                        border = BorderStroke(1.dp, if(sectionTitle == "CORE REPAIRS") Color.Red.copy(0.4f) else currentAccent.copy(0.2f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(name, color = currentText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(cmd, color = currentText.copy(0.5f), fontSize = 9.sp, fontFamily = FontFamily.Monospace, maxLines = 1)
                            }
                            Button(
                                onClick = { viewModel.runRootCommand(cmd) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if(sectionTitle == "CORE REPAIRS") Color(0xFF440000) else currentAccent
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp)
                            ) {
                                Icon(
                                    if(sectionTitle == "CORE REPAIRS") Icons.Default.Shield else Icons.Default.Download, 
                                    contentDescription = null, 
                                    modifier = Modifier.size(14.dp),
                                    tint = if(isDarkMode) Color.Black else Color.White
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("RUN", color = if(isDarkMode) Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            colors = CardDefaults.cardColors(containerColor = currentAccent.copy(alpha = 0.05f))
        ) {
            Text(
                "NOTE: If 'pkg' fails, run 'Fix Termux Permissions' first (Requires Root).", 
                color = currentAccent.copy(alpha = 0.6f), 
                fontSize = 9.sp, 
                modifier = Modifier.padding(8.dp),
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
