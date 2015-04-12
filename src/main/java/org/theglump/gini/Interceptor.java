package org.theglump.gini;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

class Interceptor {
	private Object advice;
	private Method method;
	private Set<Method> interceptedMethods;

	Interceptor(Object advice, Method method, Set<Method> interceptedMethods) {
		this.advice = advice;
		this.method = method;
		this.interceptedMethods = interceptedMethods;
	}

	public Object getAdvice() {
		return advice;
	}

	public Method getMethod() {
		return method;
	}

	public Set<Method> getInterceptedMethods() {
		return interceptedMethods;
	}
}
