package ru.idmt.commons.mrmi4.api.om;

import ru.idmt.commons.mrmi4.commons.RObject;

public interface ObjectManager {
	long add(RObject object);

	RObject get(long objectUID);

	long getUID(Class<?> iClass);
}
