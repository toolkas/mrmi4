package ru.idmt.commons.mrmi4.api.client;

import ru.idmt.commons.mrmi4.commons.RObject;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface RSession extends Closeable {
	<T extends RObject> T get(Class<T> iClass) throws IOException, TimeoutException, InterruptedException;
}
