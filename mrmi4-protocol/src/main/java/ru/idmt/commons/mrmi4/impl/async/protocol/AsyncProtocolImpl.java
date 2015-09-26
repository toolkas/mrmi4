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

	private final Object readLock = new Object();
	private final Object writeLock = new Object();
	private final Object callIdLock = new Object();

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
			output.writeShort(Command.GET_OBJECT_UID_BY_CLASS_UID);
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
			output.writeShort(Command.INVOKE);
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
			output.writeShort(AsyncCommand.INVOKE_RESULT);
			callId.write(output);
			output.writeInt(data.length);
			output.write(data);
			output.flush();
		}
	}

	public <R extends CommandReceiver> void readCommands(R receiver) throws IOException, InterruptedException {
		synchronized (readLock) {
			short command;
			while ((command = input.readShort()) != -1) {
				switch (command) {
					case Command.GET_OBJECT_UID_BY_CLASS_UID:
						CallId callId = new AsyncCallId().read(input);
						short classUID = input.readShort();

						receiver.onGetObjectUIDByClassUID(callId, classUID);
						break;
					case AsyncCommand.GET_OBJECT_UID_BY_CLASS_UID_RESULT:
						CallId callId2 = new AsyncCallId().read(input);
						long objectUID = input.readLong();
						((ClientCommandReceiver) receiver).onGetObjectUIDByClassUIDResult(callId2, objectUID);
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

						((ClientCommandReceiver) receiver).onInvokeResult(callId4, data2);
						break;
				}
			}
		}
	}

	public void writeObjectId(CallId callId, long objectUID) throws IOException {
		synchronized (writeLock) {
			output.writeShort(AsyncCommand.GET_OBJECT_UID_BY_CLASS_UID_RESULT);
			callId.write(output);
			output.writeLong(objectUID);
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
		short GET_OBJECT_UID_BY_CLASS_UID_RESULT = 3;
		short INVOKE_RESULT = 4;
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