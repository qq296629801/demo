package com.sie.iidp.demo.xxljob.executor.spring;

import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author chun
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.sie.iidp.demo.xxljob.executor")
public class SpringApp {

    private final static Logger logger = LoggerFactory.getLogger(SpringApp.class);

    private static ConfigurableApplicationContext context;

    private static SpringApp springApp;

    public static synchronized void start(String[] args, ClassLoader classLoader) {
        if (context != null) {
            return;
        }
//        TomcatURLStreamHandlerFactory.disable();
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            SpringApplication application = new SpringApplication(SpringApp.class);
            application.setWebApplicationType(WebApplicationType.NONE);
            context =  application.run(args);
            springApp = context.getBean(SpringApp.class);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    public static synchronized void stop() {
        if (context == null) {
            return;
        }

        try {
            context.stop();
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        } finally {
            context = null;
            springApp = null;
        }
    }

    public static SpringApp getSpringApp() {
        return springApp;
    }
}
