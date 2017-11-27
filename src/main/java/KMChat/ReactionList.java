package KMChat;
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

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

public class ReactionList {

    String[]  skillset = new String [] {"реакция", "владение оружием", "кулачный бой", "борьба", "парирование", "уклонение", "блокирование",
                                    "бег", "плавание", "акробатика", "физическая сила", "выносливость", "устойчивость к болезням", "внимательность",
                                    "скрытность", "выживание", "диагностика", "первая помощь", "зашивание ран", "хирургия"};

    String name;
    String DATA_PATH = "/srv/www/main_site/data/skillsets/"; //this is bad. really-really bad. TOFIX
    String turns = "";
    String info = "";
    public List<Reaction> rlist = new ArrayList<Reaction>();
    Pattern rPat = Pattern.compile("([\\p{L}0-9_-]{1,16})+\\s?(ужасно|плохо|посредственно|нормально|хорошо|отлично|превосходно|легендарно)?\\s?([+-]?[0-9]{1})?");

    ReactionList(String name) {
        this.name = name;
    }

    ReactionList(String name, String[] args) throws Exception  {
	this.name = name;
        this.add(args);
    }

    public void addInfo(String str) {
        info += str + '\n';
    }

    public String getInfo() {
        return info;
    }

    public void clearInfo() {
        info = "";
    }
    public void setName(String s) {
        name = s;
    }
    public void setTurns(String t) {
        turns = t;
    }
    public String getTurns() {
        return turns;
    }

    public void modTurns(String nick, String initial) {
    //    System.out.println("!!");
  //      System.out.println(nick +  " " + initial);
        this.turns = this.turns.replaceFirst(nick, (nick+"\\-"+initial));
        System.out.println(this.turns);
    }

    public String[] getPlayers() {
        String[] players = new String[this.getNum()];
        int i = 0;
        for (Reaction r : rlist) {
            players[i++] = r.getPlayer();
        }
        return players;
    }

    String getSkill(String nick) {
	//we need to have a file "Playernick.skills" with skills listed as following: "skill:level"
	Pattern skillpat = Pattern.compile("(.*):");
	Pattern levelpat = Pattern.compile(":(.*)");
        boolean changedNick = false;
	String skill = "реакция";
	String level = null;

	Path path = Paths.get(DATA_PATH, nick + ".skills");
	Charset charset = Charset.forName("UTF-8");
        try {
	    List<String> lines = Files.readAllLines(path, charset);
	    for (String line : lines) {
                System.out.println(line + " --- " + skill);
		if (line.startsWith(skill)) {
		    Matcher levelmat = levelpat.matcher(line);
		    while (levelmat.find()) {
		        level = levelmat.group(); //get the level of the skill
		    }
	        
                }
            }

	} catch (IOException e) {
	    System.out.println(e);
	    level = null;
	}
            if (level == null)
                return "ПЛОХО";
            String result = "";
	    //let's make the output pretty!
	    //System.out.println(result);
	    //System.out.println(desc);

	    return level.substring(1);

    }

    

    public boolean add(String[] args) throws Exception {
        try {
	String argsStr = "";
        for (int i = 0; i < args.length; i++) {
            argsStr += args[i];
            if (i != args.length-1)
                argsStr+=' ';
        }
        Matcher rMat = rPat.matcher(argsStr);
	String level;
	String mod;
        String name;
        while (rMat.find()) {
            name = "";
            level = "";
            mod = "0";
	    name = rMat.group(1);
            boolean cont = false;
	    for (Reaction r : rlist) {
		if (r.getPlayer().equals(name)) {
		    cont = true;
		}
	    }
            if (cont) {
                continue;
            }
            if (rMat.group(2) != null) {
		Pattern levelPat = Pattern.compile("(ужасно|плохо|посредственно|нормально|хорошо|отлично|превосходно|легендарно)");
		Matcher levelMat = levelPat.matcher(rMat.group(2));
		if (levelMat.matches()) {
		    level = levelMat.group();
                }
            }
            if (rMat.group(3) != null) {
                Pattern modPat = Pattern.compile("[+-]?[0-9]{1}");
		Matcher modMat = modPat.matcher(rMat.group(3));
		if (modMat.matches()) {
                    mod = modMat.group();
                }
            }
            if (level.equals("")) {
                level = getSkill(name);
          }
	    Reaction r = new Reaction(name, level, mod);
	    rlist.add(r);
        }
        } catch (Exception e) {
            throw e;
        }
	return true;
    }

    public boolean remove(String[] args) {
	for (String a : args) {
	    for (Reaction r : rlist) {
		if (r.getPlayer().equals(a)) {
		    rlist.remove(r);
		}
	    }
	}
	return true;
    }

    public boolean edit(String[] args)  {
        	String argsStr = "";
        for (int i = 0; i < args.length; i++) {
            argsStr += args[i];
            if (i != args.length-1)
                argsStr+=' ';
        }
     Matcher rMat = rPat.matcher(argsStr);
	String level = "";
	String mod = "0";
        while (rMat.find()) {
	    String name = rMat.group(1);
	    for (Reaction r : rlist) {
		if (r.getPlayer().equals(name)) {
            if (rMat.group(2) != null) {
		Pattern levelPat = Pattern.compile("(ужасно|плохо|посредственно|нормально|хорошо|отлично|превосходно|легендарно)");
		Matcher levelMat = levelPat.matcher(rMat.group(2));
		if (levelMat.matches()) {
		    level = levelMat.group();
                    r.setLevel(level);
                }
            }
            if (rMat.group(3) != null) {
                Pattern modPat = Pattern.compile("[+-]?[0-9]{1}");
		Matcher modMat = modPat.matcher(rMat.group(3));
		if (modMat.matches()) {
                    mod = modMat.group();
                    r.setMod(mod);
                }
            }
            }
            }
            }
           // rlist.add(r);
           return true;
        }

    public String getListName() {
	return name;
    }

    public int getNum() {
        int i = 0;
        for (Reaction r : rlist) i++;
        return i;
    }

    public String show() {
        String show = "§6" + this.getListName() + ":\n§7";
        for (Reaction r : rlist) {
            show += r.show() + '\n';
        }
        return show+"§f";
    }
    public String[] getLevels() {
        int num = this.getNum();
        String[] lvls = new String[num];
        int i = 0;
        for (Reaction r : rlist) {
            lvls[i++] = r.getLevel();
        }
        return lvls;

    }

    public String[] getDices() {
        int num = this.getNum();
        String[] rolls = new String[num];
        int i = 0;
        for (Reaction r : rlist) {
            rolls[i++] = r.getDice();
        }
        return rolls;
    }
}
