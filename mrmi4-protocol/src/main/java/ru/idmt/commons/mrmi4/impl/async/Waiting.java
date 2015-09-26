package ru.idmt.commons.mrmi4.impl.async;

public interface Waiting {
	void put(Object key, Object value);

	Object get(Object key);
}
