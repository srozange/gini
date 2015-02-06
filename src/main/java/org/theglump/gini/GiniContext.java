package org.theglump.gini;

import static org.reflections.ReflectionUtils.getAllFields;
import static org.reflections.ReflectionUtils.withAnnotation;

import java.lang.reflect.Field;

import org.reflections.Reflections;

import com.google.common.base.Preconditions;

/**
 * Gini is a simple DI Container : - beans are singletons - injection is done by
 * type then by name.
 * 
 * Managed classes must be annotated with {@link Managed} Field must be
 * annotated with {@link Inject}
 * 
 * @author sebastien.rozange
 * 
 */
public class GiniContext {

	private final Store store;
	private final String packageName;

	/**
	 * Initialize a new context by scanning all classes and sub-classes of the
	 * given package
	 * 
	 * Context is builded in 2 steps : - Instanciation of all managed beans
	 * (without deps) - Injection of dependencies
	 * 
	 * @param packageName
	 */
	public GiniContext(String packageName) {
		Preconditions.checkNotNull(packageName);

		this.packageName = packageName;
		this.store = new Store();

		instanciateBeans();
		injectDependencies();
	}

	/**
	 * Returns the managed bean corresponding to the given class
	 * 
	 * If the given class is an interface and 2 or more implementations exist,
	 * injection is done by field name
	 * 
	 * @param clazz
	 *            The class of searched bean
	 * @return The corresponding managed bean
	 */
	public <T> T getBean(Class<T> clazz) {
		Preconditions.checkNotNull(clazz);
		return store.getBean(clazz, null);
	}

	/**
	 * Injects managed bean in provided object (via fields annotated
	 * {@link Inject})
	 * 
	 * @param object
	 *            to perform injection on
	 */
	public void inject(Object object) {
		Preconditions.checkNotNull(object);
		injectDependencies(object);
	}

	private void instanciateBeans() {
		Reflections reflections = new Reflections(packageName);
		for (Class<?> clazz : reflections.getTypesAnnotatedWith(Managed.class)) {
			Object bean = GiniUtils.instantiate(clazz);
			store.registerBean(bean);
		}
	}

	private void injectDependencies() {
		for (Object bean : store.getBeans()) {
			injectDependencies(bean);
		}
	}

	@SuppressWarnings("unchecked")
	private void injectDependencies(Object bean) {
		for (Field field : getAllFields(bean.getClass(),
				withAnnotation(Inject.class))) {
			Object dependency = store.getBean(field.getType(), field.getName());
			GiniUtils.injectField(bean, field, dependency);
		}
	}

}
