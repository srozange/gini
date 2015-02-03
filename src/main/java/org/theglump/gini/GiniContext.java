package org.theglump.gini;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.reflections.ReflectionUtils;
import org.reflections.Reflections;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * Gini is a simple DI Container :
 * 		- beans are singletons
 * 		- injection is done by type then by name.
 * 
 * Managed classes must be annotated with {@link Managed}
 * Field must be annotated with {@link Inject}
 * 
 * @author sebastien.rozange
 *
 */
public class GiniContext {

	private final Map<Class<?>, Set<Object>> managedBeans = Maps.newHashMap();

	/**
	 * Initialize a new context by scanning all classes and sub-classes of the given package
	 * 
	 * Context is builded in 2 steps :
	 * 		- Instanciation of all managed beans (without deps)
	 * 		- Injection of dependencies
	 * 
	 * @param packageName
	 */
	public GiniContext(String packageName) {
		Preconditions.checkNotNull(packageName);
		initializeContext(packageName);
	}

	/**
	 * Returns the managed bean corresponding to the given class
	 * 
	 * If the given class is an interface and 2 or more implementations exist, injection is done by field name
	 * 
	 * @param clazz
	 * 			The class of searched bean
	 * @return
	 * 			The corresponding managed bean
	 */
	public <T> T getBean(Class<T> clazz) {
		Preconditions.checkNotNull(clazz);
		return getFromReferential(clazz, null);
	}
	
	/**
	 * Injects managed bean in provided object (via fields annotated {@link Inject})
	 * 
	 * @param object to perform injection on
	 */
	public void inject(Object object) {
		Preconditions.checkNotNull(object);
		injectDependencies(object);
	}
	
	private void initializeContext(String packageName) {
		Reflections reflections = new Reflections(packageName);
		Set<Object> beans = new HashSet<Object>();
		
		for (Class<?> clazz : reflections.getTypesAnnotatedWith(Managed.class)) {
			beans.add(instanciate(clazz));
		}
		
		for (Object bean : beans) {
			injectDependencies(bean);
		}
	}

	private Object instanciate(Class<?> clazz) {
		Object bean = GiniUtils.instantiate(clazz);
		addToReferential(bean);
		return bean;
	}
	
	private void injectDependencies(Object bean) {
		for (Field field : getCandidateFieldsForInjection(bean.getClass())) {
			GiniUtils.injectField(bean, field, getFromReferential(field.getType(), field.getName()));
		}
	}

	private void addToReferential(Object bean) {
		addToReferential(bean.getClass(), bean);
		Set<Class<?>> superTypes = ReflectionUtils.getAllSuperTypes(bean.getClass());
		for (Class<?> superType : superTypes) {
			addToReferential(superType, bean);
		}
	}
	
	private void addToReferential(Class<?> clazz, Object bean) {
		Set<Object> beans = managedBeans.get(clazz);
		if (beans == null) {
			beans = new HashSet<Object>();
			managedBeans.put(clazz, beans);
		}
		beans.add(bean);
	}
	
	@SuppressWarnings("unchecked")
	private <T> T getFromReferential(Class<T> clazz, String fieldName) {
		Set<Object> beans = managedBeans.get(clazz);
		if (beans == null) {
			throw new GiniException("Could not find an instance for " + clazz.getCanonicalName());
		} else if (beans.size() == 1) {
			return (T)beans.iterator().next();
		} else {
			for (Object bean : beans) {
				if (canInjectByName(fieldName, bean))  {
					return (T)bean;
				}
			}
		}
		throw new GiniException("Several instance for " + clazz.getCanonicalName() + " - could not find the matching one");
	}

	private boolean canInjectByName(String fieldName, Object bean) {
		return GiniUtils.className(bean.getClass()).equalsIgnoreCase(fieldName);
	}

	@SuppressWarnings("unchecked")
	protected Set<Field> getCandidateFieldsForInjection(Class<?> clazz) {
		return ReflectionUtils.getAllFields(clazz, ReflectionUtils.withAnnotation(Inject.class));
	}

}
