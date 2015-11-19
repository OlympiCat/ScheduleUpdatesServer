/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.olympicat.scheduleupdates;

import com.olympicat.scheduleupdates.serverdatarecievers.ScheduleChange;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author Avishay
 */
public class Main {
    private static List<ScheduleChange> changeArr = null;
    private static Long refreshDelay = (30l * 60l * 1000l); //    =   30m in milliseconds
    private static Thread infoReadThread = null;
    private static Boolean readDataFromThread = true;
    public static Map<Integer, ScheduleChange[]> dummyChanges = new HashMap<Integer, ScheduleChange[]>();
    public static Logger logger = Logger.getLogger("ServerLogger"); // Hooray for logging!
    private static FileHandler fh;

    public static void main(String[] args) throws IOException {
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF); // In order to remove all the log warnings, THANK GOD IT IS THAT SIMPLE
        final int PORT = 25565;
        ServerSocket serverSocket = null;
        
        // Logger initialization 
        fh = new FileHandler("serv_log.log", true); //It will create a new file everytime we start the server because the previous log is locked, I am not really sure where to close the handler.
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);
        
//        /* Dummy Test */
//        List<Byte> command = new ArrayList<>();
//        command.add(CommandConstants.SCEHDULES_HEADER);
//        command.add(CommandConstants.ADD_CANCEL_DUMMY);
//        command.add((byte) 0x18);
//        command.addAll(Arrays.asList(ArrayUtils.toObject("05".getBytes())));
//        command.addAll(Arrays.asList(ArrayUtils.toObject("20.10.2015".getBytes())));
//        command.addAll(Arrays.asList(ArrayUtils.toObject("משמש גדול".getBytes())));
//        System.out.println("Size is " + command.size());
//        byte[] commandArr = new byte[command.size()];
//        for (int i = 0; i < commandArr.length; ++i)
//            commandArr[i] = command.get(i);
//        CommandProcessor.executeCommand(commandArr);
//        /* Dummy Test End - TESTED, IT WORKS*/
//        
//        /* Dummy removal */
//        byte[] remvCommand = new byte[3];
//        remvCommand[0] = CommandConstants.SCEHDULES_HEADER;
//        remvCommand[1] = CommandConstants.REMOVE_ALL_DUMMIES_FROM_ID;
//        remvCommand[2] = (byte) 24;
//        
//        CommandProcessor.executeCommand(remvCommand);
//        /* Dummy removal end - TESTED, WORKS*/
        
        try {
            serverSocket = new ServerSocket(PORT);
            initDataRefreshThread();
            infoReadThread.start();
        } catch (Exception e) {
            logger.info("Couldn't listen on port " + PORT);
            System.exit(-1);
        }
        
        while (true) {
            logger.info("Waiting for a client."); //TODO wait for the server to finish loading data then start waiting for a client
            Socket clientSocket = serverSocket.accept();
            Thread t = new ServerThread(clientSocket);
            t.start();
        }
    }

    public static void initDataRefreshThread(){
        Runnable runnable = () -> {
            try {
                while (true) { // Thread running in the background all the time
                    if (readDataFromThread)
                        DataFactory.loadData();
                    synchronized (Thread.currentThread()){
                        Thread.currentThread().sleep(refreshDelay);
                    }
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        };
        infoReadThread = new Thread(runnable, "DataRefreshThread");
    }
    
    public static void setRefreshDelay(Long milliseconds) {
        refreshDelay = milliseconds;
    }
    
    public static void setThreadState(Boolean state) {
        readDataFromThread = state;
    }
    
}
