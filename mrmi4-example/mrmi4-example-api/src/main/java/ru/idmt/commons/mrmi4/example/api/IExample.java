package ru.idmt.commons.mrmi4.example.api;

import ru.idmt.commons.mrmi4.commons.RObject;

public interface IExample extends RObject {
	int getInt();

	int getInt(String value);

	int getInt(String value, String value2);
}
