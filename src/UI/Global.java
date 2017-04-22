package UI;

import java.awt.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by gedr on 14/01/2017.
 */
public class Global {
    public static Font plain;
    public static Font bold;
    public static Font itallic;

    public static String reminderTemplate;
    public static String titleTemplate;
    public static String dateTemplate;
    public static String textTemplate;

    public static Date dateCreated;
    public static String dateFormat;
    public static boolean americanFormat;

    public static void init() throws Exception {
        InputStream istream = Global.class.getResourceAsStream("/res/Cantarell-Regular.ttf");
        plain = Font.createFont(Font.TRUETYPE_FONT, istream);
        istream = Global.class.getResourceAsStream("/res/Cantarell-Bold.ttf");
        bold = Font.createFont(Font.TRUETYPE_FONT, istream);
        istream = Global.class.getResourceAsStream("/res/Cantarell-Oblique.ttf");
        itallic = Font.createFont(Font.TRUETYPE_FONT, istream);

        initSettings();
    }

    public static void initSettings() {
        if(!(new File(U.data).exists())) {
            new File(U.root).mkdirs();
            new File(U.data).mkdirs();
            try(PrintWriter out = new PrintWriter(U.settings)) {
                out.println("created=" + new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
                out.println("reminderTemplate=nWorkout`nMeditate`nRead for 30 minutes`nWrite diary entry for today`nSpeak to one stranger");
                out.println("theme=Light");
                out.println("titleTemplate=Title");
                out.println("dateTemplate=day, ddsuffix month year");
                out.println("textTemplate=What did you get up to today?");
                out.println("dateFormat=dd-MM-yyyy");
            } catch(FileNotFoundException e) {
                e.printStackTrace();
            }
            dateCreated = new Date();
            reminderTemplate = "nWorkout`nMeditate`nRead for 30 minutes`nWrite a diary entry for today`nSpeak to one stranger`nChores";
            U.theme = U.Theme.Light;
            titleTemplate = "Title";
            dateTemplate = "day, ddsuffix month yyyy";
            textTemplate = "What did you get up to today?";
            dateFormat = "dd-MM-yyyy";

        } else {
            try(BufferedReader br = new BufferedReader(new FileReader(U.root + "settings.txt"))) {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while(line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                String everything = sb.toString();
                for(String s : everything.split("\n")) {
                    String type = s.substring(0, s.indexOf('='));
                    String data = s.substring(s.indexOf('=') + 1, s.length());

                    switch(type) {
                        case "created":
                            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
                            dateCreated = format.parse(data);
                            break;
                        case "reminderTemplate":
                            reminderTemplate = data;
                            break;
                        case "theme":
                            U.theme = U.Theme.valueOf(data);
                            break;
                        case "titleTemplate":
                            titleTemplate = data;
                            break;
                        case "dateTemplate":
                            dateTemplate = data;
                            break;
                        case "textTemplate":
                            textTemplate = data;
                            break;
                        case "dateFormat":
                            dateFormat = data;
                            break;
                    }
                }
            } catch(ParseException | IOException e) {
                e.printStackTrace();
            }
        }

        if(dateFormat.replace("/", "-").replace(" ", "-").equals("dd-MM-yyyy")) {
            americanFormat = false;
        } else {
            americanFormat = true;
        }
    }

    public static void writeSettings() {
        try(PrintWriter out = new PrintWriter(U.settings)) {
            out.println("created=" + new SimpleDateFormat("dd-MM-yyyy").format(Global.dateCreated));
            out.println("reminderTemplate=" + Global.reminderTemplate);
            out.println("theme=" + U.theme.toString());
            out.println("titleTemplate=" + Global.titleTemplate);
            out.println("dateTemplate=" + Global.dateTemplate);
            out.println("textTemplate=" + Global.textTemplate);
            out.println("dateFormat=" + Global.dateFormat);
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void prettify(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    public static int countOccurrences(String string, String key) {
        int count = 1;
        for(int i = 0; i < string.length()-key.length(); i++) {
            if(string.substring(i,i+key.length()).equals(key)) {
                count++;
            }
        }
        return count;
    }
}
