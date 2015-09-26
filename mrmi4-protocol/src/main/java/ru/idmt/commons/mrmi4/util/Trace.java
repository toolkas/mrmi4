package ru.idmt.commons.mrmi4.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

public class Trace {
	private Trace() {
	}

	@SuppressWarnings("unchecked")
	public static <T> T get(final String category, final T object) {
		return (T) Proxy.newProxyInstance(Trace.class.getClassLoader(), object.getClass().getInterfaces(), new InvocationHandler() {
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				System.out.println("START[" + category + "." + method.getName() + "(" + (args != null ? Arrays.toString(args) : "") + ")");
				long start = System.currentTimeMillis();
				Object ret = method.invoke(object, args);
				long finish = System.currentTimeMillis();
				System.out.println("FINISH[" + category + "." + method.getName() + "(" + (args != null ? Arrays.toString(args) : "") + ") = " + ret + " (" + (finish - start) + "ms)");

				return ret;
			}
		});
	}
}