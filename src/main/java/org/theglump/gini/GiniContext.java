package org.theglump.gini;

import static org.reflections.ReflectionUtils.getAllFields;
import static org.reflections.ReflectionUtils.getMethods;
import static org.reflections.ReflectionUtils.withAnnotation;
import static org.theglump.gini.Utils.computeMethodPathes;
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
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * Gini is a simple DI Container and AOP engine : beans are singletons and
 * injection is done by type then by name.
 * 
 * Managed classes must be annotated with {@link Managed} and fields candidate
 * for injection must be annotated with {@link Inject}.
 * 
 * AOP is done by defining advices that must be annotated with @link
 * {@link Advice}.
 * 
 * An Advice contains methods annotated with {@link Around}, they are called
 * when target methods are intercepted. {@link Around}
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

	private void instanciateInterceptors() {
		Set<Method> publicManagedMethods = getManagedPublicMethods();
		for (Class<?> clazz : getAdviceClasses()) {
			Object adviceInstance = instantiate(clazz);
			for (Method aroundMethod : getAroundMethods(clazz)) {
				Set<Method> matchingMethods = getMatchingMethods(getJointpoint(aroundMethod), publicManagedMethods);
				if (matchingMethods.size() > 0) {
					store.registerInterceptor(new Interceptor(adviceInstance, aroundMethod), matchingMethods);
				}
			}
		}
	}

	private Set<Method> getMatchingMethods(final String joinpoint, Set<Method> candidateMethodsForInterception) {
		return Sets.filter(candidateMethodsForInterception, new Predicate<Method>() {

			@Override
			public boolean apply(Method method) {
				return Iterables.any(computeMethodPathes(method), new Predicate<String>() {

					@Override
					public boolean apply(String methodPath) {
						return methodPath.matches(joinpoint);
					}
				});
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

	private Set<Class<?>> getAdviceClasses() {
		return reflections.getTypesAnnotatedWith(Advice.class);
	}

	@SuppressWarnings("unchecked")
	private Set<Method> getAroundMethods(Class<?> clazz) {
		return getMethods(clazz, withAnnotation(Around.class));
	}

	private String getJointpoint(Method aroundMethod) {
		return aroundMethod.getAnnotation(Around.class).joinpoint();
	}

	private Set<Method> getManagedPublicMethods() {
		Set<Method> methods = Sets.newHashSet();
		for (Class<?> clazz : reflections.getTypesAnnotatedWith(Managed.class)) {
			methods.addAll(getPublicMethods(clazz));
		}
		return methods;
	}

}
