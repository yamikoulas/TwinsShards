package com.twinsshards.util;

import net.md_5.bungee.api.ChatColor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private ColorUtil() {}

    /**
     * Mesajdaki & ve &#RRGGBB renk kodlarını Minecraft renk formatına dönüştürür.
     */
    public static String color(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }

        try {
            Matcher matcher = HEX_PATTERN.matcher(message);
            StringBuffer buffer = new StringBuffer(message.length() + 32);

            while (matcher.find()) {
                String hex = matcher.group(1);
                ChatColor color = ChatColor.of("#" + hex);
                matcher.appendReplacement(buffer, color.toString());
            }
            matcher.appendTail(buffer);
            return ChatColor.translateAlternateColorCodes('&', buffer.toString());
        } catch (Throwable t) {
            return ChatColor.translateAlternateColorCodes('&', message);
        }
    }
}
