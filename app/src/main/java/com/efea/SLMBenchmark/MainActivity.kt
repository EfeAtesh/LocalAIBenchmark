package com.efea.SLMBenchmark

import BenchMark
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.android.billingclient.api.*
import com.efea.SLMBenchmark.ui.theme.LocalAIBenchmarkTheme
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
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

    var topk by remember { mutableIntStateOf(40) }
    var topp by remember { mutableFloatStateOf(0.95f) }
    var maxTokens by remember { mutableStateOf("1024") }
    var randomseed by remember { mutableStateOf<Integer>(0) }


    var showDialog by remember {
        mutableStateOf(!sharedPrefs.getBoolean("hide_info", false))
    }
    var dontShowAgainChecked by remember { mutableStateOf(false) }
    var benchMark by remember { mutableStateOf(false) }

    // Benchmark States
    var cpuUsage by remember { mutableStateOf(0) }
    var cpuHz by remember { mutableStateOf("N/A") }
    var ramInfo by remember { mutableStateOf("N/A") }

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
                cpuUsage = benchMarkManager.cpuUsage
                cpuHz = benchMarkManager.cpuHz
                ramInfo = benchMarkManager.getRAMINFO(context)
                delay(500) // Refresh every second
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

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
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


            if (!removedAds) {
                IconButton(
                    modifier = Modifier.padding(start = 8.dp),
                    onClick = onRemoveAdsClick
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ad_off_24dp_ffffff_fill0_wght400_grad0_opsz24),
                        contentDescription = "Remove Ads"
                    )
                }
            }
        }

        OutlinedTextField(
            value = userMsg,
            onValueChange = { userMsg = it },
            label = { Text("Enter your message") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            enabled = isLoaded
        )

        Text(text = "Temperature Point: ${"%.2f".format(temperature)}",
            modifier = Modifier.padding(top = 8.dp))

        Slider(
            value = temperature,
            onValueChange = {
                temperature = it
                modelManager.setTemp(it)
            },
            valueRange = 0.1f..5.0f,
            enabled = isLoaded
        )
        Text(text = "TopP Point: ${"%.2f".format(topp)}",
            modifier = Modifier.padding(top = 8.dp))

        Slider(
            value = topp,
            onValueChange = {
                topp = it
                modelManager.setTopP(it)
            },
            valueRange = 0.1f..1.0f,
            enabled = isLoaded
        )
        Text(text = "TopK Point: $topk",
            modifier = Modifier.padding(top = 8.dp))

        Slider(
            value = topk.toFloat(),
            onValueChange = {
                topk = it.toInt()
                modelManager.setTopK(it.toInt())
            },
            valueRange = 1f..100f,
            enabled = isLoaded
        )

        OutlinedTextField(
            value = maxTokens,
            onValueChange = { 
                maxTokens = it.filter { char -> char.isDigit() }
                modelManager.setMaxTokens(maxTokens.toIntOrNull() ?: 1024)
            },
            label = { Text("Max Tokens") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )


        Button(
            onClick = {
                if (userMsg.isNotBlank()) {
                    response += "_________________\nYour Message: "
                    modelManager.ask(userMsg, object : ModelManager.OnResultCallback {
                        override fun onResult(text: String?) {
                            response += "\n"+userMsg +"\n_________________\n" + (text ?: "No response")
                        }
                        override fun onError(error: String?) {
                            response += "\nError: $error \n"
                        }
                    })
                }
            },
            modifier = Modifier.padding(vertical = 8.dp),
            enabled = isLoaded
        ) {
            Text(text = if (isLoaded) "Send message" else "Loading model...")
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
                    modifier = Modifier.padding(top = 16.dp)
                )}

        }

        if (!removedAds) {
            BannerAd(modifier = Modifier.fillMaxWidth())
        }

        if (true) {
            Button(
                onClick = { benchMark = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),

                ) {
                Text(text = "Show BenchMark metrics")
            }
        }

        if (benchMark) {
            ModalBottomSheet(
                onDismissRequest = { benchMark = false },
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFFE0E0E0)) },
                content = {
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)) {
                        Text(text = "Real-time Metrics", fontSize = 20.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())



                        Text(text = "CPU Usage: $cpuUsage%", modifier = Modifier.padding(top = 16.dp))

                        Text(text = "CPU Speed: $cpuHz")

                        Text(text = "RAM Usage: $ramInfo", modifier = Modifier.padding(top = 8.dp))



                        Button(
                            onClick = { benchMark = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp)
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
                adUnitId = "ca-app-pub-3940256099942544/6300978111" // Test ID
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
        title = { Text("Notice") },
        text = {
            Column {
                Text((stringResource(R.string.notice)))
                Row(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .clickable { onDontShowAgainCheckedChange(!dontShowAgainChecked) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = dontShowAgainChecked,
                        onCheckedChange = onDontShowAgainCheckedChange
                    )
                    Text(text = "Don't show it again", modifier = Modifier.padding(start = 8.dp))
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Dismiss")
            }
        }
    )
}
