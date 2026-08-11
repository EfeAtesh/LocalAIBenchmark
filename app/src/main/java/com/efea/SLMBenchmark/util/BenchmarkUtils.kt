package com.efea.SLMBenchmark.util

import android.content.Context
import android.content.Intent
import android.os.Build

data class PerformanceEvaluation(
    val score: Int,
    val tier: String
)

object BenchmarkUtils {

    fun calculatePerformanceScore(
        avgCpuHz: Double,
        totalRamMb: Double,
        tps: Double = 0.0,
        isHardwareOnly: Boolean = false
    ): PerformanceEvaluation {
        val score = if (isHardwareOnly) {
            (avgCpuHz * 1.2 + (totalRamMb / 1024.0) * 800).toInt()
        } else {
            (avgCpuHz * 1.2 + (totalRamMb / 1024.0) * 800 + (tps * 500)).toInt()
        }

        val tier = when {
            score > (if (isHardwareOnly) 7000 else 8000) -> "Flagship Class (Extreme AI Performance)"
            score > (if (isHardwareOnly) 4000 else 5000) -> "Premium Mid-Range (Fast Inference)"
            score > (if (isHardwareOnly) 2000 else 3000) -> "Standard Mid-Range (Steady Performance)"
            else -> "Entry-Level (Slow Inference)"
        }

        return PerformanceEvaluation(score, tier)
    }

    fun shareBenchmarkResult(context: Context, score: Int, tier: String, info: String) {
        val model = Build.MODEL
        val manufacturer = Build.MANUFACTURER
        val shareText = """
            Local AI Benchmark Result
            Device: $manufacturer $model
            Performance Score: $score
            Rating: $tier
            $info
            
            Download the app to test your device's AI power!
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Local AI Benchmark Result")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, "Share via"))
    }
}
