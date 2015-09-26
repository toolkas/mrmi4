package ru.idmt.commons.mrmi4.api.protocol;

import java.io.Closeable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface Protocol extends Closeable {
	WaitObject<Long> getObjectByClassUID(short classUID) throws IOException;

	WaitObject<byte[]> invoke(long objectUID, short methodUID, byte[] args) throws IOException;

	void writeInvokeResult(CallId callId, byte[] data) throws IOException;

	<R extends CommandReceiver> void readCommands(R receiver) throws IOException, InterruptedException;

	void writeObjectId(CallId callId, long objectUID) throws IOException;

	boolean isClosed();

	interface CommandReceiver {
		void onGetObjectUIDByClassUID(CallId callId, short classUID);

		void onInvoke(CallId callId, long objectUID, short methodUID, byte[] data);
	}

	interface CallId {
		CallId write(DataOutput output) throws IOException;

		CallId read(DataInput input) throws IOException;
	}

	interface Command {
		short GET_OBJECT_UID_BY_CLASS_UID = 1;
		short INVOKE = 2;
	}
}