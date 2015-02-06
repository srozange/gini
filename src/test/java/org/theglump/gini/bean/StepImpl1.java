package org.theglump.gini.bean;

import org.theglump.gini.annotation.Inject;
import org.theglump.gini.annotation.Managed;

@Managed
public class StepImpl1 implements IStep {

	@Inject
	private IRule rule;

	public IRule getRule() {
		return rule;
	}

	@Override
	public String method1() {
		return "stepImpl1";
	}
}
