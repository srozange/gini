package org.theglump.gini.bean;

import org.theglump.gini.annotation.Inject;
import org.theglump.gini.annotation.Managed;

@Managed
public class StepImpl1 implements Step {

	@Inject
	private Rule rule;

	public Rule getRule() {
		return rule;
	}

	@Override
	public String implemName() {
		return "stepImpl1";
	}
}
