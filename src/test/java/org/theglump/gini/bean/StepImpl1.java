package org.theglump.gini.bean;

import org.theglump.gini.Inject;
import org.theglump.gini.Managed;

@Managed
public class StepImpl1 implements IStep {

	@Inject
	private IRule rule;
	
	public IRule getRule() {
		return rule;
	}
}
