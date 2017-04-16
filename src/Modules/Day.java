package Modules;

import UI.Global;
import UI.U;
import UI.UTextArea;
import UI.UWrap;

import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.io.*;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Created by gedr on 01/03/2017.
 */
public class Day {
    public UTextArea textArea;
    String date;
    private String[] day = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private String[] vanillaReminders = {"Drink more water", "Watch your posture", "Express your gratitude to someone", "Meditate for 5 minutes", "Clean up working space", "Dance to your favorite song!"};
    public String text;
    public ArrayList<String> reminders;
    public UWrap wrap;
    private float opacity = 0;

    public Day(String date, UTextArea writingZone) {
        this.date = date;
        this.textArea = writingZone;
        try {
            initialize();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void prioritize() {

        textArea.setText("");
        textArea.initialising = true;
        //textArea.setDocument(new DefaultStyledDocument());

        String[] lines = text.split("\n");
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);

        SimpleAttributeSet right = new SimpleAttributeSet();
        StyleConstants.setAlignment(right, StyleConstants.ALIGN_RIGHT);

        SimpleAttributeSet left = new SimpleAttributeSet();
        StyleConstants.setAlignment(left, StyleConstants.ALIGN_LEFT);

        StyledDocument doc = textArea.getStyledDocument();
        try {
            if(lines.length > 0) {
                doc.insertString(doc.getLength(), lines[0] + "\n", null);
                doc.setParagraphAttributes(0, doc.getLength(), right, false);
            }
            if(lines.length > 1) {
                doc.insertString(doc.getLength(), "" + lines[1] + "", null);
                doc.setParagraphAttributes(doc.getLength(), 1, center, false);
                doc.insertString(doc.getLength(), "\n\n", null);
            }

            if(lines.length > 3) {
                doc.setParagraphAttributes(doc.getLength(), 3, left, false);
                for(int i = 3; i < lines.length; i++) {
                    doc.insertString(doc.getLength(), lines[i] + (i != lines.length - 1 ? "\n" : ""), null);
                }
            }
        } catch(BadLocationException e) {
            e.printStackTrace();
        }
        textArea.updated();
        textArea.initialising = false;



    }

    public void save() {
        try(PrintWriter out = new PrintWriter(U.data + date + ".txt")) {
            String remindersFormatted = "";
            for(String s : reminders.toArray(new String[0])) {
                remindersFormatted += s + "`";
            }
            remindersFormatted = remindersFormatted.substring(0, remindersFormatted.length() - 1);
            out.print("text=" + text + "|reminders=" + remindersFormatted);
            //System.out.println(text);
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void initialize() throws IOException {
        if(!new File(U.data + date + ".txt").exists()) { //creating for the first time
            try(PrintWriter out = new PrintWriter(U.data + date + ".txt")) {
                String[] dateData = date.split("-");
                String dateFormatted = (dateData[0].startsWith("0") ? dateData[0].replace("0", "") : dateData[0]) + getDayOfMonthSuffix(Integer.parseInt(dateData[0]) + 1) + " " + new DateFormatSymbols().getMonths()[Integer.parseInt(dateData[1]) - 1] + " " + dateData[2];
                out.print("text=" + day[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1] + ", " + dateFormatted + "\nTitle\n\nWhat did you enjoy the most today?\nWhat were you feeling throughout the day?\nWhy were you feeling this day?\nDid you accomplish something you are proud of?\nWas there some area that you felt you could improve on?\nDid you improve someone's life today?\nDid you get out of your comfort zone?|reminders=" + Global.reminderTemplate);
            }
            initialize();
        } else {
            try(BufferedReader br = new BufferedReader(new FileReader(U.data + date + ".txt"))) {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while(line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                String everything = sb.toString();
                for(String s : everything.split("\\|")) {
                    String type = s.substring(0, s.indexOf("="));
                    String data = s.substring(s.indexOf("=") + 1, s.length());

                    switch(type) {
                        case "text":
                            text = data;
                            break;
                        case "reminders":
                            reminders = new ArrayList(Arrays.asList(data.split("`")));
                    }
                }
            }
            //textArea.setText(text);
        }
        Main.activeDays.add(this);
        textArea.updated();
    }

    static String getDayOfMonthSuffix(int n) {
        n--;
        if(n == 1) return "st";
        if(n >= 11 && n <= 13) return "th";
        switch(n % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }
}
