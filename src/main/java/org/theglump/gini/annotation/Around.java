package org.theglump.gini.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Around {

	/**
	 * Regexp matching method pathes of form : .*IStep.getStr.*
	 * 
	 * @return joinpoint
	 */
	public String joinpoint();

}
