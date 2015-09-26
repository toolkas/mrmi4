package ru.idmt.commons.mrmi4.util;

import ru.idmt.commons.mrmi4.commons.RObject;
import ru.idmt.commons.mrmi4.commons.UIDManager;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class ReflectionUtils {
	public ReflectionUtils() {
	}

	public static boolean isToString(Method method) {
		return "toString".equals(method.getName()) && String.class.equals(method.getReturnType());
	}

	public static short[] getRemoteInterfaces(UIDManager uidManager, Class<?> clazz) {
		Set<Short> interfaces = new HashSet<Short>();
		Class<?> c = clazz;
		while (c != null) {
			for (Class<?> cc : c.getInterfaces()) {
				if (RObject.class.isAssignableFrom(cc)) {
					short classUID = uidManager.getClassUID((Class<? extends RObject>) cc);
					interfaces.add(classUID);
				}
			}
			c = c.getSuperclass();
		}

		short[] values = new short[interfaces.size()];
		int index = 0;
		for (short val : interfaces) {
			values[index] = val;
		}
		return values;
	}
}
