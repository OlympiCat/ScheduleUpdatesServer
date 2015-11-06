/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.olympicat.scheduleupdates;

import com.olympicat.scheduleupdates.serverdatarecievers.ScheduleChange;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.olympicat.scheduleupdates.serverdatarecievers.SubTeacher;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataFactory {
    public static Map<Integer, ScheduleChange[]> classesChanges;
    private static Integer[] classesID = {3, 5, 6, 7, 8, 9, 10, 11, 38, 39, 40, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 41, 36}; //3 = y1; 13 = ya1; 22 = yb1;

    /**
     * Load the school changes data and saves them
     * @throws IOException
     */
    public static void loadData() throws IOException {
        final String pageURL = "http://deshalit.iscool.co.il/default.aspx";
        Pattern ptrn = Pattern.compile("<td class=\"MsgCell.+\\s+.+"); //filter through all page
        Pattern ptrn2 = Pattern.compile("[\\d.]+.+"); //just for the information line
        Matcher matcher = null;
        String xml = "";
        
        WebClient webClient = new WebClient(BrowserVersion.INTERNET_EXPLORER_11);
        HtmlPage page = webClient.getPage(pageURL);
        
        classesChanges = new HashMap<Integer, ScheduleChange[]>(); // Reset all the data everytime the func is called to avoid duplicate data
        for (Integer classID : classesID) {
            ScriptResult result = page.executeJavaScript("document.getElementById('dnn_ctr11396_TimeTableView_ClassesList').value=" + classID +";");
            page = (HtmlPage) result.getNewPage();
            result = page.executeJavaScript("__doPostBack('dnn$ctr11396$TimeTableView$btnChanges','');");
            page = (HtmlPage) result.getNewPage();

            xml = page.asXml();

            matcher = ptrn.matcher(xml);

            List<String> matches = new ArrayList<String>();
            while (matcher.find()) {
                matches.add(matcher.group());
            }

            ListIterator itr = matches.listIterator();
            while(itr.hasNext()) {
                Object element = itr.next();
                matcher = ptrn2.matcher((String)element);
                matcher.find();
                itr.set(matcher.group());
            }

            matches.stream().forEach(s -> addToMap(classID, s.split(", ")));
        }
        webClient.close();
        
       ScheduleChange[] arr = {new ScheduleChange("07.10.15", "שעה 5" ,"ניב וינשטוק", ScheduleChange.ChangeType.CANCELLED)};
//       addToMap(24, new String[] {"18.10.2015", "שעה 7", "ניב וינשטוק", "ביטול שעור"});
//       addToMap(24, new String[] {"18.10.2015", "8", "אנגל המלך", "ביטול שעור"});
//       addToMap(18, new String[] {"07.10.15", "שעה 5", "7841", "ביטול שעור"});
//                                new ScheduleChange("08.10.15", "שעה 3".charAt("שעה 5".length()-1) - '0' ,"יוסי אבוטבול", ScheduleChange.ChangeType.CANCELLED)
//                                                                                                                                                            };      
        System.out.println("Finished reading data.");
    }
    
    public static void addToMap(Integer id, String[] info) {
        System.out.println("Reading info for classID " + id);
        ScheduleChange[] changes;
        if (classesChanges.get(id) == null)
            changes = new ScheduleChange[0];
        else  
            changes = classesChanges.get(id);
       
        ScheduleChange[] changes_ = new ScheduleChange[changes.length + 1];
        int index = 0;
        for (ScheduleChange change : changes) {
            changes_[index] = change;
            index++;
        }
        changes_[index] = (new ScheduleChange(info[0], info[1].substring(info[1].length()-1) ,info[2], info[3].equals("ביטול שעור") ? ScheduleChange.ChangeType.CANCELLED : ScheduleChange.ChangeType.SUB));
        if (changes_[index].getType() == ScheduleChange.ChangeType.SUB)
            changes_[index].setSubTeacher(info[4].substring(11));
        classesChanges.put(id, changes_);
    }
}
