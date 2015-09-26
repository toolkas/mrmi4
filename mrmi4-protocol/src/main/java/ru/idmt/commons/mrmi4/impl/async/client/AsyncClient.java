package ru.idmt.commons.mrmi4.impl.async.client;

import ru.idmt.commons.mrmi4.api.protocol.Protocol;
import ru.idmt.commons.mrmi4.commons.UIDManager;
import ru.idmt.commons.mrmi4.base.client.AbstractClient;
import ru.idmt.commons.mrmi4.impl.async.Waiting;
import ru.idmt.commons.mrmi4.impl.async.protocol.AsyncProtocolImpl;
import ru.idmt.commons.mrmi4.impl.async.protocol.AsyncWaitObject;
import ru.idmt.commons.mrmi4.impl.async.protocol.ClientCommandReceiver;
import ru.idmt.commons.mrmi4.impl.async.wait.WaitingImpl;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class AsyncClient extends AbstractClient {
	private final Waiting waiting = new WaitingImpl();

	public AsyncClient(UIDManager uidManager) {
		super(uidManager);
	}

	@Override
	protected Protocol createProtocol(String host, int port) throws IOException {
		final Protocol protocol = new AsyncProtocolImpl(new Socket(host, port), waiting);

		new Thread("client mrmi output reader") {
			@Override
			public void run() {
				try {
					protocol.readCommands(new ClientCommandReceiver() {
						public void onGetObjectUIDByClassUIDResult(Protocol.CallId callId, long objectUID) {
							AsyncWaitObject<Long> object = (AsyncWaitObject<Long>) waiting.get(callId);
							object.set(objectUID);
						}

						public void onGetObjectUIDByClassUID(Protocol.CallId callId, short classUID) {
							throw new UnsupportedOperationException();
						}

						public void onInvoke(Protocol.CallId callId, long objectUID, short methodUID, byte[] data) {
							throw new UnsupportedOperationException();
						}

						public void onInvokeResult(Protocol.CallId callId, byte[] data) {
							AsyncWaitObject<byte[]> object = (AsyncWaitObject<byte[]>) waiting.get(callId);
							object.set(data);
						}
					});
				} catch (SocketException ex) {
					if (!protocol.isClosed()) {
						ex.printStackTrace();
					}
				} catch (Exception e) {
					System.out.println("waiting = " + waiting);
					e.printStackTrace();
				}
			}
		}.start();

		return protocol;
	}
}