package lishid.orebfuscator.hithack;

import java.util.HashMap;

import lishid.orebfuscator.OrebfuscatorConfig;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class BlockHitManager
{
    private static HashMap<Player, PlayerBlockTracking> playersBlockTrackingStatus = new HashMap<Player, PlayerBlockTracking>();
    
    public static boolean hitBlock(Player player, Block block)
    {
        if (player.getGameMode() == GameMode.CREATIVE)
            return true;
        
        PlayerBlockTracking playerBlockTracking = getPlayerBlockTracking(player);
        
        if (playerBlockTracking.isBlock(block))
        {
            return true;
        }
        
        long time = playerBlockTracking.getTimeDifference();
        playerBlockTracking.incrementHackingIndicator();
        playerBlockTracking.setBlock(block);
        playerBlockTracking.updateTime();
        
        int decrement = (int) (time / OrebfuscatorConfig.getAntiHitHackDecrementFactor());
        playerBlockTracking.decrementHackingIndicator(decrement);
        
        if(playerBlockTracking.getHackingIndicator() == OrebfuscatorConfig.getAntiHitHackMaxViolation())
            playerBlockTracking.incrementHackingIndicator(OrebfuscatorConfig.getAntiHitHackMaxViolation());
        
        
        if (playerBlockTracking.getHackingIndicator() > OrebfuscatorConfig.getAntiHitHackMaxViolation())
            return false;
        
        return true;
    }
    
    public static void breakBlock(Player player, Block block)
    {
        if (player.getGameMode() == GameMode.CREATIVE)
            return;
        
        PlayerBlockTracking playerBlockTracking = getPlayerBlockTracking(player);
        if (playerBlockTracking.isBlock(block))
        {
            playerBlockTracking.decrementHackingIndicator(2);
        }
    }
    
    private static PlayerBlockTracking getPlayerBlockTracking(Player player)
    {
        if (!playersBlockTrackingStatus.containsKey(player))
        {
            playersBlockTrackingStatus.put(player, new PlayerBlockTracking(player));
        }
        return playersBlockTrackingStatus.get(player);
    }
    
    public static void clearHistory(Player player)
    {
        playersBlockTrackingStatus.remove(player);
    }
    
    public static void clearAll()
    {
        playersBlockTrackingStatus.clear();
    }
}
