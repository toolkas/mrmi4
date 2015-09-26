package ru.idmt.commons.mrmi4.example.server;

import ru.idmt.commons.mrmi4.example.api.IExample;

public class ExampleImpl implements IExample {
	public int getInt() {
		return 1;
	}

	public int getInt(String value) {
		return 2;
	}

	public int getInt(String value, String value2) {
		return 3;
	}
}
