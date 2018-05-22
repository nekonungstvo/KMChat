    package KMChat;

    import java.util.*;
    import java.util.logging.Logger;
    import java.util.regex.Matcher;
    import java.util.regex.Pattern;
    import java.nio.file.*;
    import java.nio.charset.*;
    import java.io.*;
    import java.net.URL;
    import java.net.URLConnection;

    import org.apache.commons.codec.binary.Base64;

    import org.json.*;

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
    import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
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
        private static long CHID;
        private static String DATA_PATH;
        private static String modifiersUrl;
        private static String modifiersAuth;
        private static IDiscordClient client;
        private static IChannel ingameChannel;
        private Logger log = Logger.getLogger("Minecraft");
        private String path = "logs/";
        private Map<Integer, String> nMap = new Hashtable<Integer, String>();
        private Map<String, String> reminders = new Hashtable<String, String>();
        private Range[] allRanges = new Range[6];
        private String[] skillset;
        private Random rnd = new Random();
        private List<ReactionList> sprReactionList = new ArrayList<ReactionList>();
        private List<String> whoUseAutoGM;
        private List<String> whoUseAutoBD;
        private Map<String, Double> customRanges = new HashMap<String, Double>();
        private boolean wasrestarted = false;

        public void onEnable() {

            this.getConfig().options().copyDefaults(true);
            this.saveConfig();

            path = this.getConfig().getString("logsdir");
            modifiersUrl = this.getConfig().getString("modifiersUrl");
            modifiersAuth = this.getConfig().getString("modifiersAuth");
            TOKEN = this.getConfig().getString("bottoken");
            CHID = this.getConfig().getLong("channelid");
            DATA_PATH = this.getConfig().getString("datastorage");
            whoUseAutoGM = this.getConfig().getStringList("whoUseAutoGM");
            whoUseAutoBD = this.getConfig().getStringList("whoUseAutoBD");

            String remindersHolder = "plugins/KMChat/jreminders.json";
            JSONObject jreminders = null;
            try (BufferedReader br = new BufferedReader(new FileReader(remindersHolder))) {
                String line;
                if ((line = br.readLine()) != null) {
                    jreminders = new JSONObject(line);
                }
            } catch (Exception e) {
                System.out.println("Error enabling KMChat: " + e);
            }
            if (jreminders != null) {
                Iterator<String> keys = jreminders.keys();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    String value = jreminders.getString(key);
                    reminders.put(key, value);
                }
            }

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
            allRanges[1] = whisper;          //==
            allRanges[2] = strongwhisper; //=
            allRanges[3] = strongshout;   //!!!
            allRanges[4] = shout;          //!!
            allRanges[5] = weakshout;     //!   order is important

            this.getServer().getPluginManager().registerEvents((Listener) this, (Plugin) this);
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
    //      this.nMap.put(12, "ТАК ЛЕГЕНДАРНО, ЧТО ПОПАДЁТ ВО ВСЕ КНИГИ РЕКОРДОВ");
    //      this.nMap.put(12, "божественно");
            this.nMap.put(12, "КАК АЛЛАХ");

            skillset = new String[]{"реакция", "владение оружием", "кулачный бой", "борьба", "парирование", "уклонение", "блокирование",
                    "бег", "плавание", "акробатика", "физическая сила", "выносливость", "устойчивость к болезням", "внимательность",
                    "скрытность", "выживание", "диагностика", "первая помощь", "зашивание ран", "хирургия", "сила", "передвижение", "врачевание"};

            this.log.info(String.format("%s is enabled!", this.getDescription().getFullName()));
        }

        public void onDisable() {
            RequestBuffer.request(() -> ingameChannel.sendMessage("**Server is going offline!**"));
            JSONObject jreminders = new JSONObject(reminders);
            try (FileWriter file = new FileWriter("plugins/KMChat/jreminders.json")) {
                file.write(jreminders.toString());
                System.out.println("Successfully Copied JSON Object to File...");
                System.out.println("\nJSON Object: " + jreminders);
            } catch (Exception e) {
                System.out.println("[ERROR] writing JSON:\n" + e);
            }
            this.log.info(String.format("%s is disabled!", this.getDescription().getFullName()));
        }

        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent playerJoinEvent) {
            String name = playerJoinEvent.getPlayer().getName();
            String joinMessage = "§e" + name + "§f входит в игру";
            String ip = playerJoinEvent.getPlayer().getAddress().getHostName();
            String message = name + " (" + ip + ")" + " входит в игру";
            kmlog("whole", message);
            kmlog("chat", message);
            final String snd = message.replaceAll(name, "__" + name + "__");
            RequestBuffer.request(() -> ingameChannel.sendMessage(snd));
            try (FileWriter writer = new FileWriter(path + "ipgame.log", true)) {
                writer.write(name + " " + ip + "\n");
            } catch (IOException ex) {
                System.out.println("Error while writing player's ip: " + ex.getMessage());
            }
            for (String key : reminders.keySet()) {
                if (key.equalsIgnoreCase(name)) {
                    String reminder = reminders.get(key);
    //		joinMessage += "\n§e~"+reminder+" ~§f";
                    playerJoinEvent.getPlayer().sendMessage("§6~" + reminder + "~§f");
                    RequestBuffer.request(() -> ingameChannel.sendMessage("Player **" + name + "** was reminded of:\n" + reminder));
                    reminders.remove(key);
                    break;
                }
            }
            playerJoinEvent.setJoinMessage(joinMessage);
        }

        @EventHandler
        public void onPlayerLeave(PlayerQuitEvent playerQuitEvent) {
            Player player = playerQuitEvent.getPlayer();
            String name = player.getName();
            customRanges.remove(player.getName());
            playerQuitEvent.setQuitMessage("§e" + name + "§f выходит из игры");
            String ip = player.getAddress().getHostName();
            String message = name + " (" + ip + ")" + " выходит из игры";
            kmlog("whole", message);
            kmlog("chat", message);
            RequestBuffer.request(() -> ingameChannel.sendMessage(message.replaceAll(name, "__" + name + "__")));
        }


        @EventHandler
        public void onPlayerChat(AsyncPlayerChatEvent asyncPlayerChatEvent) {
            //Long beg = System.nanoTime();
            Player player = asyncPlayerChatEvent.getPlayer();
            int rangePosition = 6; // if no range specified, lands on the last element of array with descriptions

            boolean local = true;
            boolean forgm = false; //to gm chat
            boolean gmnotice = false; //***this kind of messages used by GM's***
            boolean forbd = false; //to builders' chat
            boolean norec = false; //no recipients at all
            boolean sendraw = false; //either to send a raw message to discord
            boolean strippedColon = false;

            String mes = asyncPlayerChatEvent.getMessage();
            String raw = mes;
            String name = player.getName();
            Range setRange = null;
            double range = this.getConfig().getInt("range.default");


            String adminprefix = "";
            if (player.hasPermission("KMChat.prefix")) {
                adminprefix = this.getConfig().getString("adminprefix");
            } else if (player.hasPermission("KMChat.builder") && !player.hasPermission("KMChat.prefix")) {
                adminprefix = this.getConfig().getString("bdprefix");
            }

            for (String nick : whoUseAutoGM) {
                if (player.getName().equalsIgnoreCase(nick)) {
                    if (mes.startsWith(":")) {
                        mes = mes.substring(1);
                        strippedColon = true;
                    }
                    break;
                }
            }
            for (String nick : whoUseAutoBD) {
                if (player.getName().equalsIgnoreCase(nick)) {
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
            String usesGM = null;
            Pattern dicePat = Pattern.compile("d(4|6|8|10|12|14|20|100).*");
            Matcher diceMat = dicePat.matcher(mes);
            for (String nick : whoUseAutoGM) {
                if (player.getName().equalsIgnoreCase(nick)) {

                    usesGM = nick;
                    if (mes.startsWith(";") || mes.startsWith("#") || mes.startsWith("%") || mes.startsWith("_") || mes.startsWith("-") || mes.startsWith("№") || diceMat.matches() || strippedColon) {

                        break;
                    } else {
                        mes = "-" + mes;
                        forgm = true;
                    }
                    break;
                }
            }
            for (String nick : whoUseAutoBD) {
                if (player.getName().equalsIgnoreCase(nick) && !player.getName().equalsIgnoreCase(usesGM)) {
                    if (mes.startsWith(";") || mes.startsWith("#") || mes.startsWith("%") || mes.startsWith("_") || mes.startsWith("-") || mes.startsWith("№") || diceMat.matches() || strippedColon) {
                        break;
                    } else {
                        mes = ";" + mes;
                        forbd = true;
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
                String[] vars = {"едва слышно бросает",
                        "очень тихо бросает",
                        "тихо бросает",
                        "СВЕРХГРОМКО ОБРУШИВАЕТ",
                        "очень громко бросает",
                        "громко бросает",
                        "бросает"};
                String helpmes = mes.substring(1);
                String dice = dnum(helpmes);
                if (dice != null) {
                    result = String.format("&e(( &a%s &e%s %s ))&f", name, vars[rangePosition], dice);
                } else {
                    result = String.format("&a%s&f%s: %s", name, describeRange, mes);
                }

            } else if (mes.startsWith("%")) {
                sendraw = true;
                String[] vars = {"едва слышно бросает",
                        "очень тихо бросает",
                        "тихо бросает",
                        "СВЕРХГРОМКО ОБРУШИВАЕТ",
                        "очень громко бросает",
                        "громко бросает",
                        "бросает"};
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
                    result = String.format("&e(( &a%s&e %s 4dF %s ))&f", name, vars[rangePosition], dice);

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
                String[] vars = {"~",
                        "*",
                        "**",
                        "******",
                        "*****",
                        "****",
                        "***"};
                String[] gmRanges = {"closestdm",
                        "closerdm",
                        "closedm",
                        "farestdm",
                        "farerdm",
                        "fardm",
                        "default"};
                range = this.getConfig().getInt("range." + gmRanges[rangePosition]);
                mes = mes.substring(1);
                result = "&e" + vars[rangePosition] + mes + vars[rangePosition];

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
            } else if (mes.startsWith(";") && player.hasPermission("KMChat.builder")) {
                if (mes.startsWith("; ")) {
                    mes = mes.substring(2);
                } else {
                    mes = mes.substring(1);
                }
                result = String.format("%s&a%s &f(to BD): &3(( %s ))&f", adminprefix, name, mes);
                forbd = true;

            } else if (mes.startsWith(":msg") || mes.startsWith("msg")) {
                if (!player.hasPermission("KMCore.tell")) {
                    player.sendMessage("§4Недостаточно прав.§f");
                } else {
                    player.sendMessage("§4Используйте /msg!");
                    norec = true;
                }
            }


            if (local && !forgm && !forbd && !gmnotice && mes.startsWith("((") && mes.endsWith("))")) {
                String str = "";
                if (setRange != null) {
                    str = setRange.getDescription().replaceAll("[),(, ]", "") + " в ";
                    if (rangePosition == 0) {
                        str = "едва слышно в ";
                    }
                }
                result = String.format("%s&a%s&f (%sOOC): &d%s&f", adminprefix, name, str, mes);
                if (result.matches(".*\\(\\(\\S.*")) {
                    result = result.replace("((", "(( ");
                }
                if (result.matches(".*\\S\\)\\).*")) {
                    result = result.replace("))", " ))");
                }

            }


            result = result.replaceAll("%", "%%");
            result = result.replaceAll("&([a-z0-9])", "§$1");
            result = result.replaceAll("\\]\\s\\.", "].");
            result = result.replaceAll("\\s\\]", "]");
            asyncPlayerChatEvent.setFormat(result);
            asyncPlayerChatEvent.setMessage(mes);
            kmlog("whole", result);
            kmlog("chat", result);
            String res2discord = result.replaceAll("§([a-z0-9])", "");
            res2discord = res2discord.replace(player.getName(), "**" + player.getName() + "**");
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
                        if (!player2.getWorld().equals((Object) player.getWorld())) {
                            result = result.replaceAll("§f", "§7");
                            player2.sendMessage(result);
                        } else if (player.getLocation().distanceSquared(player2.getLocation()) > range * range) {
                            result = result.replaceAll("§f", "§7");
                            player2.sendMessage(result);
                        } else {
                            recips.add(player2);
                        }
                    }
                }
                asyncPlayerChatEvent.getRecipients().addAll(recips);
            } else if (forbd) {
                asyncPlayerChatEvent.getRecipients().clear();
                LinkedList<Player> recips = new LinkedList();
                recips.add(player);
                for (Player player2 : Bukkit.getServer().getOnlinePlayers()) {
                    if (player2.hasPermission("KMChat.builder")) {
                        if (!player2.getWorld().equals((Object) player.getWorld())) {

                            result = result.replaceAll("§f", "§7");
                            player2.sendMessage(result);

                        } else if ((player.getLocation().distanceSquared(player2.getLocation())) > range * range) {

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
        }


        //---}}} Various helpers

        protected List<Player> getLocalRecipients(Player player, String mes, double d) {


            Location location = player.getLocation();
            LinkedList<Player> linkedList = new LinkedList<Player>();
            double d2 = Math.pow(d, 2.0);
            for (Player player2 : Bukkit.getServer().getOnlinePlayers()) {
                if (player2.hasPermission("KMChat.admin")) {
                    if (!player2.getWorld().equals((Object) player.getWorld())) {
                      //  if (gmInRange(player2.getName(), 0)) {
                            mes = mes.replaceAll("§e", "§7");
                            mes = mes.replaceAll("§f", "§7");
                            player2.sendMessage(mes);
                            continue;
                   //     }
                    }
                    if (location.distanceSquared(player2.getLocation()) > d2) {
                   //     System.out.println("PRE_LOCATION: "+location.distanceSquared(player2.getLocation()) + '\n'+ "PRE_D2: " + d2);
                        if (gmInRange(player2.getName(), location.distanceSquared(player2.getLocation()) )) {
                            mes = mes.replaceAll("§e", "§7");
                            mes = mes.replaceAll("§f", "§7");
                            player2.sendMessage(mes);
                            continue;
                        }
                    }
                    if (gmInRange(player2.getName(), location.distanceSquared(player2.getLocation()) )) {
                        linkedList.add(player2);
                    }
                    continue;
                }
                if (!player2.getWorld().equals((Object) player.getWorld()) || location.distanceSquared(player2.getLocation()) > d2)
                    continue;

               // if (!player2.hasPermission("KMChat.gm")) {
              //      linkedList.add(player2);
              //  } else if (gmInRange(player2.getName(), d2)) {
                    linkedList.add(player2);
             //   }

            }
            return linkedList;
        }

        protected void kmlog(String where, String what) {
            try (FileWriter writer = new FileWriter(path + where + "/" + where + "_current.log", true)) {
                Date date = new Date();
                what = what.replaceAll("§.", "");
                writer.write("[" + date.toString() + "] " + what + "\n");
            } catch (IOException ex) {
                System.out.println("Error in kmlog(): " + ex.getMessage());
            }
        }

        public void send2discord(String message) {
            if (message.length() > 2000 && message.length() < 4000) {
                final String snd1 = message.substring(0, 2000);
                RequestBuffer.request(() -> ingameChannel.sendMessage(snd1));
                final String snd2 = message.substring(2000);
                RequestBuffer.request(() -> ingameChannel.sendMessage(snd2));
            } else if (message.length() > 4000) {
                final String snd1 = message.substring(0, 2000);
                RequestBuffer.request(() -> ingameChannel.sendMessage(snd1));
                final String snd2 = message.substring(2000, 4000);
                RequestBuffer.request(() -> ingameChannel.sendMessage(snd2));
                final String snd3 = message.substring(4000);
                RequestBuffer.request(() -> ingameChannel.sendMessage(snd3));

            } else {
                final String snd = message;
                RequestBuffer.request(() -> ingameChannel.sendMessage(snd));
            }

        }

        public boolean gmInRange(String nick, double distance) {
           // System.out.println("gmInRange("+nick+", "+distance);
            //System.out.println("customRange: "+customRanges.get(nick));
            for (String str : customRanges.keySet()) {
                if (str.equalsIgnoreCase(nick)) {
                    double customRange = customRanges.get(nick);
                    if (distance < Math.pow(customRange, 2.0)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
            return true;
        }

        public String getSkill(String desc, String nick) {
            //we need to have a file "Playernick.skills" with skills listed as following: "skill:level"
            Pattern skillpat = Pattern.compile("(.*):");
            Pattern levelpat = Pattern.compile(":(.*)");
            boolean changedNick = false;
            String skill = null;
            String level = null;
            String oldskill = null;
            String gmnick = nick;
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                if (desc.startsWith(player.getName())) {
                    if (getServer().getPlayer(nick).hasPermission("KMChat.gm")) {
                        nick = player.getName();
                        try {
                            desc = desc.substring(nick.length() + 1);
                        } catch (Exception e) {
                            getServer().getPlayer(gmnick).sendMessage("§4Что-то пошло не так. Вы уверены, что ввели название навыка?");
                            return null;
                        }
                        changedNick = true;
                        break;
                    }
                }
            }

            Path path = Paths.get(DATA_PATH, nick + ".skills");
            Charset charset = Charset.forName("UTF-8");
            try {
                List<String> lines = Files.readAllLines(path, charset);

                for (String line : lines) {
                    Matcher skillmat = skillpat.matcher(line);
                    while (skillmat.find()) {
                        skill = skillmat.group().replaceAll(":", ""); //get the name of the skill
                    }
                    if (desc.toLowerCase().startsWith(skill)) { //check if this is the skill that we need
                        Matcher levelmat = levelpat.matcher(line);
                        while (levelmat.find()) {
                            level = levelmat.group(); //get the level of the skill
                        }
                        break;
                    } else {
                        skill = null;
                    }
                }



                if (skill == null) { //if we didn't find the needed skill in the file

                    for (String sk : skillset) {
                        if (desc.toLowerCase().startsWith(sk)) { //check if skill name is legit
                            oldskill = desc.substring(0, sk.length());
                            skill = sk; //set the skill name
                           // System.out.println("skill is " + skill + ", level is " + level);
                            break;
                        } else {
                            level = null; //level is ПЛОХО if we didn't find skill in file
                        }
                    }
                }

            } catch (IOException e) {
                System.out.println("Error getting skill:" + e);
                level = null;
            }

         //   System.out.println("SKILL: " + skill);
            if (level == null) {
                level = ":ПЛОХО";
            }
       //     System.out.println("!! skill is " + skill + ", level is " + level + ", oldskill = " + oldskill);
            if (skill == null) //if skill name is not legit and not found in the file
                return null;
            String result = "";
            oldskill = desc.substring(0, skill.length());
            if (oldskill == null) oldskill = skill;
            if (changedNick) {
                result = desc.replaceFirst(oldskill, level + " [" + nick + " " + skill + "] (");
            } else {
                result = desc.replaceFirst(oldskill, level + " [" + skill + "] (");
            }
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
                if (levelName.equalsIgnoreCase(entry.getValue())) {
                    return entry.getKey();
                }
            }
            return 666;
        }

        public boolean containsSkillValue(String message) {
            for (Map.Entry<Integer, String> entry : nMap.entrySet()) {
                if (message.toLowerCase().contains(entry.getValue())) {
                    return true;
                }
            }
            return false;

        }

        public String dF(String mes, String nick, boolean returnNumber) {
            boolean weusedskill = false;
            String result = "(";
            String initialNick = nick;
            int n = 666; //skill level represented as number
            float mod = 0; //optinal modificator to skill level

            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                        if (mes.startsWith((player.getName()))) {
                            nick = player.getName();
                            break;
                        }
                }


        if (!containsSkillValue(mes)) {
            //int wordsNum = 1;
            //String[] split = mes.split(" ", wordsNum + 2); //extra spot for optional modificator
            String skillmes = getSkill(mes, initialNick);
            if (skillmes != null) {
                String f_word = mes.split(" ")[0];
                String s_word = "";
                if (mes.split(" ").length > 1)
                s_word = mes.split(" ")[1];

            if ( (f_word.contains("бег") || s_word.contains("бег")) && skillmes.contains("ПЛОХО")) {
                String anotherTry = getSkill(mes.replaceFirst(nick, "").trim().toLowerCase().replace("бег", "передвижение"), nick);
                if (anotherTry!=null && !anotherTry.contains("ПЛОХО"))
                    skillmes = anotherTry;
            } else if ( (f_word.contains("передвижение") || s_word.contains("передвижение")) && skillmes.contains("ПЛОХО")) {
                String anotherTry = getSkill(mes.replaceFirst(nick, "").trim().toLowerCase().replace("передвижение", "бег"), nick);
                if (anotherTry!=null && !anotherTry.contains("ПЛОХО"))
                    skillmes = anotherTry;
            }

            if ( !mes.toLowerCase().contains("физическая") && (f_word.contains("сила") || s_word.contains("сила")) && skillmes.contains("ПЛОХО")) {
                String anotherTry = getSkill(mes.replaceFirst(nick, "").trim().toLowerCase().replace("сила", "физическая сила"), nick);
                if (anotherTry!=null && !anotherTry.contains("ПЛОХО"))
                    skillmes = anotherTry;
            } else if ( (f_word.contains("физическая") || s_word.contains("физическая")) && skillmes.contains("ПЛОХО")) {
                //System.out.println(mes.toLowerCase().replace("физическая ", ""));
                String anotherTry = getSkill(mes.replaceFirst(nick, "").trim().toLowerCase().replace("физическая ", ""), nick);
                //System.out.println(skillmes);
                if (anotherTry!=null && !anotherTry.contains("ПЛОХО"))
                    skillmes = anotherTry;
            }
            }
            
 

            if (skillmes != null) {
                mes = skillmes;
                float armorMod = 0;
                String[] armorDependentSkills = {"реакция", "уклонение", "передвижение", "бег", "акробатика", "скрытность"};
                for (String skill : armorDependentSkills) {
                    if (skillmes.toLowerCase().contains(skill)) {
                        armorMod = getArmorMod(nick);
                        break;
                    }
                }

                String armorModStr = "";
                if (armorMod == -666)
                    armorMod = 0;
                if (armorMod != 0)
                    armorModStr = "§8"+Integer.toString((int)armorMod)+"§e";

                Pattern ourSkillPat = Pattern.compile("\\(([-,+][0-9]).*");
                Matcher ourSkillMat = ourSkillPat.matcher(skillmes);
                String strmod = "";
                if (ourSkillMat.find()) {
                    strmod = ourSkillMat.group(1);
                }
		
		float woundsMod = getWoundsMod(nick);
		String woundsModStr = "";
		if (woundsMod == -666) {
		    woundsModStr = " §4без учёта ран и брони§e";
                    woundsMod = 0;
		}
		else if (woundsMod != 0) {
		    woundsModStr = "§4" + Integer.toString((int) woundsMod) +"§e";
		}
                List<String> listMods = new ArrayList<>();
                listMods.add(woundsModStr);
                listMods.add(strmod);
                listMods.add(armorModStr);
                String joinedMods = " " + String.join(" ", listMods).trim(); 
                
                if (strmod != "") {
                    if (strmod.matches("[-,+][0-9]")) {
                        mod = Float.parseFloat(strmod);
			mes = mes.replace("] (" + strmod, joinedMods + "] (");
                    }
                } else {
                    mes = mes.replace("]", joinedMods+"]");

                }
		mod = mod + woundsMod + armorMod;
                if (mes.contains("()"))
                    mes = mes.replaceFirst("\\(\\)", "");
                if (mes.contains("  "))
                    mes = mes.replaceFirst("\\s\\s", " ");
                //else if (woundsModStr != "") {
		//    mes = mes.replace("]", woundsModStr + "]");
		//    mod = woundsMod;

		//}
		    weusedskill = true;
            }
        }
        //System.out.println("SKILL = " + mes);
        String level = "";
        String comment = "";
        try {
            level = mes.split(" ", 2)[0];
            if (!weusedskill) { //if we used skill, comment is already inside () braces
                comment = mes.split(" ", 2)[1];
                mes = mes.replace(comment, "(" + comment + ")");
            }
        } catch (Exception e) {
            System.out.println("dF() caugth exception: " + e);
            level = mes;
        }
	/*
	for (Map.Entry<Integer, String> entry : nMap.entrySet()) {
	    if (level.equalsIgnoreCase(entry.getValue())) {
		n = entry.getKey();
		break;
	    }
	}
	*/
        n = getSkillValue(level);
//        System.out.println("level: " + level);
        
        if (level.equals("ПЛОХО"))
            n = 2;

        if (n == 666) {
  //      System.out.println(n);
            return null;
//        } else if (n < 1) {
//		n = 1;
//	} else if (n > 8) {
//		n = 8;
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
            } catch (Exception e) {
                System.out.println("dF() caught exception: " + e.getMessage());
            }
        }

        int[] dices = {-1, 0, 1};
        int dice = 0;
        for (int i = 0; i < 4; ++i) {
            int rndInt = rnd.nextInt(3);
            dice = dices[rndInt];
            if (dice < 0) {
                n -= 1;
                result += "-";
            } else if (dice > 0) {
                n += 1;
                result += "+";
            } else {
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


    public String dF(String mes, String nick) {
        return dF(mes, nick, false);
    }

    public String dF(String mes) {
        return dF(mes, "", false);
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
                } else {
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

    public int getNumFromDice(String dice) {
        Pattern skillPat = Pattern.compile("Результат:(§.)? ((абсолютно ублюдски|ужасно|плохо|посредственно|нормально|хорошо|отлично|превосходно|легендарно|КАК АЛЛАХ)[+-]{0,3})");
        Matcher skillMat = skillPat.matcher(dice);
        skillMat.find();
        String found = skillMat.group(2);
        int n = 666;
        for (Map.Entry<Integer, String> entry : nMap.entrySet()) {
            if (found.equalsIgnoreCase(entry.getValue())) {
                n = entry.getKey();
                break;
            }
        }
        return n;

    }

    public String reroll(String dice, ReactionList list) {
        Pattern pat1 = Pattern.compile("от (абсолютно ублюдски|ужасно|плохо|посредственно|нормально|хорошо|отлично|превосходно|легендарно|КАК АЛЛАХ)(.*)\\.\\sРезультат");
        Matcher mat1 = pat1.matcher(dice);
        mat1.find();
        String initial = mat1.group(1);
        String desc = mat1.group(2);
        String newroll = simpledF(initial, desc);
        String newrollSave = newroll;
        newroll = dice.substring(0, 6) + newroll.substring(6);
        Pattern nickPat = Pattern.compile("от (абсолютно ублюдски|ужасно|плохо|посредственно|нормально|хорошо|отлично|превосходно|легендарно|КАК АЛЛАХ)[-+]{0,4}\\s(\\[.*\\]\\s)?\\(([\\p{L}0-9_-]{1,16})+.*\\sРезультат:§?.?\\s(.*)");
        Matcher nickMat = nickPat.matcher(dice);
        System.out.println(dice);
        nickMat.find();
         String old = nickMat.group(4);
        String repl = nickMat.group(3) + "\\$" + nickMat.group(4);
        String nick = nickMat.group(3);
        if (!desc.contains("$")) {
            newroll = newroll.replaceFirst(nick, repl);
            newrollSave = newrollSave.replaceFirst(nick, repl);
        }
        list.addInfo(nickMat.group(3) + ": " + nickMat.group(4));
        String logged = "### " + dice + " ---> " + newrollSave;
        logged = logged.replaceAll("§.", "");
        kmlog("chat", logged);
        kmlog("whole", logged);
        send2discord(logged);
        return newroll;
    }

    public boolean findReroll(String[] dices, int beg, ReactionList list) {
        return findReroll(dices, beg, list, false);
    }

    public boolean findReroll(String[] dices, int beg, ReactionList list, boolean isItRerolled) {
        //if (isItRerolled) System.out.println("IT IS REROLLED!");
        //System.out.println("HERE IS WHAT WE GET:\nbeg = " + beg + ", dices:");
        if (beg == dices.length) return true;
        //int irrelevantCounter = 0;
        //for (String str : dices) {
        //    System.out.println(irrelevantCounter++ + str);
        //}
        List<String> repeats = new ArrayList<String>();
        String first = "";
        int begin = beg; //this is used to ignore previously rerolled dice
        if (begin >= dices.length) return false;
        //      System.out.println("started with: " + begin);
//        if (begin > 0) System.out.println("...which is " + dices[begin]);
        int end = -1;
        List<String> rerolled = new ArrayList<String>();
        for (int i = begin + 1; i < dices.length; i++) {
            if (!first.equals("") && (getNumFromDice(dices[i]) == getNumFromDice(first))) {
                repeats.add(dices[i]);
                end = i;
            } else if ((end < 0 &&
                    (i < dices.length - 1 &&
                            (getNumFromDice(dices[i]) == getNumFromDice(dices[i + 1]))))
                    ) {
                //if (dices[i].contains("§") && dices[i+1].contains("§") || !dices[i].contains("§") && !dices[i+1].contains("§")) {
//                if (!dices[i].contains("§") || !dices[i+1].contains("§")) {
                begin = i;
                first = dices[i];
                repeats.add(dices[i]);
                //              }
            }
        }
        if (begin < 0) return false;
        for (String dice : repeats) {
            String r = reroll(dice, list);
            rerolled.add(r);
//            System.out.println(r);
        }
        String[] rerolledStrArr = rerolled.toArray(new String[rerolled.size()]);
        bubbleSort(rerolledStrArr);
        findReroll(rerolledStrArr, -1, list, true);
        int j = 0;
        if (begin < 0) return false;
        for (int i = begin; i <= end; i++) {
            //System.out.println("--> " + begin + " <---");
            dices[i] = rerolledStrArr[j++];
        }
        //if (isItRerolled) return false;
        //System.out.println("CONTINUE FINDING REROLLS WITH END = " + end);
        if (end > 0)
            findReroll(dices, end, list);
        //    System.out.println();
        return false;
    }


    void bubbleSort(String[] arr) {
        for (int i = arr.length - 1; i > 0; i--) {
            for (int j = 0; j < i; j++) {
                if (getNumFromDice(arr[j]) > getNumFromDice(arr[j + 1])) {
                    String t = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = t;
                }
            }
        }
    }


    public String simpledF(String level, String desc) {
        int n = 666; //skill level represented as number
        float mod = 0; //optinal modificator to skill level


        String result = "";
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

        int[] dices = {-1, 0, 1};
        int dice = 0;
        for (int i = 0; i < 4; ++i) {
            int rnd = new Random().nextInt(3);
            dice = dices[rnd];
            if (dice < 0) {
                n -= 1;
                result += "-";
            } else if (dice > 0) {
                n += 1;
                result += "+";
            } else {
                result += "=";
            }
        }
        if (n < -3) {
            n = -3;
        } else if (n > 12) {
            n = 12;
        }

        return "(" + result + ") от " + level + desc + ". Результат:§o " + nMap.get(n);
    }
    
 public float getWoundsMod(String name) {
	String url = modifiersUrl + name + "/modifiers";
        //System.out.println("url: " + url);
	JSONObject responce = new JSONObject();
	try {
	    responce = readJsonFromUrl(url, modifiersAuth);
	} catch (Exception e) {
            System.out.println("ERROR while fetching wounds: " + e.getMessage());
	    return -666;
	}
	if (responce.isNull("wounds"))
	    return 0;
	return (float) responce.getDouble("wounds");
    }
    
    public float getArmorMod(String name) {
	String url = modifiersUrl + name + "/modifiers";
	JSONObject responce = new JSONObject();
	try {
	    responce = readJsonFromUrl(url, modifiersAuth);
	} catch (Exception e) {
            System.out.println("ERROR while fetching armor: " + e.getMessage());
	    return -666;
	}
	if (responce.isNull("armor"))
	    return 0;
	return (float) responce.getDouble("armor");
    }

    private static String readAll(Reader rd) throws IOException {
	StringBuilder sb = new StringBuilder();
	int cp;
	while ((cp = rd.read()) != -1) {
	    sb.append((char) cp);
	}
	return sb.toString();
    }	

    public static JSONObject readJsonFromUrl(String webpage, String authString) throws IOException, JSONException {
        URL url = new URL(webpage);

        byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
        String authStringEnc = new String(authEncBytes);
        URLConnection urlConnection = url.openConnection();

        urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
//	urlConnection.setRequestProperty("Authorization", authStringEnc);
        //System.out.println(authString);

	InputStream is = urlConnection.getInputStream();
	////InputStream is = new URL(url).openStream();

	try {
	    BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
	    String jsonText = readAll(rd);
	    JSONObject json = new JSONObject(jsonText);
	    return json;
	} finally {
	    is.close();
	}
    }

    public Reaction considerMods(Reaction reaction) {
        float woundsMod = getWoundsMod(reaction.getPlayer());
        if (woundsMod == -666) {
            woundsMod = 0;
        }
        
        float armorMod = getArmorMod(reaction.getPlayer());
        if (armorMod == -666) {
            armorMod = 0;
        }
        //System.out.println("considerMods: " + woundsMod);
        reaction.setWoundsMod((int)woundsMod);
        reaction.setArmorMod((int)armorMod);
        return reaction;


    }


    //---}}} Various helpers


    //{{{--- Commands 
    public boolean onCommand(CommandSender commandSender, Command command, String string, String[] args) {
        if (command.getName().equalsIgnoreCase("me")) {
            commandSender.sendMessage("§4/me отключено, используйте *§f");
            return true;

            //reactionLists
        } else if (command.getName().equalsIgnoreCase("react")) {
            if (!(commandSender instanceof Player)) {
                commandSender.sendMessage("§4you must be a player!§f");
                return false;
            }
            Player sender = (Player) commandSender;
            if (!sender.hasPermission("KMChat.gm")) {
                sender.sendMessage("§4Недостаточно прав.§f");
                return true;
            }
            if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
                commandSender.sendMessage("§e----------- §fHelp: react §e--------------------§8\nReact usage:\nYou can specify the name of the list after «/react». You can also add/edit/remove multiple reactions per time. {arg} is for optional arguments\n§6/react add %nick% {level} {mod}:§f add reactions\n§6/react edit %nick% {level} {mod}:§f edit reactions\n§6/react remove %nick%:§f remove reactions\n§6/react end:§а delete the list\n§6/react go:§f launch reactions, !go and =go also possible\n§6/react turns: show current queue\n§6/react list:§f see all lists\n§6/react show:§f show the list§f");
                return true;
            }
            String name = null;
            String comm = null;
            String[] cutArgs = new String[args.length];
            if (args.length > 1 && (
                    args[1].equalsIgnoreCase("add") || //in case list name is not specified
                            args[1].equalsIgnoreCase("remove") ||
                            args[1].equalsIgnoreCase("edit") ||
                            args[1].equalsIgnoreCase("end") ||
                            args[1].equalsIgnoreCase("show") ||
                            args[1].endsWith("turns") ||
                            args[1].equalsIgnoreCase("list") ||
                            args[1].equalsIgnoreCase("check") ||
                            args[1].endsWith("go"))
                    ) {

                name = args[0];
                if (name.equalsIgnoreCase("add") || name.equalsIgnoreCase("remove") || name.equalsIgnoreCase("edit") || name.equalsIgnoreCase("end") || name.equalsIgnoreCase("show") || name.equalsIgnoreCase("list") || name.equalsIgnoreCase("check") || name.equalsIgnoreCase("go")) {
                    commandSender.sendMessage("§4Некорректное название списка!§f");
                    return true;
                }
                comm = args[1];
                if (args.length > 2) {
                    cutArgs = Arrays.copyOfRange(args, 2, args.length);
                }
            } else if (args.length > 0 && (
                    args[0].equalsIgnoreCase("add") || //in case list name is specified
                            args[0].equalsIgnoreCase("remove") ||
                            args[0].equalsIgnoreCase("edit") ||
                            args[0].equalsIgnoreCase("end") ||
                            args[0].equalsIgnoreCase("check") ||
                            args[0].endsWith("turns") ||
                            args[0].equalsIgnoreCase("show") ||
                            args[0].equalsIgnoreCase("list") ||
                            args[0].endsWith("go"))
                    ) {

                name = commandSender.getName();
                comm = args[0];
                if (args.length > 1)
                    cutArgs = Arrays.copyOfRange(args, 1, args.length);
            }
            if (name == null || comm == null) {
                commandSender.sendMessage("§4Неправильный ввод!§f");
                return true;
            }

            ReactionList list = null;
            String owner = commandSender.getName();
            if (sprReactionList != null) {
                for (ReactionList ls : sprReactionList) {
                    if (ls.getListName().equalsIgnoreCase(name)) {
                        list = ls;
                        break;
                    }
                }
            }


            if (comm.equalsIgnoreCase("add") && list == null) {
                try {
                    if (cutArgs[0] == null) {
                        commandSender.sendMessage("§4Нельзя добавить пустую реакцию!§f");
                        return true;
                    }
                    ReactionList newList = new ReactionList(name, cutArgs);
                    sprReactionList.add(newList);
                    list = newList;

                    //to actualize woundsMod and armorMod
                    for (int k = 0; k < list.size(); k++) {
                            list.set(k, considerMods(list.get(k)));
                        }

                    commandSender.sendMessage("§7Имена добавлены в новосозданный список!§f\n" + list.show());
                    return true;
                } catch (Exception e) {
                    System.out.println("Ошибка при добавлении имён: " + e.toString() + '\n');
                    commandSender.sendMessage("§4" + e.getMessage() + "§f");
                    return false;
                }
            }
            if (comm.equalsIgnoreCase("list")) {
                String out = "";
                for (ReactionList rlist : sprReactionList) {
                    out += rlist.getListName() + " ";
                }
                commandSender.sendMessage("§7Списки: §6" + out + "§f");
                return true;
            }
            if (list == null) {
                commandSender.sendMessage("§7Не найдено ни одного списка с таким именем!§f");
                return true;
            }
            if (comm.equalsIgnoreCase("add")) {
                try {
                    if (cutArgs[0] == null) {
                        commandSender.sendMessage("§4Нельзя добавить пустую реакцию!§f");
                        return true;
                    }
                    list.add(cutArgs); 
                    //to actualize woundsMod and armorMod
                     for (int k = 0; k < list.size(); k++) {
                            list.set(k, considerMods(list.get(k)));
                        }
                    commandSender.sendMessage("§7Имена добавлены в список " + name + "!§f\n" + list.show());
                    return true;
                } catch (Exception e) {
                    commandSender.sendMessage("§4Ошибка при добавлении имён: " + e.getMessage() + "§f");
                    return false;
                }
            }
            if (comm.equalsIgnoreCase("remove")) {
                list.remove(cutArgs);
                commandSender.sendMessage("§7Имена удалены из списка " + name + "!§f");
                return true;
            }
            if (comm.equalsIgnoreCase("edit")) {
                list.edit(cutArgs);
                commandSender.sendMessage("§7Имена изменены в списке " + name + "!§f");
                return true;
            }
            if (comm.equalsIgnoreCase("end")) {
                sprReactionList.remove(list);
                commandSender.sendMessage("§7Удалён список " + name + "!§f");
                return true;
            }
            if (comm.equalsIgnoreCase("show")) {
                String out = "";
                out = list.show();
                commandSender.sendMessage(out);
                return true;
            }
            if (comm.endsWith("go")) {
                try {
                    list.clearInfo();
                        for (int k = 0; k < list.size(); k++) {
                            list.set(k, considerMods(list.get(k)));
                        }
                        //for (int k = 0; k < list.size(); k++) {
                        //    System.out.println(list.get(k).getModStr());
                        //}
                    Pattern pat = Pattern.compile("(.*)go");
                    Matcher mat = pat.matcher(comm);
                    if (mat.matches()) {
                        String describeRange = "";
                        int i = -1;
                        double range = this.getConfig().getInt("range.default");
                        int rangePosition = 6;
                        for (Range ran : allRanges) {
                            ++i;
                            if (ran.matches(mat.group(1)) && commandSender.hasPermission(ran.getPermission())) {
                                rangePosition = i;
                                range = this.getConfig().getInt(ran.getRange());
                                break;
                            }
                        }

                        String[] rawdices = list.getDices();
                        String[] dices = new String[rawdices.length];
                        String[] players = list.getPlayers();
                        String[] levels = list.getLevels();
                        Pattern resPat = Pattern.compile("Результат:\\s(.*)");
                        Pattern nickPat = Pattern.compile("(ужасно|плохо|посредственно|нормально|хорошо|отлично|превосходно|легендарно)\\+{0,4}\\-{0,4}\\s(\\[.*\\]\\s)?\\(([\\p{L}0-9_-]{1,16})+");
                        for (int j = 0; j < rawdices.length; j++) {
                            dices[j] = dF(rawdices[j], players[j]);

                        }

                        bubbleSort(dices);
                        findReroll(dices, -1, list);

                        Pattern oldPat = Pattern.compile("\\$(.*?)(\\s|\\))");
                        String[] modNicks = new String[dices.length];
                        for (int j = 0; j < dices.length; j++) {
                            Matcher oldMat = oldPat.matcher(dices[j]);
                            if (oldMat.find()) {
                                modNicks[j] = oldMat.group(1);
                                dices[j] = dices[j].replaceFirst("\\$.*?\\s", " ");
                                dices[j] = dices[j].replaceFirst("Результат:§o\\s(.*)", ("Результат: " + modNicks[j]));
                            } else {
                                Matcher resMat = resPat.matcher(dices[j]);
                                resMat.find();
                                String res = resMat.group(1);
                                modNicks[j] = res;
                            }
                        }
                        String[] reactName = new String[dices.length];
                        int h = 0;
                        for (int j = dices.length - 1; j >= 0; j--) {
                            Matcher matName = nickPat.matcher(dices[j]);
                            matName.find();
                            reactName[h++] = matName.group(3);
                        }

                        String reactNameOut = "§6Очередь: §a";
                        Pattern ininPat = Pattern.compile("Результат:§?.?\\s(.*)");
                        int whatever = modNicks.length - 1;
                        for (String str : reactName) {
                            String space = "-";
                            reactNameOut += str + space + modNicks[whatever--] + " " + "|" + " ";
                        }

                        // for (int j = 0; j < players.length; j++) {
                        //     reactNameOut = reactNameOut.replace(players[j], players[j] + levels[j]);
                        // }
                        list.setTurns(reactNameOut);
                        reactNameOut = list.getTurns();

                        String[] vars = {"едва слышно бросает",
                                "очень тихо бросает",
                                "тихо бросает",
                                "СВЕРХГРОМКО ОБРУШИВАЕТ",
                                "очень громко бросает",
                                "громко бросает",
                                "бросает"};


                        String noDelayMsg = "";
                        for (int count = dices.length - 1; count >= 0; count--) {
                            Pattern nickPat2 = Pattern.compile("от (ужасно|плохо|ПЛОХО|посредственно|нормально|хорошо|отлично|превосходно|легендарно|КАК АЛЛАХ)(\\+|-){0,3}\\s(\\[.*\\]\\s)?\\(([\\p{L}0-9_-]{1,16})+.*\\sРезультат:§?.?\\s(.*)");
                            Matcher nickMat2 = nickPat2.matcher(dices[count]);
                            if (nickMat2.find()) {
                                try {
                                    String name2 = nickMat2.group(4);
                                //System.out.println("found nick: " + name2);
                                Reaction r = list.getReactionByName(name2);
                                //System.out.println(dices[count]);
                                dices[count] = dices[count].replaceAll("я ?(\\+|-)?[0-9]?\\)\\.", "я " + r.getModStr() + ").");
                                dices[count] = dices[count].replaceAll("\\s\\)", ")");
                                //System.out.println(dices[count]);
                                } catch (Exception e) {
                                }
                            }
                        }        
                        for (int j = dices.length - 1; j >= 0; j--) {
                            //dice with armor + wounds mods

                            String out = String.format("§e(( §a%s §e%s %s ))§f", commandSender.getName(), vars[rangePosition], dices[j]);
                            List<Player> recips = getLocalRecipients(sender, out, range);
                            for (Player pl : recips) {
                                pl.sendMessage(out);
                            }
                            out.replaceAll("§.", "");
                            kmlog("chat", out);
                            kmlog("whole", out);
                            noDelayMsg += out + '\n';
                        }

                        send2discord(noDelayMsg);
                        List<Player> recips = getLocalRecipients(sender, reactNameOut, range);
                        for (Player pl : recips) {
                            pl.sendMessage(reactNameOut);
                            list.setTurns(reactNameOut);
                            reactNameOut = reactNameOut.replace("§.", "");
                            kmlog("chat", reactNameOut);
                            kmlog("whole", reactNameOut);
                            final String snd2 = list.getTurns();
                            RequestBuffer.request(() -> ingameChannel.sendMessage(snd2));

                        }
                        return true;
                    }
                } catch (Exception e) {
                    commandSender.sendMessage("§4Ошибка при броске реакций: " + e + "§f");
                    System.out.println(commandSender.getName() + ": ошибка при броске реакций: \n" + e.getCause() + " " + e.getMessage());
                    e.printStackTrace();
                }
            }
            if (comm.endsWith("turns")) {
                String out = list.getTurns();
                //String out = "§8" + list.getInfo() + list.getTurns();
                int range = this.getConfig().getInt("range.default");

                Pattern pat2 = Pattern.compile("(.*)turns");
                Matcher mat2 = pat2.matcher(comm);
                if (mat2.matches()) {
                    for (Range ran : allRanges) {
                        if (ran.matches(mat2.group(1)) && commandSender.hasPermission(ran.getPermission())) {
                            range = this.getConfig().getInt(ran.getRange());
                            break;
                        }
                    }
                }

                List<Player> recips = getLocalRecipients(sender, out, range);
                for (Player pl : recips) {
                    pl.sendMessage(out);
                    //                 out.replaceAll("§.", "");
                    kmlog("chat", out);
                    kmlog("whole", out);
                    //                    final String snd = out.replaceAll("§.", "");
                    //                   RequestBuffer.request(() -> ingameChannel.sendMessage(snd));
                }

                return true;
            }


            //AutoGM-chat

        } else if (command.getName().equalsIgnoreCase("alwaysgm")) {
            if (!(commandSender instanceof Player)) {
                commandSender.sendMessage("§4you must be a player!§f");
                return false;
            }
            Player sender = (Player) commandSender;
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
            if (args[0].equalsIgnoreCase("help")) {
                sender.sendMessage("§6Usage:\n§f/alwaysgm - check condition\n/alwaysgm on - turn on\n/alwaysgm off - turn off\n:message - regular chat (if alwaysgm is on)");
            } else if (args[0].equalsIgnoreCase("on")) {
                if (!whoUseAutoGM.contains(sender.getName())) {
                    whoUseAutoGM.add(sender.getName());
                    this.getConfig().set("whoUseAutoGM", whoUseAutoGM);
                    this.saveConfig();
                }
                sender.sendMessage("§7Автоматический ГМ-чат теперь §aвключён!§f");
            } else if (args[0].equalsIgnoreCase("off")) {
                if (whoUseAutoGM.contains(sender.getName())) {
                    whoUseAutoGM.remove(sender.getName());
                    this.getConfig().set("whoUseAutoGM", whoUseAutoGM);
                    this.saveConfig();
                }
                sender.sendMessage("§7Автоматический ГМ-чат теперь §4выключен!§f");
            }
            //AutoBD-chat

        } else if (command.getName().equalsIgnoreCase("alwaysbuild") || command.getName().equalsIgnoreCase("alwaysbuilder")) {
            if (!(commandSender instanceof Player)) {
                commandSender.sendMessage("§4you must be a player!§f");
                return false;
            }
            Player sender = (Player) commandSender;
            if (!sender.hasPermission("KMChat.builder")) {
                sender.sendMessage("§4Недостаточно прав.§f");
                return true;
            }
            if (args.length == 0) {
                if (whoUseAutoBD.contains(sender.getName())) {
                    sender.sendMessage("§7Автоматический билдер-чат §aвключён!§f");
                } else {
                    sender.sendMessage("§7Автоматический билдер-чат §4выключен!§f");
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("help")) {
                sender.sendMessage("§6Usage:\n§fNote that /alwaysbuild is equal to /alwaysbuilder\n/alwaysbuild - check condition\n/alwaysbuild on - turn on\n/alwaysbuild off - turn off\n:message - regular chat (if alwaysbuild is on)");
            } else if (args[0].equalsIgnoreCase("on")) {
                if (!whoUseAutoBD.contains(sender.getName())) {
                    whoUseAutoBD.add(sender.getName());
                    this.getConfig().set("whoUseAutoBD", whoUseAutoBD);
                    this.saveConfig();
                }
                sender.sendMessage("§7Автоматический билдер-чат теперь §aвключён!§f");
            } else if (args[0].equalsIgnoreCase("off")) {
                if (whoUseAutoBD.contains(sender.getName())) {
                    whoUseAutoBD.remove(sender.getName());
                    this.getConfig().set("whoUseAutoBD", whoUseAutoBD);
                    this.saveConfig();
                }
                sender.sendMessage("§7Автоматический билдер-чат теперь §4выключен!§f");
            }


        } else if (command.getName().equalsIgnoreCase("ingamerestart")) {
            if (!commandSender.hasPermission("KMCore.gm")) {
                commandSender.sendMessage("§4Недостаточно прав.§f");
                return false;
            }
            try {
            client.logout();
            client = new ClientBuilder().withToken(TOKEN).build();
            client.getDispatcher().registerListener(this);
            client.login();
                wasrestarted = true;
            } catch (Exception e) {
                System.out.println(e);
            }
            /*Player sender = (Player)commandSender;
            try {
                client.logout();
            } catch (Exception e) {
                System.out.println(e);
            }

            try {
                client.login();
            } catch (Exception e) {
                System.out.println(e);
            }
            */
            commandSender.sendMessage("§8Ingame bot was reloaded!§f");
            return true;
        } else if (command.getName().equalsIgnoreCase("msg")) {
            if (!(commandSender instanceof Player)) {
                commandSender.sendMessage("§4You must be a player!§f");
                return false;
            }

            Player sender = (Player) commandSender;
            if (!sender.hasPermission("KMCore.tell")) {
                sender.sendMessage("§4Недостаточно прав. Для связи с ГМ-ами используйте ГМ-чат с помощью символа -.§f");
                return true;
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


            String senderName = sender.getName();
            String recipName = recip.getName();
            args[0] = "&8[&a" + senderName + "&8->&a" + recipName + "&8]:&f";
            String message = "";
            for (String arg : args) {
                message = message + " " + arg;
            }

            message = message.replaceAll("&([a-z0-9])", "§$1");

            sender.sendMessage(message);
            recip.sendMessage(message);
            kmlog("whole", message);
            kmlog("chat", message);

            final String snd = message.replaceAll("§([a-z0-9])", "");
            RequestBuffer.request(() -> ingameChannel.sendMessage(snd));

            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                if (player.hasPermission("KMChat.admin")) {
                    if (player != sender && player != recip) {
                        player.sendMessage(message);
                    }
                }
            }
            return true;


            //Reminders
        } else if (command.getName().equalsIgnoreCase("remind")) {
            if (commandSender instanceof Player) {
                Player sender = (Player) commandSender;
                if (!sender.hasPermission("KMChat.gm")) {
                    sender.sendMessage("§4Недостаточно прав.§f");
                    return true;
                }
            }
            if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
                commandSender.sendMessage("§e----------- §fHelp: remind §e--------------------§8\nRemind usage: Use this to remind player of something when he goes online.\n§e/remind Player You suffer from nausea.\n/remind show§f - show current reminders.\n§e/remind remove Player§f or §e/remind delete Player§f - delete reminder.\n§e/remind edit Player You are hungry§f - overwrite an existing reminder.\n§e/remind§f or §e/remind help§f - show this message.\n");
                return true;
            } else if (args[0].equalsIgnoreCase("show")) {
                if (reminders.isEmpty()) {
                    commandSender.sendMessage("§8Currently no reminders.§f");
                    return true;
                }

                commandSender.sendMessage("§eCurrent reminders:§f");
                for (String key : reminders.keySet()) {
                    commandSender.sendMessage("§a" + key + " : §f" + reminders.get(key) + "\n§f");
                }
                return true;
            } else if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("delete")) {
                for (String key : reminders.keySet()) {
                    if (args[1].equalsIgnoreCase(key)) {
                        reminders.remove(key);
                        commandSender.sendMessage("§eReminder successfully deleted!§f");
                        return true;
                    }
                }
                commandSender.sendMessage("§4Reminder not found!§f");
                return false;
            } else if (args[0].equalsIgnoreCase("edit")) {
                boolean found = false;
                for (String key : reminders.keySet()) {
                    if (args[1].equalsIgnoreCase(key)) {
                        reminders.remove(key);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    commandSender.sendMessage("§4Reminder not found!§f");
                    return false;
                }
                String sumargs = "";
                for (int i = 2; i < args.length; i++) {
                    sumargs += " " + args[i];
                }

                reminders.put(args[1], sumargs);
                commandSender.sendMessage("§eReminder successfully edited!§f");
                return true;

            } else if (args.length > 1) {
                for (String key : reminders.keySet()) {
                    if (args[0].equalsIgnoreCase(key)) {
                        commandSender.sendMessage("§4Existing reminder for this player found! Please use /remind edit to edit.§f");
                        return false;
                    }
                }
                String sumargs = "";
                for (int i = 1; i < args.length; i++) {
                    sumargs += " " + args[i];
                }
                reminders.put(args[0], sumargs);
                commandSender.sendMessage("§eReminder successfully added!§f");
                return true;
            }

            //CUSTOM RANGE
        } else if (command.getName().equalsIgnoreCase("setrange")) {
            if (!(commandSender instanceof Player)) {
                commandSender.sendMessage("§4you must be a player!§f");
                return false;
            }
            Player sender = (Player) commandSender;
            if (!sender.hasPermission("KMChat.gm")) {
                sender.sendMessage("§4Недостаточно прав.§f");
                return true;
            }
            if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
                commandSender.sendMessage("§e----------- §fHelp: setrange §e--------------------§8\n§fПозволяет установить предел слышимости чата. Сбрасывается после перезахода.\n§e/setrange <number> §f— установить предел слышимости.\n§e/setrange show§f — показать предел.\n§e/setrange remove §fили §ereset — убрать предел.\n");
                return true;
            }
            if (args[0].equalsIgnoreCase("show")) {
                for (String str : customRanges.keySet()) {
                    if (sender.getName().equalsIgnoreCase(str)) {
                        sender.sendMessage("§8Установленный предел слышимости:§e " + customRanges.get(sender.getName()));
                        return true;
                    }
                }
                sender.sendMessage("§8Предел слышимости §eне установлен.");
                return true;
            }
            if (args[0].equalsIgnoreCase("reset") || args[0].equalsIgnoreCase("remove")) {
                customRanges.remove(sender.getName());
                sender.sendMessage("§8Предел слышимости был §eубран.");
                return true;
            }
            double range;
            try {
                range = Double.parseDouble(args[0]);
            } catch (Exception e) {
                sender.sendMessage("§4Нужно указать число!");
                return true;
            }
            if (range < 0) {
                sender.sendMessage("§4Число должно быть положительным!");
                return true;
            }
            customRanges.put(sender.getName(), range);
            sender.sendMessage("§8Предел слышимости установлен: §e" + range);
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
        if (content.startsWith(":msg") || content.startsWith("/msg")) {
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
                        player.sendMessage("<§2" + user.getName() + "§f->§a" + player.getName() + "§f>" + mes.replaceFirst(player.getName(), ""));
                        res = "<" + user.getName() + "->" + mes.replaceFirst(player.getName(), player.getName() + ">");
                        recip = player;
                        found = true;
                    }
                }
                if (!found) {
                    res = "Нет такого игрока!";
                } else {
                    for (Player admin : Bukkit.getServer().getOnlinePlayers()) {
                        if (admin.hasPermission("KMChat.admin")) {
                            if (admin != recip) {
                                admin.sendMessage("<§2" + user.getName() + "§f->§a" + recip.getName() + "§f>" + mes.replace(recip.getName(), ""));
                            }
                        }
                    }
                }

            } else if (content.startsWith("!online") || content.startsWith("!онлайн")) {
                String online = "Текущий онлайн (%s): ";
                int i = 0;
                Player[] players = Bukkit.getServer().getOnlinePlayers();
                for (Player player : players) {
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
                    try {
                        content.substring(6);
                    } catch (Exception e) {
                        return;
                    }
                    BukkitScheduler scheduler = getServer().getScheduler();
                    if (
                            scheduler.scheduleSyncDelayedTask(this, new Runnable() {
                                @Override
                                public void run() {
                                    Bukkit.getServer().dispatchCommand(new DiscordCommandSender(ingameChannel), message.getContent().substring(6));
                                }
                            }, 1L) == -1)
                        res = "_Что-то пошло не так, команда не была выполнена!_";
                }
            }
        } else {
            if (content.startsWith("^")) {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    res = "<§2" + user.getName() + "§f> " + message.getContent().substring(1);
                    player.sendMessage(res);
                }
            } else {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    if (player.hasPermission("KMChat.gm")) {
                        res = "<§2" + user.getName() + "§e to GM> §f" + message;
                        player.sendMessage(res);
                    }
                }
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
                mes.startsWith(":% ")) {

            Collection<String> collection = playerChatTabCompleteEvent.getTabCompletions();
            if (!playerChatTabCompleteEvent.getPlayer().hasPermission("KMChat.gm")) {
                collection.clear();
            }

            if (playerChatTabCompleteEvent.getLastToken().startsWith("уж")) {
                collection.add("ужасно");
            } else if (playerChatTabCompleteEvent.getLastToken().startsWith("ук")) {
                collection.add("уклонение");
            } else if (playerChatTabCompleteEvent.getLastToken().startsWith("ус")) {
                collection.add("устойчивость к болезням");
            } else if (playerChatTabCompleteEvent.getLastToken().startsWith("у")) {
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
                collection.add("врачевание");
                collection.add("выносливость");
                collection.add("выживание");
            } else if (playerChatTabCompleteEvent.getLastToken().startsWith("вл")) {
                collection.add("владение оружием");
            } else if (playerChatTabCompleteEvent.getLastToken().startsWith("вн")) {
                collection.add("внимательность");
            } else if (playerChatTabCompleteEvent.getLastToken().startsWith("вр")) {
                collection.add("врачевание");
            } else if (playerChatTabCompleteEvent.getLastToken().startsWith("в")) {
                collection.add("выносливость");
                collection.add("внимательность");
                collection.add("выживание");
            } else if (playerChatTabCompleteEvent.getLastToken().startsWith("д")) {
                collection.add("диагностика");
            } else if (playerChatTabCompleteEvent.getLastToken().startsWith("з")) {
                collection.add("зашивание ран");
            } else if (playerChatTabCompleteEvent.getLastToken().startsWith("ре")) {
                collection.add("реакция");
            } else if (playerChatTabCompleteEvent.getLastToken().startsWith("ру")) {
                collection.add("рукопашный бой");
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
            } else if (playerChatTabCompleteEvent.getLastToken().startsWith("перв")) {
                collection.add("первая помощь");
            } else if (playerChatTabCompleteEvent.getLastToken().startsWith("пере")) {
                collection.add("передвижение");
            } else if (playerChatTabCompleteEvent.getLastToken().startsWith("пер")) {
                collection.add("передвижение");
            } else if (playerChatTabCompleteEvent.getLastToken().startsWith("пе")) {
                collection.add("передвижение");
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
                collection.add("передвижение");
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
                collection.add("борьба");
                collection.add("парирование");
                collection.add("уклонение");
                collection.add("блокирование");
                collection.add("бег");
                collection.add("плавание");
                collection.add("акробатика");
                collection.add("выносливость");
                collection.add("внимательность");
                collection.add("скрытность");
                collection.add("выживание");
                collection.add("диагностика");
                collection.add("хирургия");
                collection.add("передвижение");
                collection.add("врачевание");
                //    collection.add("физическая сила");
                //    collection.add("кулачный бой");
                //    collection.add("владение оружием");
                //    collection.add("первая помощь");
                //    collection.add("зашивание ран");
                //    collection.add("рукопашный бой");
                //    collection.add("устойчивость к болезням");
            }
        }
    }
//---}}} Tabs
}
