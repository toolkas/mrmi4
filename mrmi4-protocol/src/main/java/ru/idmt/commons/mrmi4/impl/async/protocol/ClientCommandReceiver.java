package ru.idmt.commons.mrmi4.impl.async.protocol;

import ru.idmt.commons.mrmi4.api.protocol.Protocol;

public interface ClientCommandReceiver extends Protocol.CommandReceiver {
	<T> void onReceive(Protocol.CallId callId, T result);
}