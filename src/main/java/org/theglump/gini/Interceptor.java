package org.theglump.gini;

import java.lang.reflect.Method;

public class Interceptor {
	public Object advice;
	public Method method;

	public Interceptor(Object advice, Method method) {
		this.advice = advice;
		this.method = method;
	}

}
