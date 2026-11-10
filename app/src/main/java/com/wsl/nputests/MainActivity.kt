package com.wsl.nputests

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LabScreen(this)
                }
            }
        }
    }
}

@Composable
private fun LabScreen(context: Context) {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memory = ActivityManager.MemoryInfo().also(activityManager::getMemoryInfo)
    val vulkan = if (Build.VERSION.SDK_INT >= 24) {
        context.packageManager.hasSystemFeature("android.hardware.vulkan.level")
    } else false
    val abiList = Build.SUPPORTED_ABIS.joinToString()

    var result by remember { mutableStateOf<BenchmarkResult?>(null) }
    var running by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Snapdragon AI Lab", style = MaterialTheme.typography.headlineMedium)
        Text("Build 0.1.0 • Device Inspector + CPU baseline")

        InfoCard("Dispositivo") {
            InfoLine("Fabricante", Build.MANUFACTURER)
            InfoLine("Modelo", Build.MODEL)
            InfoLine("Android", "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            InfoLine("Hardware", Build.HARDWARE)
            InfoLine("Board", Build.BOARD)
            InfoLine("ABIs", abiList)
        }

        InfoCard("Memória") {
            InfoLine("RAM total", formatMb(memory.totalMem / (1024 * 1024)))
            InfoLine("RAM disponível agora", formatMb(memory.availMem / (1024 * 1024)))
            InfoLine("Low memory", if (memory.lowMemory) "Sim" else "Não")
            Text("A RAM disponível muda constantemente e não representa memória garantida para o app.",
                style = MaterialTheme.typography.bodySmall)
        }

        InfoCard("Aceleradores") {
            InfoLine("Vulkan feature flag", if (vulkan) "Presente" else "Não reportada")
            InfoLine("CPU", "Disponível • benchmark inicial")
            InfoLine("GPU", "Ainda não testada • backend será integrado")
            InfoLine("Hexagon / QNN HTP", "Ainda não testada • sem alegar execução")
        }

        InfoCard("Benchmark CPU") {
            Text("Executa uma carga determinística de contagem de números primos. Serve como linha de base, não como comparação direta com NPU/GPU.")
            Button(
                enabled = !running,
                onClick = {
                    running = true
                    scope.launch {
                        result = withContext(Dispatchers.Default) {
                            BenchmarkEngine.runCpuPrimeBenchmark()
                        }
                        running = false
                    }
                }
            ) {
                Text(if (running) "Executando..." else "Executar benchmark CPU")
            }
            result?.let {
                InfoLine("Tempo", "${it.elapsedMs} ms")
                InfoLine("Números analisados", it.operations.toString())
                InfoLine("Primos encontrados", it.score.let { score -> "%.1f primos/s".format(score) })
            }
        }

        Text("Próxima etapa: validar QNN/HTP no Snapdragon 6 Gen 3 e registrar o backend real nos logs.")
    }
}

@Composable
private fun InfoCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}

private fun formatMb(value: Long) = String.format("%,d MB", value)
