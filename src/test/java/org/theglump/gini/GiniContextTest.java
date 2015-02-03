package org.theglump.gini;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;
import org.theglump.gini.bean.Root;
import org.theglump.gini.bean.RuleImpl;
import org.theglump.gini.bean.StepImpl1;
import org.theglump.gini.bean.StepImpl2;

// Integration tests
public class GiniContextTest {
	
	@Inject
	private Root root;
	
	@Test
	public void shoud_succeed() {
		// Setup
		GiniContext ctx = new GiniContext("org.theglump.gini.bean");
		
		// Test
		root = ctx.getBean(Root.class);
		
		// Assert
		assertThat(root.getStepImpl1()).isInstanceOf(StepImpl1.class);
		assertThat(root.getStepImpl2()).isInstanceOf(StepImpl2.class);
		assertThat(root.getConcreteStep()).isInstanceOf(StepImpl1.class);
		assertThat(root.getConcreteStep().getRule()).isInstanceOf(RuleImpl.class);
	}

	@Test
	public void  shoud_succeed_with_auto_inject() {
		// Setup
		GiniContext ctx = new GiniContext("org.theglump.gini.bean");
		
		// Test
		ctx.inject(this);
		
		// Assert
		assertThat(root.getStepImpl1()).isInstanceOf(StepImpl1.class);
		assertThat(root.getStepImpl2()).isInstanceOf(StepImpl2.class);
		assertThat(root.getConcreteStep()).isInstanceOf(StepImpl1.class);
		assertThat(root.getConcreteStep().getRule()).isInstanceOf(RuleImpl.class);
	}
		
}
