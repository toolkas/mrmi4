package ru.idmt.commons.mrmi4.impl.async.wait;

import ru.idmt.commons.mrmi4.impl.async.Waiting;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WaitingImpl implements Waiting {
	private final Map values = new ConcurrentHashMap();

	public void put(Object key, Object value) {
		values.put(key, value);
	}

	public Object get(Object key) {
		return values.remove(key);
	}
}
