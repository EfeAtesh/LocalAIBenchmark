package com.efea.SLMBenchmark;

import android.app.ActivityManager;
import android.content.Context;
import java.io.RandomAccessFile;

public class BenchMark {
    // This class will provide
     /*
     CPU USAGE & Clock Speed
     Ram Usage & Clock Speed
     Gpu Usage & Clock Speed

     In real time

     thus they will be each graphed on modalbottom sheet seperately

     functions
     getCPUHz()
     getCpuUsage()
     getRAMINFO()


      */
    private int coreCount = Runtime.getRuntime().availableProcessors();

    // values just in number
    protected double cpuHz = 0.0;
    protected double cpuUsage = 0.0;
    protected double ramUsage;
    protected double totalram;

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
            cpuHz = (double) (totalFreq / activeCores) / 1000.0; // Average in MHz
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
        cpuUsage = totalUsageValue / coreCount;
        return cpuUsage;
    }

    private long readFreq(String path) {
        try (RandomAccessFile reader = new RandomAccessFile(path, "r")) {
            String line = reader.readLine();
            return line != null ? Long.parseLong(line) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public String getRAMINFO(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);

        long totalMemory = memoryInfo.totalMem / (1024 * 1024);
        long availableMemory = memoryInfo.availMem / (1024 * 1024);
        long usedMemory = totalMemory - availableMemory;

        ramUsage = (double) usedMemory;
        totalram = (double) totalMemory;

        return usedMemory + " MB / " + totalMemory + " MB";
    }
}
