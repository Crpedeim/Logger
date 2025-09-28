package logger;


import logger.enums.Severity;
import logger.pojo.Log;
import logger.service.Logger;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {



        Logger logger = Logger.getInstance();

        logger.addLog(new Log("This is log 1",Severity.WARN));
        logger.addLog(new Log("This is log 2", Severity.LOW));
        logger.addLog(new Log("This is log 3", Severity.MEDIUM));
        logger.addLog(new Log("This is log 4", Severity.HIGH));
        logger.addLog(new Log("This is log 5", Severity.CRITICAL));

        logger.appendLog();


        logger.addLog(new Log("This is log 6", Severity.WARN));
        logger.addLog(new Log("This is log 7", Severity.LOW));
        logger.addLog(new Log("This is log 8", Severity.MEDIUM));
        logger.addLog(new Log("This is log 9", Severity.HIGH));
        logger.addLog(new Log("This is log 10", Severity.CRITICAL));

        logger.appendLog();

        logger.shutdown();
 }
}

