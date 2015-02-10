package org.theglump.gini;

import static org.reflections.ReflectionUtils.getAllFields;
import static org.reflections.ReflectionUtils.getMethods;
import static org.reflections.ReflectionUtils.withAnnotation;
import static org.theglump.gini.Utils.createProxy;
import static org.theglump.gini.Utils.getProxifiedClass;
import static org.theglump.gini.Utils.getPublicMethods;
import static org.theglump.gini.Utils.injectField;
import static org.theglump.gini.Utils.instantiate;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import javax.annotation.Nonnull;

import org.reflections.Reflections;
import org.theglump.gini.annotation.Advice;
import org.theglump.gini.annotation.Around;
import org.theglump.gini.annotation.Inject;
import org.theglump.gini.annotation.Managed;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

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

		instanciateInterceptors();
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

	@SuppressWarnings("unchecked")
	private void instanciateInterceptors() {
		Set<Method> candidateMethodsForInterception = getCandidateMethodsForInterception();
		for (Class<?> clazz : reflections.getTypesAnnotatedWith(Advice.class)) {
			Object advice = instantiate(clazz);
			for (Method method : getMethods(clazz, withAnnotation(Around.class))) {
				final Around around = method.getAnnotation(Around.class);
				Set<Method> methodsToBeIntercepted = getMethodsToBeIntercepted(candidateMethodsForInterception, around.joinpoint());
				if (methodsToBeIntercepted.size() > 0) {
					Interceptor interceptor = new Interceptor(advice, method, around.joinpoint());
					store.registerInterceptor(interceptor, methodsToBeIntercepted);
				}
			}
		}
	}

	private Set<Method> getCandidateMethodsForInterception() {
		Set<Method> methods = Sets.newHashSet();
		for (Class<?> clazz : reflections.getTypesAnnotatedWith(Managed.class)) {
			methods.addAll(getPublicMethods(clazz));
		}
		return methods;
	}

	private Set<Method> getMethodsToBeIntercepted(Set<Method> candidateMethodsForInterception, final String joinpoint) {
		return Sets.filter(candidateMethodsForInterception, new Predicate<Method>() {

			@Override
			public boolean apply(Method method) {
				return Utils.computeMethodPath(method).matches(joinpoint);
			}
		});
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
