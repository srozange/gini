package org.theglump.gini;

import net.sf.cglib.proxy.MethodProxy;

/**
 * Wrapper over cglib MethodInvoker
 * 
 * @author sebastien.rozange
 * 
 */
class MethodInvokerImpl implements MethodInvoker {

	private final MethodProxy methodProxy;
	private final Object proxy;

	MethodInvokerImpl(MethodProxy methodProxy, Object proxy) {
		this.methodProxy = methodProxy;
		this.proxy = proxy;
	}

	@Override
	public Object invokeMethod(Object[] args) {
		try {
			return methodProxy.invokeSuper(proxy, args);
		} catch (Throwable e) {
			throw new GiniException("could not call method on proxy " + proxy, e);
		}
	}

}
