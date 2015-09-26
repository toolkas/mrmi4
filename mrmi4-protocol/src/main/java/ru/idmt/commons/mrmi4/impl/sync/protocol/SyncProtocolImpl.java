package ru.idmt.commons.mrmi4.impl.sync.protocol;

import ru.idmt.commons.mrmi4.api.protocol.Protocol;
import ru.idmt.commons.mrmi4.api.protocol.WaitObject;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.TimeoutException;

/**
 * Синхронная реализация протокола обмена.
 * Запись и чтение данных идут последовательно в одном потоке в одном блоке синхронизации.
 */
public class SyncProtocolImpl implements Protocol {
	private final Socket socket;
	private final DataOutputStream output;
	private final DataInputStream input;

	public SyncProtocolImpl(Socket socket) throws IOException {
		this.socket = socket;

		output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream(), 60024));
		output.flush();

		this.input = new DataInputStream(new BufferedInputStream(socket.getInputStream(), 1024));
	}

	public synchronized WaitObject<Long> getObjectByClassUID(short classUID) throws IOException {
		output.writeShort(Command.GET_OBJECT_UID_BY_CLASS_UID);
		output.writeShort(classUID);
		output.flush();

		final long objectUID = input.readLong();
		return new WaitObject<Long>() {
			public Long get() {
				return objectUID;
			}

			public Long get(long timeout) {
				return get();
			}
		};
	}

	public synchronized void writeObjectId(CallId callId, long objectUID) throws IOException {
		output.writeLong(objectUID);
		output.flush();
	}

	public synchronized WaitObject<byte[]> invoke(long objectUID, short methodUID, byte[] data) throws IOException {
		output.writeShort(Command.INVOKE);
		output.writeLong(objectUID);
		output.writeShort(methodUID);

		output.writeInt(data.length);
		output.write(data);
		output.flush();

		int n = input.readInt();
		final byte[] bytes = new byte[n];
		input.readFully(bytes, 0, n);

		return new WaitObject<byte[]>() {
			public byte[] get() {
				return bytes;
			}

			public byte[] get(long timeout) {
				return get();
			}
		};
	}

	public synchronized void writeInvokeResult(CallId callId, byte[] data) throws IOException {
		output.writeInt(data.length);
		output.write(data);
		output.flush();
	}

	public synchronized void writeGetIntResult(CallId callId, int result) throws IOException {
		output.writeInt(result);
		output.flush();
	}

	public synchronized void readCommands(CommandReceiver receiver) throws IOException {
		short command;
		while ((command = input.readShort()) != -1) {
			switch (command) {
				case Command.GET_OBJECT_UID_BY_CLASS_UID:
					receiver.onGetObjectUIDByClassUID(null, input.readShort());
					break;
				case Command.INVOKE:
					long objectUID = input.readLong();
					short methodUID = input.readShort();

					int n = input.readInt();
					final byte[] bytes = new byte[n];
					input.readFully(bytes, 0, n);

					receiver.onInvoke(null, objectUID, methodUID, bytes);
					break;
				case Command.GET_INT:
					long objectUID2 = input.readLong();
					short methodUID2 = input.readShort();

					receiver.onGetInt(null, objectUID2, methodUID2);
			}
		}
	}

	public synchronized WaitObject<Integer> getInt(long objectUID, short methodUID) throws IOException {
		output.writeShort(Command.GET_INT);
		output.writeLong(objectUID);
		output.writeShort(methodUID);
		output.flush();

		final int result = input.readInt();
		return new WaitObject<Integer>() {
			public Integer get() throws TimeoutException, InterruptedException {
				return result;
			}
		};
	}

	public boolean isClosed() {
		return socket.isClosed();
	}

	public void close() throws IOException {
		socket.close();
	}
}
