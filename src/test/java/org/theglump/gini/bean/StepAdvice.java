package org.theglump.gini.bean;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodProxy;

import org.theglump.gini.annotation.Advice;
import org.theglump.gini.annotation.Around;

@Advice
public class StepAdvice {

	@Around(target = ".*Step.*method1")
	public String intercept(Object bean, Method method, Object[] args,
			MethodProxy proxy) throws Throwable {
		return "interceptor => " + proxy.invokeSuper(bean, args);
	}

}
