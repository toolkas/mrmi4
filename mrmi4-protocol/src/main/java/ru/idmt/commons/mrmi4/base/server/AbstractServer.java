package ru.idmt.commons.mrmi4.base.server;

import org.apache.log4j.Logger;
import ru.idmt.commons.mrmi4.api.om.ObjectManager;
import ru.idmt.commons.mrmi4.api.protocol.Protocol;
import ru.idmt.commons.mrmi4.api.server.RServer;
import ru.idmt.commons.mrmi4.api.uid.UIDManager;
import ru.idmt.commons.mrmi4.base.protocol.RA;
import ru.idmt.commons.mrmi4.base.protocol.RO;
import ru.idmt.commons.mrmi4.commons.RObject;
import ru.idmt.commons.mrmi4.util.ReflectionUtils;

import java.io.*;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.Executor;

public abstract class AbstractServer implements RServer {
	private static final Logger LOGGER = Logger.getLogger(AbstractServer.class);

	private final int serverPort;
	private boolean running;

	public AbstractServer(int serverPort) {
		this.serverPort = serverPort;
	}

	public State start(final UIDManager uidManager, final ObjectManager objectManager) throws IOException {
		final Date started = new Date();

		final ServerSocket serverSocket = new ServerSocket(serverPort);

		final Executor executor = createExecutor();

		running = true;
		final Thread main = new Thread("Server") {
			@Override
			public void run() {
				while (running) {
					try {
						final Socket socket = serverSocket.accept();
						final Protocol protocol = createProtocol(socket);

						new Thread("CommandsReader[" + socket + "]") {
							@Override
							public void run() {
								try {
									protocol.readCommands(new Protocol.CommandReceiver() {
										public void onGetObjectUIDByClassUID(final Protocol.CallId callId, final short classUID) {
											executor.execute(new Runnable() {
												public void run() {
													try {
														Class<?> iClass = uidManager.getClassByUID(classUID);
														long objectUID = objectManager.getUID(iClass);
														protocol.writeObjectId(callId, objectUID);
													} catch (Exception e) {
														LOGGER.error(e.getMessage(), e);
													}
												}
											});
										}

										public void onInvoke(final Protocol.CallId callId, final long objectUID, final short methodUID, final byte[] data) {
											executor.execute(new Runnable() {
												public void run() {
													try {
														final RObject object = objectManager.get(objectUID);
														Method method = uidManager.getMethodByUID(methodUID);

														Object[] args = (Object[]) deserializeObject(objectManager, data);

														final Object ret = method.invoke(object, args);

														byte[] out = serializeObject(uidManager, objectManager, ret);
														protocol.writeInvokeResult(callId, out);
													} catch (Exception e) {
														LOGGER.error(e.getMessage(), e);
													}
												}

											});
										}
									});
								} catch (Exception ex) {
									LOGGER.error("CommandsReader[" + socket + "]: " + ex.getMessage(), ex);
								}
							}
						}.start();
					} catch (Exception ex) {
						LOGGER.error("main loop[" + serverSocket + "]: " + ex.getMessage(), ex);
					}
				}
			}
		};
		main.start();

		return new State() {
			public int port() {
				return serverPort;
			}

			public Date started() {
				return started;
			}

			public void stop() throws IOException {
				running = false;
				main.interrupt();
				serverSocket.close();
			}
		};
	}

	protected byte[] serializeObject(final UIDManager uidManager, final ObjectManager objectManager, Object ret) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(baos) {
				{
					enableReplaceObject(true);
				}

				@Override
				protected Object replaceObject(Object obj) throws IOException {
					if (obj instanceof RObject) {
						short[] interfaces = ReflectionUtils.getRemoteInterfaces(uidManager, obj.getClass());
						long objectUID = objectManager.add((RObject) obj);
						return new RO(interfaces, objectUID);
					}
					return super.replaceObject(obj);
				}
			};
			oos.writeObject(ret);

		} finally {
			if (oos != null) {
				oos.close();
			}
		}

		return baos.toByteArray();
	}

	protected Object deserializeObject(final ObjectManager objectManager, byte[] data) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(bais) {
				{
					enableResolveObject(true);
				}

				@Override
				protected Object readObjectOverride() throws IOException, ClassNotFoundException {
					return super.readObjectOverride();
				}

				@Override
				protected Object resolveObject(Object obj) throws IOException {
					if (obj instanceof RA) {
						RA ra = (RA) obj;
						return objectManager.get(ra.getUid());
					}
					return super.resolveObject(obj);
				}
			};
			return ois.readObject();
		} finally {
			if (ois != null) {
				ois.close();
			}
		}
	}

	protected abstract Executor createExecutor();

	protected abstract Protocol createProtocol(Socket socket) throws IOException;
}
