package org.theglump.gini;

import static org.reflections.ReflectionUtils.getAllFields;
import static org.reflections.ReflectionUtils.getAllSuperTypes;
import static org.reflections.ReflectionUtils.withAnnotation;
import static org.theglump.gini.GiniUtils.injectField;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

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

	private final Map<Class<?>, Set<Object>> typeToBeans = Maps.newHashMap();
	private final Set<Object> beans = Sets.newHashSet();
	
	private final String packageName;
	
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
		this.packageName = packageName;
		
		instanciateBeans();
		injectDependencies();
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
		return getBean(clazz, null);
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
	
	private void instanciateBeans() {
		Reflections reflections = new Reflections(packageName);
		for (Class<?> clazz : reflections.getTypesAnnotatedWith(Managed.class)) {
			registerBean(GiniUtils.instantiate(clazz));
		}
	}
	
	private void injectDependencies() {
		for (Object bean : beans) {
			injectDependencies(bean);
		}
	} 
	
	@SuppressWarnings("unchecked")
	private void injectDependencies(Object bean) {
		for (Field field : getAllFields(bean.getClass(), withAnnotation(Inject.class))) {
			injectField(bean, field, getBean(field.getType(), field.getName()));
		}
	}

	private void registerBean(Object bean) {
		beans.add(bean);
		registerBean(bean.getClass(), bean);
		for (Class<?> superType : getAllSuperTypes(bean.getClass())) {
			registerBean(superType, bean);
		}
	}
	
	private void registerBean(Class<?> clazz, Object bean) {
		Set<Object> beans = typeToBeans.get(clazz);
		if (beans == null) {
			beans = new HashSet<Object>();
			typeToBeans.put(clazz, beans);
		}
		beans.add(bean);
	}

	@SuppressWarnings("unchecked")
	private <T> T getBean(Class<T> clazz, String fieldName) {
		Set<Object> beans = typeToBeans.get(clazz);
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

}
