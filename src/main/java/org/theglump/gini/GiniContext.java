package org.theglump.gini;

import static org.reflections.ReflectionUtils.getAllFields;
import static org.reflections.ReflectionUtils.withAnnotation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.theglump.gini.annotation.Around;
import org.theglump.gini.annotation.Inject;
import org.theglump.gini.annotation.Managed;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;

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
	private final Reflections reflections;

	private final Map<Class<?>, SetMultimap<Method, Advice>> advisedMethods = Maps
			.newHashMap();

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
		this.store = new Store();

		instanciateBeans();
		collectAdvices();
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
		for (Class<?> clazz : reflections.getTypesAnnotatedWith(Managed.class)) {
			Object bean = Utils.instantiate(clazz);
			store.registerBean(bean);
		}
	}

	@SuppressWarnings("unchecked")
	private void collectAdvices() {
		for (Class<?> clazz : reflections
				.getTypesAnnotatedWith(org.theglump.gini.annotation.Advice.class)) {
			for (Method method : ReflectionUtils.getMethods(clazz,
					withAnnotation(Around.class))) {
				Around around = method.getAnnotation(Around.class);
				matchMethods(around.target(),
						new Advice(Utils.instantiate(clazz), method));
			}
		}

		for (Class<?> clazz : advisedMethods.keySet()) {
			Enhancer enhancer = new Enhancer();
			enhancer.setSuperclass(clazz);
			enhancer.setCallback(methodInterceptor);
			Object proxy = enhancer.create();
			store.replaceBean(proxy);
		}
	}

	@SuppressWarnings("unchecked")
	private void matchMethods(String target, Advice advice) {
		Set<Object> beans = store.getBeans();
		for (Object bean : beans) {
			Set<Method> methods = ReflectionUtils.getMethods(bean.getClass(),
					new Predicate<Method>() {

						@Override
						public boolean apply(final Method method) {
							return Modifier.isPublic(method.getModifiers());
						}

					});

			for (Method method : methods) {
				String id = bean.getClass().getPackage().getName() + "."
						+ Utils.className(bean.getClass()) + "."
						+ method.getName();

				if (id.matches(target)) {
					SetMultimap<Method, Advice> methodToAdvices = advisedMethods
							.get(bean);
					if (methodToAdvices == null) {
						methodToAdvices = HashMultimap.create();
						advisedMethods.put(bean.getClass(), methodToAdvices);
					}
					methodToAdvices.put(method, advice);
				}
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
		for (Field field : getAllFields(Utils.getRealClass(bean.getClass()),
				withAnnotation(Inject.class))) {
			Object dependency = store.getBean(field.getType(), field.getName());
			Utils.injectField(bean, field, dependency);
		}
	}

	private MethodInterceptor methodInterceptor = new net.sf.cglib.proxy.MethodInterceptor() {

		@Override
		public Object intercept(Object bean, Method method, Object[] args,
				MethodProxy proxy) throws Throwable {

			SetMultimap<Method, Advice> advices = GiniContext.this.advisedMethods
					.get(bean.getClass().getSuperclass());
			if (advices.containsKey(method)) {
				Set<Advice> set = advices.get(method);
				Advice advice = set.iterator().next();
				return advice.method.invoke(advice.advice, new Object[] { bean,
						method, args, proxy });
			}
			return proxy.invokeSuper(bean, args);
		}

	};

	private class Advice {

		Object advice;
		Method method;

		Advice(Object advice, Method method) {
			this.advice = advice;
			this.method = method;
		}

	}

}
