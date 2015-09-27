package ru.idmt.commons.mrmi4.base.client;

import ru.idmt.commons.mrmi4.api.client.RClient;
import ru.idmt.commons.mrmi4.api.client.RSession;
import ru.idmt.commons.mrmi4.api.protocol.Protocol;
import ru.idmt.commons.mrmi4.api.protocol.WaitObject;
import ru.idmt.commons.mrmi4.base.protocol.RA;
import ru.idmt.commons.mrmi4.base.protocol.RO;
import ru.idmt.commons.mrmi4.commons.RException;
import ru.idmt.commons.mrmi4.commons.RList;
import ru.idmt.commons.mrmi4.commons.RObject;
import ru.idmt.commons.mrmi4.commons.UIDManager;
import ru.idmt.commons.mrmi4.util.ReflectionUtils;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
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
				@SuppressWarnings("unchecked")
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
				if (ReflectionUtils.isToString(method)) {
					return "RObject[" + objectUID + "]";
				}

				short methodUID = uidManager.getMethodUID(method);

				Class returnType = method.getReturnType();
				if(returnType == int.class || returnType == Integer.class) {
					if(method.getParameterTypes().length == 0) {
						WaitObject<Integer> intValue = protocol.getInt(objectUID, methodUID);
						return intValue.get();
					}
				}

				if(returnType == List.class) {
					if(method.getParameterTypes().length == 0) {
						RList list = method.getAnnotation(RList.class);
						if(list!=null) {
							final Class<? extends RObject> componentType = list.value();
							final List<RObject> result = new ArrayList<RObject>();
							protocol.getList(objectUID, methodUID, new Protocol.OnItem() {
								public void process(long elementId) {
									RObject element = createProxy(new Class[]{componentType}, elementId, uidManager, protocol);
									result.add(element);
								}
							});

							return result;
						}
					}
				}

				byte[] data = serializeObject(args);

				WaitObject<byte[]> waitObject = protocol.invoke(objectUID, methodUID, data);
				byte[] result = waitObject.get();

				return deserializeObject(protocol, result);
			}
		});
	}

	protected byte[] serializeObject(Object object) throws IOException {
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
			output.writeObject(object);
		} finally {
			if (output != null) {
				output.close();
			}
		}

		return baos.toByteArray();
	}

	protected Object deserializeObject(final Protocol protocol, byte[] result) throws IOException, ClassNotFoundException {
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

	private interface RInvocationHandler extends InvocationHandler {
		long getObjectUID();
	}
}
