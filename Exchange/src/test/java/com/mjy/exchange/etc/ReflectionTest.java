package com.mjy.exchange.etc;

import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.hibernate.validator.internal.util.Contracts.assertTrue;

class MyService {
    private MyComponent myComponent;

    public MyComponent getMyComponent() {
        return myComponent;
    }
}

class MyComponent {
    public String getMessage() {
        return "Hello from MyComponent!";
    }
}

class MyService2 {
    @PostConstruct
    public void init() {
        System.out.println("Initialization done!");
    }
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface PostConstruct {
}

interface MyService3 {
    void performAction();
}

class MyServiceImpl implements MyService3 {
    @Override
    public void performAction() {
        System.out.println("Action performed by MyServiceImpl");
    }
}

class MyServiceInvocationHandler implements InvocationHandler {

    private final MyService3 target;

    public MyServiceInvocationHandler(MyService3 target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("Before method execution");
        Object result = method.invoke(target, args);
        System.out.println("After method execution");
        return result;
    }
}

public class ReflectionTest {
    // 1. 빈 관리 및 의존성 주입
    // private 라고 선언 되어 있어도 스프링은 리플렉션을 통해서 의존성을 주입할수 있다.
    @Test
    public void testDependencyInjectionUsingReflection() throws Exception {
        // 객체 생성
        MyService myService = new MyService();

        // 리플렉션을 통해 의존성 주입
        Field field = MyService.class.getDeclaredField("myComponent");
        field.setAccessible(true);
        field.set(myService, new MyComponent());

        // 주입된 값 확인
        assertNotNull(myService.getMyComponent());
    }

    // 2. 애노테이션 처리
    @Test
    public void testPostConstructAnnotationUsingReflection() throws Exception {
        // 리플렉션을 통해 메서드에 있는 애노테이션 처리
        Method method = MyService2.class.getMethod("init");
        if (method.isAnnotationPresent(PostConstruct.class)) {
            PostConstruct postConstruct = method.getAnnotation(PostConstruct.class);
            assertTrue(postConstruct != null, "PostConstruct annotation should be present");
            // 메서드 실행
            method.invoke(new MyService2());
        }
    }

    // 3. AOP (Aspect-Oriented Programming)
    @Test
    public void testAopUsingReflection() throws Exception {
        // AOP를 적용하여 메서드 실행 전후 처리
        Method method = MyService2.class.getMethod("init");

        // 메서드 실행 전
        System.out.println("Before method execution");

        // 실제 메서드 실행
        method.invoke(new MyService2());

        // 메서드 실행 후
        System.out.println("After method execution");
    }

    // 4. 동적 프록시 생성
    @Test
    public void testDynamicProxy() {
        MyService3 proxy = (MyService3) Proxy.newProxyInstance(
                MyService3.class.getClassLoader(),
                new Class[]{MyService3.class},
                new MyServiceInvocationHandler(new MyServiceImpl())
        );

        // 동적 프록시 메서드 호출
        proxy.performAction();
    }
}
