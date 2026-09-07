package com.twinsshards.config;

import com.twinsshards.TwinsShards;
import com.twinsshards.util.ColorUtil;
import com.twinsshards.util.NumberFormatter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private final TwinsShards plugin;
    private FileConfiguration config;
    private final Map<String, FileConfiguration> languageConfigs = new HashMap<>();
    private String defaultLanguage;

    public ConfigManager(TwinsShards plugin) {
        this.plugin = plugin;
    }

    public void load() {
        // config.yml
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();

        // Varsayılan dil
        this.defaultLanguage = config.getString("language", "tr").toLowerCase();
        if (!defaultLanguage.equals("en") && !defaultLanguage.equals("tr")) {
            defaultLanguage = "tr";
        }

        // lang klasörü
        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        saveLangResource("lang/messages_tr.yml");
        saveLangResource("lang/messages_en.yml");

        languageConfigs.clear();
        loadLanguageFile("tr", new File(langFolder, "messages_tr.yml"), "lang/messages_tr.yml");
        loadLanguageFile("en", new File(langFolder, "messages_en.yml"), "lang/messages_en.yml");

        // NumberFormatter güncellemesi
        List<String> suffixes = config.getStringList("formatting.suffixes");
        int decimalPlaces = config.getInt("formatting.decimal-places", 2);
        NumberFormatter.reload(suffixes, decimalPlaces);
    }

    private void loadLanguageFile(String langCode, File file, String resourcePath) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        InputStream defaultStream = plugin.getResource(resourcePath);
        if (defaultStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            yaml.setDefaults(defConfig);
        }
        languageConfigs.put(langCode, yaml);
    }

    private void saveLangResource(String resourcePath) {
        File target = new File(plugin.getDataFolder(), resourcePath);
        if (!target.exists()) {
            plugin.saveResource(resourcePath, false);
        }
    }

    public void reload() {
        load();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getMessages(String lang) {
        if (lang != null && languageConfigs.containsKey(lang.toLowerCase())) {
            return languageConfigs.get(lang.toLowerCase());
        }
        return languageConfigs.getOrDefault(defaultLanguage, languageConfigs.get("tr"));
    }

    public String getCurrentLanguage() {
        return defaultLanguage;
    }

    public String getPrefix(String lang) {
        FileConfiguration msgs = getMessages(lang);
        return ColorUtil.color(msgs.getString("prefix", "&#00c6ff&lTwinsShards &8» &r"));
    }

    public String getMessage(String path, String lang) {
        FileConfiguration msgs = getMessages(lang);
        String msg = msgs.getString(path);
        if (msg == null) {
            return "§cMissing message: " + path;
        }
        msg = msg.replace("%prefix%", getPrefix(lang));
        return ColorUtil.color(msg);
    }

    public String getMessage(String path) {
        return getMessage(path, defaultLanguage);
    }

    public List<String> getMessageList(String path, String lang) {
        FileConfiguration msgs = getMessages(lang);
        List<String> list = msgs.getStringList(path);
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> colored = new ArrayList<>(list.size());
        String prefix = getPrefix(lang);
        for (String line : list) {
            line = line.replace("%prefix%", prefix);
            colored.add(ColorUtil.color(line));
        }
        return colored;
    }

    public List<String> getMessageList(String path) {
        return getMessageList(path, defaultLanguage);
    }

    public String getFormattedForLang(String path, String lang, String... placeholders) {
        String msg = getMessage(path, lang);
        if (placeholders.length % 2 != 0) {
            return msg;
        }
        for (int i = 0; i < placeholders.length; i += 2) {
            String target = placeholders[i];
            String replacement = placeholders[i + 1];
            if (target != null && replacement != null) {
                msg = msg.replace(target, replacement);
            }
        }
        return msg;
    }

    public String getFormatted(String path, String... placeholders) {
        return getFormattedForLang(path, defaultLanguage, placeholders);
    }
}
