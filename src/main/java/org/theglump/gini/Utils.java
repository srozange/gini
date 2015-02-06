package org.theglump.gini;

import java.lang.reflect.Field;

public class Utils {

	protected static void injectField(Object object, Field field,
			Object toInject) {
		try {
			field.setAccessible(true);
			field.set(object, toInject);
		} catch (IllegalArgumentException e) {
			throw new GiniException(e);
		} catch (IllegalAccessException e) {
			throw new GiniException(e);
		}
	}

	protected static Object instantiate(Class<?> clazz) {
		try {
			return clazz.newInstance();
		} catch (InstantiationException e) {
			throw new GiniException(e);
		} catch (IllegalAccessException e) {
			throw new GiniException(e);
		}
	}

	protected static String className(Class<?> clazz) {
		String name = clazz.getName();
		return name.substring(name.lastIndexOf(".") + 1);
	}

	protected static Class<?> getRealClass(Class<?> clazz) {
		return clazz.getCanonicalName().contains("CGLIB") ? clazz
				.getSuperclass() : clazz;
	}

}
