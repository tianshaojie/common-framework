package io.github.jsbd.common.lang;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

public class FunctorAsync implements Closure {

    private static final Logger logger = LoggerFactory.getLogger(FunctorAsync.class);

    private Executor exec;
    private Closure functor;

    @Override
    public void execute(final Object... args) {
        exec.execute(new Runnable() {

            public void run() {
                try {
                    functor.execute(args);
                } catch (Exception e) {
                    logger.error("execute:", e);
                }
            }
        });
    }

    public void setExec(Executor exec) {
        this.exec = exec;
    }

    public void setFunctor(Closure functor) {
        this.functor = functor;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FunctorAsync other = (FunctorAsync) obj;
        if (functor == null) {
            if (other.functor != null)
                return false;
        } else if (!functor.equals(other.functor))
            return false;
        return true;
    }
}
