package KMChat;
import java.util.Map;
import java.util.Hashtable;

public class Reaction {
    
    private Map<Integer, String> nMap = new Hashtable<Integer, String>();


    String player = null;
    String level = "";
    int mod = 0;

    Reaction (String pl) {
	player = pl;
    }

    Reaction(String pl, String lvl) {
	player = pl;
	level = lvl;
    }

    Reaction(String pl, String lvl, String m) {
	player = pl;
	level = lvl;
	mod = Integer.parseInt(m);
    }

    public boolean setMod(String m) {
	int mod = Integer.parseInt(m);
	if (mod < -9 || mod > 9)
	    return false;
	this.mod = mod;
	return true;
    }

    public boolean setLevel(String level) {
        this.level = level;
        return true;
    }

    public String getPlayer() {
	return player;
    }
    public String getLevel() {
	return level;
    }
    public int getMod() {
	return mod;
    }
    public String getModStr() {
	String modStr = Integer.toString(mod);
	return modStr;
    }

    public String show() {
        String sign = "";
        if (mod > 0)
            sign = "+";
        String out = (player + " " + level + " " + sign + mod);
        return out;
    }

    public String getDice() {
            String sign = "";
            if (this.mod > 0) sign = "+"; 
        if (this.level.equals("")) {
            String helpMod = "";
            if (mod != 0) helpMod = Integer.toString(mod);
            String mes4dice = "реакция " + sign + helpMod + this.player;
            return mes4dice;
        }

	nMap.put(-3, "абсолютно ублюдски");
        nMap.put(-2, "ужасно---");
        nMap.put(-1, "ужасно--");
        nMap.put(-1, "ужасно--");
        nMap.put(0, "ужасно-");
        nMap.put(1, "ужасно");
        nMap.put(2, "плохо");
        nMap.put(3, "посредственно");
        nMap.put(4, "нормально");
        nMap.put(5, "хорошо");
        nMap.put(6, "отлично");
        nMap.put(7, "превосходно");
        nMap.put(8, "легендарно");
        nMap.put(9, "легендарно+");
        nMap.put(10, "легендарно++");
        nMap.put(11, "легендарно+++");	
        nMap.put(12, "божественно");
        int levelInt = 2;
        for (Map.Entry<Integer, String> entry : nMap.entrySet()) {
            if (level.equals(entry.getValue())) {
                levelInt = entry.getKey();
            }
        }
        levelInt += mod;
        String helpMod = "";
        if (mod != 0) helpMod = Integer.toString(mod);
        if (!sign.equals("")) sign = " " + sign;
        if (mod < 0) helpMod = " " + helpMod;
        String mes = nMap.get(levelInt) + " " + this.player + " реакция"  + sign + helpMod;
        return mes;
    }
}
