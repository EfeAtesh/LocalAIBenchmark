package com.efea.SLMBenchmark;

import android.app.ActivityManager;
import android.content.Context;
import java.io.RandomAccessFile;

public class BenchMark {
    private final int coreCount = Runtime.getRuntime().availableProcessors();

    private double cpuHz = 0.0;
    private double cpuUsage = 0.0;
    private double ramUsageMb = 0.0;
    private double totalRamMb = 0.0;

    public double getCPUHz() {
        long totalFreq = 0;
        int activeCores = 0;

        for (int i = 0; i < coreCount; i++) {
            long freq = readFreq("/sys/devices/system/cpu/cpu" + i + "/cpufreq/scaling_cur_freq");
            if (freq > 0) {
                totalFreq += freq;
                activeCores++;
            }
        }

        if (activeCores > 0) {
            cpuHz = (double) (totalFreq / activeCores) / 1000.0;
        } else {
            cpuHz = 0.0;
        }
        return cpuHz;
    }

    public double getCpuUsage() {
        double totalUsageValue = 0;

        for (int i = 0; i < coreCount; i++) {
            long cur = readFreq("/sys/devices/system/cpu/cpu" + i + "/cpufreq/scaling_cur_freq");
            long max = readFreq("/sys/devices/system/cpu/cpu" + i + "/cpufreq/cpuinfo_max_freq");

            if (max > 0) {
                totalUsageValue += (double) (cur * 100.0) / max;
            }
        }
        cpuUsage = coreCount > 0 ? totalUsageValue / coreCount : 0.0;
        return cpuUsage;
    }

    public String getRamInfo(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) {
            return "N/A";
        }
        
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);

        long totalMemory = memoryInfo.totalMem / (1024 * 1024);
        long availableMemory = memoryInfo.availMem / (1024 * 1024);
        long usedMemory = totalMemory - availableMemory;

        ramUsageMb = (double) usedMemory;
        totalRamMb = (double) totalMemory;

        return usedMemory + " MB / " + totalMemory + " MB";
    }

    private long readFreq(String path) {
        try (RandomAccessFile reader = new RandomAccessFile(path, "r")) {
            String line = reader.readLine();
            return line != null ? Long.parseLong(line.trim()) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public double getRamUsageMb() {
        return ramUsageMb;
    }

    public double getTotalRamMb() {
        return totalRamMb;
    }
}
