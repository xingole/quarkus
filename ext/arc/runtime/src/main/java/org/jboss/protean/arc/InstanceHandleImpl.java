package org.jboss.protean.arc;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.spi.AlterableContext;
import javax.enterprise.context.spi.CreationalContext;
import javax.inject.Singleton;

/**
 *
 * @author Martin Kouba
 *
 * @param <T>
 */
class InstanceHandleImpl<T> implements InstanceHandle<T> {

    @SuppressWarnings("unchecked")
    public static final <T> InstanceHandle<T> unresolvable() {
        return (InstanceHandle<T>) UNRESOLVABLE;
    }

    static final InstanceHandleImpl<Object> UNRESOLVABLE = new InstanceHandleImpl<Object>(null, null, null, null);

    private final InjectableBean<T> bean;

    private final T instance;

    private final CreationalContext<T> creationalContext;

    private final CreationalContext<?> parentCreationalContext;

    InstanceHandleImpl(InjectableBean<T> bean, T instance, CreationalContext<T> creationalContext) {
        this(bean, instance, creationalContext, null);
    }

    InstanceHandleImpl(InjectableBean<T> bean, T instance, CreationalContext<T> creationalContext, CreationalContext<?> parentCreationalContext) {
        this.bean = bean;
        this.instance = instance;
        this.creationalContext = creationalContext;
        this.parentCreationalContext = parentCreationalContext;
    }

    @Override
    public boolean isAvailable() {
        return instance != null;
    }

    @Override
    public T get() {
        return instance;
    }

    @Override
    public void release() {
        if (isAvailable()) {
            if (bean.getScope().equals(ApplicationScoped.class) || bean.getScope().equals(RequestScoped.class) || bean.getScope().equals(Singleton.class)) {
                ((AlterableContext) Arc.container().getContext(bean.getScope())).destroy(bean);
            } else {
                destroy();
            }
        }
    }

    void destroy() {
        if (parentCreationalContext != null) {
            parentCreationalContext.release();
        } else {
            bean.destroy(instance, creationalContext);
        }
    }

}
