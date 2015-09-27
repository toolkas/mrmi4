package ru.idmt.commons.mrmi4.example.server;

import ru.idmt.commons.mrmi4.example.api.IExample;
import ru.idmt.commons.mrmi4.example.api.IValue;

import java.util.ArrayList;
import java.util.List;

public class ExampleImpl implements IExample {
	private final List<IValue> values = new ArrayList<IValue>();

	public ExampleImpl() {
		for(int i = 0; i< 10; i++) {
			values.add(new IValue() {
				public int get() {
					return 10;
				}
			});
		}
	}

	public int getInt() {
		return 1;
	}

	public int getInt(String value) {
		return 2;
	}

	public int getInt(String value, String value2) {
		return 3;
	}

	public List<IValue> getValues() {
		return values;
	}
}
