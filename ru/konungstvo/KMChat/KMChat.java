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
        this.nMap.put(-3, "так ужасно, что хуже уже некуда");
        this.nMap.put(-2, "ужасно---");
        this.nMap.put(-1, "ужасно--");
        this.nMap.put(0, "ужасно-");
        this.nMap.put(1, "ужасно");
        this.nMap.put(2, "плохо");
        this.nMap.put(3, "посредственно");
        this.nMap.put(4, "нормально");
        this.nMap.put(5, "хорошо");
        this.nMap.put(6, "отлично");
        this.nMap.put(7, "превосходно");
        this.nMap.put(8, "легендарно");
        this.nMap.put(9, "легендарно+");
        this.nMap.put(10, "легендарно++");
        this.nMap.put(11, "легендарно+++");
        this.nMap.put(12, "ТАК ЛЕГЕНДАРНО, ЧТО ПОПАДЁТ ВО ВСЕ КНИГИ РЕКОРДОВ");
        this.log.info(String.format("%s is enabled!", this.getDescription().getFullName()));
    }

    public void onDisable() {
        this.log.info(String.format("%s is disabled!", this.getDescription().getFullName()));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent playerJoinEvent) {
        String string = playerJoinEvent.getPlayer().getName();
        playerJoinEvent.setJoinMessage("§e" + string + "§f входит в игру");
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent playerQuitEvent) {
        String string = playerQuitEvent.getPlayer().getName();
        playerQuitEvent.setQuitMessage("§e" + string + "§f выходит из игры");
    }

    @EventHandler
    public void onChatTab(PlayerChatTabCompleteEvent playerChatTabCompleteEvent) {
        if (playerChatTabCompleteEvent.getChatMessage().startsWith("% ")) {
            Collection collection = playerChatTabCompleteEvent.getTabCompletions();
            collection.clear();
            if (playerChatTabCompleteEvent.getLastToken().startsWith("у")) {
                collection.add("ужасно");
            } else if (playerChatTabCompleteEvent.getLastToken().startsWith("п")) {
                if (playerChatTabCompleteEvent.getLastToken().startsWith("пр")) {
                    collection.add("превосходно");
                }
                if (playerChatTabCompleteEvent.getLastToken().startsWith("пл")) {
                    collection.add("плохо");
                }
                if (playerChatTabCompleteEvent.getLastToken().startsWith("по")) {
                    collection.add("посредственно");
                } else {
                    collection.add("плохо");
                    collection.add("посредственно");
                    collection.add("превосходно");
                }
            } else if (playerChatTabCompleteEvent.getLastToken().startsWith("н")) {
                collection.add("нормально");
            } else if (playerChatTabCompleteEvent.getLastToken().startsWith("х")) {
                collection.add("хорошо");
            } else if (playerChatTabCompleteEvent.getLastToken().startsWith("о")) {
                collection.add("отлично");
            } else {
                collection.add("ужасно");
                collection.add("плохо");
                collection.add("посредственно");
                collection.add("нормально");
                collection.add("хорошо");
                collection.add("отлично");
                collection.add("превосходно");
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
        if ((string.startsWith("d") || string.startsWith("к")) && player.hasPermission("KMChat.dice")) {
            boolean bl2 = false;
            int n = 6;
            if (string.startsWith("d4") || string.startsWith("к4")) {
                n = 4;
            } else if (string.startsWith("d6") || string.startsWith("к6")) {
                n = 6;
            } else if (string.startsWith("d8") || string.startsWith("к8")) {
                n = 8;
            } else if (string.startsWith("d10") || string.startsWith("к10")) {
                n = 10;
            } else if (string.startsWith("d12") || string.startsWith("к12")) {
                n = 12;
            } else if (string.startsWith("d14") || string.startsWith("к14")) {
                n = 14;
            } else if (string.startsWith("d20") || string.startsWith("к20")) {
                n = 20;
            } else {
                bl2 = true;
            }
            if (!bl2) {
                Random random = new Random();
                int n2 = random.nextInt();
                n2 = Math.abs(n2);
                n2 %= n;
                string4 = "&e(( " + string2 + "&e бросает d" + n + ". Выпадает " + ++n2 + " ))&f";
            }
        } else if (string.startsWith("% ")) {
            int n = 2;
            try {
                string = string.substring(2);
            }
            catch (Exception exception) {
                n = 666;
                string4 = string;
            }
            if (string.startsWith("ужасно")) {
                n = 1;
            } else if (string.startsWith("плохо")) {
                n = 2;
            } else if (string.startsWith("посредственно")) {
                n = 3;
            } else if (string.startsWith("нормально")) {
                n = 4;
            } else if (string.startsWith("хорошо")) {
                n = 5;
            } else if (string.startsWith("отлично")) {
                n = 6;
            } else if (string.startsWith("превосходно")) {
                n = 7;
            } else if (string.startsWith("легендарно")) {
                n = 8;
            } else {
                n = 2;
                string = "ПЛОХО";
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
                string4 = "&e(( " + string2 + "&e бросает 4dF (" + string5 + ") от " + string + ". Результат: " + string6 + " ))&f";
            }
        } else if (string.startsWith("===% ")) {
            int n = 2;
            try {
                string = string.substring(5);
            }
            catch (Exception exception) {
                n = 666;
                string4 = string;
            }
            if (string.startsWith("ужасно")) {
                n = 1;
            } else if (string.startsWith("плохо")) {
                n = 2;
            } else if (string.startsWith("посредственно")) {
                n = 3;
            } else if (string.startsWith("нормально")) {
                n = 4;
            } else if (string.startsWith("хорошо")) {
                n = 5;
            } else if (string.startsWith("отлично")) {
                n = 6;
            } else if (string.startsWith("превосходно")) {
                n = 7;
            } else if (string.startsWith("легендарно")) {
                n = 8;
            } else {
                n = 2;
                string = "ПЛОХО";
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
                d = this.getConfig().getInt("range.weakwhisper");
                string4 = "&e(( " + string2 + "&e неслышно бросает 4dF (" + string5 + ") от " + string + ". Результат: " + string6 + " ))&f";
            }            
        } else if ((string.startsWith("#") || string.startsWith("№")) && player.hasPermission("KMChat.dm")) {
            string = string.substring(1);
            d = this.getConfig().getInt("range.dm");
            string4 = "&e***" + string + "***";
        } else if (string.startsWith("=#") && player.hasPermission("KMChat.dm")) {
            string = string.substring(2);
            d = this.getConfig().getInt("range.closedm");
            string4 = "&e***" + string + "***";
        } else if ((string.startsWith("!#") || string.startsWith("дал№")) && player.hasPermission("KMChat.dm")) {
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
            string4 = String.format("%s&a%s&f (про себя): %s", string3, string2, string);
        } else if ((string.startsWith("@@") || string.startsWith("==")) && player.hasPermission("KMChat.whisper")) {
            string = string.substring(2);
            d = this.getConfig().getInt("range.whisper");
            string4 = String.format("%s&a%s&f (шепчет): %s", string3, string2, string);
        } else if ((string.startsWith("@") || string.startsWith("=")) && player.hasPermission("KMChat.whisper")) {
            string = string.substring(1);
            d = this.getConfig().getInt("range.weakwhisper");
            string4 = String.format("%s&a%s&f (вполголоса): %s", string3, string2, string);
        } else if (string.startsWith("!!!") && player.hasPermission("KMChat.shout")) {
            string = string.substring(3);
            d = this.getConfig().getInt("range.strongshout");
            string4 = String.format("%s&a%s&f (орёт): %s", string3, string2, string);
        } else if (string.startsWith("!!") && player.hasPermission("KMChat.shout")) {
            string = string.substring(2);
            d = this.getConfig().getInt("range.shout");
            string4 = String.format("%s&a%s&f (кричит): %s", string3, string2, string);
        } else if (string.startsWith("!") && player.hasPermission("KMChat.shout")) {
            string = string.substring(1);
            d = this.getConfig().getInt("range.weakshout");
            string4 = String.format("%s&a%s&f (прикрикивает): %s", string3, string2, string);
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
        string4 = string4.replaceAll("&([a-z0-9])", "§$1");
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
                    string = string.replaceAll("§e", "§7");
                    player2.sendMessage(string.replaceAll("§f", "§7"));
                    continue;
                }
                if (location.distanceSquared(player2.getLocation()) > d2) {
                    string = string.replaceAll("§e", "§7");
                    player2.sendMessage(string.replaceAll("§f", "§7"));
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
