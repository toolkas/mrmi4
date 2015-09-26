package ru.idmt.commons.mrmi4.impl.sync.client;

import ru.idmt.commons.mrmi4.api.protocol.Protocol;
import ru.idmt.commons.mrmi4.api.uid.UIDManager;
import ru.idmt.commons.mrmi4.base.client.AbstractClient;
import ru.idmt.commons.mrmi4.impl.sync.protocol.SyncProtocolImpl;

import java.io.IOException;
import java.net.Socket;

public class SyncClient extends AbstractClient {
	public SyncClient(UIDManager uidManager) {
		super(uidManager);
	}

	@Override
	protected Protocol createProtocol(String host, int port) throws IOException {
		Socket socket = new Socket(host, port);
		return new SyncProtocolImpl(socket);
	}
}
