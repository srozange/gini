package org.theglump.gini;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Set;

import org.junit.Test;
import org.theglump.gini.bean.Advice1;

public class InterceptorHelperTest {

	@Test
	public void should_compute_interceptors() {
		// Setup
		InterceptorHelper interceptorHelper = new InterceptorHelper("org.theglump.gini.bean");

		// Test
		Set<Interceptor> interceptors = interceptorHelper.computeInterceptors();

		// Assert
		assertThat(interceptors).isNotNull().hasSize(2);

		Interceptor i1 = interceptorByMethod(interceptors, "intercept1");
		assertThat(i1).isNotNull();
		assertThat(i1.getAdvice()).isInstanceOf(Advice1.class);
		assertThat(i1.getInterceptedMethods()).hasSize(2);

		Interceptor i2 = interceptorByMethod(interceptors, "intercept2");
		assertThat(i2).isNotNull();
		assertThat(i2.getAdvice()).isInstanceOf(Advice1.class);
		assertThat(i2.getInterceptedMethods()).hasSize(1);
	}

	private Interceptor interceptorByMethod(Set<Interceptor> interceptors, String methodName) {
		for (Interceptor interceptor : interceptors) {
			if (methodName.equals(interceptor.getMethod().getName())) {
				return interceptor;
			}
		}
		return null;
	}
}
