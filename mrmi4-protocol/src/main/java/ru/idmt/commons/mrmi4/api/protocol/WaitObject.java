package ru.idmt.commons.mrmi4.api.protocol;

public interface WaitObject<T> {
	T get() throws InterruptedException;
}
