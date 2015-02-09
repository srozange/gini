package org.theglump.gini;

/**
 * This class allow to call a method on the proxyfied object Proxified object
 * 
 * cannot be call directly since those call will be intercepted by
 * MethodInterceptor (endless loop)
 * 
 * @author sebastien.rozange
 * 
 */
public interface MethodInvoker {

	/**
	 * Invoke the advised method
	 * 
	 * @param args
	 * @return
	 */
	Object invokeMethod(Object[] args);

}
