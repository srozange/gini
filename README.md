# Gini

Gini is an ultra light dependency injection and AOP engine.

*Note : This project is just a POC i'm working on to learn IOC.*

Beans managed by Gini are singletons only, they are injected by type, then by field name if several candidates for injection are found.

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

System.out.println(root.getFoo1().getImplemName());
>> fooImpl1

System.out.println(root.getFoo2().getImplemName());
>> fooImpl2
```

## AOP example

###  Advice

Code executed during method interception is defined in methods annotated with the @Around annotations. Those methods are contained in classes annotated with the @advice annotation.

Target methods are defined with a joinpoint (property of the @Around annotation). A joinpoint is a regular expression matching method patterns of form *package.class.method*.

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

System.out.println(root.getFoo1().getImplemName());
>> intercepted => fooImpl1

System.out.println(root.getFoo2().getImplemName());
>> intercepted => fooImpl2
```
