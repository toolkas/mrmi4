package ru.idmt.commons.mrmi4.example;

import ru.idmt.commons.mrmi4.api.om.ObjectManager;
import ru.idmt.commons.mrmi4.api.server.RServer;
import ru.idmt.commons.mrmi4.commons.RObject;
import ru.idmt.commons.mrmi4.impl.async.server.AsyncServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class ExampleServer {
	private static AtomicLong ID = new AtomicLong(0);

	public static void main(String[] args) throws IOException {
		final List<String> list = new ArrayList<String>();
		for (int index = 0; index < 100; index++) {
			list.add("TEST");
		}

		final Map<Long, RObject> objects = new HashMap<Long, RObject>();
		objects.put(1L, new IExample() {
			public String getValue() {
				return "TESTTESTTESTTESTTESTTESTTESTTEST";
			}

			public List<String> getList() {
				return list;
			}
		});

		RServer server = new AsyncServer(6969, 50);
		RServer.State state = server.start(new ExampleUIDManager(), new ObjectManager() {
			public long add(RObject object) {
				long id = ID.incrementAndGet();
				objects.put(id, object);
				return id;
			}

			public RObject get(long objectUID) {
				return objects.get(objectUID);
			}

			public long getUID(Class<?> iClass) {
				if (iClass.equals(IExample.class)) {
					return 1;
				}
				return 0;
			}
		});
		System.out.println("RServer[" + state.port() + "] started at " + state.started());
	}
}
