 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.olympicat.scheduleupdates;

import com.olympicat.scheduleupdates.serverdatarecievers.ScheduleChange;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 *
 * @author Avishay
 */
public class ServerThread extends Thread {
    static int num = 0;
    Socket client;
    
    public ServerThread(Socket client) {
        this.client = client;
    }
    
    public void run() {
        ObjectOutputStream is;
        ObjectInputStream in;
        Integer classID;
        try {
            if (Main.LOG)
                Main.logger.info("Client is in!");
            
            is = new ObjectOutputStream(client.getOutputStream()); 
            in = new ObjectInputStream(client.getInputStream());          
            classID = (Integer) in.readObject();
            System.out.println("Recieved request for id " + classID);
            if (DataFactory.classesChanges.get(classID) != null) {
                ScheduleChange[] su = DataFactory.classesChanges.get(classID);
                ScheduleChange[] newSu = null;
                if (Main.dummyChanges.get(classID) != null) { // If there are additional changes in the dummies map at the classID
                    newSu = new ScheduleChange[su.length + Main.dummyChanges.get(classID).length]; // this shit is necessary because I need to change the size of the array to add the dummies
                    Integer index = 0;
                    for (ScheduleChange regChange : DataFactory.classesChanges.get(classID)) {
                        newSu[index] = regChange;
                        index++;
                    }
                    
                    for (ScheduleChange dummy : Main.dummyChanges.get(classID)) {
                        newSu[index] = dummy;
                        index++;
                        
                    }
                } else
                    newSu = su; // in case there are no dummies
                if (Main.LOG)
                    Main.logger.info("Sent with size " + newSu.length + "\n" + newSu[0].getTeacherName());
                is.writeObject(newSu);
                if (Main.LOG)
                    Main.logger.info("Object sent for ID " + classID + " to " + client.getInetAddress().getHostAddress());
            }
            else {
                is.writeObject(new ScheduleChange[0]);
                if (Main.LOG)
                    Main.logger.info("Sent empty object.");
            }

            is.close();
            in.close();
            client.close();
            
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
   
