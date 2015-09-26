package ru.idmt.commons.mrmi4.impl.async.protocol;

import ru.idmt.commons.mrmi4.api.protocol.Protocol;

public interface ClientCommandReceiver extends Protocol.CommandReceiver {
	void onGetObjectUIDByClassUIDResult(Protocol.CallId callId, long objectUID);

	void onInvokeResult(Protocol.CallId callId, byte[] data);
}