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
import java.util.regex.Matcher;  
import java.util.regex.Pattern;
import java.util.Arrays;
import java.nio.file.*;
import java.nio.charset.*;
import java.io.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
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
import org.bukkit.scheduler.BukkitScheduler;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;
import sx.blah.discord.util.RateLimitException;

public class KMChat
extends JavaPlugin
implements Listener {
    private static String TOKEN;
    private static String CHID;
    private static String DATA_PATH;
    private static IDiscordClient client;
    private static IChannel ingameChannel;
    private Logger log = Logger.getLogger("Minecraft");
    private String path = "logs/";
    private Map<Integer, String> nMap = new Hashtable<Integer, String>();
    private Range[] allRanges = new Range[6];
    private String[] skillset;
    private List<String> whoUseAutoGM;
    private boolean wasrestarted = false;
    public void onEnable() {

        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
        
        path = this.getConfig().getString("logsdir");
        TOKEN = this.getConfig().getString("bottoken");
        CHID = this.getConfig().getString("channelid"); 
        DATA_PATH = this.getConfig().getString("datastorage");
        whoUseAutoGM = this.getConfig().getStringList("whoUseAutoGM");
	
        System.out.println("Logging bot in...");
        client = new ClientBuilder().withToken(TOKEN).build();
        client.getDispatcher().registerListener(this);
        client.login();
        
	Range weakwhisper = new Range("weakwhisper", "whisper", " (едва слышно)", "===", "@@@");
	Range whisper = new Range("whisper", "whisper", " (шепчет)", "==", "@@");
	Range strongwhisper = new Range("strongwhisper", "whisper", " (вполголоса)", "=", "@");
	
	Range strongshout = new Range("strongshout", "shout", " (орёт)", "!!!");
	Range shout = new Range("shout", "shout", " (кричит)", "!!");
	Range weakshout = new Range("weakshout", "shout", " (восклицает)", "!");

	allRanges[0] = weakwhisper;   //===
	allRanges[1] = whisper;	      //==
	allRanges[2] = strongwhisper; //=
	allRanges[3] = strongshout;   //!!!
	allRanges[4] = shout;	      //!!
	allRanges[5] = weakshout;     //!   order is important

        this.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this);
	//this.nMap.put(-3, "так ужасно, что хуже уже некуда");
	this.nMap.put(-3, "абсолютно ублюдски");
        this.nMap.put(-2, "ужасно---");
        this.nMap.put(-1, "ужасно--");
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
        //this.nMap.put(12, "ТАК ЛЕГЕНДАРНО, ЧТО ПОПАДЁТ ВО ВСЕ КНИГИ РЕКОРДОВ");
        this.nMap.put(12, "божественно");

	skillset = new String [] {"реакция", "владение оружием", "кулачный бой", "борьба", "парирование", "уклонение", "блокирование",
		                    "бег", "плавание", "акробатика", "физическая сила", "выносливость", "устойчивость к болезням", "внимательность",
				    "скрытность", "выживание", "диагностика", "первая помощь", "зашивание ран", "хирургия"};

        this.log.info(String.format("%s is enabled!", this.getDescription().getFullName()));
    }

    public void onDisable() {	
	RequestBuffer.request(() -> ingameChannel.sendMessage("**Server is going offline!**"));
        this.log.info(String.format("%s is disabled!", this.getDescription().getFullName()));
    }
   
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent playerJoinEvent) {
	String name = playerJoinEvent.getPlayer().getName();
	playerJoinEvent.setJoinMessage("§e" + name + "§f входит в игру");
	String ip = playerJoinEvent.getPlayer().getAddress().getHostName();
	String message =  name + " ("+ip+")" + " входит в игру";
	kmlog("whole", message);
	kmlog("chat",  message);
	final String snd = message.replaceAll(name, "__"+name+"__");
	RequestBuffer.request(() -> ingameChannel.sendMessage(snd));
	try(FileWriter writer = new FileWriter(path + "ipgame.log", true)) {
	    writer.write(name + " " + ip + "\n");
	}
	catch(IOException ex){
	    System.out.println(ex.getMessage());
	}
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent playerQuitEvent) {
	Player player = playerQuitEvent.getPlayer(); 
    	String name = player.getName();
    	playerQuitEvent.setQuitMessage("§e" + name + "§f выходит из игры");
	String ip = player.getAddress().getHostName();
	String message = name + " ("+ip+")" + " выходит из игры";
	kmlog("whole", message);
	kmlog("chat", message);
	RequestBuffer.request(() -> ingameChannel.sendMessage(message.replaceAll(name, "__"+name+"__")));
    }



    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent asyncPlayerChatEvent) {
	//Long beg = System.nanoTime();
	Player player = asyncPlayerChatEvent.getPlayer();
	int rangePosition = 6; // if no range specified, lands on the last element of array with descriptions

	boolean local = true;
	boolean forgm = false; //to gm chat
	boolean norec = false; //no recipients at all
	boolean sendraw = false; //either to send a raw message to discord

	String mes = asyncPlayerChatEvent.getMessage();
	String raw = mes;
        System.out.println(mes);
	String name = player.getName();
	Range setRange = null;
	double range = this.getConfig().getInt("range.default");
	
	String adminprefix = "";
	if (player.hasPermission("KMChat.prefix")) {
	    adminprefix = this.getConfig().getString("adminprefix");
	}
	boolean strippedColon = false;
        for (String nick : whoUseAutoGM) {
            if (player.getName().equals(nick)) {
                if (mes.startsWith(":")) {
                    mes = mes.substring(1);
                    strippedColon = true;
                }
            break;
            } 
        }
	String describeRange = "";
	int i = -1;
	for (Range ran : allRanges) {
	    ++i;
	    if (ran.matches(mes) && player.hasPermission(ran.getPermission())) {
		rangePosition = i;
		mes = mes.substring(ran.getSymbol().length());
		range = this.getConfig().getInt(ran.getRange());
		describeRange = ran.getDescription();
		setRange = ran;
		break;
	    }
	}
	
        for (String nick : whoUseAutoGM) {
            if (player.getName().equals(nick)) {
                if (mes.startsWith(":")) {
                    mes = mes.substring(1);
                } else if (mes.startsWith("#") || mes.startsWith("%") || mes.startsWith("_") || mes.startsWith("-") || mes.startsWith("№") || strippedColon) {
                    break;
                } else {
                    mes = "-" + mes;
                    forgm = true;
                }
            break;
            } 
        }
	String result = String.format("%s&a%s&f%s: %s", adminprefix, name, describeRange, mes);
	Pattern p = Pattern.compile("-d(4|6|8|10|12|14|20|100).*"); 
	Matcher m = p.matcher(mes);  
	if (mes.startsWith("-d") && m.matches() && player.hasPermission("kmchat.dice")) {
	    String helpmes = mes.substring(2);
	    String dice = dnum(helpmes);
	    if (dice != null) {
		sendraw = true;
		forgm = true;
		result = String.format("&a%s &f(to GM) &eбросает %s &f", name, dice);
	    }

	} else if (mes.startsWith("d") && player.hasPermission("kmchat.dice")) {
	    sendraw = true;
	    String[] vars = { "едва слышно бросает", 
			      "очень тихо бросает", 
			      "тихо бросает", 
			      "СВЕРХГРОМКО ОБРУШИВАЕТ", 
			      "очень громко бросает", 
			      "громко бросает", 
			      "бросает" };
	    String helpmes = mes.substring(1);
	    String dice = dnum(helpmes);
	    if (dice != null) {
		result = String.format("&e(( %s &e%s %s ))&f", name, vars[rangePosition], dice);
	    } else
		result = String.format("%s&a%s&f%s: %s", adminprefix, name, describeRange, mes);

	} else if (mes.startsWith("%")) {
	    sendraw = true;
	    String[] vars = { "едва слышно бросает",
                              "очень тихо бросает",
                              "тихо бросает",
                              "СВЕРХГРОМКО ОБРУШИВАЕТ",
                              "очень громко бросает",
                              "громко бросает",
                              "бросает" };
	    int n = 2;
            if (mes.startsWith("% ")) {
	    	mes = mes.substring(2);
	    } else {
		mes = mes.substring(1);
	    }
	    String dice = dF(mes, player.getName());
	    if (dice == null) {
		player.sendMessage("§4Для броска дайса пропишите: \"% значение\"§f");	
		norec = true;
	    } else
		result = String.format("&e(( %s %s 4dF %s ))&f", name, vars[rangePosition], dice);
	
	} else if (mes.startsWith("-%")) {
	    sendraw = true;
	    int n = 2;
	    forgm = true;
            if (mes.startsWith("-% ")) {
	    	mes = mes.substring(3);
	    } else {
		mes = mes.substring(2);
	    }
	    String dice = dF(mes, player.getName());
	    if (dice == null) {
		player.sendMessage("§4Для броска дайса пропишите: \"% значение\"§f");	
		norec = true;
	    }
	    result = String.format("&a%s &f(to GM) &eбросает 4dF %s &f", name, dice);
	
	} else if ((mes.startsWith("#") || mes.startsWith("№")) && player.hasPermission("KMChat.dm")) {
	    sendraw = true;
	    String[] vars = { "~",
			      "*",
			      "**",
			      "******",
			      "*****",
			      "****",
			      "***" };
	    String[] gmRanges = { "closestdm",
				  "closerdm",
				  "closedm",
				  "farestdm",
				  "farerdm",
				  "fardm",
				  "default" };
	    range = this.getConfig().getInt("range."+gmRanges[rangePosition]);
	    mes = mes.substring(1);
	    result = "&e" +vars[rangePosition] + mes + vars[rangePosition];
		
	} else if (mes.startsWith("*") && player.hasPermission("KMChat.me") && setRange == null) {
	    mes = mes.substring(1);
	    range = this.getConfig().getInt("range.me");
	    result = String.format("* %s&a%s&f %s", adminprefix, name, mes);
		
	} else if (mes.startsWith("^") && player.hasPermission("KMChat.global")) {
	    local = false;
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
    
	} else if (mes.startsWith(":msg") || mes.startsWith("msg")) {
            if (!player.hasPermission("KMCore.tell")) {
                player.sendMessage("§4Недостаточно прав.§f");
            } else {
		player.sendMessage("§4Используйте /msg!");
		norec = true;
	    }
	}

	if (mes.startsWith("((") && mes.endsWith("))")) {
            String str = "";
	    if (setRange != null) {
		str = setRange.getDescription().replaceAll("[),(, ]","") + " в ";
		if (rangePosition == 0) {
		    str = "едва слышно в ";
		    }
		}
	    result = String.format("%s&a%s&f (%sOOC): &d%s&f", adminprefix, name, str, mes);
            if (result.matches(".*\\(\\(\\S.*")) {
                result = result.replace("((", "(( ");
            }
            if (result.matches(".*\\S\\)\\).*")) {
                System.out.println(result);
                result = result.replace("))", " ))");
            }

	}


	result = result.replaceAll("%", "%%");
	result = result.replaceAll("&([a-z0-9])", "§$1");
	result = result.replaceAll("\\]\\s\\.", "].");
	asyncPlayerChatEvent.setFormat(result);
	asyncPlayerChatEvent.setMessage(mes);
	kmlog("whole", result);
	kmlog("chat", result);
	String res2discord = result.replaceAll("§([a-z0-9])", "");
	res2discord = res2discord.replace(player.getName(), "**"+player.getName()+"**");
	String mes2dis = null;
	if (sendraw) {
	    String raw2dis = "**" + player.getName() + "**: " + raw;
	    mes2dis = raw2dis + '\n' + res2discord;
	} else {
	    mes2dis = res2discord;
	}
	final String sendme = mes2dis;
	RequestBuffer.request(() -> ingameChannel.sendMessage(sendme));

	if (norec) {
	    asyncPlayerChatEvent.getRecipients().clear();
	} else if (forgm) {
	    asyncPlayerChatEvent.getRecipients().clear();
	    LinkedList<Player> recips = new LinkedList();
	    recips.add(player);
            for (Player player2 : Bukkit.getServer().getOnlinePlayers()) {
                if (player2.hasPermission("KMChat.admin")) {
                    if (player.getLocation().distanceSquared(player2.getLocation()) > range*range) {
                        result = result.replaceAll("§f", "§7");
                        player2.sendMessage(result);
                    } else {
		        recips.add(player2);
                    }
		}
	    }
	    asyncPlayerChatEvent.getRecipients().addAll(recips);

	} else if (local) {
	    asyncPlayerChatEvent.getRecipients().clear();
	    asyncPlayerChatEvent.getRecipients().addAll(this.getLocalRecipients(player, result, range));
	}
	//System.out.println("range: " + range);
	//Long end = System.nanoTime();
	//System.out.println("speed:" + (end-beg));
    }
    
    
    //---}}} Various helpers
    
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
    protected void kmlog(String where, String what) {
	try(FileWriter writer = new FileWriter(path + where + "/" + where + "_current.log", true)) {
	    Date date = new Date();
	    what = what.replaceAll("§.", "");
	    writer.write("[" + date.toString() + "] " + what + "\n");
    	}
	catch(IOException ex){
	   System.out.println(ex.getMessage());
	}
    }

    protected String getSkill(String desc, String nick) {
	//we need to have a file "Playernick.skills" with skills listed as following: "skill:level"
	Path path = Paths.get(DATA_PATH, nick + ".skills");
	Charset charset = Charset.forName("UTF-8");
	Pattern skillpat = Pattern.compile("(.*):");
	Pattern levelpat = Pattern.compile(":(.*)");

	String skill = null;
	String level = null;

        try {
	    List<String> lines = Files.readAllLines(path, charset);

	    for (String line : lines) {
		Matcher skillmat = skillpat.matcher(line);
		while (skillmat.find()) {
		    skill = skillmat.group().replaceAll(":",""); //get the name of the skill
		}
		if (desc.toLowerCase().startsWith(skill)) {
		    Matcher levelmat = levelpat.matcher(line);
		    while (levelmat.find()) {
		        level = levelmat.group(); //get the level of the skill
		    }
		    break;
		} else {
			skill = null;
		}
	    }
	    if (skill == null) {
	        for (String sk : skillset) {
	            if (desc.toLowerCase().startsWith(sk)) {
		        skill = sk;
	    //System.out.println("skill is " + skill + ", level is " + level);
                        break;
		    } else {
                        level = null;
                    }
	        }
	    }

	} catch (IOException e) {
	    System.out.println(e);
	    level = null;
	}
	    if (level == null) {
		level = ":ПЛОХО";
            }
//                        System.out.println("skill is " + skill + ", level is " + level);
            if (skill == null)
                return null;
	    String result = desc.replaceFirst(skill, level + " ["+skill+"] (");
	    //let's make the output pretty!
	    //System.out.println(result);
	    //System.out.println(desc);

	    result = result.replaceFirst("\\(\\s", "\\(");
	    result = result.substring(1, result.length()) + ")";
	    if (result.contains("()"))
		result = result.replaceFirst("\\(\\)", "");
	    return result;

    }

    public int getSkillValue(String levelName) {
	for (Map.Entry<Integer, String> entry : nMap.entrySet()) {
            if (levelName.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return 666;
    }

    public boolean containsSkillValue(String message) {
        for (Map.Entry<Integer, String> entry : nMap.entrySet()) {
            if (message.startsWith(entry.getValue())) {
                return true;
            }
        }
        return false;

    }

    public String dF(String mes, String nick) {
	boolean weusedskill = false;
	String result = "(";
	int n = 666; //skill level represented as number
	float mod = 0; //optinal modificator to skill level

        if (!containsSkillValue(mes)) {
		//int wordsNum = 1;
		//String[] split = mes.split(" ", wordsNum + 2); //extra spot for optional modificator
                String skillmes = getSkill(mes, nick);
                if (skillmes != null) {
		    mes = skillmes;
		
	        Pattern ourSkillPat = Pattern.compile("\\(([-,+][0-9]).*");
                Matcher ourSkillMat = ourSkillPat.matcher(skillmes);
                String strmod = null;
                if (ourSkillMat.find()) {
                    strmod = ourSkillMat.group(1);
                }

		if (strmod != null) {
		    //String strmod = split[wordsNum];
		    if (strmod.matches("[-,+][0-9]")) {
			mes = mes.replace( "] (" + strmod, " " + strmod + "] (");
			if (mes.contains("()"))
				mes = mes.replaceFirst("\\(\\)", "");
			mod = Float.parseFloat(strmod);
		    }
		}
		weusedskill = true;
                }
	    }
	
	String level = "";
	String comment = "";
	try {
	    level = mes.split(" ", 2)[0];
	    if (!weusedskill) { //if we used skill, comment is already inside (braces)
		comment = mes.split(" ", 2)[1];
		mes = mes.replace(comment, "("+comment+")");
	    }
	} catch (Exception e) {
	    System.out.println("dF() caugth exception: " + e);
	    level = mes;
	}
	/*
	for (Map.Entry<Integer, String> entry : nMap.entrySet()) {
	    if (level.equals(entry.getValue())) {
		n = entry.getKey();
		break;
	    }
	}
	*/
	n = getSkillValue(level);
	if (level.equals("ПЛОХО"))
		n = 2;

	if (n == 666) {
	    return null;
        } else if (n < 1) {
		n = 1;
	} else if (n > 8) {
		n = 8;
	}

	int oldn = n;
	n = n + Math.round(mod);

	if (n < -2) {
		n = -2;
	} else if (n > 11) {
		n = 11;
	}
	if (mod != 0) {
		try {
			mes = mes.replaceFirst(level, nMap.get(n));
		}
		catch (Exception e) {
			System.out.println(e);
		}
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
	if (n < -3) {
		n = -3;
	} else if (n > 12) {
		n = 12;
	}
	result += String.format(") от %s. Результат: %s", mes, nMap.get(n));
	result = result.replace("( ", "("); 
	return result; 
    }	

    public String dF(String mes) {
	return dF(mes, "");
    }

    protected String dnum(String mes) {
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
    //---}}} Various helpers
    
    
    //{{{--- Commands 
       public boolean onCommand(CommandSender commandSender, Command command, String string, String[] args) {
            Player sender = (Player)commandSender;
	if (command.getName().equalsIgnoreCase("me")) {
                commandSender.sendMessage("§4/me отключено, используйте *§f");
                return true;    

        } else if (command.getName().equalsIgnoreCase("alwaysgm")) {
            if (!(commandSender instanceof Player)) {
                commandSender.sendMessage("§4You must be a player!§f");
                return false;
            }
        if (!sender.hasPermission("KMChat.gm")) {
                sender.sendMessage("§4Недостаточно прав.§f");
                return true;
        }
        if (args.length == 0) {
                if (whoUseAutoGM.contains(sender.getName())) {
                    sender.sendMessage("§7Автоматический ГМ-чат §aвключён!§f");
                } else {
                    sender.sendMessage("§7Автоматический ГМ-чат §4выключен!§f");
                }
            return true;
        }
        if (args[0].equals("help")) {
                sender.sendMessage("§6Usage:\n§f/alwaysgm - check condition\n/alwaysgm on - turn on\n/alwaysgm off - turn off\n:message - regular chat (if alwaysgm is on)");
        } else if (args[0].equals("on")) {
                    if (!whoUseAutoGM.contains(sender.getName())) {
                        whoUseAutoGM.add(sender.getName());
                        this.getConfig().set("whoUseAutoGM", whoUseAutoGM);
                        this.saveConfig();
                    }
                    sender.sendMessage("§7Автоматический ГМ-чат теперь §aвключён!§f"); 
        } else if (args[0].equals("off")) {
                    if (whoUseAutoGM.contains(sender.getName())) {
                        whoUseAutoGM.remove(sender.getName());
                        this.getConfig().set("whoUseAutoGM", whoUseAutoGM);
                        this.saveConfig();
                    }   
                    sender.sendMessage("§7Автоматический ГМ-чат теперь §4выключен!§f");
        }

        } else if (command.getName().equalsIgnoreCase("ingamerestart")) {
                if (!sender.hasPermission("KMCore.gm")) {
                    sender.sendMessage("§4Недостаточно прав.§f");
                    return false;
            }
            try {
                client.logout();
            } catch (Exception e) {
                System.out.println(e);
            }
                wasrestarted = true;
            try {
              client.login();
            } catch (Exception e) {
                System.out.println(e);
            }
            sender.sendMessage("§8Ingame bot was reloaded!§f");
            return true;
	} else if (command.getName().equalsIgnoreCase("msg")) {
	    if (!(commandSender instanceof Player)) {
	        commandSender.sendMessage("§4You must be a player!§f");
                return false;
            }
	    
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

	    final String snd = message.replaceAll("§([a-z0-9])","");
	    RequestBuffer.request(() -> ingameChannel.sendMessage(snd));

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
    //---}}} Commands
    
    //{{{--- IngameBot
    @EventSubscriber
    public void onReady(ReadyEvent event) {
        System.out.println("Bot is now ready!");
	ingameChannel = client.getChannelByID(CHID);
        String mes = "**Server is going online!**";
        if (wasrestarted) {
            mes = "**I was reloaded!**";
        }
        final String snd = mes;
	RequestBuffer.request(() -> ingameChannel.sendMessage(snd));
    }
    
    
    @EventSubscriber
    public void onMessage(MessageReceivedEvent event) throws RateLimitException, DiscordException, MissingPermissionsException {
	IMessage message = event.getMessage();
	String content = message.getContent();
	IUser user = message.getAuthor();
	if (user.isBot()) return;
	
	IChannel channel = message.getChannel();
	if (channel != ingameChannel) return;

	IGuild guild = message.getGuild();
	String[] split = message.getContent().split(" ");

    String res = null;
	if ( content.startsWith(":msg") || content.startsWith("/msg") ) {
	    res = "Пользуйтесь !msg {player} {message}";
	
	} else if (content.startsWith("!")) {
	    if (content.startsWith("!help")) {
		res = "Все команды начинаются с символа !\n!exec {command} — выполнить команду в игре;\n!online либо !онлайн — показать текущий онлайн;\n!msg {ник} {сообщение} — написать игроку в лс\n!d{число} — дайс (виден только в дискорде);\n!% значение — фуджедайс (виден только в дискорде);\n!help — вывести эту справку.";
		
	    } else if (content.startsWith("!d")) {
		String dice = dnum(message.getContent().substring(2));
		res = user.mention() + " бросает " + dice;

	    } else if (content.startsWith("!% ")) {
		String dice = dF(message.getContent().substring(3));
		if (dice == null)
		    res = "Для броска дайса пропишите: \"% значение\".";	
		else
		    res = user.mention() + " бросает " + dice;
	    
	    } else if (message.getContent().startsWith("!msg ")) {
		String mes = content.substring(5);
		Player[] players = Bukkit.getServer().getOnlinePlayers();
		boolean found = false;
		Player recip = null;
		for (Player player : players) {
		    if (mes.startsWith(player.getName())) {
			player.sendMessage("<§2"+user.getName()+"§f->§a"+player.getName()+"§f>"+mes.replace(player.getName(), ""));
			res = "<"+user.getName()+"->"+mes.replace(player.getName(), player.getName()+">");
			recip = player;
			found = true;
		    }
		}
		if (!found) {
		    res = "Нет такого игрока!";
		} else {
		    for (Player admin: Bukkit.getServer().getOnlinePlayers()) {
		       if (admin.hasPermission("KMChat.admin")) {
			    if (admin != recip) {
			        admin.sendMessage("<§2"+user.getName()+"§f->§a"+recip.getName()+"§f>"+mes.replace(recip.getName(), ""));
			    }
			}
		    }
		}

	    } else if (content.startsWith("!online") || content.startsWith("!онлайн")) {
		String online = "Текущий онлайн (%s): ";
		int i = 0;
		Player[] players = Bukkit.getServer().getOnlinePlayers();
		for (Player player: players) {
		    i++;
		    if (player.hasPermission("KMChat.admin"))
			online += "**" + player.getName() + "**";
		    else
			online += player.getName();
		    if (i == players.length)
			online += ".";
		    else 
			online += ", ";
		}
		if (i == 0)
		    res = "Никого онлайн!";
		else
		    res = String.format(online, i);
	    
	    } else if (content.startsWith("!exec ")) {
		if (content.startsWith("!exec msg")) {
		    res = "Так не получится. Пользуйтесь !msg {player} {message}";
		} else {
		String mes = "";
		try { content.substring(6); }
		catch (Exception e) { return; }
		BukkitScheduler scheduler = getServer().getScheduler();
		if (
		scheduler.scheduleSyncDelayedTask(this, new Runnable() {    
		    @Override
		    public void run() {
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), message.getContent().substring(6));    
		    }
		}, 1L) == -1)
		    res = "_Что-то пошло не так, команда не была выполнена!_";
		}
	    }
	} else {
	    for (Player player : Bukkit.getServer().getOnlinePlayers()) {
		player.sendMessage("<§2"+user.getName()+"§f> "+message);
	    }
	}
	final String snd = res;
	if (snd != null) {
	    RequestBuffer.request(() -> ingameChannel.sendMessage(snd));
	}
    }
    //---}}} IngameBot
    
    //---{{{ Tabs
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
	    mes.startsWith("!!!% ") ||
	    mes.startsWith(":% ") ) {

	    Collection<String> collection = playerChatTabCompleteEvent.getTabCompletions();
	    collection.clear();

	    if (playerChatTabCompleteEvent.getLastToken().startsWith("уж")) {
		collection.add("ужасно");
	    } else if (playerChatTabCompleteEvent.getLastToken().startsWith("ук")) {
                collection.add("уклонение");
	    } else if (playerChatTabCompleteEvent.getLastToken().startsWith("ус")) {
                collection.add("устойчивость к болезням");
	    } else if (playerChatTabCompleteEvent.getLastToken().startsWith("у")) {
                collection.add("устойчивость к болезням");
		collection.add("ужасно");
                collection.add("уклонение");
	    } else if (playerChatTabCompleteEvent.getLastToken().startsWith("а")) {
                collection.add("акробатика");
	    } else if (playerChatTabCompleteEvent.getLastToken().startsWith("бe")) {
                collection.add("бег");
	    } else if (playerChatTabCompleteEvent.getLastToken().startsWith("бл")) {
                collection.add("блокирование");
	    } else if (playerChatTabCompleteEvent.getLastToken().startsWith("бо")) {
                collection.add("борьба");
	    } else if (playerChatTabCompleteEvent.getLastToken().startsWith("б")) {
                collection.add("борьба");
                collection.add("блокирование");
                collection.add("бег");
	    } else if (playerChatTabCompleteEvent.getLastToken().startsWith("вын")) {
                collection.add("выносливость");
	    } else if (playerChatTabCompleteEvent.getLastToken().startsWith("выж")) {
                collection.add("выживание");
	    } else if (playerChatTabCompleteEvent.getLastToken().startsWith("вы")) {
                collection.add("выносливость");
                collection.add("выживание");
	    } else if (playerChatTabCompleteEvent.getLastToken().startsWith("вл")) {
                collection.add("владение оружием");
	    } else if (playerChatTabCompleteEvent.getLastToken().startsWith("вн")) {
                collection.add("внимательность");
	    } else if (playerChatTabCompleteEvent.getLastToken().startsWith("в")) {
                collection.add("владение оружием");
                collection.add("выносливость");
                collection.add("внимательность");
                collection.add("выживание");
	    } else if (playerChatTabCompleteEvent.getLastToken().startsWith("д")) {
                collection.add("диагностика");
	    } else if (playerChatTabCompleteEvent.getLastToken().startsWith("з")) {
                collection.add("зашивание ран");
	    } else if (playerChatTabCompleteEvent.getLastToken().startsWith("р")) {
                collection.add("реакция");
	    } else if (playerChatTabCompleteEvent.getLastToken().startsWith("к")) {
                collection.add("кулачный бой");
	    } else if (playerChatTabCompleteEvent.getLastToken().startsWith("ф")) {
                collection.add("физическая сила");
	    } else if (playerChatTabCompleteEvent.getLastToken().startsWith("с")) {
                collection.add("скрытность");
	    } else if (playerChatTabCompleteEvent.getLastToken().startsWith("хи")) {
                collection.add("хирургия");
	    } else if (playerChatTabCompleteEvent.getLastToken().startsWith("хо")) {
                collection.add("хорошо");
	    } else if (playerChatTabCompleteEvent.getLastToken().startsWith("х")) {
                collection.add("хирургия");
                collection.add("хорошо");
	    } else if (playerChatTabCompleteEvent.getLastToken().startsWith("первая помощь")) {
		collection.add("первая помощь");
	    } else if (playerChatTabCompleteEvent.getLastToken().startsWith("пр")) {
		collection.add("превосходно");
	    } else if (playerChatTabCompleteEvent.getLastToken().startsWith("пла")) {
                collection.add("плавание");
	    } else if (playerChatTabCompleteEvent.getLastToken().startsWith("пло")) {
                collection.add("плохо");
	    } else if (playerChatTabCompleteEvent.getLastToken().startsWith("пл")) {
                collection.add("плохо");
                collection.add("плавание");
	    } else if (playerChatTabCompleteEvent.getLastToken().startsWith("по")) {
		collection.add("посредственно");
	    } else if (playerChatTabCompleteEvent.getLastToken().startsWith("п")) {
		collection.add("плохо");
		collection.add("посредственно");
		collection.add("превосходно");
                collection.add("парирование");
                collection.add("плавание");
                collection.add("первая помощь");
	    } else if (playerChatTabCompleteEvent.getLastToken().startsWith("н")) {
		collection.add("нормально");
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

                collection.add("реакция");
                collection.add("владение оружием");
                collection.add("кулачный бой");
                collection.add("борьба");
                collection.add("парирование");
                collection.add("уклонение");
                collection.add("блокирование");
                collection.add("бег");
                collection.add("плавание");
                collection.add("акробатика");
                collection.add("физическая сила");
                collection.add("выносливость");
                collection.add("устойчивость к болезням");
                collection.add("внимательность");
                collection.add("скрытность");
                collection.add("выживание");
                collection.add("диагностика");
                collection.add("первая помощь");
                collection.add("зашивание ран");
                collection.add("хирургия");
	    }	
	}
    }	
//---}}} Tabs
}
