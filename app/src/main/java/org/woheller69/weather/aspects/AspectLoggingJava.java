package org.woheller69.weather.aspects;

import android.util.Log;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.woheller69.weather.MethodStat;

import static org.woheller69.weather.JobMainAppInsertRunnable.insert_locker;
import static org.woheller69.weather.activities.SplashActivity.fd;
import static org.woheller69.weather.activities.SplashActivity.methodIdMap;
import static org.woheller69.weather.activities.SplashActivity.methodStats;
import static org.woheller69.weather.activities.SplashActivity.readAshMem;

@Aspect
public class AspectLoggingJava {
    static {
        System.loadLibrary("native-lib");
    }

//    private static final String POINTCUT_METHOD =
//            "execution(* org.woheller69.weather.activities.*.*(..))";

    final String POINTCUT_METHOD_RAINVIEWER =
            "execution(* org.woheller69.weather.activities.RainViewerActivity.*(..))";
    @Pointcut(POINTCUT_METHOD_RAINVIEWER)
    public void executeRainViewer() {
    }

    final String POINTCUT_METHOD_ManageLocationsActivity =
            "execution(* org.woheller69.weather.activities.ManageLocationsActivity.*(..))";
    @Pointcut(POINTCUT_METHOD_ManageLocationsActivity)
    public void executeManageLocationsActivity() {
    }

    final String POINTCUT_METHOD_SideChannelJob =
            "execution(* org.woheller69.weather.SideChannelJob.*(..))";
    @Pointcut(POINTCUT_METHOD_SideChannelJob)
    public void executeSideChannelJob() {
    }



    final String POINTCUT_METHOD_SPLASH_runView =
            "execution(* org.woheller69.weather.activities.SplashActivity.runView(..))";
    @Pointcut(POINTCUT_METHOD_SPLASH_runView)
    public void executeSplashRunView() {
    }

    final String POINTCUT_METHOD_SPLASH =
            "execution(* org.woheller69.weather.activities.SplashActivity.getRecordCount(..))";
    @Pointcut(POINTCUT_METHOD_SPLASH)
    public void executeSplashGetRecordCount() {
    }


    final String POINTCUT_METHOD_CHILDA =
            "execution(* org.woheller69.weather.activities.ChildA.*(..))";

    @Pointcut(POINTCUT_METHOD_CHILDA)
    public void executeChildA() {
    }

    final String POINTCUT_METHOD_CHILDB =
            "execution(* org.woheller69.weather.activities.ChildB.*(..))";

    @Pointcut(POINTCUT_METHOD_CHILDB)
    public void executeChildB() {
    }

    final String POINTCUT_METHOD_CHILDC =
            "execution(* org.woheller69.weather.activities.ChildC.*(..))";

    @Pointcut(POINTCUT_METHOD_CHILDC)
    public void executeChildC() {
    }


//    @Pointcut("!within(org.woheller69.weather.activities.SplashActivity.*.*(..))")
//    public void notAspectSplashActivity() { }
//
//    @Pointcut("!within(org.woheller69.weather.activities.*.onCreate(..))")
//    public void notAspect() { }
//
//    @Around("executeManageLocationsActivity() || executeSplashRunView() || executeSplashGetRecordCount() " +
//            "|| executeRainViewer() || executeChildA() || executeChildB() || executeChildC()")
//    public Object weaveJoinPoint(ProceedingJoinPoint joinPoint) throws Throwable {
//        try {
//            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
//            methodIdMap.putIfAbsent(methodSignature.toString(), methodIdMap.size());
//
//            long startFd = fd > 0 ? readAshMem(fd) : -1;
////            long startT = System.currentTimeMillis();
//
//            Object result = joinPoint.proceed();
////            long endT = System.currentTimeMillis();
//
//            long endFd = fd > 0 ? readAshMem(fd) : -1;
//
//            MethodStat methodStat = new MethodStat(methodIdMap.get(methodSignature.toString()), startFd, endFd);
////            Log.d("#Aspect ", methodStat.getId()+" "+startFd+" "+endFd);
//            insert_locker.lock();
//            if (methodStats.isEmpty()) {
//                methodStats.add(methodStat);
//
//            } else if (!methodStats.get(methodStats.size() - 1).equals(methodStat)) {
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

//    @Before("executeManageLocationsActivity() || executeSplashRunView() || executeSplashGetRecordCount() " +
//            "|| executeRainViewer() || executeChildA() || executeChildB() || executeChildC()")
    @Before("executeRainViewer() || executeChildA() || executeChildB() || executeChildC()")
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
//        Log.d("#Aspect ", methodStat.getId()+" "+startFd+" "+endFd);
        insert_locker.lock();
        if (methodStats.isEmpty()) {
            methodStats.add(methodStat);

        } else if (!methodStats.get(methodStats.size() - 1).equals(methodStat)) {
            methodStats.add(methodStat);
        }
        insert_locker.unlock();

    }

    public static native long readAshMem1(int fd);
}
