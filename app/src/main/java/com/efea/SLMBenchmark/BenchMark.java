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


    private int totalUsage = 0;
    private int coreCount = Runtime.getRuntime().availableProcessors();


    public String getCPUHz(){


        try {
            RandomAccessFile reader2 = new RandomAccessFile("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq", "r");

            String line2 = reader2.readLine();
            reader2.close();

            long freqinMegacycles2 = Long.parseLong(line2) / 1000;


            return freqinMegacycles2 + " MHz";


        } catch (Exception e) {
            return "N/A";
        }


    }
    public int getCpuUsage() {
        int totalUsage = 0;
        int coreCount = Runtime.getRuntime().availableProcessors();

        for (int i = 0; i < coreCount; i++) {
            long cur = readFreq("/sys/devices/system/cpu/cpu" + i + "/cpufreq/scaling_cur_freq");
            long max = readFreq("/sys/devices/system/cpu/cpu" + i + "/cpufreq/cpuinfo_max_freq");

            if (max > 0) {
                totalUsage += (int) ((cur * 100) / max);
            }
        }
        return totalUsage / coreCount; // Returns average percentage across cores
    }

    private long readFreq(String path) {
        try (RandomAccessFile reader = new RandomAccessFile(path, "r")) {
            String line = reader.readLine();
            return line != null ? Long.parseLong(line) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public String getRAMINFO(Context context){

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);

        long totalMemory = memoryInfo.totalMem / (1024 * 1024);
        long availableMemory = memoryInfo.availMem / (1024 * 1024);
        long usedMemory = totalMemory - availableMemory;

        return usedMemory + " MB / " + totalMemory + " MB";


    }



}
