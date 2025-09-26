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
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Logger {

    private Datastore fileStore = new FileStore();

    private Set<Log> logTrackSet = new HashSet<>(); // capacity

    private Queue<Set<Log>> logsProcessingQueue = new ArrayDeque<>(); // capacity

    private Integer timeout = 100;

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
                try{

                    Set<Log>  copyTrackSet = DeepCopyUtil.deepCopy(logTrackSet);
                    put(this.logsProcessingQueue,copyTrackSet);// deep copy error here!
                    flushLogProcessingSet();


                    service.submit(()->{
                        try{
                            Instant startTime = Instant.now();
                            long st = startTime.getEpochSecond();
                            fileStore.appendLog(logsProcessingQueue.peek());
                            logsProcessingQueue.remove();

                            Instant endTime = Instant.now();
                            long et = endTime.getEpochSecond();


                            Integer latency = Math.toIntExact(et)  - Math.toIntExact(st);

                            if(latency > this.timeout){
                                deleteLogs();
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

    private <T> void put(Collection<T> collection, T item){
            collection.add(item);
    }

    private void deleteLogs(){
        fileStore.deleteLog();
    }



}
