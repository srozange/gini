package org.theglump.gini.bean;

import java.lang.reflect.Method;

import org.theglump.gini.MethodInvoker;
import org.theglump.gini.annotation.Advice;
import org.theglump.gini.annotation.Around;

@Advice
public class Advice1 {

	@Around(joinpoint = ".*Step.*implemName")
	public String intercept1(Object bean, Method method, Object[] args, MethodInvoker methodInvoker) {
		return "interceptor1 => " + methodInvoker.invokeMethod(args);
	}

	@Around(joinpoint = ".*getRuleName")
	public String intercept2(Object bean, Method method, Object[] args, MethodInvoker methodInvoker) {
		return "interceptor2 => " + methodInvoker.invokeMethod(args);
	}

}
