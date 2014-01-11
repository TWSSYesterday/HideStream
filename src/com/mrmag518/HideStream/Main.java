package com.mrmag518.HideStream;

import com.mrmag518.HideStream.Files.Config;
import com.mrmag518.HideStream.Files.StreamDB;
import com.mrmag518.HideStream.Util.Log;
import com.mrmag518.HideStream.Util.MetricsLite;
import com.mrmag518.HideStream.Util.Updater;

import java.io.File;
import java.io.IOException;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    public static String latestUpdate = "null";
    public static String prefix = "§f[§3HideStream§f] ";
    public static Main instance = null;
    
    @Override
    public void onDisable() {
        Log.info("Version " + getDescription().getVersion() + " disabled.");
    }
    
    @Override
    public void onEnable() {
        instance = this;
        new EventManager().register();
        if(!getDataFolder().exists()) getDataFolder().mkdir();
        Config.init();
        if(Config.PPT_ENABLED) StreamDB.init();
        getCommand("hidestream").setExecutor(new Commands());
        
        if(Config.UPDATE_CHECKING) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    updateCheck();
                }
            }).start();
            
            getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
                @Override
                public void run() {
                    updateCheck();
                }
            }, 20*60*60*4, 20*60*60*4);
        }
        
        try {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        } catch (IOException e) {}
        Log.info("Version " + getDescription().getVersion() + " enabled.");
    }
    
    public File getDataFile() {
        return getFile();
    }
    
    private void updateCheck() {
        Log.info("Running updater ..");
        
        try {
            Updater updater = new Updater(this, 37123, getFile(), Updater.UpdateType.NO_DOWNLOAD, false);

            Updater.UpdateResult result = updater.getResult();
            switch(result) {
                case NO_UPDATE:
                    Log.info("No update was found.");
                    break;
                case FAIL_DBO:
                    Log.warning("Failed to contact dev.bukkkit.org!");
                    break;
                case UPDATE_AVAILABLE:
                    latestUpdate = updater.getLatestName();
                    Log.info(" ------------- ");
                    Log.info("A new version has been found! (" + latestUpdate + ")");
                    Log.info("You are currently running " + getDescription().getFullName());
                    Log.info(" ------------- ");
                    break;
                case DISABLED:
                    Log.info("Updater checker has been disabled in the updater config.");
                    break;
                case FAIL_APIKEY:
                    Log.warning("The API key you have provided is incorrect!");
                    break;
            }
        } catch(RuntimeException e) {
            Log.warning("Failed to establish a connection to dev.bukkit.org!");
        }
    }
}