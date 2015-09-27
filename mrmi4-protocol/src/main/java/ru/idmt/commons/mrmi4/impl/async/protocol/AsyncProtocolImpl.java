package ru.idmt.commons.mrmi4.impl.async.protocol;

import ru.idmt.commons.mrmi4.api.protocol.Protocol;
import ru.idmt.commons.mrmi4.api.protocol.WaitObject;
import ru.idmt.commons.mrmi4.impl.async.Waiting;

import java.io.*;
import java.net.Socket;
import java.util.Random;

public class AsyncProtocolImpl implements Protocol {
	private final Waiting waiting;

	private final Socket socket;
	private final DataOutputStream output;
	private final DataInputStream input;
	private final Random random = new Random();

	private final Object readLock = new Object() {
		@Override
		public String toString() {
			return "ReadLock()";
		}
	};
	private final Object writeLock = new Object() {
		@Override
		public String toString() {
			return "WriteLock()";
		}
	};
	private final Object callIdLock = new Object() {
		@Override
		public String toString() {
			return "CreateCallIdLock()";
		}
	};

	private String category;

	public AsyncProtocolImpl(Socket socket, final Waiting waiting) throws IOException {
		this.socket = socket;
		this.waiting = waiting;

		category = socket.getInetAddress() + ":" + socket.getPort() + ":";


		output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream(), 60024));
		output.flush();

		this.input = new DataInputStream(new BufferedInputStream(socket.getInputStream(), 1024));
	}

	public WaitObject<Long> getObjectByClassUID(short classUID) throws IOException {
		CallId callId = createCallId();

		AsyncWaitObject<Long> object = new AsyncWaitObject<Long>(callId);
		waiting.put(callId, object);

		synchronized (writeLock) {
			output.writeByte(Command.GET_OBJECT_UID_BY_CLASS_UID);
			callId.write(output);
			output.writeShort(classUID);
			output.flush();
		}

		return object;
	}

	public WaitObject<byte[]> invoke(long objectUID, short methodUID, byte[] data) throws IOException {
		CallId callId = createCallId();

		AsyncWaitObject<byte[]> object = new AsyncWaitObject<byte[]>(callId);
		waiting.put(callId, object);

		synchronized (writeLock) {
			output.writeByte(Command.INVOKE);
			callId.write(output);
			output.writeLong(objectUID);
			output.writeShort(methodUID);

			output.writeInt(data.length);
			output.write(data);
			output.flush();
		}

		return object;
	}

	public void writeInvokeResult(CallId callId, byte[] data) throws IOException {
		synchronized (writeLock) {
			output.writeByte(AsyncCommand.INVOKE_RESULT);
			callId.write(output);
			output.writeInt(data.length);
			output.write(data);
			output.flush();
		}
	}

	public void writeGetIntResult(CallId callId, int result) throws IOException {
		synchronized (writeLock) {
			output.writeByte(AsyncCommand.GET_INT_RESULT);
			callId.write(output);
			output.writeInt(result);
			output.flush();
		}
	}

	public <R extends CommandReceiver> void readCommands(R receiver) throws IOException, InterruptedException {
		synchronized (readLock) {
			byte command;
			while ((command = input.readByte()) != -1) {
				switch (command) {
					case Command.GET_OBJECT_UID_BY_CLASS_UID:
						CallId callId = new AsyncCallId().read(input);
						short classUID = input.readShort();

						receiver.onGetObjectUIDByClassUID(callId, classUID);
						break;
					case AsyncCommand.GET_OBJECT_UID_BY_CLASS_UID_RESULT:
						CallId callId2 = new AsyncCallId().read(input);
						long objectUID = input.readLong();
						((ClientCommandReceiver) receiver).onReceive(callId2, objectUID);
						break;
					case Command.INVOKE:
						CallId callId3 = new AsyncCallId().read(input);
						long objectUID2 = input.readLong();
						short methodUID = input.readShort();

						int n = input.readInt();
						byte[] bytes = new byte[n];
						input.readFully(bytes, 0, n);

						receiver.onInvoke(callId3, objectUID2, methodUID, bytes);
						break;
					case AsyncCommand.INVOKE_RESULT:
						CallId callId4 = new AsyncCallId().read(input);

						int n2 = input.readInt();
						byte[] data2 = new byte[n2];
						input.readFully(data2, 0, n2);

						((ClientCommandReceiver) receiver).onReceive(callId4, data2);
						break;
					case AsyncCommand.GET_INT:
						CallId callId5 = new AsyncCallId().read(input);

						long objectUID3 = input.readLong();
						short methodUID2 = input.readShort();

						receiver.onGetInt(callId5, objectUID3, methodUID2);
						break;
					case AsyncCommand.GET_INT_RESULT:
						CallId callId6 = new AsyncCallId().read(input);
						int result = input.readInt();

						((ClientCommandReceiver) receiver).onReceive(callId6, result);
						break;
					case AsyncCommand.GET_LIST:
						CallId callId7 = new AsyncCallId().read(input);

						long objectId = input.readLong();
						short methodUID3 = input.readShort();

						receiver.onGetList(callId7, objectId, methodUID3);
						break;
					case AsyncCommand.GET_LIST_RESULT:
						CallId callId8 = new AsyncCallId().read(input);

						int n3 = input.readInt();
						long[] elementIds = new long[n3];

						for (int index = 0; index < n3; index++) {
							elementIds[index] = input.readLong();
						}

						((ClientCommandReceiver) receiver).onReceive(callId8, elementIds);
						break;
				}
			}
		}
	}

	public void writeObjectId(CallId callId, long objectUID) throws IOException {
		synchronized (writeLock) {
			output.writeByte(AsyncCommand.GET_OBJECT_UID_BY_CLASS_UID_RESULT);
			callId.write(output);
			output.writeLong(objectUID);
			output.flush();
		}
	}

	public WaitObject<Integer> getInt(long objectUID, short methodUID) throws IOException {
		CallId callId = createCallId();

		AsyncWaitObject<Integer> object = new AsyncWaitObject<Integer>(callId);
		waiting.put(callId, object);

		synchronized (writeLock) {
			output.writeByte(Command.GET_INT);
			callId.write(output);
			output.writeLong(objectUID);
			output.writeShort(methodUID);
			output.flush();
		}

		return object;
	}

	public void getList(long objectUID, short methodUID, OnItem onItem) throws IOException, InterruptedException {
		CallId callId = createCallId();

		AsyncWaitObject<long[]> object = new AsyncWaitObject<long[]>(callId);
		waiting.put(callId, object);

		synchronized (writeLock) {
			output.writeByte(Command.GET_LIST);
			callId.write(output);
			output.writeLong(objectUID);
			output.writeShort(methodUID);
			output.flush();
		}

		Object value = object.get();
		long[] elementIds = (long[]) value;
		for (long elementId : elementIds) {
			onItem.process(elementId);
		}
	}

	public void writeGetListResult(CallId callId, int size, OnWriteGetList onWrite) throws IOException {
		synchronized (writeLock) {
			output.writeByte(AsyncCommand.GET_LIST_RESULT);
			callId.write(output);
			output.writeInt(size);

			for (int index = 0; index < size; index++) {
				long elementId = onWrite.getElementId(index);
				output.writeLong(elementId);
			}
			output.flush();
		}
	}

	public boolean isClosed() {
		return socket.isClosed();
	}

	public void close() throws IOException {
		socket.close();
	}

	private CallId createCallId() {
		synchronized (callIdLock) {
			String value = category + random.nextInt();
			return new AsyncCallId(value);
		}
	}

	private interface AsyncCommand extends Command {
		byte GET_OBJECT_UID_BY_CLASS_UID_RESULT = 10;
		byte INVOKE_RESULT = 11;
		byte GET_INT_RESULT = 12;
		byte GET_LIST_RESULT = 13;
	}

	private static class AsyncCallId implements CallId {
		private String value;

		public AsyncCallId(String value) {
			this.value = value;
		}

		public AsyncCallId() {
		}

		public CallId write(DataOutput output) throws IOException {
			output.writeUTF(value);
			return this;
		}

		public CallId read(DataInput input) throws IOException {
			value = input.readUTF();
			return this;
		}


		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			AsyncCallId that = (AsyncCallId) o;

			return !(value != null ? !value.equals(that.value) : that.value != null);

		}

		@Override
		public int hashCode() {
			return value != null ? value.hashCode() : 0;
		}

		@Override
		public String toString() {
			return value;
		}
	}
}