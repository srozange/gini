package org.theglump.gini;

import static org.reflections.ReflectionUtils.getMethods;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

import com.google.common.base.Predicate;

/**
 * Set of utils method mostly related to reflection
 * 
 * @author sebastien.rozange
 * 
 */
public class Utils {

	private static final Predicate<Method> PUBLIC_METHOD_PREDICATE = new Predicate<Method>() {

		@Override
		public boolean apply(final Method method) {
			return Modifier.isPublic(method.getModifiers());
		}

	};

	protected static void injectField(Object object, Field field, Object toInject) {
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

	protected static Object createProxy(Class<?> clazz, MethodInterceptor methodInterceptor) {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(clazz);
		enhancer.setCallback(methodInterceptor);
		Object proxy = enhancer.create();
		return proxy;
	}

	@SuppressWarnings("unchecked")
	protected static Set<Method> getPublicMethods(Class<?> clazz) {
		return getMethods(clazz, PUBLIC_METHOD_PREDICATE);

	}

	protected static String className(Class<?> clazz) {
		String name = clazz.getName();
		return name.substring(name.lastIndexOf(".") + 1);
	}

	protected static Class<?> getProxifiedClass(Class<?> clazz) {
		return clazz.getCanonicalName().contains("CGLIB") ? clazz.getSuperclass() : clazz;
	}

	protected static String computeMethodPath(Method method) {
		return method.getDeclaringClass().getName() + "." + method.getName();
	}

}
