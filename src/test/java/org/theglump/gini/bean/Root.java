package org.theglump.gini.bean;

import org.theglump.gini.annotation.Inject;
import org.theglump.gini.annotation.Managed;

@Managed
public class Root {
	
	@Inject
	private Step stepImpl1;
	
	@Inject
	private Step stepImpl2;
	
	@Inject
	private StepImpl1 concreteStep;

	public Step getStep1() {
		return stepImpl1;
	}

	public Step getStep2() {
		return stepImpl2;
	}

	public StepImpl1 getConcreteStep() {
		return concreteStep;
	}
	
}
