package me.damascus2000.pingo.utils;

import java.util.ArrayList;
import java.util.HashMap;

public class QueueMap<K, V> extends HashMap<K, V> {

    private final ArrayList<K> q;
    private final int maxSize;

    public QueueMap(){
        this(8);
    }

    public QueueMap(int maxSize){
        super();
        this.maxSize = maxSize;
        this.q = new ArrayList<>();
    }

    @Override
    public V put(K key, V value){
        if (q.size() == maxSize){
            this.remove(q.get(0));
            q.remove(0);
        }
        super.put(key, value);
        q.add(key);
        return value;
    }

    @Override
    public V remove(Object key){
        q.remove(key);
        return super.remove(key);
    }

    public boolean contains(K key){
        return q.contains(key);
    }

}
