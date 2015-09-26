package ru.idmt.commons.mrmi4.commons;

import ru.idmt.commons.mrmi4.commons.RObject;

import java.lang.reflect.Method;

public interface UIDManager {
	short getClassUID(Class<? extends RObject> iClass);

	Class<?> getClassByUID(short classUID);

	short getMethodUID(Method method);

	Method getMethodByUID(short methodUID);
}