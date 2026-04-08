package com.data.profile.common.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Config
 */
public class Config {

    private Map<String,Object> config = new HashMap<>();

    public Config() {

    }

    public Config(Map<String,Object> config){
        if(config != null) {
            this.config = config;
        }
    }

    public String getString(String key){
        return String.valueOf(config.get(key));
    }

    public String getString(String key, String defaultValue) {
        Object value = config.get(key);
        return value != null ? String.valueOf(value) : defaultValue;
    }

    public List<String> getStringList(String key){
        return (List<String>)config.get(key);
    }

    public Integer getInt(String key){
        return Integer.valueOf(String.valueOf(config.get(key)));
    }

    public Integer getInt(String key, Integer defaultValue) {
        Object value = config.get(key);
        return value != null ? Integer.valueOf(String.valueOf(value)) : defaultValue;
    }

    public Boolean getBoolean(String key){
        return Boolean.valueOf(String.valueOf(config.get(key)));
    }

    public Double getDouble(String key){
        return Double.valueOf(String.valueOf(config.get(key)));
    }

    public Long getLong(String key){
        return Long.valueOf(String.valueOf(config.get(key)));
    }

    public Long getLong(String key, Long defaultValue) {
        Object value = config.get(key);
        return value != null ? Long.valueOf(String.valueOf(value)) : defaultValue;
    }

    public Boolean has(String key) {
        return config.get(key) != null;
    }

    public Set<Entry<String, Object>> entrySet() {
        return config.entrySet();
    }

    public Map<String,Object> configMap() {
        return config;
    }

    public boolean isEmpty() {
        return config.size() <= 0;
    }

    public void put(String key ,Object value) {
        this.config.put(key,value);
    }

    public void merge(Map<String,Object> config) {
        config.forEach(this.config::putIfAbsent);
    }

    public Object remove(String key) {
        return config.remove(key);
    }
}
