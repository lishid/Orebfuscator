package lishid.orebfuscator.utils;

public class MemoryManager
{
    public static int MaxCollectPercent = 80;
    public static int AutoCollectPercent = 90;
    public static boolean buffer = false;
    
    public static void CheckAndCollect()
    {
        long used = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long total = Runtime.getRuntime().totalMemory();
        long max = Runtime.getRuntime().maxMemory();
        
        if (((float) used * 100 / total) > AutoCollectPercent)
        {
            Collect();
        }
        else if (((float) used * 100 / max) > MaxCollectPercent)
        {
            Collect();
        }
        else
        {
            buffer = false;
        }
    }
    
    public static void Collect()
    {
        if (buffer == false)
        {
            buffer = true;
            // Orebfuscator.log("Memory is low, performing optimizations.");
            System.gc();
        }
    }
}
