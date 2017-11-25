package KMChat;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

public class ReactionList {

    String name;
    public List<Reaction> rlist = new ArrayList<Reaction>();
    Pattern rPat = Pattern.compile("([\\p{L}0-9_-]{1,16})+\\s?(ужасно|плохо|посредственно|нормально|хорошо|отлично|превосходно|легендарно)?\\s?([+-]?[0-9]{1})?");

    ReactionList(String name) {
        this.name = name;
    }

    ReactionList(String name, String[] args)  {
	this.name = name;
        this.add(args);
    }

    public boolean add(String[] args) {
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

	    Reaction r = new Reaction(name, level, mod);
	    rlist.add(r);
        }
	return true;
    }

    public boolean remove(String[] args) {
	for (String a : args) {
	    for (Reaction r : rlist) {
		if (r.getPlayer().equals(a)) {
		    rlist.remove(r);
		    return true;
		}
	    }
	}
	return false;
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

    public String getName() {
	return name;
    }

    public int getNum() {
        int i = 0;
        for (Reaction r : rlist) i++;
        return i;
    }

    public String show() {
        String show = "§6" + this.getName() + ":\n§7";
        for (Reaction r : rlist) {
            show += r.show() + '\n';
        }
        return show+"§f";
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
