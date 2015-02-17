# gini
Gini is an ultra light dependency injection and AOP engine I developped for learning purpose.

Beans managed by Gini are singletons only, they are injected by type, then by field name if several candidates for injection are found.

AOP allows method call interceptions. In order to do so, Gini uses [CGLib](https://github.com/cglib/cglib) to create dynamic proxies.

###  Interface IStep
```java
package org.theglump.gini.bean;

public interface IStep {

	String getImplemName();

}
```
###  Class StepImpl1

We use the @Managed annotation to declare it as a bean managed by Gini.

```java
package org.theglump.gini.bean;

@Managed
public class StepImpl1 implements IStep {

	@Override
	public String getImplemName() {
		return "stepImpl1";
	}
}
```

###  Class StepImpl2

```java
package org.theglump.gini.bean;

@Managed
public class StepImpl2 implements IStep {

	@Override
	public String getImplemName() {
		return "stepImpl2";
	}
}
```

###  Injected class

Fields needing injection must be annotated with the @Inject annotation.

```java
package org.theglump.gini.bean;

@Managed
public class Root {
	
	@Inject
	private IStep stepImpl1;
	
	@Inject
	private IStep stepImpl2;
	
	public IStep getStep1()  {...}
	public IStep getStep2()  {...}
}
```

###  Let's try it out

```java
GiniContext ctx = new GiniContext("org.theglump.gini.bean");
Root root = ctx.getBean(Root.class);

System.out.println(root.getStep1().getImplemName());
>> stepImpl1

System.out.println(root.getStep2().getImplemName());
>> stepImpl2
```

## AOP example

###  Advice

An advice must be annotated with the @Advice annotation, it contains interception methods annotated with the @Around annotation.

Target methods are defined with a joinpoint (field of the @Around annotation). A joinpoint is a regexp with the following formalism : *package.class.method*.

```java
package org.theglump.gini.bean;

@Advice
public class Advice {

	@Around(joinpoint = ".*IStep.getImplemName")
	public String intercept(Object bean, Method method, Object[] args, MethodInvoker methodInvoker) {
		return "intercepted => " + methodInvoker.invokeMethod(args);
	}
}
```

###  Let's try it out

```java
System.out.println(root.getStep1().getImplemName());
>> intercepted => stepImpl1

System.out.println(root.getStep2().getImplemName());
>> intercepted => stepImpl2
```
