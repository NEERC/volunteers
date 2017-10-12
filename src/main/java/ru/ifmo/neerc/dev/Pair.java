package ru.ifmo.neerc.dev;

import lombok.Data;

/**
 * Created by Lapenok Akesej on 12.10.2017.
 */
@Data
public class Pair<K, V> {
    private final K key;
    private final V value;
}
