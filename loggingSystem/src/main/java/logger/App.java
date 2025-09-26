package logger;


import logger.pojo.Log;
import logger.service.Logger;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {



        Logger logger = Logger.getInstance();

        logger.addLog(new Log("Starting from here"));
        logger.addLog(new Log("Read data from DB"));
        logger.addLog(new Log("got exception"));
        logger.addLog(new Log("done"));

        logger.appendLog();

//        System.out.println(Logger.getInstance());
//        System.out.println(Logger.getInstance());

//        String asciiLogger =
//                " _                            _  _   \n" +
//                        "| |    ___   ___  _ __   __| || |_ \n" +
//                        "| |   / _ \\ / _ \\| '_ \\ / _` || __|\n" +
//                        "| |__| (_) | (_) | | | | (_| || |_ \n" +
//                        "|_____\\___/ \\___/|_| |_|\\__,_| \\__|\n";
//
//        System.out.print(asciiLogger);
//
//        System.out.println("Press ? for help");

    }
}
