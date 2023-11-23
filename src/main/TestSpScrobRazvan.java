package com.example.test2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

// Loggable.java
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.stereotype.Component;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@interface Loggable {
}

interface BattleFormation {
    void march();
}

class GnomesBattalion implements BattleFormation {
    @Override
    public void march() {
        System.out.println("Gnomii au pornit la lupta!");
    }
}

class ElfBattalion implements BattleFormation {
    @Override
    public void march() {
        System.out.println("Elfii sunt gata de atac!");
    }
}

class EntsBattalion implements BattleFormation {
    @Override
    public void march() {
        System.out.println("Entii sunt pe pozitii!");
    }
}

interface BattalionFactory {
    BattleFormation createBattalion();
}

class GnomesBattalionFactory implements BattalionFactory {
    @Override
    public BattleFormation createBattalion() {
        return new GnomesBattalion();
    }
}

class ElfBattalionFactory implements BattalionFactory {
    @Override
    public BattleFormation createBattalion() {
        return new ElfBattalion();
    }
}

class EntsBattalionFactory implements BattalionFactory {
    @Override
    public BattleFormation createBattalion() {
        return new EntsBattalion();
    }
}

class CompositeBattalion implements BattleFormation {
    private List<BattleFormation> battalions = new ArrayList<>();

    public void addBattalion(BattleFormation battalion) {
        battalions.add(battalion);
    }

    @Override
    public void march() {
        System.out.println("Batalionul este gata de lupta: ");
        for (BattleFormation battalion : battalions) {
            battalion.march();
        }
    }
}

@RestController
class ArmyController {
    private final ArmyService armyService;

    public ArmyController(ArmyService armyService) {
        this.armyService = armyService;
    }

    @GetMapping("/march")
    @Loggable
    public String march() {
        armyService.march();
        return "Armata este in miscare!";
    }
}


@Service
class ArmyService {
    public void march() {
        System.out.println("Armata este in miscare!");
    }
}

@Component
class ArmyBuilder {
    private final CompositeBattalion army;

    public ArmyBuilder() {
        this.army = new CompositeBattalion();
    }

    public ArmyBuilder addBattalion(BattalionFactory factory) {
        army.addBattalion(factory.createBattalion());
        return this;
    }

    public CompositeBattalion build() {
        return army;
    }
}


@Component
class ArmyServiceProxy {
    public ArmyService createProxy(ArmyService armyService) {
        ProxyFactory factory = new ProxyFactory(armyService);
        factory.addAdvice(new ArmyServiceLogger());
        return (ArmyService) factory.getProxy();
    }
}


class ArmyServiceLogger implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        System.out.println("Logging: " + invocation.getMethod().getName());
        return invocation.proceed();
    }
}

@SpringBootApplication
public class Test2Application {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Test2Application.class, args);

        ArmyService armyService = context.getBean(ArmyServiceProxy.class).createProxy(context.getBean(ArmyService.class));
        armyService.march();

        context.close();
    }
}


