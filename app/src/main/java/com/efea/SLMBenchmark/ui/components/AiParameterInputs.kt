package com.efea.SLMBenchmark.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AiParameterInputs(
    userMsg: String,
    onUserMsgChange: (String) -> Unit,
    temperature: Float,
    onTemperatureChange: (Float) -> Unit,
    topP: Float,
    onTopPChange: (Float) -> Unit,
    topKText: String,
    onTopKTextChange: (String) -> Unit,
    maxTokensText: String,
    onMaxTokensTextChange: (String) -> Unit,
    randomSeedText: String,
    onRandomSeedTextChange: (String) -> Unit,
    isLoaded: Boolean,
    onSendClick: () -> Unit
) {
    OutlinedTextField(
        value = userMsg,
        onValueChange = onUserMsgChange,
        label = { Text("Enter your message") },
        placeholder = { Text("This is an example message for benchmark testing.") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        enabled = isLoaded
    )

    Text(
        text = "Temperature Point: ${"%.2f".format(temperature)}",
        modifier = Modifier.padding(top = 2.dp)
    )

    Slider(
        value = temperature,
        onValueChange = onTemperatureChange,
        valueRange = 0.1f..5.0f,
        enabled = isLoaded
    )

    Text(
        text = "TopP Point: ${"%.2f".format(topP)}",
        modifier = Modifier.padding(top = 2.dp)
    )

    Slider(
        value = topP,
        onValueChange = onTopPChange,
        valueRange = 0.1f..1.0f,
        enabled = isLoaded
    )

    OutlinedTextField(
        value = topKText,
        onValueChange = { input -> onTopKTextChange(input.filter { it.isDigit() }) },
        label = { Text("Top K") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        enabled = isLoaded
    )

    OutlinedTextField(
        value = maxTokensText,
        onValueChange = { input -> onMaxTokensTextChange(input.filter { it.isDigit() }) },
        label = { Text("Max Tokens") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        enabled = isLoaded
    )

    OutlinedTextField(
        value = randomSeedText,
        onValueChange = { input -> onRandomSeedTextChange(input.filter { it.isDigit() }) },
        label = { Text("Random Seed (Optional)") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        enabled = isLoaded
    )

    Button(
        onClick = onSendClick,
        modifier = Modifier.padding(vertical = 8.dp),
        enabled = isLoaded
    ) {
        Text(text = if (isLoaded) "Send message & Start Benchmark" else "Loading model...")
    }
}
