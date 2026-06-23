package com.api.reports.cache;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Caché en memoria con expiración por tiempo, sin dependencias externas.
 * Evita repetir la misma llamada HTTP costosa (ej. traer todas las órdenes/productos
 * de una tienda) en cada uno de los endpoints de Reports que se consultan en una misma sesión del panel admin.
 */
public class TtlCache<K, V> {

    private record Entry<V>(V value, Instant expiresAt) {}

    private final ConcurrentHashMap<K, Entry<V>> store = new ConcurrentHashMap<>();
    private final Duration ttl;

    public TtlCache(Duration ttl) {
        this.ttl = ttl;
    }

    public V getOrCompute(K key, Supplier<V> loader) {
        Entry<V> entry = store.get(key);
        if (entry != null && Instant.now().isBefore(entry.expiresAt())) {
            return entry.value();
        }
        V value = loader.get();
        store.put(key, new Entry<>(value, Instant.now().plus(ttl)));
        return value;
    }
}
