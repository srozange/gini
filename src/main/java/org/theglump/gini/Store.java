package org.theglump.gini;

import static org.reflections.ReflectionUtils.getAllSuperTypes;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Sets;

/**
 * Store managed beans and offer convenience methods to query them
 * 
 * @author sebastien.rozange
 * 
 */
class Store {

	private final HashMultimap<Class<?>, Object> typeToBeans = HashMultimap
			.create();
	private final Set<Object> beans = Sets.newHashSet();

	protected void registerBean(Object bean) {
		beans.add(bean);
		typeToBeans.put(bean.getClass(), bean);
		for (Class<?> superType : getAllSuperTypes(bean.getClass())) {
			typeToBeans.put(superType, bean);
		}
	}

	@SuppressWarnings("unchecked")
	protected <T> T getBean(Class<T> clazz, String fieldName) {
		Set<Object> beans = typeToBeans.get(clazz);
		if (beans == null) {
			throw new GiniException("Could not find an instance for "
					+ clazz.getCanonicalName());
		} else if (beans.size() == 1) {
			return (T) beans.iterator().next();
		} else {
			for (Object bean : beans) {
				if (canInjectByName(fieldName, bean)) {
					return (T) bean;
				}
			}
		}
		throw new GiniException("Several instance for "
				+ clazz.getCanonicalName()
				+ " - could not find the matching one");
	}

	protected Set<Object> getBeans() {
		return Collections.unmodifiableSet(beans);
	}

	private boolean canInjectByName(String fieldName, Object bean) {
		return GiniUtils.className(bean.getClass()).equalsIgnoreCase(fieldName);
	}

}
