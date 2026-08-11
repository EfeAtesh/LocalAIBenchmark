# LocalAIBenchmark

[![License](https://img.shields.io/badge/License-MIT-blue.svg)]()
[![Platform](https://img.shields.io/badge/Platform-Android-lightgrey)]()
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9%2B-orange)]()

**LocalAIBenchmark** is an Android application designed for tech reviewers, developers, and AI enthusiasts to evaluate On-Device AI (Edge AI) inference performance on smartphones. The app runs controlled Small Language Model (SLM) benchmarks using Google's **Gemma 3 1B** model via Google MediaPipe GenAI LLM Inference API and reports real-time system metrics (CPU clock speed, CPU usage, RAM utilization, and tokens-per-second throughput).

---

## Important: Model File Prerequisite

Due to file size limitations, the model `.task` binary is **NOT included** directly in this repository.

### Downloading the Model
1. You must manually download the **Gemma 3 1B INT4** model file (`gemma3-1b-it-int4.task`).
   - Official source: [Kaggle - Gemma MediaPipe Models](https://www.kaggle.com/models/google/gemma-3/tfLite) or [Hugging Face](https://huggingface.co/google/gemma-3-1b-it-int4-gpu).
2. Save the file with the exact name:
   ```text
   gemma3-1b-it-int4.task
   ```
3. Place the downloaded `.task` file into one of the following asset locations in the project:
   - Internal Assets: `app/src/main/assets/gemma3-1b-it-int4.task`
   - **OR** Asset Pack: `model_assets/src/main/assets/gemma3-1b-it-int4.task`

---

## Core Features

- **On-Device LLM Benchmark**: Runs local inference using MediaPipe GenAI and calculates tokens per second (t/s) and generation latency (ms).
- **Real-Time Hardware Metrics**: Monitors CPU frequency, active CPU core usage, and RAM consumption in real-time with dynamic Compose Line Charts.
- **Dynamic Performance Scoring**: Evaluates your device hardware & inference throughput to assign a performance score and device class rating (Flagship, Premium Mid-Range, Standard Mid-Range, Entry-Level).
- **AI Parameter Controls**: Adjust Temperature, Top-P (Nucleus Sampling), Top-K, Max Tokens, and Random Seed dynamically.
- **Share Benchmark Results**: Generate and share summary benchmark cards directly to other apps.

---

## Tech Stack & Clean Architecture

- **Language**: Kotlin & Java
- **UI Framework**: Jetpack Compose with Material 3 Design
- **Charts**: Compose Charts (`ir.ehsannarmani.compose_charts`)
- **AI Engine**: Google MediaPipe GenAI Tasks (`com.google.mediapipe:tasks-genai`)
- **Asset Management**: Play Asset Delivery (`com.google.android.play:asset-delivery-ktx`)
- **Monetization**: Google Mobile Ads & Android In-App Billing

---

## How to Build & Run

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/EfeAtesh/LocalAIBenchmark.git
   cd LocalAIBenchmark
   ```

2. **Add Model Asset**:
   - Create the directory `app/src/main/assets/` if it doesn't exist.
   - Copy `gemma3-1b-it-int4.task` into `app/src/main/assets/`.

3. **Build via Android Studio or Gradle**:
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run on a Physical Device**:
   - For realistic LLM performance and hardware metric readings, running on a physical Android device is recommended.

---

## Play Store

- **Download on Google Play**: [Local AI Benchmark](https://play.google.com/store/apps/details?id=com.efea.SLMBenchmark)
- Check out [LibreChat](https://play.google.com/store/apps/details?id=com.efeates.localllm) for an on-device chat client.

---

## License

This project is licensed under the MIT License - see the `LICENSE_Version3` file for details.
