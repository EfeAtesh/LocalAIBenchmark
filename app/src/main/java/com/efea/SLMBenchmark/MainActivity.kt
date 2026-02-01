package com.efea.SLMBenchmark

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.android.billingclient.api.*
import com.efea.SLMBenchmark.ui.theme.LocalAIBenchmarkTheme
import com.efea.SLMBenchmark.ui.theme.primaryLight
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.PopupProperties
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private lateinit var billingClient: BillingClient
    private var removedAdsState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MobileAds.initialize(this) {}
        setupBillingClient()

        enableEdgeToEdge()
        setContent {
            LocalAIBenchmarkTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        MainScreen(
                            removedAds = removedAdsState.value,
                            onRemoveAdsClick = { launchPurchaseFlow() }
                        )
                    }
                }
            }
        }
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(this)
            .setListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (purchase in purchases) {
                        handlePurchase(purchase)
                    }
                }
            }
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryPurchases()
                }
            }
            override fun onBillingServiceDisconnected() {}
        })
    }

    private fun queryPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in purchases) {
                    if (purchase.products.contains("remove_ads") && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        removedAdsState.value = true
                    }
                }
            }
        }
    }

    private fun launchPurchaseFlow() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("remove_ads")
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetailsList[0])
                            .build()
                    ))
                    .build()
                billingClient.launchBillingFlow(this, billingFlowParams)
            } else {
                runOnUiThread {
                    Toast.makeText(this, "Purchase item not found in Play Store.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        removedAdsState.value = true
                        runOnUiThread {
                            Toast.makeText(this, "Ads successfully removed!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                removedAdsState.value = true
            }
        }
    }
}

fun shareBenchmarkResult(context: Context, score: Int, tier: String, info: String) {
    val model = Build.MODEL
    val manufacturer = Build.MANUFACTURER
    val shareText = """
        🚀 Local AI Benchmark Result
        📱 Device: $manufacturer $model
        🏆 Performance Score: $score
        🎖️ Rating: $tier
        📊 $info
        
        Download the app to test your device's AI power!
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Local AI Benchmark Result")
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, "Share via"))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(removedAds: Boolean, onRemoveAdsClick: () -> Unit) {
    val context = LocalContext.current
    val modelManager = remember { ModelManager(context) }
    val sharedPrefs = remember { context.getSharedPreferences("LocalLLMPrefs", Context.MODE_PRIVATE) }
    val benchMarkManager = remember { BenchMark() }

    var userMsg by remember { mutableStateOf("") }
    var response by remember { mutableStateOf("") }
    var temperature by remember { mutableFloatStateOf(0.7f) }
    var isLoaded by remember { mutableStateOf(false) }

    var topKText by remember { mutableStateOf("40") }
    var topP by remember { mutableFloatStateOf(0.95f) }
    var maxTokensText by remember { mutableStateOf("1024") }
    var randomSeedText by remember { mutableStateOf("0") }

    var benchmarkInfo by remember { mutableStateOf("") }

    var showDialog by remember {
        mutableStateOf(!sharedPrefs.getBoolean("hide_info", false))
    }
    var dontShowAgainChecked by remember { mutableStateOf(false) }
    var benchMark by remember { mutableStateOf(false) }

    // Benchmark States
    var cpuUsage by remember { mutableStateOf(0.0) }
    var cpuHz by remember { mutableStateOf(0.0) }
    var ramInfo by remember { mutableStateOf("N/A") }
    var ramUsage by remember { mutableStateOf(0.0) }
    var totalram by remember { mutableStateOf(0.0) }

    var cpuHistory by remember { mutableStateOf(listOf<Double>()) }
    var cpuHzHistory by remember { mutableStateOf(listOf<Double>()) }
    var ramHistory by remember { mutableStateOf(listOf<Double>()) }

    var showPerformanceDialog by remember { mutableStateOf(false) }
    var performanceScore by remember { mutableStateOf(0) }
    var performanceTier by remember { mutableStateOf("") }

    LaunchedEffect(modelManager) {
        modelManager.initModel(object : ModelManager.OnLoadedCallback {
            override fun onSuccess() {
                isLoaded = true
            }
            override fun onError(error: String?) {
                response += "Error loading model: $error"
            }
        })
    }

    // Benchmark Refresh Loop
    LaunchedEffect(benchMark) {
        if (benchMark) {
            while (true) {
                cpuUsage = benchMarkManager.getCpuUsage()
                cpuHz = benchMarkManager.getCPUHz()
                ramInfo = benchMarkManager.getRAMINFO(context)
                ramUsage = benchMarkManager.ramUsage
                totalram = benchMarkManager.totalram

                val currentHistory = cpuHistory.toMutableList()
                currentHistory.add(cpuUsage)
                if (currentHistory.size > 20) currentHistory.removeAt(0)
                cpuHistory = currentHistory

                val currentHistory2 = cpuHzHistory.toMutableList()
                currentHistory2.add(cpuHz)
                if (currentHistory2.size > 20) currentHistory2.removeAt(0)
                cpuHzHistory = currentHistory2

                val currentHistory3 = ramHistory.toMutableList()
                currentHistory3.add(ramUsage)
                if (currentHistory3.size > 20) currentHistory3.removeAt(0)
                ramHistory = currentHistory3

                delay(500)
            }
        }
    }

    if (showDialog) {
        ShowNotice(
            onDismissRequest = { showDialog = false },
            dontShowAgainChecked = dontShowAgainChecked,
            onDontShowAgainCheckedChange = { dontShowAgainChecked = it },
            onConfirm = {
                if (dontShowAgainChecked) {
                    sharedPrefs.edit().putBoolean("hide_info", true).apply()
                }
                showDialog = false
            }
        )
    }

    if (showPerformanceDialog) {
        AlertDialog(
            onDismissRequest = { showPerformanceDialog = false },
            icon = { Icon(Icons.Default.Build, contentDescription = null) },
            title = { Text("Benchmark Results") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Device Score", fontSize = 14.sp, color = Color.Gray)
                    Text(text = performanceScore.toString(), fontSize = 48.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(text = performanceTier, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = benchmarkInfo, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "The score considers your average CPU clock speed, total RAM capacity, and actual AI inference tokens-per-second.", fontSize = 11.sp, lineHeight = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPerformanceDialog = false }) {
                    Text("Dismiss")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    shareBenchmarkResult(context, performanceScore, performanceTier, benchmarkInfo)
                }) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                    Text("Share Results")
                }
            }
        )
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Local AI Benchmark",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 24.sp,
            )

            IconButton(
                onClick = {
                    showDialog = true
                }
            ) {
                Icon(imageVector = Icons.Default.Info, contentDescription = "Info")
            }



                IconButton(
                    modifier = Modifier.padding(start = 8.dp),
                    onClick = onRemoveAdsClick
                ) {
                    if (!removedAds) {
                    Icon(
                        painter = painterResource(id = R.drawable.ad_off_24dp_ffffff_fill0_wght400_grad0_opsz24),
                        contentDescription = "Remove Ads"
                    )}
                    else {
                        Icon(
                            painter = painterResource(id = R.drawable.coffee_cup_svgrepo_com),
                            contentDescription = "Buy him a coffee"
                        )
                    }
                }

        }

        OutlinedTextField(
            value = userMsg,
            onValueChange = { userMsg = it },
            label = { Text("Enter your message") },
            placeholder = { Text("This is an example message for benchmark testing.") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            enabled = isLoaded
        )

        Text(text = "Temperature Point: ${"%.2f".format(temperature)}",
            modifier = Modifier.padding(top = 2.dp))

        Slider(
            value = temperature,
            onValueChange = {
                temperature = it
                modelManager.setTemp(it)
            },
            valueRange = 0.1f..5.0f,
            enabled = isLoaded
        )

        Text(text = "TopP Point: ${"%.2f".format(topP)}",
            modifier = Modifier.padding(top = 2.dp))

        Slider(
            value = topP,
            onValueChange = {
                topP = it
                modelManager.setTopP(it)
            },
            valueRange = 0.1f..1.0f,
            enabled = isLoaded
        )

        OutlinedTextField(
            value = topKText,
            onValueChange = { input ->
                topKText = input.filter { it.isDigit() }
                modelManager.setTopK(topKText.toIntOrNull() ?: 40)
            },
            label = { Text("Top K") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = isLoaded
        )

        OutlinedTextField(
            value = maxTokensText,
            onValueChange = { input ->
                maxTokensText = input.filter { it.isDigit() }
                modelManager.setMaxTokens(maxTokensText.toIntOrNull() ?: 1024)
            },
            label = { Text("Max Tokens") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = isLoaded
        )

        OutlinedTextField(
            value = randomSeedText,
            onValueChange = { input ->
                randomSeedText = input.filter { it.isDigit() }
                modelManager.setRandomSeed(randomSeedText.toIntOrNull())
            },
            label = { Text("Random Seed (Optional)") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = isLoaded
        )

        Button(
            onClick = {
                if (userMsg.isNotBlank()) {
                    benchmarkInfo = "Generating..."
                    response += "━━━━━━━━━━━━━━━━━━━━━━\nYour Message: " + userMsg + "\n━━━━━━━━━━━━━━━━━━━━━━\n"
                    modelManager.ask(userMsg, object : ModelManager.OnResultCallback {
                        override fun onResult(text: String?, durationMs: Long, tps: Double) {
                            response += "\n"+ (text ?: "No response") + "\n"
                            
                            // Calculate Performance Score dynamically
                            val avgHz = if (cpuHzHistory.isNotEmpty()) cpuHzHistory.average() else cpuHz
                            performanceScore = (avgHz * 1.2 + (totalram / 1024.0) * 800 + (tps * 500)).toInt()
                            performanceTier = when {
                                performanceScore > 8000 -> "Flagship Class (Extreme AI Performance)"
                                performanceScore > 5000 -> "Premium Mid-Range (Fast Inference)"
                                performanceScore > 3000 -> "Standard Mid-Range (Steady Performance)"
                                else -> "Entry-Level (Slow Inference)"
                            }
                            
                            benchmarkInfo = "Latest Speed: ${"%.2f".format(tps)} t/s | Time: $durationMs ms"
                            showPerformanceDialog = true
                        }
                        override fun onError(error: String?) {
                            response += "\nError: $error \n"
                            benchmarkInfo = "Error occurred"
                        }
                    })
                }
            },
            modifier = Modifier.padding(vertical = 8.dp),
            enabled = isLoaded
        ) {
            Text(text = if (isLoaded) "Send message & Start Benchmark" else "Loading model...")
        }

        if (benchmarkInfo.isNotEmpty()) {
            Text(
                text = benchmarkInfo,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        SelectionContainer (modifier = Modifier
            .weight(1f)
            .padding(2.dp)
            .verticalScroll(rememberScrollState())) {
            Card(                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor =MaterialTheme.colorScheme.secondaryContainer
                )) {
                Text(
                    text = response,
                    modifier = Modifier.padding(16.dp)
                )}

        }

        if (!removedAds) {
            BannerAd(modifier = Modifier.fillMaxWidth())
        }

        Button(
            onClick = { benchMark = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        ) {
            Text(text = "Show BenchMark metrics")
        }

        if (benchMark) {
            ModalBottomSheet(
                onDismissRequest = { benchMark = false },
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFFE0E0E0)) },
                content = {
                    val labelProperties = LabelProperties(
                        enabled = true,
                        textStyle = TextStyle(color = primaryLight)
                    )
                    val indicatorProperties = HorizontalIndicatorProperties(
                        textStyle = TextStyle(color = primaryLight)
                    )
                    
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())) {

                        Text(text = "Real-time Metrics", fontSize = 20.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding( 16.dp))

                        LineChart(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(horizontal = 22.dp),
                            data = listOf(
                                Line(
                                    values = cpuHistory,
                                    color = SolidColor(primaryLight),
                                    firstGradientFillColor = primaryLight.copy(alpha = .5f),
                                    secondGradientFillColor = Color.Green,
                                    gradientAnimationDelay = 0,
                                    drawStyle = DrawStyle.Stroke(width = 2.dp),
                                )
                            ),
                            labelProperties = labelProperties,
                            indicatorProperties = indicatorProperties,
                            popupProperties = PopupProperties(enabled = false),
                        )
                        Text(text = "CPU Usage: ${"%.2f".format(cpuUsage)}%", modifier = Modifier.padding(top = 16.dp).padding(vertical = 16.dp))

                        LineChart(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(horizontal = 22.dp),
                            data = listOf(
                                Line(
                                    values = cpuHzHistory,
                                    color = SolidColor(primaryLight),
                                    firstGradientFillColor = primaryLight.copy(alpha = .5f),
                                    secondGradientFillColor = Color.Green,
                                    gradientAnimationDelay = 0,
                                    drawStyle = DrawStyle.Stroke(width = 2.dp),
                                )
                            ),
                            labelProperties = labelProperties,
                            indicatorProperties = indicatorProperties,
                            popupProperties = PopupProperties(enabled = false),
                        )

                        Text(text = "CPU Speed: ${"%.2f".format(cpuHz)} MHz",modifier = Modifier.padding(top = 16.dp).padding(vertical = 16.dp))

                        LineChart(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(horizontal = 22.dp),
                            data = listOf(
                                Line(
                                    values = ramHistory,
                                    color = SolidColor(primaryLight),
                                    firstGradientFillColor = primaryLight.copy(alpha = .5f),
                                    secondGradientFillColor = Color.Green,
                                    gradientAnimationDelay = 0,
                                    drawStyle = DrawStyle.Stroke(width = 2.dp),
                                )
                            ),
                            labelProperties = labelProperties,
                            indicatorProperties = indicatorProperties,
                            popupProperties = PopupProperties(enabled = false),
                        )


                        Text(text = "RAM Usage: $ramInfo", modifier = Modifier.padding(top = 16.dp).padding(vertical = 16.dp))

                        Button(
                            onClick = { 
                                val avgHz = if (cpuHzHistory.isNotEmpty()) cpuHzHistory.average() else cpuHz
                                performanceScore = (avgHz * 1.2 + (totalram / 1024.0) * 800).toInt()
                                performanceTier = when {
                                    performanceScore > 7000 -> "Flagship Class (Extreme AI Performance)"
                                    performanceScore > 4000 -> "Premium Mid-Range (Fast Inference)"
                                    performanceScore > 2000 -> "Standard Mid-Range (Steady Performance)"
                                    else -> "Entry-Level (Slow Inference)"
                                }
                                benchmarkInfo = "Device Hardware Baseline"
                                showPerformanceDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text("Analyze Device Performance Point")
                        }

                        Button(
                            onClick = { benchMark = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text("Close")
                        }
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LocalAIBenchmarkTheme {
        MainScreen(removedAds = false, onRemoveAdsClick = {})
    }
}

@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = "ca-app-pub-5664498532905225/5281566359" // set by 31/01/2026
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

@Composable
fun ShowNotice(
    onDismissRequest: () -> Unit,
    dontShowAgainChecked: Boolean,
    onDontShowAgainCheckedChange: (Boolean) -> Unit,
    onConfirm: () -> Unit
){
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("AI Parameter Guide") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Welcome to Local AI Benchmark! ", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = stringResource(R.string.notice) +  "\n Here is a quick guide to the AI settings:", fontWeight = FontWeight.Bold)


                Text(text = "🌡️ Temperature:", fontWeight = FontWeight.SemiBold)
                Text(text = "Controls randomness. Lower values make output focused and deterministic; higher values (e.g., 1.0+) make it more creative but potentially incoherent.", fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "🎯 Top-P (Nucleus Sampling):", fontWeight = FontWeight.SemiBold)
                Text(text = "Limits the model to a cumulative probability of the most likely tokens. 0.95 means it only looks at the top 95% of candidates.", fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "🔢 Top-K:", fontWeight = FontWeight.SemiBold)
                Text(text = "Limits the model to the top K most likely next words. A value of 40 means the model only chooses from the 40 best options.", fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "📏 Max Tokens:", fontWeight = FontWeight.SemiBold)
                Text(text = "The maximum length of the response. Setting this too high may drain battery or cause long generation times.", fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "🎲 Random Seed:", fontWeight = FontWeight.SemiBold)
                Text(text = "If set, the model will produce the exact same result for the same prompt. Useful for consistent benchmarking.", fontSize = 13.sp)
                
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .clickable { onDontShowAgainCheckedChange(!dontShowAgainChecked) },
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
