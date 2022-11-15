package com.minepalm.syncer.player.bukkit.metadata;

import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang.Validate;
import org.bukkit.OfflinePlayer;
import org.bukkit.metadata.MetadataStoreBase;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class CustomPlayerMetadataStore extends MetadataStoreBase<OfflinePlayer> {

    private final Map<String, Map<Plugin, MetadataValue>> metadataMap = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, MetadataValue>> playerMetadataMap = new ConcurrentHashMap<>();
    private final Map<Plugin, Map<UUID, String>> pluginMetadataMap = new ConcurrentHashMap<>();

    private final Logger logger;

    public void setMetadata(OfflinePlayer subject, String metadataKey, @NotNull MetadataValue newMetadataValue) {
        Plugin owningPlugin = newMetadataValue.getOwningPlugin();
        Validate.notNull(owningPlugin, "Plugin cannot be null");
        String key = this.disambiguate(subject, metadataKey);
        Map<Plugin, MetadataValue> entry = this.metadataMap.computeIfAbsent(key, k -> new WeakHashMap<>(1));
        synchronized (entry){
            entry.put(owningPlugin, newMetadataValue);
        }
        setMetadataCustom(subject, metadataKey, newMetadataValue);
    }

    private void setMetadataCustom(OfflinePlayer subject, String metadataKey, @NotNull MetadataValue newMetadataValue){
        Plugin owningPlugin = newMetadataValue.getOwningPlugin();
        val playerMap =
                playerMetadataMap.computeIfAbsent(subject.getUniqueId(), k -> new WeakHashMap<>());
        val pluginMap =
                pluginMetadataMap.computeIfAbsent(owningPlugin, k -> new WeakHashMap<>());

        set(playerMap, metadataKey, newMetadataValue);
        set(pluginMap, subject.getUniqueId(), metadataKey);
        logger.info("set player metadata name: "+subject.getName()+", key: "+metadataKey+", value: "+ newMetadataValue.value());
    }

    private <K, V> void set(Map<K, V> map, K key, V value){
        synchronized (map) {
            map.put(key, value);
        }
    }

    public List<MetadataValue> getMetadata(OfflinePlayer subject, String metadataKey) {
        String key = this.disambiguate(subject, metadataKey);
        Map<Plugin, MetadataValue> entry = this.metadataMap.get(key);
        if (entry != null) {
            Collection<MetadataValue> values = entry.values();
            return Collections.unmodifiableList(new ArrayList<>(values));
        } else {
            return Collections.emptyList();
        }
    }

    public List<MetadataValue> getMetadataCustom(OfflinePlayer subject, String metadataKey){
        val map = playerMetadataMap.get(subject.getUniqueId());
        if(map != null && map.containsKey(metadataKey)) {
            return ImmutableList.copyOf(Collections.singletonList(map.get(metadataKey)));
        }else{
            return Collections.emptyList();
        }
    }

    public boolean hasMetadata(OfflinePlayer subject, String metadataKey) {
        String key = this.disambiguate(subject, metadataKey);
        boolean result = this.metadataMap.containsKey(key);
        logger.info("has player metadata name: "+subject.getName()+", key: "+metadataKey+", value: "+ result);
        return result;
    }

    private boolean hasMetadataCustom(OfflinePlayer subject, String metadataKey){
        return this.playerMetadataMap.containsKey(subject.getUniqueId()) &&
                this.playerMetadataMap.get(subject.getUniqueId()).containsKey(metadataKey);
    }

    public void removeMetadata(OfflinePlayer subject, String metadataKey, Plugin owningPlugin) {
        Validate.notNull(owningPlugin, "Plugin cannot be null");
        String key = this.disambiguate(subject, metadataKey);
        Map<Plugin, MetadataValue> entry = this.metadataMap.get(key);
        if (entry != null) {
            synchronized(entry) {
                entry.remove(owningPlugin);
                if (entry.isEmpty()) {
                    this.metadataMap.remove(key);
                }

            }
        }
        removeMetadataCustom(subject, metadataKey, owningPlugin);
    }

    private void removeMetadataCustom(OfflinePlayer subject, String metadataKey, @NotNull Plugin owningPlugin) {
        logger.info("remove player metadata name: "+subject.getName()+", " +
                "key: "+metadataKey+", value: "+ getMetadataCustom(subject, metadataKey));
        removeMetadataOfPlugin(subject, metadataKey, owningPlugin);
        removeMetadataOfPlayer(subject, metadataKey, owningPlugin);
    }

    private void removeMetadataOfPlayer(OfflinePlayer subject, String metadataKey, @NotNull Plugin owningPlugin){
        val map = this.playerMetadataMap.get(subject.getUniqueId());
        if(map != null){
            synchronized (map){
                map.remove(metadataKey);
                if(map.isEmpty()){
                    this.playerMetadataMap.remove(subject.getUniqueId());
                }
            }
        }
    }

    private void removeMetadataOfPlugin(OfflinePlayer subject, String metadataKey, @NotNull Plugin owningPlugin){
        val map = this.pluginMetadataMap.get(owningPlugin);
        if(map != null){
            synchronized (map){
                map.remove(metadataKey);
                if(map.isEmpty()){
                    this.pluginMetadataMap.remove(owningPlugin);
                }
            }
        }
    }

    public void invalidateAll(Plugin owningPlugin) {
        Validate.notNull(owningPlugin, "Plugin cannot be null");

        for(Map<Plugin, MetadataValue> values : metadataMap.values()){
            if (values.containsKey(owningPlugin)) {
                (values.get(owningPlugin)).invalidate();
            }
        }
        invalidateAllCustom(owningPlugin);
    }

    private void invalidateAllCustom(Plugin owningPlugin) {
        logger.info("invalidateAll metadata plugin: "+owningPlugin.getName());
        val map = pluginMetadataMap.get(owningPlugin);
        if(map != null){
            for (Map.Entry<UUID, String> entry : map.entrySet()) {
                val playerMap = this.playerMetadataMap.get(entry.getKey());
                if(playerMap != null){
                    val metadata = playerMap.get(entry.getValue());
                    metadata.invalidate();
                }
            }
        }
    }

    public void removeAll(Plugin owningPlugin) {
        Validate.notNull(owningPlugin, "Plugin cannot be null");
        Iterator<Map<Plugin, MetadataValue>> iterator = this.metadataMap.values().iterator();

        while(iterator.hasNext()) {
            Map<Plugin, MetadataValue> values = iterator.next();
            values.remove(owningPlugin);

            if (values.isEmpty()) {
                iterator.remove();
            }
        }
        removeAllCustom(owningPlugin);
    }

    private void removeAllCustom(Plugin owningPlugin) {
        logger.info("removeAll metadata plugin: "+owningPlugin.getName());
        val map = pluginMetadataMap.get(owningPlugin);
        if(map != null){
            for (Map.Entry<UUID, String> entry : map.entrySet()) {
                val playerMap = this.playerMetadataMap.get(entry.getKey());
                if(playerMap != null){
                    playerMap.remove(entry.getValue());
                }
            }
        }
    }
    
    @Override
    protected String disambiguate(OfflinePlayer player, String metadataKey) {
        return player.getUniqueId() + ":" + metadataKey;
    }
    
}
