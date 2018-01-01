package Modules;

import UI.Global;
import UI.U;
import UI.UTextArea;
import UI.UWrap;

import javax.swing.text.*;
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
    public String text;
    public ArrayList<String> reminders;
    public UWrap wrap;
    public String plainText;

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
        if(new File(U.data + date + ".txt").exists()) {

            textArea.loadText(text);

        } else {
            String[] dateData = date.split("-");
            String dateFormatted = Global.dateTemplate.replace("dd", (dateData[0].startsWith("0") ? dateData[0].replace("0", "") : dateData[0])).
                    replace("MM", dateData[1]).
                    replace("yyyy", dateData[2]).
                    replace("year", dateData[2]).
                    replace("YY", dateData[2].substring(dateData[2].length() - 2, dateData[2].length())).
                    replace("day", day[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1]).
                    replace("month", new DateFormatSymbols().getMonths()[Integer.parseInt(dateData[1]) - 1]).
                    replace("suffix", getDayOfMonthSuffix(Integer.parseInt(dateData[0]) + 1));
            StyleContext sc = new StyleContext();
            Style center = sc.addStyle("center", null);
            center.addAttribute(StyleConstants.Alignment, StyleConstants.ALIGN_CENTER);
            StyleConstants.setUnderline(center, true);

            Style right = sc.addStyle("right", null);
            right.addAttribute(StyleConstants.Alignment, StyleConstants.ALIGN_RIGHT);
            StyleConstants.setUnderline(right, false);

            Style left = sc.addStyle("left", null);
            left.addAttribute(StyleConstants.Alignment, StyleConstants.ALIGN_LEFT);
            StyleConstants.setUnderline(left, false);

            StyledDocument doc = textArea.getStyledDocument();
            try {
                //doc.insertString(0," \n", right);
                doc.insertString(doc.getLength(), ""+dateFormatted + "", null);
                doc.setParagraphAttributes(0, doc.getLength(),right, false);

                doc.insertString(doc.getLength(), "\n" + Global.titleTemplate + "", null);
                doc.setParagraphAttributes(doc.getLength(), 1, center, false);
                doc.insertString(doc.getLength(), "\n", null);

                doc.setParagraphAttributes(doc.getLength(), 2, left, false);
                doc.insertString(doc.getLength(), Global.textTemplate, null);
            } catch(BadLocationException e) {
                e.printStackTrace();
            }
            text = textArea.getStyledText();
            try {
                plainText = textArea.getDocument().getText(0, textArea.getDocument().getLength());
            } catch(BadLocationException e) {
                e.printStackTrace();
            }
            reminders = new ArrayList(Arrays.asList(Global.reminderTemplate.split("`")));
            save();


        }
        textArea.repaint();
        textArea.updateUI();
        textArea.revalidate();
        textArea.updated();
        ;
        textArea.initialising = false;

    }

    public void save() {
        try(PrintWriter out = new PrintWriter(U.data + date + ".txt")) {
            String remindersFormatted = "";
            for(String s : reminders.toArray(new String[0])) {
                if(s.trim().equals("n") || s.trim().equals("y")) continue;
                remindersFormatted += s.trim() + "`";
            }
            remindersFormatted = remindersFormatted.substring(0, remindersFormatted.length() - 1).trim();
            while((remindersFormatted.length() - remindersFormatted.replace("`nadd reminder", "").length())/"`nadd reminder".length() > 1)
                remindersFormatted = remindersFormatted.substring(0, remindersFormatted.lastIndexOf("`nadd reminder")).trim();
            if(!remindersFormatted.endsWith("`nadd reminder")) remindersFormatted += "`nadd reminder";
            out.print("text=EQUALS=" + text + "|SPLIT|ptext=EQUALS=" + plainText + "|SPLIT|reminders=EQUALS=" + remindersFormatted);
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void initialize() throws IOException {
        if(new File(U.data + date + ".txt").exists()) {
            try(BufferedReader br = new BufferedReader(new FileReader(U.data + date + ".txt"))) {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while(line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                String everything = sb.toString();
                for(String s : everything.split("\\|SPLIT\\|")) {
                    String type = s.substring(0, s.indexOf("=EQUALS="));
                    String data = s.substring(s.indexOf("=EQUALS=") + "=EQUALS=".length(), s.length());

                    switch(type) {
                        case "text":
                            text = data;
                            break;
                        case "ptext":
                            plainText = data;
                            break;
                        case "reminders":
                            reminders = new ArrayList(Arrays.asList(data.split("`")));
                    }
                }
            }
        }
        Main.activeDays.add(this);
    }

    static String getDayOfMonthSuffix(int n) {
        if(--n == 1) return "st";
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
