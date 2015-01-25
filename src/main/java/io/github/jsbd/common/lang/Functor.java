package io.github.jsbd.common.lang;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Functor implements Closure {

    private static final Logger logger = LoggerFactory.getLogger(Functor.class);

    private Object target = null;
    private Method method = null;
    private boolean canceled = false;

    public Functor(Object target, String methodName) {
        this.target = target;
        if (null == this.target) {
            throw new RuntimeException(" target is null.");
        }

        Method[] methods = null;
        Class<?> itr = target.getClass();
        while (!itr.equals(Object.class)) {
            methods = (Method[]) ArrayUtils.addAll(itr.getDeclaredMethods(), methods);
            itr = itr.getSuperclass();
        }
        for (Method methodItr : methods) {
            if (methodItr.getName().equals(methodName)) {
                methodItr.setAccessible(true);
                this.method = methodItr;
            }
        }
        if (null == this.method) {
            throw new RuntimeException("method [" + target.getClass() + "." + methodName + "] !NOT! exist.");
        }
    }

    @Override
    public void execute(Object... args) {
        if (!canceled) {
            try {
                method.invoke(this.target, args);
            } catch (IllegalArgumentException e) {
                logger.error("execute", e);
            } catch (IllegalAccessException e) {
                logger.error("execute", e);
            } catch (InvocationTargetException e) {
                logger.error("execute", e);
            }
        }
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Functor other = (Functor) obj;
        if (target == null) {
            if (other.target != null)
                return false;
        } else if (!method.equals(other.method))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(this.target);
        sb.append(".");
        sb.append(this.method.getName());
        if (this.canceled) {
            sb.append("[canceled]");
        }
        return sb.toString();
    }

}
