package com.efea.SLMBenchmark.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.efea.SLMBenchmark.R

@Composable
fun NoticeDialog(
    onDismissRequest: () -> Unit,
    dontShowAgainChecked: Boolean,
    onDontShowAgainCheckedChange: (Boolean) -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("AI Parameter Guide") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Welcome to Local AI Benchmark!", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.notice) + "\n Here is a quick guide to the AI settings:",
                    fontWeight = FontWeight.Bold
                )

                Text(text = "Temperature:", fontWeight = FontWeight.SemiBold)
                Text(
                    text = "Controls randomness. Lower values make output focused and deterministic; higher values (e.g., 1.0+) make it more creative but potentially incoherent.",
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Top-P (Nucleus Sampling):", fontWeight = FontWeight.SemiBold)
                Text(
                    text = "Limits the model to a cumulative probability of the most likely tokens. 0.95 means it only looks at the top 95% of candidates.",
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Top-K:", fontWeight = FontWeight.SemiBold)
                Text(
                    text = "Limits the model to the top K most likely next words. A value of 40 means the model only chooses from the 40 best options.",
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Max Tokens:", fontWeight = FontWeight.SemiBold)
                Text(
                    text = "The maximum length of the response. Setting this too high may drain battery or cause long generation times.",
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Random Seed:", fontWeight = FontWeight.SemiBold)
                Text(
                    text = "If set, the model will produce the exact same result for the same prompt. Useful for consistent benchmarking.",
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.clickable { onDontShowAgainCheckedChange(!dontShowAgainChecked) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = dontShowAgainChecked,
                        onCheckedChange = onDontShowAgainCheckedChange
                    )
                    Text(text = "Don't show it again at start", modifier = Modifier.padding(start = 8.dp))
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Continue")
            }
        }
    )
}
