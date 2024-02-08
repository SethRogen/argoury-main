package com.runescape.logic.item;

import com.runescape.Constants;
import com.runescape.Static;
import com.runescape.adapter.protocol.ClientConfigurationAdapter;
import com.runescape.adapter.protocol.cache.RS2CacheAdapter;
import com.runescape.io.XMLSession;
import com.runescape.utility.Configuration;
import com.runescape.utility.Logging;
import com.runescape.utility.StringParser;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

/**
 * @author Lazaro
 */
public class ItemDefinitionGrabber {
    private static Logger logger = Logging.log();

    private static Map<Integer, ItemXMLDefinition> defs = null;
    private static Queue<Integer> itemIds = new ArrayDeque<Integer>();

    public static void main(String[] args) {
        try {
            Static.parser = new StringParser();
            Static.parser.registerVariable("WORK_DIR", "./data");
            Static.conf = new Configuration(Static.parseString(Constants.CONFIGURATION_FILE));
            Static.xml = new XMLSession();
            Static.xml.loadAliases(Static.parseString("%WORK_DIR%/aliases.xml"));
            PropertyConfigurator.configure(Static.conf.getString("logging_config_file"));
            Static.clientConf = new ClientConfigurationAdapter();
            logger.info("Initiating the item definition grabber...");
            Static.rs2Cache = new RS2CacheAdapter();
            Static.rs2Cache.load(new File(Static.parseString("%WORK_DIR%/cache/rs2cache")));

            //defs = Static.xml.readObject("items.xml");

            //Static.xml.writeObject(defs, "items.xml");

            if (true)
                return;

            /*Map<Integer, ItemXMLDefinition> defs2 = new TreeMap<Integer, ItemXMLDefinition>();
         for(int i=0; i<20000; i++) {
             if(defs.containsKey(i)) {
                 defs2.put(i, defs.get(i));
             }
         }

         if(true) {
             Static.xml.writeObject(defs2, "items.xml");
             return;
         }   */

            int start = 0;
            for (int i = 20000; i >= 0; i--) {
                if (defs.containsKey(i)) {
                    start = i;
                    break;
                }
            }
            start++;
            logger.info("Starting at item id: " + start);
            for (int i = start; i < 20000; i++) {
                if (defs.containsKey(i)) {
                    continue;
                }
                itemIds.offer(i);
            }
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    Integer id;
                    while ((id = itemIds.remove()) != null) {
                        if (lookupItem(id)) {
                            logger.debug("Dumped item: " + id);
                        }
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                    }
                }
            };
            for (int i = 0; i < 3; i++) {
                new Thread(r).start();
            }
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        synchronized (defs) {
                            Static.xml.writeObject(defs, "items.xml");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }));
        } catch (Throwable e) {
            logger.error("Error caught while grabbing items!", e);
        }
    }

    private static boolean lookupItem(int id) {
        while (true) {
            try {
                ItemDefinition def = ItemDefinition.forId(id);
                if (def == null) {
                    return false;
                }
                BufferedReader site = new BufferedReader(new InputStreamReader(new URL("http://runehq.com/databasesearch.php?db=item&query=" + id + "&field=2").openConnection().getInputStream()));
                int webId = findBestItemResult(site, def);
                if (webId == -1) { // Switch to searching by name
                    site = new BufferedReader(new InputStreamReader(new URL("http://runehq.com/databasesearch.php?db=item&query=" + def.name.replace(" ", "+") + "&field=0").openConnection().getInputStream()));
                    webId = findBestItemResult(site, def);
                    if (webId == -1) {
                        return false;
                    }
                }
                site = new BufferedReader(new InputStreamReader(new URL("http://runehq.com/database.php?type=item&id=" + webId).openConnection().getInputStream()));
                ItemXMLDefinition xmlDef = parseRuneHQWebDef(site, def);
                if (xmlDef == null) {
                    return false;
                }
                /*if (def.isEquipable() && def.getEquipmentSlot() == Constants.Equipment.WEAPON_SLOT) {
                    try {
                        site = new BufferedReader(new InputStreamReader(new URL("http://runescape.wikia.com/" + fixDefNameForRSWikia(def.name.replace(" ", "_").replace("'", ""))).openConnection().getInputStream()));
                        parseRSWikiaDef(site, def, xmlDef);
                    } catch (IOException e) {
                    } catch (Exception e) {
                        logger.error("Error drumping from RuneScape Wikia: " + id, e);
                    }
                }*/
                synchronized (defs) {
                    defs.put(id, xmlDef);
                }
                return true;
            } catch (ConnectException e) {
                continue;
            } catch (Exception e) {
                logger.error("Error caught while looking up item: " + id, e);
                return false;
            }
        }
    }

    private static String fixDefNameForRSWikia(String string) {
        for (int index = string.indexOf("("); index != -1; index = string.indexOf("(")) {
            StringBuilder sb = new StringBuilder();
            int i = index;
            do {
                if (i >= string.length())
                    break;
                char c = string.toCharArray()[i];
                sb.append(c);
                if (c == ')')
                    break;
                i++;
            } while (true);
            string = string.replace(sb.toString(), "");
        }
        return string.replace("_", " ").trim().replace(" ", "_");
    }

    private static void parseRSWikiaDef(BufferedReader site, ItemDefinition def, ItemXMLDefinition xDef) throws Exception {
        String line;
        while ((line = site.readLine()) != null) {
            /*if (line.contains("<a href=\"/wiki/Attack_speed\" title=\"Attack speed\">Speed</a>:")) {
                String speedStr = line.substring(line.indexOf("title=\"File:speed") + "title=\"File:speed".length(), line.indexOf("title=\"File:speed") + "title=\"File:speed".length() + 1);
                xDef.speed = Integer.parseInt(speedStr);
            }*/
        }
    }

    private static ItemXMLDefinition parseRuneHQWebDef(BufferedReader site, ItemDefinition def) throws Exception {
        ItemXMLDefinition xDef = new ItemXMLDefinition();
        String line;
        while ((line = site.readLine()) != null) {
            try {
                String txt = stripHTML(line);
                if (txt.equals("Weight:")) {
                    String weightStr = stripHTML(site.readLine());
                    if (!weightStr.equals("Unknown")) {
                        xDef.weight = Double.parseDouble(weightStr.replace(" kg", "").trim());
                    }
                } else if (txt.equals("Attack Bonuses")) {
                    /*if (xDef.bonus == null) {
                        xDef.bonus = new double[18];
                    }
                    site.readLine();
                    site.readLine();
                    for (int i = 0; i < 5; i++) {
                        site.readLine();
                        String bStr = stripHTML(site.readLine()).replace("%", "");
                        if (!bStr.equals("Unknown")) {
                            double b = Double.parseDouble(bStr.substring(1));
                            if (bStr.charAt(0) == '-') {
                                b = -b;
                            }
                            xDef.bonus[i] = b;
                        }
                        site.readLine();
                        site.readLine();
                    }*/
                } else if (txt.equals("Defence Bonuses")) {
                    /*if (xDef.bonus == null) {
                        xDef.bonus = new double[18];
                    }
                    site.readLine();
                    site.readLine();
                    for (int i = 5; i < 14; i++) {
                        site.readLine();
                        String bStr = stripHTML(site.readLine()).replace("%", "");
                        if (!bStr.equals("Unknown")) {
                            double b = Double.parseDouble(bStr.substring(1));
                            if (bStr.charAt(0) == '-') {
                                b = -b;
                            }
                            xDef.bonus[i] = b;
                        }
                        site.readLine();
                        site.readLine();
                    }*/
                } else if (txt.equals("Other Bonuses")) {
                    /*if (xDef.bonus == null) {
                        xDef.bonus = new double[18];
                    }
                    site.readLine();
                    site.readLine();
                    for (int i = 14; i < 18; i++) {
                        site.readLine();
                        String bStr = stripHTML(site.readLine()).replace("%", "");
                        if (!bStr.equals("Unknown")) {
                            double b = Double.parseDouble(bStr.substring(1));
                            if (bStr.charAt(0) == '-') {
                                b = -b;
                            }
                            xDef.bonus[i] = b;
                        }
                        site.readLine();
                        site.readLine();
                    }*/
                } else if (txt.equals("Examine Information:")) {
                    xDef.examine = stripHTML(site.readLine());
                }
            } catch (Exception e) {
                logger.error("Error while parsing line!", e);
            }
        }
        return xDef;
    }

    private static String stripHTML(String s) {
        return s.replaceAll("\\<.*?>", "").trim();
    }

    private static int findBestItemResult(BufferedReader site, ItemDefinition def) throws Exception {
        String line;
        while ((line = site.readLine()) != null) {
            if (line.contains("<p class=\"center\">")) {
                return -1;
            }
            if (line.contains("/database.php?type=item&amp;id=")) {
                if (!line.contains(def.name)) {
                    continue;
                }
                return Integer.parseInt(line.substring(line.indexOf("/database.php?type=item&amp;id=") + "/database.php?type=item&amp;id=".length(), line.indexOf("/database.php?type=item&amp;id=") + "/database.php?type=item&amp;id=".length() + 6));
            }
        }
        return -1;
    }
}
