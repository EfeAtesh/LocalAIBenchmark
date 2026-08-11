package com.efea.SLMBenchmark.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.efea.SLMBenchmark.ui.theme.primaryLight
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.PopupProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BenchmarkMetricsSheet(
    onDismissRequest: () -> Unit,
    cpuUsage: Double,
    cpuHz: Double,
    ramInfo: String,
    cpuHistory: List<Double>,
    cpuHzHistory: List<Double>,
    ramHistory: List<Double>,
    onAnalyzePerformanceClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Real-time Metrics",
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

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
                Text(
                    text = "CPU Usage: ${"%.2f".format(cpuUsage)}%",
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .padding(vertical = 16.dp)
                )

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
                Text(
                    text = "CPU Speed: ${"%.2f".format(cpuHz)} MHz",
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .padding(vertical = 16.dp)
                )

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

                Text(
                    text = "RAM Usage: $ramInfo",
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .padding(vertical = 16.dp)
                )

                Button(
                    onClick = onAnalyzePerformanceClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Analyze Device Performance Point")
                }

                Button(
                    onClick = onDismissRequest,
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
