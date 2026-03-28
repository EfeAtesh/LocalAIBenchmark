# LocalAIBenchmark

[![license](https://img.shields.io/badge/license-MIT-blue.svg)]()
[![platform](https://img.shields.io/badge/platform-Android-lightgrey)]()
[![kotlin](https://img.shields.io/badge/kotlin-1.8%2B-orange)]()

LocalAIBenchmark is an Android app that helps tech reviewers and engineers assess Edge AI (on-device AI) compatibility and performance. The app runs controlled Small Language Model (SLM) inference scenarios using Google Gemma (via MediaPipe GenAI LLM inference) and reports results as benchmark points that can be exported and shared.

Core idea
- Provide reproducible, reviewer-friendly device benchmark points for on-device language model inference.
- Measure latency, throughput, memory and runtime behavior under representative prompts and inference settings.
- Target audience: tech reviewers, device testers, QA engineers evaluating on-device AI readiness.

What the app actually does (high level)
- Detects device capabilities (CPU cores/architecture, RAM, NNAPI/Vulkan/accelerator availability)
- Loads an on-device LLM task packaged via Play Asset Delivery (asset pack name: `model_assets`) or internal assets
- Runs standardized prompts/scenarios with warmup + measured iterations
- Measures and displays latency percentiles, throughput, memory usage, and other runtime metadata
- Exports a “benchmark point” (JSON + human-readable summary) for later aggregation
- Provides an in-app UI (Jetpack Compose) that shows settings and contextual guidance for AI parameters

Important repo / runtime details discovered in source
- Package id: com.efea.SLMBenchmark
- Android minimum SDK: 24 (minSdk in app/build.gradle.kts)
- Current versionName (in app/build.gradle.kts): 1.2.1
- Model asset pipeline:
  - Uses Play Asset Delivery (asset pack `model_assets`) and also attempts to load a bundled asset.
  - Model file checked/used: `gemma3-1b-it-int4.task` (see ModelManager.java)
  - MediaPipe GenAI LLM Inference API is used: `com.google.mediapipe.tasks.genai.llminference.LlmInference`
- Bench measurement helpers:
  - BenchMark.java reads CPU frequencies per-core and reports CPU/RAM statistics (reads /sys/devices/system/cpu/... and ActivityManager memory info)
- Dependencies of note (from app/build.gradle.kts):
  - com.google.android.play:asset-delivery-ktx
  - com.google.mediapipe:tasks-genai (Gemma integration)
  - com.google.android.gms:play-services-ads
  - Android billing client (optional paid features)
  - Compose, compose charts library for visualizing metrics

UI & settings (in-app guidance)
The app shows a notice and an "AI Parameter Guide" alert dialog containing the exact user-facing guidance used in the app. Key texts (copied from the app UI) include:

- "Welcome to Local AI Benchmark!"
- Notice string resource (R.string.notice): "This app is intented for devices' benchmark on measuring its performance when it comes to run a Small Language Model."

AI setting explanations shown in-app:
- 🌡️ Temperature:
  - "Controls randomness. Lower values make output focused and deterministic; higher values (e.g., 1.0+) make it more creative but potentially incoherent."
- 🎯 Top-P (Nucleus Sampling):
  - "Limits the model to a cumulative probability of the most likely tokens. 0.95 means it only looks at the top 95% of candidates."
- 🔢 Top-K:
  - "Limits the model to the top K most likely next words. A value of 40 means the model only chooses from the 40 best options."
- 📏 Max Tokens:
  - "The maximum length of the response. Setting this too high may drain battery or cause long generation times."
- 🎲 Random Seed:
  - "If set, the model will produce the exact same result for the same prompt. Useful for consistent benchmarking."

Play Store listing
- Play Store link visible in the code/comments: https://play.google.com/store/apps/details?id=com.efea.SLMBenchmark

How to run (developer)
1. Prerequisites
   - Android Studio (latest stable)
   - Java JDK 11+
   - Android SDK and a device or emulator (physical device recommended for accurate edge metrics)

2. Build & run
   - Open the project in Android Studio
   - Let Gradle sync
   - Run on a physical device (recommended) or emulator
   - If you plan to use the model via Play Asset Delivery, ensure asset pack delivery is set up or include the model in internal assets for testing

3. In-app benchmark flow
   - Launch app on device
   - The app shows a welcome/notice and AI settings guide
   - Select a benchmark scenario and start — the app performs warm-up runs followed by measurement runs and shows results and export options

Model loading behavior (summary)
- The app first attempts to find `gemma3-1b-it-int4.task` in internal assets. If not present it uses Play Asset Delivery (`model_assets`) to get the model pack and then loads the model file from the asset pack path.
- ModelManager.java handles the copy/verification and starts the MediaPipe GenAI LlmInference session on the model file.

Export & reports
- Benchmarks produce a “benchmark point” object that contains:
  - Device info and runtime configuration
  - Latency percentiles (median / p95 / p99)
  - Throughput metrics (requests/sec, tokens/sec)
  - Memory usage snapshots
  - Raw sample outputs for sanity checks
- UI allows JSON export and share options (PNG/Markdown summaries may be available)

Privacy & telemetry
- Any remote sharing or telemetry should be opt-in (verify in the app settings). The app shows explicit export/share options.

Contributing
- Open an issue for feature requests or device-adapter support
- Fork and create a feature branch for changes
- Include reproducible example scenarios or tests for significant additions
- Document scenario formats and model adapter behavior

Notes & acknowledgements
- Uses Google Gemma via MediaPipe GenAI tasks
- Uses Play Asset Delivery for packageable large model assets
- Includes ads & in-app billing (configurable/optional for users)

Maintainer
- Maintained by EfeAtesh — open an issue on the repository for questions or feature requests.
