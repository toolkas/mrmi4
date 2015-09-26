package ru.idmt.commons.mrmi4.api.protocol;

import java.util.concurrent.TimeoutException;

public interface WaitObject<T> {
	T get(long timeout) throws InterruptedException, TimeoutException;

	T get() throws TimeoutException, InterruptedException;
}
