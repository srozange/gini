package org.theglump.gini;

import static org.fest.assertions.Assertions.assertThat;
import static org.theglump.gini.Utils.getPublicMethods;
import static org.theglump.gini.Utils.instantiate;

import java.lang.reflect.Method;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.theglump.gini.bean.Advice1;
import org.theglump.gini.bean.IStep;
import org.theglump.gini.bean.StepImpl1;
import org.theglump.gini.bean.StepImpl2;

import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

public class BeanStoreTest {

	private BeanStore store;
	private Method interceptorMethod;
	private Interceptor interceptor;

	private static final StepImpl1 IMPL1 = new StepImpl1();
	private static final StepImpl2 IMPL2 = new StepImpl2();
	private static final Class<?> IMPL1_CLASS = StepImpl1.class;
	private static final Class<?> INTERFACE = IStep.class;

	@Before
	public void setup() {
		store = new BeanStore();
		interceptorMethod = getPublicMethods(StepImpl1.class).iterator().next();
		interceptor = new Interceptor(instantiate(Advice1.class), interceptorMethod);
	}

	@Test
	public void should_find_bean_when_requested_by_concrete_class() {
		store.registerBean(IMPL1);

		assertThat(store.getBean(IMPL1_CLASS)).isEqualTo(IMPL1);
	}

	@Test
	public void should_find_bean_when_requested_by_interface() {
		store.registerBean(IMPL1);

		assertThat(store.getBean(INTERFACE)).isEqualTo(IMPL1);
	}

	@Test
	public void should_find_bean_when_several_implem_and_requested_by_interface() {
		store.registerBean(IMPL1);
		store.registerBean(IMPL2);

		assertThat(store.getBean(INTERFACE, "stepImpl1")).isEqualTo(IMPL1);
	}

	@Test(expected = GiniException.class)
	public void should_throw_exception_when_several_implem_and_requested_by_interface() {
		store.registerBean(IMPL1);
		store.registerBean(IMPL2);

		store.getBean(INTERFACE);
	}

	@Test
	public void should_find_interceptors_for_submitted_method() {
		store.registerInterceptor(interceptor, Sets.newHashSet(interceptorMethod));

		Set<Interceptor> fetchedInterceptors = store.getInterceptorsForMethod(interceptorMethod);

		assertThat(fetchedInterceptors).isNotEmpty().hasSize(1);
		assertThat(fetchedInterceptors.iterator().next()).isEqualTo(interceptor);
	}

	@Test
	public void should_return_incerceptor_per_method_map_for_submitted_class() {
		store.registerInterceptor(interceptor, Sets.newHashSet(interceptorMethod));

		SetMultimap<Method, Interceptor> interceptorsForMethodMap = store.getInterceptorsForMethodMap(StepImpl1.class);

		assertThat(interceptorsForMethodMap).isNotNull();
		assertThat(interceptorsForMethodMap.get(interceptorMethod)).isNotEmpty().hasSize(1);
		assertThat(interceptorsForMethodMap.get(interceptorMethod).iterator().next()).isEqualTo(interceptor);
	}

}
