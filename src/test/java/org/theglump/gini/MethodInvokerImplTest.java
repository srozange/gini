package org.theglump.gini;

import net.sf.cglib.proxy.MethodProxy;

import org.junit.Test;
import org.mockito.Mockito;

public class MethodInvokerImplTest {

	@Test
	public void should_invoke_method() throws Throwable {
		// Setup
		MethodProxy methodProxy = Mockito.mock(MethodProxy.class);
		String proxy = "proxy";
		MethodInvoker methodInvoker = new MethodInvokerImpl(methodProxy, proxy);

		// Test
		String[] args = new String[] { "arg1", "arg2" };
		methodInvoker.invokeMethod(args);

		// Assert
		Mockito.verify(methodProxy).invokeSuper(proxy, args);
	}

}
