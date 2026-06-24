package com.api.gateway.cache;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import reactor.core.publisher.Mono;

/**
 * Caché reactiva en memoria con expiración por tiempo, sin dependencias externas.
 * Evita que N requests paralelas del mismo frontend (ej. un dashboard cargando
 * varios widgets a la vez) disparen N llamadas idénticas a un servicio downstream
 * dentro de la ventana de TTL.
 */
public class TtlCache<K, V> {

    private record Entry<V>(V value, Instant expiresAt) {}

    private final ConcurrentHashMap<K, Entry<V>> store = new ConcurrentHashMap<>();
    private final Duration ttl;

    public TtlCache(Duration ttl) {
        this.ttl = ttl;
    }

    public Mono<V> getOrCompute(K key, Supplier<Mono<V>> loader) {
        Entry<V> entry = store.get(key);
        if (entry != null && Instant.now().isBefore(entry.expiresAt())) {
            return Mono.just(entry.value());
        }
        return loader.get().doOnNext(value -> store.put(key, new Entry<>(value, Instant.now().plus(ttl))));
    }
}
