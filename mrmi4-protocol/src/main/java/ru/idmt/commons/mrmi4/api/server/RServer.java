package ru.idmt.commons.mrmi4.api.server;

import ru.idmt.commons.mrmi4.api.om.ObjectManager;
import ru.idmt.commons.mrmi4.api.uid.UIDManager;

import java.io.IOException;
import java.util.Date;

public interface RServer {
	State start(UIDManager uidManager, ObjectManager objectManager) throws IOException;

	interface State {
		int port();

		Date started();

		void stop() throws IOException;
	}
}
