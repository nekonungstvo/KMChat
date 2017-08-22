package KMChat;

public class Range {
    private String permission;
    private String range;
    private String description;
    private String symbol;
    private String extraSymbol;

    Range(String ran, String perm, String desc, String sym) {
	permission = "KMChat." + perm;
	range = "range." + ran;
	description = desc;
	symbol = sym;
	extraSymbol = null;
    }
    
    Range(String ran, String perm, String desc, String sym, String extra) {
	if (sym.length() != extra.length()) {
	    throw new Error("Symbol and extra symbol must be the same length!");
	}
	permission = "KMChat." + perm;
	range = "range." + ran;
	description = desc;
	symbol = sym;
	extraSymbol = extra;
    }

    public String getPermission() { return permission; }
    public String getRange() { return range; }
    public String getDescription() { return description; }
    public String getSymbol() { return symbol; }
    
    public boolean matches(String str) {
	if (str.startsWith(symbol))
	    return true;
	if (extraSymbol != null && str.startsWith(extraSymbol))
	    return true;
	return false;
    }

}
