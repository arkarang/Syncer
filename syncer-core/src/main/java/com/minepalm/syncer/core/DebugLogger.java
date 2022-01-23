package com.minepalm.syncer.core;

import lombok.Setter;

import java.util.concurrent.*;
import java.util.logging.Logger;

public class DebugLogger {

    @Setter
    static Logger logger;
    @Setter
    static boolean enableDebug = false;
    private static final ExecutorService service = Executors.newFixedThreadPool(4);

    public static void log(String text){
        if(enableDebug) {
            logger.info(text);
        }
    }

    public static void error(Throwable ex){
        if(enableDebug) {
            logger.severe("Exception : "+ex.getClass().getSimpleName()+", "+ex.getMessage());
            for (StackTraceElement stackTraceElement : ex.getStackTrace()) {
                logger.severe(stackTraceElement.toString());
            }
            if(ex.getCause() != null){
                Throwable cause = ex.getCause();
                logger.severe("Exception Cause : "+cause.getClass().getSimpleName()+", "+cause.getMessage());
                for (StackTraceElement stackTraceElement : cause.getStackTrace()) {
                    logger.severe(stackTraceElement.toString());
                }
            }
        }
    }

    public static void handle(String tag, CompletionStage<?> future){
        if(enableDebug){
            if(future == null){
                log("handle: "+tag+" is null");
                return;
            }
            future.handle((value, exception)->{
                log("handle: "+tag);
                if(exception != null){
                    error(exception);
                }
                return null;
            });
        }
    }

    public static void timeout(String tag, CompletableFuture<?> future, long mills){
        if(enableDebug) {
            service.submit(() -> {
                try {
                    future.get(mills, TimeUnit.MILLISECONDS);
                    log(tag + " executed without timeout");
                } catch (Exception e) {
                    log(tag + " timeout : " + mills + "ms");
                }
            });
        }
    }
}
