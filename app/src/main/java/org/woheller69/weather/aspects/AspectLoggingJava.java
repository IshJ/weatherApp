package org.woheller69.weather.aspects;

import android.os.Looper;
import android.util.Log;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
public class AspectLoggingJava {

    private static final String POINTCUT_METHOD =
            "execution(* org.woheller69.weather.activities.*.*(..))";

    @Pointcut(POINTCUT_METHOD)
    public void execute() {}

    @Around("execute()")
    public Object weaveJoinPoint(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            String className = methodSignature.getDeclaringType().getSimpleName();
            String methodName = methodSignature.getName();
            boolean isMain = (Looper.myLooper() == Looper.getMainLooper());

            long startT = System.currentTimeMillis();
            Object result = joinPoint.proceed();


            Log.v("LoggingVM "+className, className+" "+ methodName+" "+ (System.currentTimeMillis()-startT));
            return result;
        } catch (Exception e) {
            return joinPoint.proceed();
        }
    }

}
