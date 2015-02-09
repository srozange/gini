package org.theglump.gini;

import static org.fest.assertions.Assertions.assertThat;
import static org.theglump.gini.Utils.getPublicMethods;
import static org.theglump.gini.Utils.instantiate;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.theglump.gini.bean.Advice1;
import org.theglump.gini.bean.IStep;
import org.theglump.gini.bean.StepImpl1;
import org.theglump.gini.bean.StepImpl2;

import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

public class BeanStoreTest {

	private static final StepImpl1 STEP_IMPL_1 = new StepImpl1();
	private static final StepImpl2 STEP_IMPL_2 = new StepImpl2();

	private BeanStore store = new BeanStore();

	@Test
	public void should_find_bean_when_requested_with_concrete_class() {
		store.registerBean(STEP_IMPL_1);

		assertThat(store.getBean(StepImpl1.class, null)).isEqualTo(STEP_IMPL_1);
	}

	@Test
	public void should_find_bean_when_requested_with_interface() {
		store.registerBean(STEP_IMPL_1);

		assertThat(store.getBean(IStep.class, null)).isEqualTo(STEP_IMPL_1);
		assertThat(store.getBean(IStep.class, "stepImpl1")).isEqualTo(STEP_IMPL_1);
	}

	@Test(expected = GiniException.class)
	public void should_throw_exception_when_several_implementation_of_requested_bean() {
		store.registerBean(STEP_IMPL_1);
		store.registerBean(STEP_IMPL_2);

		assertThat(store.getBean(IStep.class, null)).isEqualTo(STEP_IMPL_2);
	}

	@Test
	public void should_find_interceptor_when_submitted_path_matches() {
		Interceptor interceptor = new Interceptor(null, null, ".*Class1.method1");

		store.registerInterceptor(interceptor);

		Set<Interceptor> interceptors = store.findInterceptor("org.theglump.gini.Class1.method1");
		assertThat(interceptors).isNotNull().hasSize(1);
		assertThat(interceptors.iterator().next()).isEqualTo(interceptor);
	}

	@Test
	public void should_find_interceptors_for_submitted_method() {
		Method m = getPublicMethods(Advice1.class).iterator().next();
		Interceptor interceptor = new Interceptor(instantiate(Advice1.class), m, ".*Class1.method1");
		HashSet<Interceptor> interceptors = Sets.newHashSet(interceptor);

		store.addInterceptorsForMethod(m, interceptors);

		Set<Interceptor> interceptors_new = store.getInterceptorsForMethod(m);
		assertThat(interceptors_new).isEqualTo(interceptors);
	}

	@Test
	public void should_return_incerceptor_per_method_map_for_submitted_class() {
		Method m = getPublicMethods(Advice1.class).iterator().next();
		Interceptor interceptor = new Interceptor(instantiate(Advice1.class), m, ".*Class1.method1");
		HashSet<Interceptor> interceptors = Sets.newHashSet(interceptor);

		store.addInterceptorsForMethod(m, interceptors);

		SetMultimap<Method, Interceptor> interceptors_new = store.getInterceptorsForMethodMap(Advice1.class);
		assertThat(interceptors_new).isNotNull();
		assertThat(interceptors_new.get(m)).isEqualTo(interceptors);
	}

}
