package org.theglump.gini.bean;

import java.lang.reflect.Method;

import org.theglump.gini.MethodInvoker;
import org.theglump.gini.annotation.Advice;
import org.theglump.gini.annotation.Around;

@Advice
public class Advice1 {

	@Around(joinpoint = ".*Step.*method1")
	public String intercept(Object bean, Method method, Object[] args, MethodInvoker methodInvoker) {
		return "interceptor => " + methodInvoker.invokeMethod(args);
	}

}
