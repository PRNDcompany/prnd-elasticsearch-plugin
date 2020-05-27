package org.elasticsearch.index.analysis;

import java.util.HashMap;
import java.util.Map;

public class Dictionary {
    final private Map<String, Double> map;

    public Dictionary(Map<String, Double> map) {
         this.map = new HashMap<>(map);
    }

    public Double get(String key) {
        if (map.containsKey(key)) {
            return map.get(key);
        }
        return 10.0 * key.length();
    }
}
