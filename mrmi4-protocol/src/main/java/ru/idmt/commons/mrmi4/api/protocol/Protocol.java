package ru.idmt.commons.mrmi4.api.protocol;

import java.io.Closeable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface Protocol extends Closeable {
	WaitObject<Long> getObjectByClassUID(short classUID) throws IOException;

	WaitObject<byte[]> invoke(long objectUID, short methodUID, byte[] args) throws IOException;

	void writeInvokeResult(CallId callId, byte[] data) throws IOException;

	void writeGetIntResult(CallId callId, int result) throws IOException;

	<R extends CommandReceiver> void readCommands(R receiver) throws IOException, InterruptedException;

	void writeObjectId(CallId callId, long objectUID) throws IOException;

	boolean isClosed();

	//Performance
	WaitObject<Integer> getInt(long objectUID, short methodUID) throws IOException;

	interface CommandReceiver {
		void onGetObjectUIDByClassUID(CallId callId, short classUID);

		void onInvoke(CallId callId, long objectUID, short methodUID, byte[] data);

		void onGetInt(CallId callId, long objectUID, short methodUID);
	}

	interface CallId {
		CallId write(DataOutput output) throws IOException;

		CallId read(DataInput input) throws IOException;
	}

	interface Command {
		byte GET_OBJECT_UID_BY_CLASS_UID = 1;
		byte INVOKE = 2;

		//Performance
		byte GET_INT = 3;
	}
}