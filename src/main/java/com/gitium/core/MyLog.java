package com.gitium.core;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MyLog {

    public static void error(Throwable error, String msg) {
        Logger logger = Logger.getGlobal();
        logger.log(Level.WARNING, msg, error);
    }

    public static void debug(String msg) {
        Logger logger = Logger.getGlobal();
        logger.log(Level.WARNING, msg);
    }

}