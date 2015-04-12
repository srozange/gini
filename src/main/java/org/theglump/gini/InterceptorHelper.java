package org.theglump.gini;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.reflections.ReflectionUtils;
import org.theglump.gini.annotation.Advice;
import org.theglump.gini.annotation.Around;
import org.theglump.gini.annotation.Managed;

import java.lang.reflect.Method;
import java.util.Set;

import static org.reflections.ReflectionUtils.getMethods;
import static org.reflections.ReflectionUtils.withAnnotation;

public class InterceptorHelper {

    private final org.reflections.Reflections reflections;

    protected InterceptorHelper(String packageName) {
        this.reflections = new org.reflections.Reflections(packageName);
    }

    protected Set<Interceptor> computeInterceptors() {
        Set<Interceptor> interceptors = Sets.newHashSet();
        Set<Method> publicManagedMethods = getManagedPublicMethods();
        for (Class<?> clazz : reflections.getTypesAnnotatedWith(Advice.class)) {
            Object advice = Reflections.instantiate(clazz);
            for (Method aroundMethod : getMethods(clazz, withAnnotation(Around.class))) {
                String jointpoint = jointpoint(aroundMethod);
                Set<Method> targetMethods = getTargetMethods(jointpoint, publicManagedMethods);
                if (targetMethods.size() > 0) {
                    Interceptor interceptor = new Interceptor(advice, aroundMethod, targetMethods);
                    interceptors.add(interceptor);
                }
            }
        }
        return interceptors;
    }

    private Set<Method> getTargetMethods(final String joinpoint, Set<Method> candidateMethodsForInterception) {
        return Sets.filter(candidateMethodsForInterception, new Predicate<Method>() {

            @Override
            public boolean apply(Method method) {
                return Iterables.any(computeMethodPathes(method), new Predicate<String>() {

                    @Override
                    public boolean apply(String methodPath) {
                        return methodPath.matches(joinpoint);
                    }
                });
            }

        });
    }

    private String jointpoint(Method aroundMethod) {
        return aroundMethod.getAnnotation(Around.class).joinpoint();
    }

    private Set<Method> getManagedPublicMethods() {
        Set<Method> methods = Sets.newHashSet();
        for (Class<?> clazz : reflections.getTypesAnnotatedWith(Managed.class)) {
            methods.addAll(Reflections.getPublicMethods(clazz));
        }
        return methods;
    }

    private Set<String> computeMethodPathes(Method method) {
        Set<String> methodPathes = Sets.newHashSet();
        methodPathes.add(method.getDeclaringClass().getName() + "." + method.getName());
        for (Class<?> clazz : ReflectionUtils.getAllSuperTypes(method.getDeclaringClass())) {
            methodPathes.add(clazz.getName() + "." + method.getName());
        }
        return methodPathes;
    }

}
