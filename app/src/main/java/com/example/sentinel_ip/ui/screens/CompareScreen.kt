package com.example.sentinel_ip.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sentinel_ip.data.model.CheckData
import com.example.sentinel_ip.data.model.CountryCodeMapper
import com.example.sentinel_ip.ui.components.ServiceQuotaBanner
import com.example.sentinel_ip.ui.components.ThreatGauge
import com.example.sentinel_ip.ui.viewmodel.SentinelViewModel
import com.example.sentinel_ip.ui.viewmodel.UiState

@Composable
fun CompareScreen(
    viewModel: SentinelViewModel,
    onOpenSettings: () -> Unit
) {
    var ipInputA by remember { mutableStateOf("") }
    var ipInputB by remember { mutableStateOf("") }

    val compareStateA by viewModel.compareStateA.collectAsState()
    val compareStateB by viewModel.compareStateB.collectAsState()
    val endpointQuotas by viewModel.endpointQuotas.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        ServiceQuotaBanner(
            quota = endpointQuotas["check"],
            defaultServiceName = "IP Audit Service",
            defaultLimit = 1000
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "A/B Node Threat Comparison",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Compare two IP addresses side-by-side",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dual Input Fields
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = ipInputA,
                onValueChange = { ipInputA = it },
                modifier = Modifier.weight(1f),
                label = { Text("Target Node A") },
                placeholder = { Text("118.25.6.39") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = ipInputB,
                onValueChange = { ipInputB = it },
                modifier = Modifier.weight(1f),
                label = { Text("Target Node B") },
                placeholder = { Text("185.220.101.5") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(10.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                viewModel.compareIps(ipInputA, ipInputB)
            },
            enabled = ipInputA.isNotBlank() && ipInputB.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(44.dp)
        ) {
            Text("Compare Targets", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Compare Output Grid
        if (compareStateA is UiState.Loading || compareStateB is UiState.Loading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (compareStateA is UiState.Error || compareStateB is UiState.Error) {
            val errorMsg = (compareStateA as? UiState.Error)?.message ?: (compareStateB as? UiState.Error)?.message ?: "Error"
            if (errorMsg.contains("API Key", ignoreCase = true)) {
                com.example.sentinel_ip.ui.components.ApiKeyPromptCard(onOpenSettings = onOpenSettings)
            } else {
                Text(text = errorMsg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
        } else if (compareStateA is UiState.Success<*> && compareStateB is UiState.Success<*>) {
            val dataA = (compareStateA as UiState.Success<CheckData>).data
            val dataB = (compareStateB as UiState.Success<CheckData>).data

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CompareNodeCard(data = dataA, label = "Node A", modifier = Modifier.weight(1f))
                CompareNodeCard(data = dataB, label = "Node B", modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun CompareNodeCard(
    data: CheckData,
    label: String,
    modifier: Modifier = Modifier
) {
    val countryName = CountryCodeMapper.getCountryName(data.countryCode)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = data.ipAddress,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            ThreatGauge(score = data.abuseConfidenceScore, size = 110.dp, strokeWidth = 10.dp)

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Country: $countryName (${data.countryCode ?: "N/A"})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("ISP: ${data.isp ?: "N/A"}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Domain: ${data.domain ?: "N/A"}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Tor Exit: ${if (data.isTor == true) "YES" else "No"}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Total Reports: ${data.totalReports}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
