package ru.konungstvo.KMChat;

import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;
import java.util.Date;
import java.io.*;
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
    private String path = "/home/narg/actualserver/logs/";

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

    public void kmlog(String where, String what) {
		try(FileWriter writer = new FileWriter(path + where + "/" + where + "_current.log", true)) {
			Date date = new Date();
			what = what.replaceAll("§.", "");
	        writer.write("[" + date.toString() + "] " + what + "\n");
    	}
	    catch(IOException ex){
    	  	System.out.println(ex.getMessage());
	    }
    }

    public String dF(String mes) {
		String result = "&e бросает 4dF (";
		int n = -1;
		for (Map.Entry<Integer, String> entry : nMap.entrySet()) {
			if (mes.startsWith(entry.getValue())) {
				n = entry.getKey();
				break;
			}
		}
	        if (n < 0) {
			n = 2;
			mes = "ПЛОХО";
		}
		int[] dices = {-1, 0, 1};
		int dice = 0;
	for (int i = 0; i < 4; ++i) {
		int rnd = new Random().nextInt(3);
		dice = dices[rnd];
		if (dice < 0) {
			n -= 1;
			result += "-";
		}
		else if (dice > 0) {
			n += 1;
			result += "+";
		}
		else {
			result += "=";
		}
		
	}
	result += ") от " + mes + ". Результат: " + nMap.get(n) + " ))&f"; 
return result; 
}

@EventHandler
public void onPlayerJoin(PlayerJoinEvent playerJoinEvent) {
String name = playerJoinEvent.getPlayer().getName();
playerJoinEvent.setJoinMessage("§e" + name + "§f входит в игру");
String ip = playerJoinEvent.getPlayer().getAddress().getHostName();
kmlog("whole", name + " ("+ip+")" + " входит в игру");
try(FileWriter writer = new FileWriter(path + "ipgame.log", true)) {
    writer.write(name + " " + ip + "\n");
}
catch(IOException ex){
    System.out.println(ex.getMessage());
}

}

@EventHandler
public void onPlayerLeave(PlayerQuitEvent playerQuitEvent) {
String name = playerQuitEvent.getPlayer().getName();
playerQuitEvent.setQuitMessage("§e" + name + "§f выходит из игры");
	String ip = playerQuitEvent.getPlayer().getAddress().getHostName();
kmlog("whole", name + " ("+ip+")" + " выходит из игры");
}

@EventHandler
public void onChatTab(PlayerChatTabCompleteEvent playerChatTabCompleteEvent) {
if (playerChatTabCompleteEvent.getChatMessage().startsWith("% ")) {
    Collection<String> collection = playerChatTabCompleteEvent.getTabCompletions();
    collection.clear();
    if (playerChatTabCompleteEvent.getLastToken().startsWith("у")) {
	collection.add("ужасно");
	} else if (playerChatTabCompleteEvent.getLastToken().startsWith("пр")) {
	    collection.add("превосходно");
	} else if (playerChatTabCompleteEvent.getLastToken().startsWith("пл")) {
	    collection.add("плохо");
    } else if (playerChatTabCompleteEvent.getLastToken().startsWith("по")) {
	    collection.add("посредственно");
	} else if (playerChatTabCompleteEvent.getLastToken().startsWith("п")) {
	    collection.add("плохо");
	    collection.add("посредственно");
	    collection.add("превосходно");
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
String mes = asyncPlayerChatEvent.getMessage();
String name = player.getName();
double range = this.getConfig().getInt("range.default");

String adminprefix = "";
if (player.hasPermission("KMChat.prefix")) {
    adminprefix = this.getConfig().getString("adminprefix");
}
String result = String.format("%s&a%s&f: %s", adminprefix, name, mes);

if ((mes.startsWith("d") || mes.startsWith("к")) && player.hasPermission("KMChat.dice")) {
    boolean bl2 = false;
    int n = 6;
    if (mes.startsWith("d4") || mes.startsWith("к4")) {
	n = 4;
    } else if (mes.startsWith("d6") || mes.startsWith("к6")) {
	n = 6;
    } else if (mes.startsWith("d8") || mes.startsWith("к8")) {
	n = 8;
    } else if (mes.startsWith("d10") || mes.startsWith("к10")) {
	n = 10;
    } else if (mes.startsWith("d12") || mes.startsWith("к12")) {
	n = 12;
    } else if (mes.startsWith("d14") || mes.startsWith("к14")) {
	n = 14;
    } else if (mes.startsWith("d20") || mes.startsWith("к20")) {
	n = 20;
    } else {
	bl2 = true;
    }
    if (!bl2) {
	Random random = new Random();
	int n2 = random.nextInt();
	n2 = Math.abs(n2);
	n2 %= n;
	result = "&e(( " + name + "&e бросает d" + n + ". Выпадает " + ++n2 + " ))&f";
    }
} else if (mes.startsWith("% ")) {
    int n = 2;
    try {
	mes = mes.substring(2);
    }
    catch (Exception exception) {
	n = 666;
	result = mes;
    }
    if (n != 666) {
	String dice = dF(mes);
			result = "&e(( " + name + dice;
    }
} else if (mes.startsWith("===% ")) {
    int n = 2;
    try {
	mes = mes.substring(5);
    }
    catch (Exception exception) {
	n = 666;
	result = mes;
    }
    if (n != 666) {
	range = this.getConfig().getInt("range.weakwhisper");
			String dice = dF(mes);
			dice.replaceAll("бросает", "едва слышно бросает");
	result = "&e(( " + name + dice;
    }            
} else if (mes.startsWith("==% ")) {
    int n = 2;
    try {
	mes = mes.substring(4);
    }
    catch (Exception exception) {
	n = 666;
	result = mes;
    }
    if (n != 666) {
	range = this.getConfig().getInt("range.whisper");
			String dice = dF(mes);
			dice.replaceAll("бросает", "очень тихо бросает");
	result = "&e(( " + name + dice;
    }      
} else if (mes.startsWith("=% ")) {
    int n = 2;
    try {
	mes = mes.substring(3);
    }
    catch (Exception exception) {
	n = 666;
	result = mes;
    }
    if (n != 666) {
	range = this.getConfig().getInt("range.strongwhisper");
			String dice = dF(mes);
			dice.replaceAll("бросает", "тихо бросает");
	result = "&e(( " + name + dice;
    }      
} else if (mes.startsWith("!!!% ")) {
    int n = 2;
    try {
	mes = mes.substring(5);
    }
    catch (Exception exception) {
	n = 666;
	result = mes;
    }
    if (n != 666) {
	range = this.getConfig().getInt("range.strongshout");
			String dice = dF(mes);
			dice.replaceAll("бросает", "СВЕРХГРОМКО ОБРУШИВАЕТ");
	result = "&e(( " + name + dice;
    }      
} else if (mes.startsWith("!!% ")) {
    int n = 2;
    try {
	mes = mes.substring(4);
    }
    catch (Exception exception) {
	n = 666;
	result = mes;
    }
    if (n != 666) {
	range = this.getConfig().getInt("range.shout");
			String dice = dF(mes);
			dice.replaceAll("бросает", "очень громко бросает");
	result = "&e(( " + name + dice;
    }      
} else if (mes.startsWith("!% ")) {
    int n = 2;
    try {
	mes = mes.substring(3);
    }
    catch (Exception exception) {
	n = 666;
	result = mes;
    }
    if (n != 666) {
	range = this.getConfig().getInt("range.weakshout");
			String dice = dF(mes);
			dice.replaceAll("бросает", "громко бросает");
	result = "&e(( " + name + dice;
    }                  
} else if ((mes.startsWith("#") || mes.startsWith("№")) && player.hasPermission("KMChat.dm")) {
    mes = mes.substring(1);
    range = this.getConfig().getInt("range.dm");
    result = "&e***" + mes + "***";
} else if ((mes.startsWith("=#") || mes.startsWith("=№")) && player.hasPermission("KMChat.dm")) {
    mes = mes.substring(2);
    range = this.getConfig().getInt("range.closedm");
    result = "&e**" + mes + "**";
} else if ((mes.startsWith("==#") || mes.startsWith("==№")) && player.hasPermission("KMChat.dm")) {
    mes = mes.substring(3);
    range = this.getConfig().getInt("range.closerdm");
    result = "&e*" + mes + "*";
} else if ((mes.startsWith("===#") || mes.startsWith("===№")) && player.hasPermission("KMChat.dm")) {
    mes = mes.substring(4);
    range = this.getConfig().getInt("range.closestdm");
    result = "&e~" + mes + "~";
} else if ((mes.startsWith("!#") || mes.startsWith("!№")) && player.hasPermission("KMChat.dm")) {
    mes = mes.substring(2);
    range = this.getConfig().getInt("range.fardm");
    result = "&e****" + mes + "****";
} else if ((mes.startsWith("!!#") || mes.startsWith("!!№")) && player.hasPermission("KMChat.dm")) {
    mes = mes.substring(3);
    range = this.getConfig().getInt("range.farerdm");
    result = "&e*****" + mes + "*****";
} else if ((mes.startsWith("!!!#") || mes.startsWith("!!!№")) && player.hasPermission("KMChat.dm")) {
    mes = mes.substring(4);
    range = this.getConfig().getInt("range.farestdm");
    result = "&e******" + mes + "******";
} else if (mes.startsWith("*") && player.hasPermission("KMChat.me")) {
    mes = mes.substring(1);
    range = this.getConfig().getInt("range.me");
    result = String.format("* %s&a%s&f %s", adminprefix, name, mes);
} else if ((mes.startsWith("@@@") || mes.startsWith("===")) && player.hasPermission("KMChat.whisper")) {
    mes = mes.substring(3);
    range = this.getConfig().getInt("range.weakwhisper");
    result = String.format("%s&a%s&f (едва слышно): %s", adminprefix, name, mes);
} else if ((mes.startsWith("@@") || mes.startsWith("==")) && player.hasPermission("KMChat.whisper")) {
    mes = mes.substring(2);
    range = this.getConfig().getInt("range.whisper");
    result = String.format("%s&a%s&f (шепчет): %s", adminprefix, name, mes);
} else if ((mes.startsWith("@") || mes.startsWith("=")) && player.hasPermission("KMChat.whisper")) {
    mes = mes.substring(1);
    range = this.getConfig().getInt("range.strongwhisper");
    result = String.format("%s&a%s&f (вполголоса): %s", adminprefix, name, mes);
} else if (mes.startsWith("!!!") && player.hasPermission("KMChat.shout")) {
    mes = mes.substring(3);
    range = this.getConfig().getInt("range.strongshout");
    result = String.format("%s&a%s&f (орёт): %s", adminprefix, name, mes);
} else if (mes.startsWith("!!") && player.hasPermission("KMChat.shout")) {
    mes = mes.substring(2);
    range = this.getConfig().getInt("range.shout");
    result = String.format("%s&a%s&f (кричит): %s", adminprefix, name, mes);
} else if (mes.startsWith("!") && player.hasPermission("KMChat.shout")) {
    mes = mes.substring(1);
    range = this.getConfig().getInt("range.weakshout");
    result = String.format("%s&a%s&f (восклицает): %s", adminprefix, name, mes);
} else if (mes.startsWith("?") && player.hasPermission("KMChat.global")) {
    bl = false;
    mes = mes.substring(1);
    result = String.format("%s&a%s&f: &b(( %s ))&f", adminprefix, name, mes);
} else if (mes.startsWith("_")) {
    mes = "((" + mes.substring(1) + "))";
}
if (mes.startsWith("((") && mes.endsWith("))")) {
    result = String.format("%s&a%s&f (OOC): &d%s&f", adminprefix, name, mes);
}
result = result.replaceAll("&([a-z0-9])", "§$1");
result = result.replaceAll("%", "%%");
asyncPlayerChatEvent.setFormat(result);
asyncPlayerChatEvent.setMessage(mes);
	kmlog("whole", result);
	kmlog("chat", result);
if (bl) {
    asyncPlayerChatEvent.getRecipients().clear();
    asyncPlayerChatEvent.getRecipients().addAll(this.getLocalRecipients(player, result, range));
}
}

protected List<Player> getLocalRecipients(Player player, String mes, double d) {
Location location = player.getLocation();
LinkedList<Player> linkedList = new LinkedList<Player>();
double d2 = Math.pow(d, 2.0);
for (Player player2 : Bukkit.getServer().getOnlinePlayers()) {
    if (player2.hasPermission("KMChat.admin")) {
	if (!player2.getWorld().equals((Object)player.getWorld())) {
	    mes = mes.replaceAll("§e", "§7");
				mes = mes.replaceAll("§f", "§7");
	    player2.sendMessage(mes);
	    continue;
	}
	if (location.distanceSquared(player2.getLocation()) > d2) {
                    mes = mes.replaceAll("§e", "§7");
		   			mes = mes.replaceAll("§f", "§7");
                    player2.sendMessage(mes);
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
