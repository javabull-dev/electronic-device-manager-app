package cn.ljpc.electronic.util;

import java.util.HashMap;
import java.util.Map;

public class MapUtil {

    private Map<String, String> map = new HashMap<>();

    public static MapUtil crateMap() {
        return new MapUtil();
    }

    public MapUtil data(String key, String value) {
        map.put(key, value);
        return this;
    }

    public Map<String, String> getMap() {
        return map;
    }
}
