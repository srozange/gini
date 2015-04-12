package org.theglump.gini;

import static org.reflections.ReflectionUtils.getAllFields;
import static org.reflections.ReflectionUtils.withAnnotation;
import static org.theglump.gini.Reflections.createProxy;
import static org.theglump.gini.Reflections.getProxifiedClass;
import static org.theglump.gini.Reflections.injectField;
import static org.theglump.gini.Reflections.instantiate;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.collect.SetMultimap;
import org.reflections.Reflections;
import org.theglump.gini.annotation.Advice;
import org.theglump.gini.annotation.Around;
import org.theglump.gini.annotation.Inject;
import org.theglump.gini.annotation.Managed;

import com.google.common.base.Preconditions;

/**
 * Gini is a simple DI Container and AOP engine : beans are singletons and
 * injection is done by type then by name.
 * 
 * Managed classes must be annotated with {@link Managed} and candidate fields
 * for injection must be annotated with {@link Inject}.
 * 
 * AOP is done by defining advices annotated with @link {@link Advice}.
 * 
 * An Advice contains methods annotated with {@link Around}, they are called
 * during interception of target methods.
 * 
 * @author sebastien.rozange
 * 
 */
public class Gini {

	private final BeanStore store;
	private final Reflections reflections;
	private final InterceptorHelper interceptorHelper;

	/**
	 * Initialize a new context by scanning all classes and sub-classes of the
	 * given package
	 *
	 * @param packageName
	 */
	public Gini(String packageName) {
		Preconditions.checkNotNull(packageName);

		this.store = new BeanStore();
		this.reflections = new Reflections(packageName);
		this.interceptorHelper = new InterceptorHelper(packageName);

		registerInterceptors();
		registerBeans();
		injectDependencies();
	}

	/**
	 * Returns the managed bean corresponding to the given class
	 *
	 * If given class is an interface and 2 or more implementations exist,
	 * injection is done matching field name and class name
	 *
	 * @param clazz
	 *            The class of searched bean
	 * @return The corresponding managed bean
	 */
	@Nonnull
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

	private void registerInterceptors() {
		Set<Interceptor> interceptors = interceptorHelper.computeInterceptors();
		store.registerInterceptors(interceptors);
	}

	private void registerBeans() {
		for (Class<?> clazz : reflections.getTypesAnnotatedWith(Managed.class)) {
			Object bean;
			if (store.hasInterceptors(clazz)) {
				bean = createProxy(clazz);
			} else {
				bean = instantiate(clazz);
			}
			store.registerBean(bean);
		}
	}

	private Object createProxy(Class<?> clazz) {
		SetMultimap<Method, Interceptor> interceptorsPerMethod = store.getInterceptorsPerMethod(clazz);
		MethodInterceptor methodInterceptor = new MethodInterceptor(interceptorsPerMethod);
		return org.theglump.gini.Reflections.createProxy(clazz, methodInterceptor);
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
