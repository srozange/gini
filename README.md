# Gini

Gini is an ultra light dependency injection and AOP engine.

Beans managed by Gini are singletons, they are injected by type, then by field name if several candidates for injection are found.

Gini allows to intercept method calls on managed beans (AOP). In order to do so, Gini uses [CGLib](https://github.com/cglib/cglib) to create dynamic proxies.

## Dependency injection example

###  Foo.java

```java
public interface Foo {

	String getImplemName();

}
```
###  FooImpl1.java

We use the @Managed annotation to declare bean as managed by Gini.

```java
@Managed
public class FooImpl1 implements Foo {

	@Override
	public String getImplemName() {
		return "fooImpl1";
	}
}
```

###  FooImpl2.java

```java
@Managed
public class FooImpl2 implements Foo {

	@Override
	public String getImplemName() {
		return "fooImpl2";
	}
}
```

###  Injection

Fields in need for injection must be annotated with the @Inject annotation.

```java
@Managed
public class Root {
	
	@Inject
	private Foo fooImpl1;
	
	@Inject
	private Foo fooImpl2;
	
	public Foo getFoo1()  {
		return fooImpl1;
	}
	
	public Foo getFoo2()  {
		return fooImpl2;
	}
}
```

###  Let's try it out

```java
GiniContext ctx = new GiniContext("org.theglump.gini.example");
Root root = ctx.getBean(Root.class);

println(root.getFoo1().getImplemName());
>> fooImpl1

println(root.getFoo2().getImplemName());
>> fooImpl2
```

## AOP example

###  Advice

AOP allows to intercept method calls in order to execute specific code.

Code to be executed must be defined in methods annotated with the @Arround annotation. An @Around annotation contains a joinpoint field that defines with methods will be intercepted, it consists in a regular expression matching method patterns of form *package.class.method*.

```java
@Advice
public class Advice {

	@Around(joinpoint = ".Foo.getImplemName")
	public String intercept(Object bean, Method method, Object[] args, MethodInvoker methodInvoker) {
		return "intercepted => " + methodInvoker.invokeMethod(args);
	}
}
```

###  Let's try it out

```java
GiniContext ctx = new GiniContext("org.theglump.gini.example");
Root root = ctx.getBean(Root.class);

println(root.getFoo1().getImplemName());
>> intercepted => fooImpl1

println(root.getFoo2().getImplemName());
>> intercepted => fooImpl2
```
