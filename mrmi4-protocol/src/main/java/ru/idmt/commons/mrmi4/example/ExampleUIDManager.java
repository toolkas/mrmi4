package ru.idmt.commons.mrmi4.example;

import com.spellmaster.micrormi.MicroRemote;
import ru.idmt.commons.mrmi4.api.uid.UIDManager;
import ru.idmt.commons.mrmi4.commons.RObject;

import java.lang.reflect.Method;

public class ExampleUIDManager implements UIDManager {
	public short getClassUID(Class<? extends RObject> iClass) {
		if (iClass == IExample.class) {
			return 1;
		}

		if (iClass.equals(MicroRemote.class)) {
			return 2;
		}
		throw new IllegalArgumentException();
	}

	public Class<?> getClassByUID(short classUID) {
		if (classUID == 1) {
			return IExample.class;
		}

		if (classUID == 2) {
			return MicroRemote.class;
		}
		throw new IllegalArgumentException();
	}

	public short getMethodUID(Method method) {
		if (method.getName().equals("getValue")) {
			return 1;
		}

		if (method.getName().equals("getList")) {
			return 2;
		}

		throw new IllegalArgumentException();
	}

	public Method getMethodByUID(short methodUID) {
		if (methodUID == 1) {
			try {
				return IExample.class.getMethod("getValue");
			} catch (NoSuchMethodException ex) {
				throw new RuntimeException(ex);
			}
		}

		if (methodUID == 2) {
			try {
				return IExample.class.getMethod("getList");
			} catch (NoSuchMethodException ex) {
				throw new RuntimeException(ex);
			}
		}
		throw new IllegalArgumentException();
	}
}
