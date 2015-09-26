package ru.idmt.commons.mrmi4.example.server;

import org.xml.sax.SAXException;
import ru.idmt.commons.mrmi4.api.server.RServer;
import ru.idmt.commons.mrmi4.commons.ObjectManagerImpl;
import ru.idmt.commons.mrmi4.commons.UIDManager;
import ru.idmt.commons.mrmi4.example.api.IExample;
import ru.idmt.commons.mrmi4.example.api.Loader;
import ru.idmt.commons.mrmi4.impl.async.server.AsyncServer;
import ru.idmt.commons.mrmi4.uid.ReflectionsUIDManager;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URISyntaxException;

public class ExampleServer {
	public static void main(String[] args) throws IOException, SAXException, URISyntaxException, ParserConfigurationException, NoSuchMethodException, ClassNotFoundException {
		UIDManager uidManager = new ReflectionsUIDManager(Loader.class.getResource("example.xml"));
		ObjectManagerImpl objectManager = new ObjectManagerImpl();
		objectManager.register(IExample.class, new IExample() {
			public int getInt() {
				return 1;
			}

			public int getInt(String value) {
				return 12;
			}

			public int getInt(String value, String value2) {
				return 15;
			}
		});

		RServer server = new AsyncServer(6969, 20);
		RServer.State state = server.start(uidManager, objectManager);
		System.out.println("RServer[" + state.port() + "] started at " + state.started());
	}
}
