package org.woheller69.weather.aspects;

import android.os.Looper;
import android.util.Log;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.woheller69.weather.JobMainAppInsertRunnable;
import org.woheller69.weather.MethodStat;

import static org.woheller69.weather.JobMainAppInsertRunnable.insert_locker;
import static org.woheller69.weather.activities.SplashActivity.fd;
import static org.woheller69.weather.activities.SplashActivity.groundTruthValues;
import static org.woheller69.weather.activities.SplashActivity.methodIdMap;
import static org.woheller69.weather.activities.SplashActivity.methodStats;
import static org.woheller69.weather.activities.SplashActivity.readAshMem;

@Aspect
public class AspectLoggingJava {

    private static final String POINTCUT_METHOD =
            "execution(* org.woheller69.weather.activities.*.*(..))";
// final String POINTCUT_METHOD =
//            "execution(* org.woheller69.weather.activities.RainViewerActivity.*.*(..))";

    @Pointcut(POINTCUT_METHOD)
    public void execute() {
    }

//    @Pointcut("!within(org.woheller69.weather.activities.SplashActivity.*.*(..))")
//    public void notAspectSplashActivity() { }
//
//    @Pointcut("!within(org.woheller69.weather.activities.*.onCreate(..))")
//    public void notAspect() { }
//
//    @Around("notAspect() && notAspectSplashActivity && execute()")
//    public Object weaveJoinPoint(ProceedingJoinPoint joinPoint) throws Throwable {
//        try {
//            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
//            methodIdMap.putIfAbsent(methodSignature.toString(), methodIdMap.size());
//
//            long startT = System.currentTimeMillis();
//            long startFd = fd > 0 ? readAshMem(fd) : -1;
//            Object result = joinPoint.proceed();
//            long endFd = fd > 0 ? readAshMem(fd) : -1;
//            MethodStat methodStat = new MethodStat(methodIdMap.get(methodSignature.toString()), startFd, endFd);
//
//            insert_locker.lock();
//            if (!methodStats.get(methodStats.size() - 1).equals(methodStat)) {
//                methodStats.add(methodStat);
//            }
//            insert_locker.unlock();
//
////            Log.d("LoggingVM ",methodStat.toString());
////            Log.v("LoggingVM ",
////                    methodSignature.toString()+" "+methodSignature.toLongString()+ methodSignature.toShortString());
//            return result;
//        } catch (Exception e) {
//            return joinPoint.proceed();
//        }
//    }

    @Before("execute()")
    public void weaveJoinPoint(JoinPoint joinPoint) throws Throwable {
        if (joinPoint == null) {
            return;
        }
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        methodIdMap.putIfAbsent(methodSignature.toString(), methodIdMap.size());

        long startFd = fd > 0 ? readAshMem(fd) : -1;
//        long endFd = fd > 0 ? readAshMem(fd) : -1;
        long endFd = startFd;
        MethodStat methodStat = new MethodStat(methodIdMap.get(methodSignature.toString()), startFd, endFd);

        insert_locker.lock();
        if (methodStats.isEmpty()) {
            methodStats.add(methodStat);

        } else if (!methodStats.get(methodStats.size() - 1).equals(methodStat)) {
            methodStats.add(methodStat);
        }
        insert_locker.unlock();

    }


}
