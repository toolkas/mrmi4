package ru.idmt.commons.mrmi4.example.api;

import com.spellmaster.micrormi.MicroRemote;
import ru.idmt.commons.mrmi4.commons.RObject;

public interface IValue extends RObject, MicroRemote {
	int get();
}
