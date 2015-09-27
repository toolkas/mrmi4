package ru.idmt.commons.mrmi4.example.api;

import com.spellmaster.micrormi.MicroRemote;
import ru.idmt.commons.mrmi4.commons.RList;
import ru.idmt.commons.mrmi4.commons.RObject;

import java.util.List;

public interface IExample extends RObject, MicroRemote {
	int getInt();

	int getInt(String value);

	int getInt(String value, String value2);

	@RList(IValue.class)
	List<IValue> getValues();
}
