package org.theglump.gini.bean;

import org.theglump.gini.annotation.Managed;

@Managed
public class StepImpl2 implements IStep {

	@Override
	public String method1() {
		return "stepImpl2";
	}

}
