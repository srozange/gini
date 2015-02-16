package org.theglump.gini;

import java.lang.reflect.Method;

class Interceptor {
	Object advice;
	Method method;

	Interceptor(Object advice, Method method) {
		this.advice = advice;
		this.method = method;
	}

}
