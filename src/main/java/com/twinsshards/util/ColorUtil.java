package com.twinsshards.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

    private ColorUtil() {}

    /**
     * Mesajdaki MiniMessage (<gradient>, <color>, vb.), &#RRGGBB ve & renk kodlarını
     * Minecraft renk formatına dönüştürür.
     */
    public static String color(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }

        // 1. MiniMessage desteği (Eğer <...> etiketleri içeriyorsa)
        if (message.contains("<") && message.contains(">")) {
            try {
                Component component = MINI_MESSAGE.deserialize(message);
                message = LEGACY_SERIALIZER.serialize(component);
            } catch (Throwable ignored) {
                // Parse hatası olursa devam et
            }
        }

        // 2. Hex &#RRGGBB desteği
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
