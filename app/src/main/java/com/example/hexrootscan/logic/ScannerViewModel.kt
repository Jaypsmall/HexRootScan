package com.example.hexrootscan.logic

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

enum class Screen {
    SCANNER, TERMINAL, EXPLORER, SHODAN, SETTINGS
}

class ScannerViewModel : ViewModel() {
    var currentScreen by mutableStateOf(Screen.SCANNER)
    
    var target by mutableStateOf("")
    var options by mutableStateOf("-sS -Pn -T4")
    var logs by mutableStateOf(listOf("💀 SYSTEM INITIALIZED - ROOT ACCESS GRANTED"))
    
    // Terminal dedicada
    var terminalLogs by mutableStateOf(listOf("HEX-ROOT-TERMINAL v1.0", "Type 'help' for commands", ""))
    var terminalInput by mutableStateOf("")

    var isDarkMode by mutableStateOf(true)
    var shodanKey by mutableStateOf("")

    fun runRootCommand(command: String) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { logs = logs + "[#] > $command" }
            try {
                val process = Runtime.getRuntime().exec("su")
                val os = DataOutputStream(process.outputStream)
                val setupEnv = "export PATH=/data/data/com.termux/files/usr/bin:/data/data/com.termux/files/usr/bin/applets:\$PATH\n" +
                              "export LD_LIBRARY_PATH=/data/data/com.termux/files/usr/lib\n" +
                              "export HOME=/data/data/com.termux/files/home\n"
                
                os.write(setupEnv.toByteArray())
                os.write(("$command\n").toByteArray())
                os.write("exit\n".toByteArray())
                os.flush()

                BufferedReader(InputStreamReader(process.inputStream)).forEachLine { line ->
                    viewModelScope.launch(Dispatchers.Main) { logs = logs + line }
                }
                BufferedReader(InputStreamReader(process.errorStream)).forEachLine { line ->
                    viewModelScope.launch(Dispatchers.Main) { logs = logs + "![ERR] $line" }
                }
                process.waitFor()
                withContext(Dispatchers.Main) { logs = logs + "[✔] SESSION_FINISHED" }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { logs = logs + "![CRITICAL] ${e.message}" }
            }
        }
    }
    
    fun clearLogs() {
        logs = listOf("💀 TERMINAL RESET")
    }

    fun runTerminalCommand() {
        val cmd = terminalInput.trim()
        if (cmd.isEmpty()) return
        
        terminalLogs = terminalLogs + "root@hex:# $cmd"
        terminalInput = ""
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val process = Runtime.getRuntime().exec("su")
                val os = DataOutputStream(process.outputStream)
                val setupEnv = "export PATH=/data/data/com.termux/files/usr/bin:/data/data/com.termux/files/usr/bin/applets:\$PATH\n" +
                              "export LD_LIBRARY_PATH=/data/data/com.termux/files/usr/lib\n" +
                              "export HOME=/data/data/com.termux/files/home\n"
                
                os.write(setupEnv.toByteArray())
                os.write(("$cmd\n").toByteArray())
                os.write("exit\n".toByteArray())
                os.flush()

                BufferedReader(InputStreamReader(process.inputStream)).forEachLine { line ->
                    viewModelScope.launch(Dispatchers.Main) { terminalLogs = terminalLogs + line }
                }
                BufferedReader(InputStreamReader(process.errorStream)).forEachLine { line ->
                    viewModelScope.launch(Dispatchers.Main) { terminalLogs = terminalLogs + "![ERR] $line" }
                }
                process.waitFor()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { terminalLogs = terminalLogs + "![CRITICAL] ${e.message}" }
            }
        }
    }
}
