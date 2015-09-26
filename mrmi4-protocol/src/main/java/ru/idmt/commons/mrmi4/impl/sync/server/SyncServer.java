package ru.idmt.commons.mrmi4.impl.sync.server;

import org.jetbrains.annotations.NotNull;
import ru.idmt.commons.mrmi4.api.protocol.Protocol;
import ru.idmt.commons.mrmi4.base.server.AbstractServer;
import ru.idmt.commons.mrmi4.impl.sync.protocol.SyncProtocolImpl;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executor;

public class SyncServer extends AbstractServer {
	public SyncServer(int serverPort) {
		super(serverPort);
	}

	@Override
	protected Executor createExecutor() {
		return new Executor() {
			public void execute(@NotNull Runnable command) {
				command.run();
			}
		};
	}

	@Override
	protected Protocol createProtocol(Socket socket) throws IOException {
		return new SyncProtocolImpl(socket);
	}
}
