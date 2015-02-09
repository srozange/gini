package org.theglump.gini;

import java.lang.reflect.Method;
import java.util.Set;

import net.sf.cglib.proxy.MethodProxy;

import com.google.common.collect.SetMultimap;

/**
 * This class is used to proxify a bean It delegates the call to the related
 * advisor if method need to be advised
 * 
 * @author sebastien.rozange
 * 
 */
class MethodInterceptor implements net.sf.cglib.proxy.MethodInterceptor {

	private final SetMultimap<Method, Interceptor> interceptorsForMethod;

	MethodInterceptor(SetMultimap<Method, Interceptor> interceptorsForMethod) {
		this.interceptorsForMethod = interceptorsForMethod;
	}

	@Override
	public Object intercept(Object bean, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		Set<Interceptor> interceptors = interceptorsForMethod.get(method);

		if (interceptors.size() > 0) {
			Interceptor interceptor = interceptors.iterator().next();
			Object[] adviceArguments = new Object[] { bean, method, args, new MethodInvokerImpl(proxy, bean) };
			return interceptor.method.invoke(interceptor.advice, adviceArguments);
		}

		return proxy.invokeSuper(bean, args);
	}

}
