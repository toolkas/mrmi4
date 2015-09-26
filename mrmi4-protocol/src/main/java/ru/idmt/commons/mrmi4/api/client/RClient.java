package ru.idmt.commons.mrmi4.api.client;

import ru.idmt.commons.mrmi4.commons.RException;

public interface RClient {
	RSession connect(String host, int port) throws RException;
}