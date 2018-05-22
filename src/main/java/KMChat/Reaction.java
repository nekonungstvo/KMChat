package KMChat;
import java.util.Map;
import java.util.Hashtable;


public class Reaction {
    
    private Map<Integer, String> nMap = new Hashtable<Integer, String>();


    String player = null;
    String level = "";
    int mod = 0;
    int woundsMod = 0;
    int armorMod = 0;

    Reaction() {};

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
    
    public boolean setWoundsMod(int m) {
	this.woundsMod = m;
	return true;
    }
    
    public boolean setArmorMod(int m) {
	this.armorMod = m;
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
    public int getFinalMod() {
        return mod + armorMod + woundsMod;
    }

    public String getModStr() {
	String modStr = "";
        if (woundsMod == -666 || armorMod == -666)
            return modStr + " §4без учёта брони и ран§e";

        if (mod!=0)
            modStr = Integer.toString(mod);
            if (mod > 0)
                modStr = "+" + modStr;
        
        if (woundsMod!=0)
            modStr = "§4" + Integer.toString(woundsMod) + "§e " + modStr;
        if (armorMod!=0)
            modStr += " §8" + Integer.toString(armorMod) + "§e";
        modStr = modStr.replaceAll("\\s\\s", " ");
	return modStr.replace("  ", " ");
    }

    public String show() {
        String sign = "";
        String out = (player + " " + level + " " + getModStr().replace("§e","§7"));
        return out;
    }

    public String getDice() {
        String sign = "";
        if (this.getFinalMod() > 0) sign = "+";

        //если мы используем автобросок
        if (this.level.equals("")) {
            String helpMod = "";
            if (getFinalMod() != 0) helpMod = Integer.toString(getFinalMod());
            String mes4dice = "реакция " + sign + helpMod + this.player;
            return mes4dice;
        }
        //если мы используем уровень навыка вместо автоброска
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
        levelInt += getFinalMod();
        String helpMod = "";
        if (getFinalMod() != 0) helpMod = Integer.toString(getFinalMod());
            

        if (!sign.equals("")) sign = " " + sign;
        if (getFinalMod() < 0) helpMod = " " + helpMod;
        String mes = nMap.get(levelInt) + " " + this.player + " реакция"  + sign + helpMod;
        return mes;
    }


}
