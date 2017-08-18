package KMChat;

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
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class KMChat
extends JavaPlugin
implements Listener {
    private Logger log = Logger.getLogger("Minecraft");
    private Map<Integer, String> nMap = new Hashtable<Integer, String>();
    private String path = "logs/";

    public void onEnable() {
        this.getConfig().options().copyDefaults(true);
	this.saveConfig();
	path = this.getConfig().getString("logsdir");
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
	String result = "(";
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
	result += String.format(") от %s. Результат: %s", mes, nMap.get(n)); 
	return result; 
    }	

    public String dnum(String mes) {
	int[] poss = {4, 6, 8, 10, 12, 14, 20};
        int n = -1;
        for (int i = 0; i < poss.length; ++i) {
	    int compare = poss[i];
	    if (mes.startsWith(Integer.toString(compare))) {
		n = poss[i];
		if (mes.startsWith("100")) {
		    n = 100;
		    mes = mes.substring(3);
		}
		else {
		    mes = mes.substring(Integer.toString(compare).length());
		}
	    break;
	    }
	}
	String hlp = "";
	if (!mes.isEmpty()) {
	    if (mes.startsWith(" ")) {
		mes = mes.substring(1);
	    }
            hlp = " (" + mes + ")";
	}

	if (n > 0) {
	    int dice = new Random().nextInt(n);

        this.log.info(mes);
	    return String.format("d%s%s. Выпадает %s", n, hlp, ++dice);
	} else {
	    return null;
	}
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent playerJoinEvent) {
	String name = playerJoinEvent.getPlayer().getName();
	playerJoinEvent.setJoinMessage("§e" + name + "§f входит в игру");
	String ip = playerJoinEvent.getPlayer().getAddress().getHostName();
	kmlog("whole", name + " ("+ip+")" + " входит в игру");
	kmlog("chat", name + " ("+ip+")" + " входит в игру");
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
	kmlog("chat", name + " ("+ip+")" + " выходит из игры");
    }

    @EventHandler
    public void onChatTab(PlayerChatTabCompleteEvent playerChatTabCompleteEvent) {
    	String mes = playerChatTabCompleteEvent.getChatMessage();
    	if (mes.startsWith("% ") ||
	    mes.startsWith("-% ") ||
            mes.startsWith("=% ") ||
	    mes.startsWith("!% ") ||
	    mes.startsWith("==% ") ||
	    mes.startsWith("!!% ") ||
	    mes.startsWith("===% ") ||
	    mes.startsWith("!!!% ") ) {

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

    public boolean onCommand(CommandSender commandSender, Command command, String string, String[] args) {
	if (command.getName().equalsIgnoreCase("me")) {
                commandSender.sendMessage("§4/me отключено, используйте *§f");
                return true;

	} else if (command.getName().equalsIgnoreCase("msg")) {
	    if (!(commandSender instanceof Player)) {
	        commandSender.sendMessage("§4You must be a player!§f");
                return false;
            }
            Player sender = (Player)commandSender;
            if (args.length == 0) {
		sender.sendMessage("§8...и чего сказать хотел?§f");
                return true;
            }

	    Player recip = sender.getServer().getPlayer(args[0]);
	    if (recip == null) {
		sender.sendMessage("§4Нет такого игрока!§f");
		return true;
	    }
	    
            if (!sender.hasPermission("KMCore.tell")) {
		sender.sendMessage("§4Недостаточно прав.§f");
		return true;
            }


            String senderName = sender.getName();
	    String recipName = recip.getName();
            args[0] = "&8[&a"+ senderName + "&8->&a" + recipName + "&8]:&f";
	    String message = "";
            for (String arg : args) {
		message = message + " " + arg;
            }
	    
	    message = message.replaceAll("&([a-z0-9])", "§$1");
            sender.sendMessage(message);
            recip.sendMessage(message);
	    kmlog("whole", message);
	    kmlog("chat", message);
            for (Player player: Bukkit.getServer().getOnlinePlayers()) {
	        if (player.hasPermission("KMChat.admin")) {
		    if (player != sender && player != recip) {
			player.sendMessage(message);
		    }
		}
	    }
	    return true;
	}
	return true;
	
    }



    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent asyncPlayerChatEvent) {
	Player player = asyncPlayerChatEvent.getPlayer();
	boolean local = true;
	boolean forgm = false;
	String mes = asyncPlayerChatEvent.getMessage();
	String name = player.getName();
	double range = this.getConfig().getInt("range.default");

	String adminprefix = "";
	if (player.hasPermission("KMChat.prefix")) {
	    adminprefix = this.getConfig().getString("adminprefix");
	}
	String result = String.format("%s&a%s&f: %s", adminprefix, name, mes);
	
	    

	if (mes.startsWith("-d") && player.hasPermission("kmchat.dice")) {
	    String helpmes = mes.substring(2);
	    String dice = dnum(helpmes);
	    forgm = true;
	    if (dice != null) {
		result = String.format("&a%s &f(to GM) &eбросает %s &f", name, dice);
	    }

	} else if (mes.startsWith("===d") && player.hasPermission("kmchat.dice")) {
	    String helpmes = mes.substring(4);
	    String dice = dnum(helpmes);
	    if (dice != null) {
		result = String.format("&e(( %s едва слышно бросает %s ))&f", name, dice);
		range = this.getConfig().getInt("range.weakwhisper");
	    }
	} else if (mes.startsWith("==d") && player.hasPermission("kmchat.dice")) {
	    String helpmes = mes.substring(3);
	    String dice = dnum(helpmes);
	    if (dice != null) {
		result = String.format("&e(( %s очень тихо бросает %s ))&f", name, dice);
		range = this.getConfig().getInt("range.whisper");
	    }

	} else if (mes.startsWith("=d") && player.hasPermission("kmchat.dice")) {
	    String helpmes = mes.substring(2);
	    String dice = dnum(helpmes);
	    if (dice != null) {
		result = String.format("&e(( %s тихо бросает %s ))&f", name, dice);
		range = this.getConfig().getInt("range.strongwhisper");
	    }

	} else if (mes.startsWith("!d") && player.hasPermission("kmchat.dice")) {
	    String helpmes = mes.substring(2);
	    String dice = dnum(helpmes);
	    if (dice != null) {
		result = String.format("&e(( %s громко бросает %s ))&f", name, dice);
		range = this.getConfig().getInt("range.weakshout");
	    }

	} else if (mes.startsWith("!!d") && player.hasPermission("kmchat.dice")) {
	    String helpmes = mes.substring(3);
	    String dice = dnum(helpmes);
	    if (dice != null) {
		result = String.format("&e(( %s очень громко бросает %s ))&f", name, dice);
		range = this.getConfig().getInt("range.shout");
	    }
	
	} else if (mes.startsWith("!!!d")  && player.hasPermission("kmchat.dice")) {
	    String helpmes = mes.substring(4);
	    String dice = dnum(helpmes);
	    if (dice != null) {
		result = String.format("&e(( %s СВЕРХГРОМКО ОБРУШИВАЕТ %s ))&f", name, dice);
		range = this.getConfig().getInt("range.strongshout");
	    }

	} else if (mes.startsWith("d") && player.hasPermission("kmchat.dice")) {
	    String helpmes = mes.substring(1);
	    String dice = dnum(helpmes);
	    if (dice != null) {
		result = String.format("&e(( %s &e бросает %s ))&f", name, dice);
	    }


	} else if (mes.startsWith("%")) {
	    int n = 2;
            if (mes.startsWith("% ")) {
	    	mes = mes.substring(2);
	    } else {
		mes = mes.substring(1);
	    }
	    String dice = dF(mes);
	    result = String.format("&e(( %s бросает 4dF %s ))&f", name, dice);
	
	} else if (mes.startsWith("-%")) {
	    int n = 2;
	    forgm = true;
            if (mes.startsWith("-% ")) {
	    	mes = mes.substring(3);
	    } else {
		mes = mes.substring(2);
	    }
	    String dice = dF(mes);
	    result = String.format("&a%s &f(to GM) &eбросает 4dF %s &f", name, dice);

	} else if (mes.startsWith("===%")) {
	    int n = 2;
	    if (mes.startsWith("===% ")) {
	        mes = mes.substring(5);
	    } else {
	        mes = mes.substring(4);
	    }
	    range = this.getConfig().getInt("range.weakwhisper");
	    String dice = dF(mes);
	    result = String.format("&e((%s едва слышно бросает 4dF %s ))&f", name, dice);            

	} else if (mes.startsWith("==%")) {
	    int n = 2;
	    if (mes.startsWith("==% ")) {
	        mes = mes.substring(4); 
	    } else {
		mes = mes.substring(3);
	    }
	    range = this.getConfig().getInt("range.whisper");
	    String dice = dF(mes);
	    result = String.format("&e(( %s очень тихо бросает 4dF %s ))&f", name, dice);

	} else if (mes.startsWith("=%")) {
	    int n = 2;
	    if (mes.startsWith("=% ")) {
	        mes = mes.substring(3);
	    } else {
	        mes = mes.substring(2);
	    }
	    range = this.getConfig().getInt("range.strongwhisper");
	    String dice = dF(mes);
	    result = String.format("&e(( %s тихо бросает 4dF %s ))&f", name, dice);
	
	} else if (mes.startsWith("!!!%")) {
	    int n = 2;
	    if (mes.startsWith("!!!% ")) {
	        mes = mes.substring(5);
	    } else {
		mes = mes.substring(4);
	    }
	    range = this.getConfig().getInt("range.strongshout");
	    String dice = dF(mes);
	    result = String.format("&e(( %s СВЕРХГРОМКО ОБРУШИВАЕТ 4dF %s ))&f", name, dice);
	
	} else if (mes.startsWith("!!%")) {
	    int n = 2;
	    if (mes.startsWith("!!% ")) {
	       mes = mes.substring(4);
	    } else {
	        mes = mes.substring(3);
	    }
	    range = this.getConfig().getInt("range.shout");
	    String dice = dF(mes);
	    result = String.format("&e(( %s очень громко бросает 4dF %s ))&f", name, dice);

	} else if (mes.startsWith("!%")) {
	    int n = 2;
	    if (mes.startsWith("!% ")) {
	        mes = mes.substring(3);
	    } else {
	        mes = mes.substring(2);
	    }
	    range = this.getConfig().getInt("range.weakshout");
	    String dice = dF(mes);
	    result = String.format("&e(( %s громко бросает 4dF %s ))&f", name, dice);
		
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
		
	} else if (mes.startsWith("^") && player.hasPermission("KMChat.global")) {
	    local = true;
	    mes = mes.substring(1);
	    result = String.format("%s&a%s&f: &b(( %s ))&f", adminprefix, name, mes);
		
	} else if (mes.startsWith("_")) {
	    mes = "(( " + mes.substring(1) + " ))";
	
	} else if (mes.startsWith("-")) {
	    if (mes.startsWith("- ")) {
		mes = mes.substring(2);
	    } else {
		mes = mes.substring(1);
	    }
	    result = String.format("%s&a%s &f(to GM): &6(( %s ))&f", adminprefix, name, mes);
	    forgm = true;
	}

	if (mes.startsWith("((") && mes.endsWith("))")) {
	    result = String.format("%s&a%s&f (OOC): &d%s&f", adminprefix, name, mes);
	}


	result = result.replaceAll("%", "%%");
	result = result.replaceAll("&([a-z0-9])", "§$1");
	asyncPlayerChatEvent.setFormat(result);
	asyncPlayerChatEvent.setMessage(mes);
	kmlog("whole", result);
	kmlog("chat", result);

	if (forgm) {
	    asyncPlayerChatEvent.getRecipients().clear();
	    LinkedList<Player> recips = new LinkedList();
	    recips.add(player);
            for (Player player2 : Bukkit.getServer().getOnlinePlayers()) {
                if (player2.hasPermission("KMChat.admin")) {
		    recips.add(player2);
		}
	    }
	    asyncPlayerChatEvent.getRecipients().addAll(recips);

	} else if (local) {
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
