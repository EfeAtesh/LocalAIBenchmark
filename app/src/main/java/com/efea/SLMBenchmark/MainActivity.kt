package com.efea.SLMBenchmark

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.efea.SLMBenchmark.ui.components.AiParameterInputs
import com.efea.SLMBenchmark.ui.components.BannerAd
import com.efea.SLMBenchmark.ui.components.BenchmarkMetricsSheet
import com.efea.SLMBenchmark.ui.components.NoticeDialog
import com.efea.SLMBenchmark.ui.components.PerformanceResultDialog
import com.efea.SLMBenchmark.ui.components.TopAppBarHeader
import com.efea.SLMBenchmark.ui.theme.LocalAIBenchmarkTheme
import com.efea.SLMBenchmark.util.BenchmarkUtils
import com.google.android.gms.ads.MobileAds
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

    LaunchedEffect(benchMark) {
        if (benchMark) {
            while (true) {
                cpuUsage = benchMarkManager.getCpuUsage()
                cpuHz = benchMarkManager.getCPUHz()
                ramInfo = benchMarkManager.getRamInfo(context)
                ramUsage = benchMarkManager.ramUsageMb
                totalram = benchMarkManager.totalRamMb

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
        NoticeDialog(
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
        PerformanceResultDialog(
            score = performanceScore,
            tier = performanceTier,
            benchmarkInfo = benchmarkInfo,
            onDismiss = { showPerformanceDialog = false },
            onShare = {
                BenchmarkUtils.shareBenchmarkResult(context, performanceScore, performanceTier, benchmarkInfo)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        TopAppBarHeader(
            removedAds = removedAds,
            onInfoClick = { showDialog = true },
            onRemoveAdsClick = onRemoveAdsClick
        )

        AiParameterInputs(
            userMsg = userMsg,
            onUserMsgChange = { userMsg = it },
            temperature = temperature,
            onTemperatureChange = {
                temperature = it
                modelManager.setTemp(it)
            },
            topP = topP,
            onTopPChange = {
                topP = it
                modelManager.setTopP(it)
            },
            topKText = topKText,
            onTopKTextChange = { input ->
                topKText = input
                modelManager.setTopK(topKText.toIntOrNull() ?: 40)
            },
            maxTokensText = maxTokensText,
            onMaxTokensTextChange = { input ->
                maxTokensText = input
                modelManager.setMaxTokens(maxTokensText.toIntOrNull() ?: 1024)
            },
            randomSeedText = randomSeedText,
            onRandomSeedTextChange = { input ->
                randomSeedText = input
                modelManager.setRandomSeed(randomSeedText.toIntOrNull())
            },
            isLoaded = isLoaded,
            onSendClick = {
                if (userMsg.isNotBlank()) {
                    benchmarkInfo = "Generating..."
                    response += "━━━━━━━━━━━━━━━━━━━━━━\nYour Message: " + userMsg + "\n━━━━━━━━━━━━━━━━━━━━━━\n"
                    modelManager.ask(userMsg, object : ModelManager.OnResultCallback {
                        override fun onResult(text: String?, durationMs: Long, tps: Double) {
                            response += "\n" + (text ?: "No response") + "\n"

                            val avgHz = if (cpuHzHistory.isNotEmpty()) cpuHzHistory.average() else cpuHz
                            val evaluation = BenchmarkUtils.calculatePerformanceScore(
                                avgCpuHz = avgHz,
                                totalRamMb = totalram,
                                tps = tps,
                                isHardwareOnly = false
                            )
                            performanceScore = evaluation.score
                            performanceTier = evaluation.tier

                            benchmarkInfo = "Latest Speed: ${"%.2f".format(tps)} t/s | Time: $durationMs ms"
                            showPerformanceDialog = true
                        }

                        override fun onError(error: String?) {
                            response += "\nError: $error \n"
                            benchmarkInfo = "Error occurred"
                        }
                    })
                }
            }
        )

        if (benchmarkInfo.isNotEmpty()) {
            Text(
                text = benchmarkInfo,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        SelectionContainer(
            modifier = Modifier
                .weight(1f)
                .padding(2.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Text(
                    text = response,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        if (!removedAds) {
            BannerAd(modifier = Modifier.fillMaxWidth())
        }

        Button(
            onClick = { benchMark = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(text = "Show BenchMark metrics")
        }

        if (benchMark) {
            BenchmarkMetricsSheet(
                onDismissRequest = { benchMark = false },
                cpuUsage = cpuUsage,
                cpuHz = cpuHz,
                ramInfo = ramInfo,
                cpuHistory = cpuHistory,
                cpuHzHistory = cpuHzHistory,
                ramHistory = ramHistory,
                onAnalyzePerformanceClick = {
                    val avgHz = if (cpuHzHistory.isNotEmpty()) cpuHzHistory.average() else cpuHz
                    val evaluation = BenchmarkUtils.calculatePerformanceScore(
                        avgCpuHz = avgHz,
                        totalRamMb = totalram,
                        isHardwareOnly = true
                    )
                    performanceScore = evaluation.score
                    performanceTier = evaluation.tier
                    benchmarkInfo = "Device Hardware Baseline"
                    showPerformanceDialog = true
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
