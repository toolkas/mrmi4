package ru.idmt.commons.mrmi4.impl.async.protocol;

import ru.idmt.commons.mrmi4.api.protocol.WaitObject;

public class AsyncWaitObject<T> implements WaitObject<T> {
	private final Object lock = new Object();

	private Object key;
	private volatile T value;
	private volatile boolean updated = false;

	public AsyncWaitObject(Object key) {
		this.key = key;
	}

	public T get() throws InterruptedException {
		synchronized (lock) {
			while (!updated) {
				lock.wait();
			}
			return value;
		}
	}

	public void set(T value) {
		synchronized (lock) {
			updated = true;
			this.value = value;
			lock.notify();
		}
	}

	@Override
	public String toString() {
		return "AsyncWaitObject{key=" + key + '}';
	}
}
