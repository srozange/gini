package org.theglump.gini.bean;

import org.theglump.gini.annotation.Managed;

@Managed
public class RuleImpl implements Rule {

	public String getRuleName() {
		return "ruleImpl";
	}

}
