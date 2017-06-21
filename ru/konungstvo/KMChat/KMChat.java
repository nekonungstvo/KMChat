/*
 * Decompiled with CFR 0_122.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.Server
 *  org.bukkit.World
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.configuration.file.FileConfigurationOptions
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.AsyncPlayerChatEvent
 *  org.bukkit.event.player.PlayerChatTabCompleteEvent
 *  org.bukkit.event.player.PlayerJoinEvent
 *  org.bukkit.event.player.PlayerQuitEvent
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.PluginDescriptionFile
 *  org.bukkit.plugin.PluginManager
 *  org.bukkit.plugin.java.JavaPlugin
 */
package ru.konungstvo.KMChat;

import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.FileConfigurationOptions;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class KMChat
extends JavaPlugin
implements Listener {
    private Logger log = Logger.getLogger("Minecraft");
    private Map<Integer, String> nMap = new Hashtable<Integer, String>();

    public void onEnable() {
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
        this.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this);
        this.nMap.put(-3, "\u0442\u0430\u043a \u0443\u0436\u0430\u0441\u043d\u043e, \u0447\u0442\u043e \u0445\u0443\u0436\u0435 \u0443\u0436\u0435 \u043d\u0435\u043a\u0443\u0434\u0430");
        this.nMap.put(-2, "\u0443\u0436\u0430\u0441\u043d\u043e---");
        this.nMap.put(-1, "\u0443\u0436\u0430\u0441\u043d\u043e--");
        this.nMap.put(0, "\u0443\u0436\u0430\u0441\u043d\u043e-");
        this.nMap.put(1, "\u0443\u0436\u0430\u0441\u043d\u043e");
        this.nMap.put(2, "\u043f\u043b\u043e\u0445\u043e");
        this.nMap.put(3, "\u043f\u043e\u0441\u0440\u0435\u0434\u0441\u0442\u0432\u0435\u043d\u043d\u043e");
        this.nMap.put(4, "\u043d\u043e\u0440\u043c\u0430\u043b\u044c\u043d\u043e");
        this.nMap.put(5, "\u0445\u043e\u0440\u043e\u0448\u043e");
        this.nMap.put(6, "\u043e\u0442\u043b\u0438\u0447\u043d\u043e");
        this.nMap.put(7, "\u043f\u0440\u0435\u0432\u043e\u0441\u0445\u043e\u0434\u043d\u043e");
        this.nMap.put(8, "\u043b\u0435\u0433\u0435\u043d\u0434\u0430\u0440\u043d\u043e");
        this.nMap.put(9, "\u043b\u0435\u0433\u0435\u043d\u0434\u0430\u0440\u043d\u043e+");
        this.nMap.put(10, "\u043b\u0435\u0433\u0435\u043d\u0434\u0430\u0440\u043d\u043e++");
        this.nMap.put(11, "\u043b\u0435\u0433\u0435\u043d\u0434\u0430\u0440\u043d\u043e+++");
        this.nMap.put(12, "\u0422\u0410\u041a \u041b\u0415\u0413\u0415\u041d\u0414\u0410\u0420\u041d\u041e, \u0427\u0422\u041e \u041f\u041e\u041f\u0410\u0414\u0401\u0422 \u0412\u041e \u0412\u0421\u0415 \u041a\u041d\u0418\u0413\u0418 \u0420\u0415\u041a\u041e\u0420\u0414\u041e\u0412");
        this.log.info(String.format("%s is enabled!", this.getDescription().getFullName()));
    }

    public void onDisable() {
        this.log.info(String.format("%s is disabled!", this.getDescription().getFullName()));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent playerJoinEvent) {
        String string = playerJoinEvent.getPlayer().getName();
        playerJoinEvent.setJoinMessage("\u00a7e" + string + "\u00a7f \u0432\u0445\u043e\u0434\u0438\u0442 \u0432 \u0438\u0433\u0440\u0443");
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent playerQuitEvent) {
        String string = playerQuitEvent.getPlayer().getName();
        playerQuitEvent.setQuitMessage("\u00a7e" + string + "\u00a7f \u0432\u044b\u0445\u043e\u0434\u0438\u0442 \u0438\u0437 \u0438\u0433\u0440\u044b");
    }

    @EventHandler
    public void onChatTab(PlayerChatTabCompleteEvent playerChatTabCompleteEvent) {
        if (playerChatTabCompleteEvent.getChatMessage().startsWith("%%% ")) {
            Collection collection = playerChatTabCompleteEvent.getTabCompletions();
            collection.clear();
            if (playerChatTabCompleteEvent.getLastToken().startsWith("\u0443")) {
                collection.add("\u0443\u0436\u0430\u0441\u043d\u043e");
            } else if (playerChatTabCompleteEvent.getLastToken().startsWith("\u043f")) {
                if (playerChatTabCompleteEvent.getLastToken().startsWith("\u043f\u0440")) {
                    collection.add("\u043f\u0440\u0435\u0432\u043e\u0441\u0445\u043e\u0434\u043d\u043e");
                }
                if (playerChatTabCompleteEvent.getLastToken().startsWith("\u043f\u043b")) {
                    collection.add("\u043f\u043b\u043e\u0445\u043e");
                }
                if (playerChatTabCompleteEvent.getLastToken().startsWith("\u043f\u043e")) {
                    collection.add("\u043f\u043e\u0441\u0440\u0435\u0434\u0441\u0442\u0432\u0435\u043d\u043d\u043e");
                } else {
                    collection.add("\u043f\u043b\u043e\u0445\u043e");
                    collection.add("\u043f\u0440\u0435\u0432\u043e\u0441\u0445\u043e\u0434\u043d\u043e");
                    collection.add("\u043f\u043e\u0441\u0440\u0435\u0434\u0441\u0442\u0432\u0435\u043d\u043d\u043e");
                }
            } else if (playerChatTabCompleteEvent.getLastToken().startsWith("\u043d")) {
                collection.add("\u043d\u043e\u0440\u043c\u0430\u043b\u044c\u043d\u043e");
            } else if (playerChatTabCompleteEvent.getLastToken().startsWith("\u0445")) {
                collection.add("\u0445\u043e\u0440\u043e\u0448\u043e");
            } else if (playerChatTabCompleteEvent.getLastToken().startsWith("\u043e")) {
                collection.add("\u043e\u0442\u043b\u0438\u0447\u043d\u043e");
            } else {
                collection.add("\u0443\u0436\u0430\u0441\u043d\u043e");
                collection.add("\u043f\u043b\u043e\u0445\u043e");
                collection.add("\u043f\u043e\u0441\u0440\u0435\u0434\u0441\u0442\u0432\u0435\u043d\u043d\u043e");
                collection.add("\u043d\u043e\u0440\u043c\u0430\u043b\u044c\u043d\u043e");
                collection.add("\u0445\u043e\u0440\u043e\u0448\u043e");
                collection.add("\u043e\u0442\u043b\u0438\u0447\u043d\u043e");
                collection.add("\u043f\u0440\u0435\u0432\u043e\u0441\u0445\u043e\u0434\u043d\u043e");
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent asyncPlayerChatEvent) {
        Player player = asyncPlayerChatEvent.getPlayer();
        boolean bl = true;
        String string = asyncPlayerChatEvent.getMessage();
        String string2 = player.getName();
        double d = this.getConfig().getInt("range.default");
        String string3 = "";
        if (player.hasPermission("KMChat.prefix")) {
            string3 = this.getConfig().getString("adminprefix");
        }
        String string4 = String.format("%s&a%s&f: %s", string3, string2, string);
        if ((string.startsWith("d") || string.startsWith("\u043a")) && player.hasPermission("KMChat.dice")) {
            boolean bl2 = false;
            int n = 6;
            if (string.startsWith("d4") || string.startsWith("\u043a4")) {
                n = 4;
            } else if (string.startsWith("d6") || string.startsWith("\u043a4")) {
                n = 6;
            } else if (string.startsWith("d8") || string.startsWith("\u043a8")) {
                n = 8;
            } else if (string.startsWith("d10") || string.startsWith("\u043a10")) {
                n = 10;
            } else if (string.startsWith("d12") || string.startsWith("\u043a12")) {
                n = 12;
            } else if (string.startsWith("d14") || string.startsWith("\u043a14")) {
                n = 14;
            } else if (string.startsWith("d20") || string.startsWith("\u043a20")) {
                n = 20;
            } else {
                bl2 = true;
            }
            if (!bl2) {
                Random random = new Random();
                int n2 = random.nextInt();
                n2 = Math.abs(n2);
                n2 %= n;
                string4 = "&e(( " + string2 + "&e \u0431\u0440\u043e\u0441\u0430\u0435\u0442 d" + n + ". \u0412\u044b\u043f\u0430\u0434\u0430\u0435\u0442 " + ++n2 + " ))&f";
            }
        } else if (string.startsWith("4dF") || string.startsWith("%%%")) {
            int n = 2;
            try {
                string = string.substring(4);
            }
            catch (Exception exception) {
                n = 666;
                string4 = string;
            }
            if (string.startsWith("\u0443\u0436\u0430\u0441\u043d\u043e")) {
                n = 1;
            } else if (string.startsWith("\u043f\u043b\u043e\u0445\u043e")) {
                n = 2;
            } else if (string.startsWith("\u043f\u043e\u0441\u0440\u0435\u0434\u0441\u0442\u0432\u0435\u043d\u043d\u043e")) {
                n = 3;
            } else if (string.startsWith("\u043d\u043e\u0440\u043c\u0430\u043b\u044c\u043d\u043e")) {
                n = 4;
            } else if (string.startsWith("\u0445\u043e\u0440\u043e\u0448\u043e")) {
                n = 5;
            } else if (string.startsWith("\u043e\u0442\u043b\u0438\u0447\u043d\u043e")) {
                n = 6;
            } else if (string.startsWith("\u043f\u0440\u0435\u0432\u043e\u0441\u0445\u043e\u0434\u043d\u043e")) {
                n = 7;
            } else if (string.startsWith("\u043b\u0435\u0433\u0435\u043d\u0434\u0430\u0440\u043d\u043e")) {
                n = 8;
            } else {
                n = 2;
                string = "\u041f\u041b\u041e\u0425\u041e";
            }
            Random random = new Random();
            String string5 = "";
            int n3 = random.nextInt();
            for (int i = 0; i < 4; ++i) {
                n3 = random.nextInt();
                n3 = Math.abs(n3);
                string5 = (n3 = n3 % 3 - 1) < 0 ? string5 + "-" : (n3 > 0 ? string5 + "+" : string5 + "=");
                n += n3;
            }
            String string6 = "";
            string6 = this.nMap.get(n);
            if (n != 666) {
                string4 = "&e(( " + string2 + "&e \u0431\u0440\u043e\u0441\u0430\u0435\u0442 4dF (" + string5 + ") \u043e\u0442 " + string + ". \u0420\u0435\u0437\u0443\u043b\u044c\u0442\u0430\u0442: " + string6 + " ))&f";
            }
        } else if ((string.startsWith("#") || string.startsWith("\u2116")) && player.hasPermission("KMChat.dm")) {
            string = string.substring(1);
            d = this.getConfig().getInt("range.dm");
            string4 = "&e***" + string + "***";
        } else if (string.startsWith("=#") && player.hasPermission("KMChat.dm")) {
            string = string.substring(2);
            d = this.getConfig().getInt("range.closedm");
            string4 = "&e***" + string + "***";
        } else if ((string.startsWith("!#") || string.startsWith("\u0434\u0430\u043b\u2116")) && player.hasPermission("KMChat.dm")) {
            string = string.substring(2);
            d = this.getConfig().getInt("range.fardm");
            string4 = "&e***" + string + "***";
        } else if (string.startsWith("*") && player.hasPermission("KMChat.me")) {
            string = string.substring(1);
            d = this.getConfig().getInt("range.me");
            string4 = String.format("* %s&a%s&f %s", string3, string2, string);
        } else if ((string.startsWith("@@@") || string.startsWith("===")) && player.hasPermission("KMChat.whisper")) {
            string = string.substring(3);
            d = this.getConfig().getInt("range.strongwhisper");
            string4 = String.format("%s&a%s&f (\u043f\u0440\u043e \u0441\u0435\u0431\u044f): %s", string3, string2, string);
        } else if ((string.startsWith("@@") || string.startsWith("==")) && player.hasPermission("KMChat.whisper")) {
            string = string.substring(2);
            d = this.getConfig().getInt("range.whisper");
            string4 = String.format("%s&a%s&f (\u0448\u0435\u043f\u0447\u0435\u0442): %s", string3, string2, string);
        } else if ((string.startsWith("@") || string.startsWith("=")) && player.hasPermission("KMChat.whisper")) {
            string = string.substring(1);
            d = this.getConfig().getInt("range.weakwhisper");
            string4 = String.format("%s&a%s&f (\u0432\u043f\u043e\u043b\u0433\u043e\u043b\u043e\u0441\u0430): %s", string3, string2, string);
        } else if (string.startsWith("!!!") && player.hasPermission("KMChat.shout")) {
            string = string.substring(3);
            d = this.getConfig().getInt("range.strongshout");
            string4 = String.format("%s&a%s&f (\u043e\u0440\u0451\u0442): %s", string3, string2, string);
        } else if (string.startsWith("!!") && player.hasPermission("KMChat.shout")) {
            string = string.substring(2);
            d = this.getConfig().getInt("range.shout");
            string4 = String.format("%s&a%s&f (\u043a\u0440\u0438\u0447\u0438\u0442): %s", string3, string2, string);
        } else if (string.startsWith("!") && player.hasPermission("KMChat.shout")) {
            string = string.substring(1);
            d = this.getConfig().getInt("range.weakshout");
            string4 = String.format("%s&a%s&f (\u043f\u0440\u0438\u043a\u0440\u0438\u043a\u0438\u0432\u0430\u0435\u0442): %s", string3, string2, string);
        } else if (string.startsWith("?") && player.hasPermission("KMChat.global")) {
            bl = false;
            string = string.substring(1);
            string4 = String.format("%s&a%s&f: &b(( %s ))&f", string3, string2, string);
        } else if (string.startsWith("_")) {
            string = "((" + string.substring(1) + "))";
        }
        if (string.startsWith("((") && string.endsWith("))")) {
            string4 = String.format("%s&a%s&f (OOC): &d%s&f", string3, string2, string);
        }
        string4 = string4.replaceAll("&([a-z0-9])", "\u00a7$1");
        string4 = string4.replaceAll("%", "%%");
        asyncPlayerChatEvent.setFormat(string4);
        asyncPlayerChatEvent.setMessage(string);
        if (bl) {
            asyncPlayerChatEvent.getRecipients().clear();
            asyncPlayerChatEvent.getRecipients().addAll(this.getLocalRecipients(player, string4, d));
        }
    }

    protected List<Player> getLocalRecipients(Player player, String string, double d) {
        Location location = player.getLocation();
        LinkedList<Player> linkedList = new LinkedList<Player>();
        double d2 = Math.pow(d, 2.0);
        for (Player player2 : Bukkit.getServer().getOnlinePlayers()) {
            if (player2.hasPermission("KMChat.admin")) {
                if (!player2.getWorld().equals((Object)player.getWorld())) {
                    player2.sendMessage(string.replaceAll("\u00a7f", "\u00a77"));
                    continue;
                }
                if (location.distanceSquared(player2.getLocation()) > d2) {
                    player2.sendMessage(string.replaceAll("\u00a7f", "\u00a77"));
                    continue;
                }
                linkedList.add(player2);
                continue;
            }
            if (!player2.getWorld().equals((Object)player.getWorld()) || location.distanceSquared(player2.getLocation()) > d2) continue;
            linkedList.add(player2);
        }
        return linkedList;
    }
}
