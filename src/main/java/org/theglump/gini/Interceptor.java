package org.theglump.gini;

import java.lang.reflect.Method;

public class Interceptor {
	public String jointpoint;
	public Object advice;
	public Method method;

	public Interceptor(Object advice, Method method, String jointpoint) {
		this.jointpoint = jointpoint;
		this.advice = advice;
		this.method = method;
	}

}
