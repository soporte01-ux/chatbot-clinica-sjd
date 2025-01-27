package pe.com.s2o.clinica.shorter;

import java.util.HashMap;
import java.util.Map;

public class UrlStorage {
    private final Map<String, String> storage = new HashMap<>();

    public void save(String slug, String longUrl) {
        storage.put(slug, longUrl);
    }

    public String get(String slug) {
        return storage.get(slug);
    }

    public boolean exists(String slug) {
        return storage.containsKey(slug);
    }
}
