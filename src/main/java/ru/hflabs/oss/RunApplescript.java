package ru.hflabs.oss;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: maximb
 * Date: 15.01.13
 * Time: 11:34
 * To change this template use File | Settings | File Templates.
 */
public class RunApplescript {
    private final static String currentFolder = "Calendar";
    private static String calStart = "12/1/2012";
    private static String calEnd = "12/31/2012";
    private static String asFormat ;
    public static void main(String[] args) {
        if (args.length==0) return;
        long startTime = System.currentTimeMillis();
        long delta;
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = null;
        boolean internal = false;
        try {
            reader = new BufferedReader(new FileReader(args[0]));
        } catch (FileNotFoundException e) {
            reader = new BufferedReader(new InputStreamReader(RunApplescript.class
                    .getClass().getClassLoader().getResourceAsStream("outlook.applescript")));
            internal=true;

        }
        char[] buf = new char[1024];
        int numRead;
        try {
            while ((numRead = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
                buf = new char[1024];
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String script = (internal && args.length == 3?String.format(fileData.toString(),args[0],args[1],args[2]):fileData.toString());
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("AppleScript");
        ArrayList<Object> retVal;
        try {
            retVal = (ArrayList<Object>)engine.eval(script);
            delta = System.currentTimeMillis() - startTime;
            for (Object obj:retVal)
            System.out.println(ReflectionToStringBuilder.reflectionToString(obj));
            System.out.println("Time:" + Long.toString(delta));
            System.out.println("Items:" + Integer.toString(retVal.size()));
            System.out.println("Average: " + (retVal.size()*1000/delta) + "records/sec");
        } catch (ScriptException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }
}
