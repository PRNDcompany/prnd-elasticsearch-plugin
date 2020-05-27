package org.elasticsearch.index.analysis;

import java.util.HashMap;
import java.util.Map;

public class Dictionary {
    final private double INVALID_KEY_SCORE_UNIT = 100.0;
    final private Map<String, Double> map;

    public Dictionary(Map<String, Double> map) {
         this.map = new HashMap<>(map);
    }

    public Double get(String key) {
        if (map.containsKey(key)) {
            return map.get(key);
        }
        return INVALID_KEY_SCORE_UNIT * key.length();
    }
}
