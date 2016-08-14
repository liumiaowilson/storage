package org.wilson.storage.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import org.apache.log4j.Logger;

public class CommonUtils {
    private static final Logger logger = Logger.getLogger(CommonUtils.class);
    
    public static boolean isOpenShiftApp() {
        String name = System.getenv("OPENSHIFT_GEAR_NAME");
        return name != null;
    }
    
    public static String getFilesDir() {
        return getDataDir() + "files/";
    }
    
    public static String getDataDir() {
        if(isOpenShiftApp()) {
            return System.getenv("OPENSHIFT_DATA_DIR");
        }
        else {
            return "";
        }
    }
    
    public static boolean initKey(String key) throws IOException {
        File keyFile = new File(getDataDir() + "key");
        if(!keyFile.exists()) {
            PrintWriter pw = new PrintWriter(keyFile);
            pw.write(key);
            pw.flush();
            pw.close();
            
            return true;
        }
        else {
            logger.error("Key already exists!");
            return false;
        }
    }
    
    public static String getKey() throws IOException {
        File keyFile = new File(getDataDir() + "key");
        if(!keyFile.exists()) {
            logger.error("Key does not exist");
            return null;
        }
        else {
            Scanner scanner = new Scanner(keyFile);
            String key = scanner.nextLine();
            if(key != null) {
                key = key.trim();
            }
            scanner.close();
            return key;
        }
    }
}
