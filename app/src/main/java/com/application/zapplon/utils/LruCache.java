package com.application.zapplon.utils;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class LruCache<K, V> {

	private final HashMap<K, V> mLruMap;
	private final HashMap<K, Entry<K, V>> mWeakMap = new HashMap<K, Entry<K, V>>();
	private ReferenceQueue<V> mQueue = new ReferenceQueue<V>();

	@SuppressWarnings("serial")
	public LruCache(final int capacity) {
		mLruMap = new LinkedHashMap<K, V>(16, 0.75f, true) {
			@Override
			protected boolean removeEldestEntry(Entry<K, V> eldest) {
				return size() > capacity;
			}
		};
	}

	private static class Entry<K, V> extends WeakReference<V> {
		K mKey;

		public Entry(K key, V value, ReferenceQueue<V> queue) {
			super(value, queue);
			mKey = key;
		}
	}

	@SuppressWarnings("unchecked")
	private void cleanUpWeakMap() {
		Entry<K, V> entry = (Entry<K, V>) mQueue.poll();
		while (entry != null) {
			mWeakMap.remove(entry.mKey);
			entry = (Entry<K, V>) mQueue.poll();
		}
	}

	public synchronized V put(K key, V value) {
		cleanUpWeakMap();
		mLruMap.put(key, value);
		Entry<K, V> entry = mWeakMap.put(key, new Entry<K, V>(key, value,
				mQueue));

		return entry == null ? null : entry.get();
	}

	public synchronized V get(K key) {
		cleanUpWeakMap();
		V value = mLruMap.get(key);
		if (value != null)
			return value;
		Entry<K, V> entry = mWeakMap.get(key);
		return entry == null ? null : entry.get();
	}

	public synchronized void clear() {
		mLruMap.clear();
		mWeakMap.clear();
		mQueue = new ReferenceQueue<V>();
	}

	public synchronized boolean remove(K key) {
		V value = mLruMap.get(key);
		Entry<K, V> entry = mWeakMap.get(key);
		if (value != null && entry != null) {
			V previous = mLruMap.remove(key);
			if (previous != null) {
				mWeakMap.remove(key);
			}
			// cleanUpWeakMap();
			return true;
		}
		return false;
	}
}