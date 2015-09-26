package ru.idmt.commons.mrmi4.base.client;

import ru.idmt.commons.mrmi4.api.client.RClient;
import ru.idmt.commons.mrmi4.api.client.RSession;
import ru.idmt.commons.mrmi4.api.protocol.Protocol;
import ru.idmt.commons.mrmi4.api.uid.UIDManager;
import ru.idmt.commons.mrmi4.base.protocol.RA;
import ru.idmt.commons.mrmi4.base.protocol.RO;
import ru.idmt.commons.mrmi4.util.ReflectionUtils;
import ru.idmt.commons.mrmi4.commons.RException;
import ru.idmt.commons.mrmi4.commons.RObject;
import ru.idmt.commons.mrmi4.api.protocol.WaitObject;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.TimeoutException;

public abstract class AbstractClient implements RClient {
	private final UIDManager uidManager;

	public AbstractClient(UIDManager uidManager) {
		this.uidManager = uidManager;
	}

	public RSession connect(String host, int port) throws RException {
		try {
			final Protocol protocol = createProtocol(host, port);
			return new RSession() {
				public <T extends RObject> T get(Class<T> iClass) throws IOException, TimeoutException, InterruptedException {
					short classUID = uidManager.getClassUID(iClass);
					long objectUID = protocol.getObjectByClassUID(classUID).get();
					return (T) createProxy(new Class[]{iClass}, objectUID, uidManager, protocol);
				}

				public void close() throws IOException {
					protocol.close();
				}
			};
		} catch (IOException ex) {
			throw new RException(ex);
		}
	}

	protected abstract Protocol createProtocol(String host, int port) throws IOException;

	protected RObject createProxy(Class<?>[] interfaces, final long objectUID, final UIDManager uidManager, final Protocol protocol) {
		return (RObject) Proxy.newProxyInstance(getClass().getClassLoader(), interfaces, new RInvocationHandler() {
			public long getObjectUID() {
				return objectUID;
			}

			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				if(ReflectionUtils.isToString(method)) {
					return "RObject[" + objectUID + "]";
				}

				short methodUID = uidManager.getMethodUID(method);

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream output = null;
				try {
					output = new ObjectOutputStream(baos) {
						{
							enableReplaceObject(true);
						}

						@Override
						protected Object replaceObject(Object obj) throws IOException {
							if (obj instanceof RObject) {
								if (Proxy.isProxyClass(obj.getClass())) {
									InvocationHandler handler = Proxy.getInvocationHandler(obj);
									if (handler instanceof RInvocationHandler) {
										long uid = ((RInvocationHandler) handler).getObjectUID();
										return new RA(uid);
									}
								}
							}
							return super.replaceObject(obj);
						}
					};
					output.writeObject(args);
				} finally {
					if (output != null) {
						output.close();
					}
				}

				byte[] data = baos.toByteArray();
				WaitObject<byte[]> waitObject = protocol.invoke(objectUID, methodUID, data);
				byte[] result = waitObject.get();

				ByteArrayInputStream bais = new ByteArrayInputStream(result);
				ObjectInputStream input = null;
				try {
					input = new ObjectInputStream(bais) {
						{
							enableResolveObject(true);
						}

						@Override
						protected Object resolveObject(Object obj) throws IOException {
							if (obj instanceof RO) {
								RO ro = (RO) obj;

								Class<?>[] interfaces = new Class[ro.getClassUIDs().length];
								for (int index = 0; index < ro.getClassUIDs().length; index++) {
									interfaces[index] = uidManager.getClassByUID(ro.getClassUIDs()[index]);
								}
								return createProxy(interfaces, ro.getUid(), uidManager, protocol);
							}
							return super.resolveObject(obj);
						}
					};
					return input.readObject();
				} finally {
					if (input != null) {
						input.close();
					}
				}
			}
		});
	}

	private interface RInvocationHandler extends InvocationHandler {
		long getObjectUID();
	}
}
