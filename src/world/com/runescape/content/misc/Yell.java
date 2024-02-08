package com.runescape.content.misc;

import com.runescape.Static;
import com.runescape.logic.player.Player;
import com.runescape.logic.player.Player.Rights;
import com.runescape.logic.utility.NodeCollection;

import java.util.LinkedList;

public class Yell {

    public static enum YellType {
        PUBLIC,
        ADMINS,
        STAFF,
        MODERATORS,
        AREA_BOUND;

        public static String getColourForType(YellType type) {
            switch (type) {
                case PUBLIC:
                    return "<col=0000FF>";
                case ADMINS:
                    return "<col=FF0000>";
                case STAFF:
                    return "<col=FF00FF>";
                case MODERATORS:
                    return "<col=00FF00";
                case AREA_BOUND:
                    return "<col=00FF00";
            }
            return "";
        }

        public static YellType forString(String type) {
            type = type.toUpperCase();
            return YellType.valueOf(type);
        }
    }

    public static void yell(YellType type, String message) {
        int index = message.indexOf(" ");
        message = message.substring(index + 1);
        message = optimizeString(message);
        try {
            String yell = "";
            NodeCollection<Player> targets = new NodeCollection<Player>();
            switch (type) {
                case PUBLIC:
                    targets = Static.world.getPlayers();
                    yell += "<col=FFFF00>[" + YellType.getColourForType(type) + "PUBLIC YELL<col=FFFF00>] ";
                    break;
		case ADMINS:
			for (Player p : Static.world.getPlayers()) {
				if (p.getRights() == Rights.ADMINISTRATOR) {
					targets.add(p);
				}				}
			yell += "<col=FFFF00>[" + YellType.getColourForType(type) + "ADMIN YELL<col=FFFF00>] ";
			break;
		case STAFF:
			for (Player p : Static.world.getPlayers()) {
				if (p.getRights() == Rights.ADMINISTRATOR || p.getRights() == Rights.MODERATOR) {
					targets.add(p);
				}
		}
			yell += "<col=FFFF00>[<col=0000FF>ADMIN YELL<col=FFFF00>] ";
			break;
		case MODERATORS:
			for (Player p : Static.world.getPlayers()) {
				if (p.getRights() == Rights.MODERATOR) {
				targets.add(p);
				}
			}
			yell += "<col=FFFF00>[<col=0000FF>ADMIN YELL<col=FFFF00>] " ;
			break;
		case AREA_BOUND:
			break;
            }
            LinkedList<String> strings = new LinkedList<String>();
            yell += YellType.getColourForType(type) + message;
            int startOffset = 0;
            boolean cut = false;
            int i = 0;
            for (i = 0; i < yell.length(); i++) {
                char c = yell.charAt(i);
                if (c == '\\') {
                    strings.add(YellType.getColourForType(type) + yell.substring(startOffset, i));
                    startOffset = i + 1;
                    cut = true;
                }

            }
            if (!cut)
                strings.add(YellType.getColourForType(type) + yell);
            else
                strings.add(YellType.getColourForType(type) + yell.substring(startOffset, i));
            for (Player p : targets) {
                if (p != null && p.isConnected() && p.inGame()) {
                    for (String s : strings)
                        Static.proto.sendMessage(p, s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String optimizeString(String message) {
        char[] buffer = message.toCharArray();
        boolean endMarker = true;
        for (int i = 0; i < buffer.length; i++) {
            char c = buffer[i];
            if (c == '/') {
                boolean copy = true;
                char c2 = buffer[i + 1];
                switch (c2) {
                    case '1':
                        c = '!';
                        break;
                    case '2':
                        c = '@';
                        break;
                    case '3':
                        c = '#';
                        break;
                    case '4':
                        c = '$';
                        break;
                    case '5':
                        c = '%';
                        break;
                    case '6':
                        c = '\63';
                        break;
                    case '8':
                        c = '*';
                        break;
                    case '9':
                        c = '(';
                        break;
                    case '0':
                        c = ')';
                    case 'a':
                        c = '\'';
                        break;
                    case 's':
                        c = ';';
                        break;
                    default:
                        copy = false;
                        break;
                }
                if (copy) {
                    char[] b1 = new char[i + 1];
                    System.arraycopy(buffer, 0, b1, 0, i);
                    b1[i] = c;

                    char[] b2 = new char[buffer.length - i - 2];
                    if (i + 2 < buffer.length - 1) {
                        System.arraycopy(buffer, i + 2, b2, 0, buffer.length - i - 2);
                    }
                    buffer = new char[buffer.length - 1];
                    System.arraycopy(b1, 0, buffer, 0, b1.length);
                    if (i + 2 < buffer.length - 1) {
                        System.arraycopy(b2, 0, buffer, i + 1, b2.length);
                    }
                }
            }
            if (endMarker && c >= 'a' && c <= 'z') {
                buffer[i] -= 0x20;
                endMarker = false;
            }
            if (c == '.' || c == '!' || c == '?') {
                endMarker = true;
            }
        }
        return new String(buffer, 0, buffer.length);
    }

}
