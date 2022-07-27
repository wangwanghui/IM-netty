package com.octv.im.util;



import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * 用于持有spring的ApplicaitonContext,可在任何代码任何地方任何时候中取出ApplicaitonContext
 *
 * @version 1.0
 */
@Slf4j
@Component(value = ApplicationContextHolder.APPLICATION_CONTEXT_HOLDER)
@Lazy(value = false)
public class ApplicationContextHolder implements ApplicationContextAware, DisposableBean {

    public static final String APPLICATION_CONTEXT_HOLDER = "applicationContextHolder";
    private static ApplicationContext applicationContext = null;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        if (applicationContext != null) {
            throw new IllegalStateException("ApplicationContextHolder already holded 'applicationContext'.");
        }
        applicationContext = context;
        log.debug("holded applicationContext,displayName: {}", applicationContext.getDisplayName());
    }

    public static ApplicationContext getApplicationContext() {
        assertContextInjected();
        return applicationContext;
    }

    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }

    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

    public static void clear() {
        applicationContext = null;
    }

    @Override
    public void destroy() throws Exception {
        clear();
    }

    private static void assertContextInjected() {
        if (applicationContext == null) {
            throw new IllegalStateException(
                    "'applicationContext' property is null,ApplicationContextHolder not yet init.");
        }
    }
}