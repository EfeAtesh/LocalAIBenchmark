package com.efea.SLMBenchmark.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.efea.SLMBenchmark.R

@Composable
fun TopAppBarHeader(
    removedAds: Boolean,
    onInfoClick: () -> Unit,
    onRemoveAdsClick: () -> Unit
) {
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
            fontSize = 24.sp
        )

        IconButton(onClick = onInfoClick) {
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
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.coffee_cup_svgrepo_com),
                    contentDescription = "Buy him a coffee"
                )
            }
        }
    }
}
