package org.theglump.gini.bean;

import org.theglump.gini.Inject;
import org.theglump.gini.Managed;

@Managed
public class Root {
	
	@Inject
	private IStep stepImpl1;
	
	@Inject
	private IStep stepImpl2;
	
	@Inject
	private StepImpl1 concreteStep;

	public IStep getStep1() {
		return stepImpl1;
	}

	public IStep getStep2() {
		return stepImpl2;
	}

	public StepImpl1 getConcreteStep() {
		return concreteStep;
	}
	
}
