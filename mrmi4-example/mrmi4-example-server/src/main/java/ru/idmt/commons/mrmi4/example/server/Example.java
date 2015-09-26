package ru.idmt.commons.mrmi4.example.server;

import java.io.*;
import java.util.Arrays;

public class Example {
	public static void main(String[] args) throws IOException {
		short value = 10;

		byte[] bytes = new byte[2];
		bytes[0] = (byte) ((value >>> 8) & 0xFF);
		bytes[1] = (byte) ((value >>> 0) & 0xFF);

		System.out.println(Arrays.toString(bytes));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutput output = new DataOutputStream(baos);
		output.writeShort(value);
		System.out.println(Arrays.toString(baos.toByteArray()));
	}
}
