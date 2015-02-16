package org.theglump.gini;

/**
 * This class allows one to call a method on the proxyfied object.
 * 
 * It's usefull since method call cannot be made directly on proxy as it will be
 * endelessly intercepted.
 * 
 * @author sebastien.rozange
 * 
 */
public interface MethodInvoker {

	/**
	 * Invoke the advised method
	 * 
	 * @param args
	 * @return method call result
	 */
	Object invokeMethod(Object[] args);

}
