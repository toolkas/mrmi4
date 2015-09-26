package ru.idmt.commons.mrmi4.impl.async.server;

import ru.idmt.commons.mrmi4.api.protocol.Protocol;
import ru.idmt.commons.mrmi4.base.server.AbstractServer;
import ru.idmt.commons.mrmi4.impl.async.Waiting;
import ru.idmt.commons.mrmi4.impl.async.protocol.AsyncProtocolImpl;
import ru.idmt.commons.mrmi4.impl.async.wait.WaitingImpl;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AsyncServer extends AbstractServer {
	private final Waiting waiting = new WaitingImpl();
	private final int threads;

	public AsyncServer(int serverPort, int threads) {
		super(serverPort);
		this.threads = threads;
	}

	@Override
	protected Executor createExecutor() {
		return Executors.newFixedThreadPool(threads);
	}

	@Override
	protected Protocol createProtocol(Socket socket) throws IOException {
		return new AsyncProtocolImpl(socket, waiting);
	}
}
