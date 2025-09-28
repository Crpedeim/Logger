package logger.service;

import logger.data.Datastore;
import logger.data.FileStore;
import logger.enums.Severity;
import logger.pojo.Log;
import logger.utils.DeepCopyUtil;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.*;

public class Logger {

    private Datastore fileStore = new FileStore();

    private Set<Log> logTrackSet = new HashSet<>(); // capacity

    private Queue<Set<Log>> logsProcessingQueue = new ArrayDeque<>(); // capacity



    private static Logger logger = null;

    ExecutorService service = Executors.newFixedThreadPool(10);

    public static Logger getInstance(){
        if(logger == null){
            logger = new Logger();
        }
        return logger;
    }

    public void addLog(Log log){
        // Todo: add timestamp and thread and stacktrace
        synchronized (Logger.class){

            Timestamp timestamp = new Timestamp(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));//potential bug in searching
            // logs in query processor

            StackTraceElement[] elements = Thread.currentThread().getStackTrace();

            StringBuilder stackElements = new StringBuilder();

            for(StackTraceElement element: elements){
                stackElements.append("\tat").append(element.toString()).append("\n");
            }

            String stackTrace = stackElements.toString();

            log.setStackTrace(stackTrace);
            log.setTimestamp(timestamp);
            log.setThreadId(Long.toString(Thread.currentThread().getId()));
            log.setThreadName(Thread.currentThread().getName());
            log.setSeverity(log.getSeverity() == null? Severity.LOW : log.getSeverity());
            put(this.logTrackSet,log);

        }

    }

    public void appendLog(){
        // Todo: Handle exception of append Log from datastore
            synchronized (Logger.class){

                if (logger == null) {
                    throw new IllegalStateException("Logger has not been initialized yet");
                }

                try{

                    Set<Log>  copyTrackSet = DeepCopyUtil.deepCopy(logTrackSet);
                    put(this.logsProcessingQueue,copyTrackSet);// deep copy error here!
                    flushLogProcessingSet();


                    service.submit(()->{
                        try{

                            synchronized (Object.class){
                                Set<Log> logs = logsProcessingQueue.peek();
                                logsProcessingQueue.remove();
                                fileStore.appendLog(logs);
                            }
                        }
                        catch (Exception e){
                            e.getStackTrace();
                        }
                    });


                }
                catch(Exception e){
                    e.getStackTrace();
                }
            }

    }

    private void flushLogProcessingSet(){
            logTrackSet.clear();
    }

    private void flushLogProcessingQueue() {
        logsProcessingQueue.clear();
    }

    private <T> void put(Collection<T> collection, T item){
            collection.add(item);
    }

    private void deleteLogs(){
        fileStore.deleteLog();
    }

    public void shutdown() {
//        try {
//            if (dataStore instanceof  FileStore) {
//                ((FileStore) dataStore).fileClose();
//            }
//
//            service.shutdown();
//            try {
//                if (!service.awaitTermination(10, TimeUnit.SECONDS)) {
//                    service.shutdownNow();
//                }
//            }
//            catch (InterruptedException e) {
//                service.shutdownNow();
//                Thread.currentThread().interrupt();
//            }
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }

        try {
            ThreadPoolExecutor executor = (ThreadPoolExecutor) this.service;
            executor.shutdown();
            boolean isCompleted = executor.awaitTermination(100, TimeUnit.SECONDS);
            if(!isCompleted) {
                throw new RuntimeException("Executor shutdown timed out");
            }

            if (fileStore instanceof  FileStore) {
                ((FileStore) fileStore).fileClose();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
