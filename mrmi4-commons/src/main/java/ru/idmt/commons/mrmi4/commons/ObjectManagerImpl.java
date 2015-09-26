package ru.idmt.commons.mrmi4.commons;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ObjectManagerImpl implements ObjectManager {
	private final AtomicLong counter = new AtomicLong(Long.MIN_VALUE);

	private final Map<Long, RObject> objectByUID = new ConcurrentHashMap<Long, RObject>();
	private final Map<Class, Long> objectByClass = new ConcurrentHashMap<Class, Long>();

	public long add(RObject object) {
		long objectUID = counter.incrementAndGet();
		objectByUID.put(objectUID, object);
		return objectUID;
	}

	public RObject get(long objectUID) {
		RObject object = objectByUID.get(objectUID);
		if (object == null) {
			throw new IllegalArgumentException("there is no ROBJECT[" + objectUID + "]");
		}
		return object;
	}

	public long getUID(Class<?> iClass) {
		return objectByClass.get(iClass);
	}

	public synchronized <T extends RObject> void register(Class<T> iClass, T object) {
		long objectUID = add(object);
		if (objectByClass.containsKey(iClass)) {
			throw new IllegalArgumentException(iClass + " already registered");
		}
		objectByClass.put(iClass, objectUID);
	}
}
