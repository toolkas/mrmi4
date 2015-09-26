package ru.idmt.commons.mrmi4.example;

import com.spellmaster.micrormi.MicroRemote;
import ru.idmt.commons.mrmi4.commons.RObject;

import java.util.List;

public interface IExample extends RObject, MicroRemote {
	String getValue();

	List<String> getList();
}
