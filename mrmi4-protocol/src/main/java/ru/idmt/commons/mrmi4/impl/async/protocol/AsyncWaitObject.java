package ru.idmt.commons.mrmi4.impl.async.protocol;

import ru.idmt.commons.mrmi4.api.protocol.WaitObject;

import java.util.concurrent.TimeoutException;

public class AsyncWaitObject<T> implements WaitObject<T> {
	private final Object lock = new Object();

	private Object key;
	private volatile T value;
	private volatile boolean updated = false;

	public AsyncWaitObject(Object key) {
		this.key = key;
	}

	public T get(long timeout) throws InterruptedException, TimeoutException {
		synchronized (lock) {
			while (!updated) {
				lock.wait();
			}
			return value;
		}
	}

	public T get() throws TimeoutException, InterruptedException {
		return get(Long.MAX_VALUE);
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
