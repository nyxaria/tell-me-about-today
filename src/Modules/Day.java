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
    private boolean creating;
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
            SimpleAttributeSet center = new SimpleAttributeSet();
            StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);

            SimpleAttributeSet right = new SimpleAttributeSet();
            StyleConstants.setAlignment(right, StyleConstants.ALIGN_RIGHT);

            SimpleAttributeSet left = new SimpleAttributeSet();
            StyleConstants.setAlignment(left, StyleConstants.ALIGN_LEFT);

            StyledDocument doc = textArea.getStyledDocument();
            try {
                doc.insertString(doc.getLength(), dateFormatted + "\n", null);
                doc.setParagraphAttributes(0, doc.getLength(), right, false);

                doc.insertString(doc.getLength(), "" + Global.titleTemplate + "", null);
                doc.setParagraphAttributes(doc.getLength(), 1, center, false);
                doc.insertString(doc.getLength(), "\n", null);

                doc.setParagraphAttributes(doc.getLength(), 2, left, false);
                doc.insertString(doc.getLength(), Global.textTemplate, null);
            } catch(BadLocationException e) {
                e.printStackTrace();
            }
            textArea.updated();
            text = textArea.getStyledText();
            plainText = textArea.getText();
            reminders = new ArrayList(Arrays.asList(Global.reminderTemplate.split("`")));
            save();

            SimpleAttributeSet aSet = new SimpleAttributeSet();
            StyleConstants.setLineSpacing(aSet, 1);
            doc.setParagraphAttributes(0, doc.getLength(), aSet, false);

        }
        textArea.initialising = false;

    }

    public void save() {
        try(PrintWriter out = new PrintWriter(U.data + date + ".txt")) {
            String remindersFormatted = "";
            for(String s : reminders.toArray(new String[0])) {
                remindersFormatted += s + "`";
            }
            remindersFormatted = remindersFormatted.substring(0, remindersFormatted.length() - 1);
            out.print("text=" + text + "|ptext=" + plainText + "|reminders=" + remindersFormatted);
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
                for(String s : everything.split("\\|")) {
                    String type = s.substring(0, s.indexOf("="));
                    String data = s.substring(s.indexOf("=") + 1, s.length());

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
            //textArea.setText(text);
        }
        Main.activeDays.add(this);
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
