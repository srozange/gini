package org.theglump.gini;

import static org.reflections.ReflectionUtils.getAllFields;
import static org.reflections.ReflectionUtils.getMethods;
import static org.reflections.ReflectionUtils.withAnnotation;
import static org.theglump.gini.Utils.computeMethodPath;
import static org.theglump.gini.Utils.createProxy;
import static org.theglump.gini.Utils.getProxifiedClass;
import static org.theglump.gini.Utils.getPublicMethods;
import static org.theglump.gini.Utils.injectField;
import static org.theglump.gini.Utils.instantiate;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import org.reflections.Reflections;
import org.theglump.gini.annotation.Advice;
import org.theglump.gini.annotation.Around;
import org.theglump.gini.annotation.Inject;
import org.theglump.gini.annotation.Managed;

import com.google.common.base.Preconditions;

/**
 * Gini is a simple DI Container and AOP engine : - beans are singletons -
 * injection is done by type then by name.
 * 
 * Managed classes must be annotated with {@link Managed} Field must be
 * annotated with {@link Inject}
 * 
 * Advice must be annotated with @link {@link Advice} - Intercepting method must
 * then declare the @link {@link Around}
 * 
 * @author sebastien.rozange
 * 
 */
public class GiniContext {

	private final BeanStore store;
	private final Reflections reflections;

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
		this.reflections = new Reflections(packageName);
		this.store = new BeanStore();

		computeAdvices();
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

	private void computeAdvices() {
		instanciateInterceptors();
		findInterceptedMethods();
	}

	@SuppressWarnings("unchecked")
	private void instanciateInterceptors() {
		for (Class<?> clazz : reflections.getTypesAnnotatedWith(Advice.class)) {
			for (Method method : getMethods(clazz, withAnnotation(Around.class))) {
				Around around = method.getAnnotation(Around.class);
				Interceptor interceptor = new Interceptor(instantiate(clazz), method, around.joinpoint());
				store.registerInterceptor(interceptor);
			}
		}
	}

	private void findInterceptedMethods() {
		for (Class<?> clazz : reflections.getTypesAnnotatedWith(Managed.class)) {
			for (Method method : getPublicMethods(clazz)) {
				Set<Interceptor> interceptors = store.findInterceptor(computeMethodPath(method));
				if (interceptors.size() > 0) {
					store.addInterceptorsForMethod(method, interceptors);
				}
			}
		}
	}

	private void instanciateBeans() {
		for (Class<?> clazz : reflections.getTypesAnnotatedWith(Managed.class)) {
			if (store.hasInterceptors(clazz)) {
				MethodInterceptor methodInterceptor = new MethodInterceptor(store.getInterceptorsForMethodMap(clazz));
				store.registerBean(createProxy(clazz, methodInterceptor));
			} else {
				store.registerBean(instantiate(clazz));
			}
		}
	}

	private void injectDependencies() {
		for (Object bean : store.getBeans()) {
			injectDependencies(bean);
		}
	}

	@SuppressWarnings("unchecked")
	private void injectDependencies(Object bean) {
		for (Field field : getAllFields(getProxifiedClass(bean.getClass()), withAnnotation(Inject.class))) {
			Object dependency = store.getBean(field.getType(), field.getName());
			injectField(bean, field, dependency);
		}
	}

}
