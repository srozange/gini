package org.theglump.gini;

import static org.reflections.ReflectionUtils.getAllSuperTypes;
import static org.theglump.gini.Utils.getProxifiedClass;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

/**
 * Store managed beans and offer convenience methods to query them
 * 
 * @author sebastien.rozange
 * 
 */
class BeanStore {

	private final HashMultimap<Class<?>, Object> typeToBeans = HashMultimap.create();
	private final Set<Object> beans = Sets.newHashSet();
	private final Map<Class<?>, SetMultimap<Method, Interceptor>> interceptedMethods = Maps.newHashMap();
	private final Set<Interceptor> interceptors = Sets.newHashSet();

	private static final Set<Interceptor> EMPTY_INTERCEPTOR_SET = Collections.unmodifiableSet(new HashSet<Interceptor>());
	private static final SetMultimap<Method, Interceptor> EMPTY_INTERCEPTOR_FOR_METHODS_MAP = ImmutableSetMultimap.of();

	protected void registerBean(Object bean) {
		Class<?> clazz = getProxifiedClass(bean.getClass());
		beans.add(bean);
		typeToBeans.put(clazz, bean);
		for (Class<?> superType : getAllSuperTypes(clazz)) {
			typeToBeans.put(superType, bean);
		}
	}

	@SuppressWarnings("unchecked")
	protected <T> T getBean(Class<T> clazz, String concreteClassName) {
		Set<Object> beans = typeToBeans.get(clazz);
		if (beans == null) {
			throw new GiniException("Could not find an instance for " + clazz.getCanonicalName());
		} else if (beans.size() == 1) {
			return (T) beans.iterator().next();
		} else {
			for (Object bean : beans) {
				if (canInjectByName(concreteClassName, bean)) {
					return (T) bean;
				}
			}
		}
		throw new GiniException("Several instance for " + clazz.getCanonicalName() + " - could not find the matching one");
	}

	protected Set<Object> getBeans() {
		return Collections.unmodifiableSet(beans);
	}

	protected void registerInterceptor(Interceptor interceptor, Set<Method> methods) {
		interceptors.add(interceptor);
		for (Method m : methods) {
			Class<?> proxifiedClass = getProxifiedClass(m.getDeclaringClass());
			SetMultimap<Method, Interceptor> _interceptors = interceptedMethods.get(proxifiedClass);
			if (_interceptors == null) {
				_interceptors = HashMultimap.create();
				interceptedMethods.put(proxifiedClass, _interceptors);
			}
			_interceptors.put(m, interceptor);

		}
	}

	protected Set<Interceptor> getInterceptorsForMethod(Method method) {
		Class<?> proxifiedClass = getProxifiedClass(method.getDeclaringClass());
		SetMultimap<Method, Interceptor> interceptorForMethod = interceptedMethods.get(proxifiedClass);
		if (interceptorForMethod != null) {
			return Collections.unmodifiableSet(interceptorForMethod.get(method));
		}
		return EMPTY_INTERCEPTOR_SET;
	}

	protected SetMultimap<Method, Interceptor> getInterceptorsForMethodMap(Class<?> clazz) {
		Class<?> proxifiedClass = getProxifiedClass(clazz);
		if (interceptedMethods.containsKey(proxifiedClass)) {
			return ImmutableSetMultimap.copyOf(interceptedMethods.get(proxifiedClass));
		}
		return EMPTY_INTERCEPTOR_FOR_METHODS_MAP;
	}

	protected boolean hasInterceptors(Class<?> clazz) {
		return interceptedMethods.containsKey(clazz);
	}

	private boolean canInjectByName(String fieldName, Object bean) {
		return Utils.className(getProxifiedClass(bean.getClass())).equalsIgnoreCase(fieldName);
	}

}
