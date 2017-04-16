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
    public static Date dateCreated;

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
                out.println("reminderTemplate=nWorkout`nMeditate`Read for 30 minutes`nWrite diary entry for today`nSpeak to one stranger");
            } catch(FileNotFoundException e) {
                e.printStackTrace();
            }
            Global.dateCreated = new Date();
            Global.reminderTemplate = "nWorkout`nMeditate`nRead for 30 minutes`nWrite diary entry for today`nSpeak to one stranger`nChores";

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
                    }
                }
            } catch(ParseException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeSettings() {
        try(PrintWriter out = new PrintWriter(U.settings)) {
            out.println("created=" + Global.dateCreated);
            out.println("reminderTemplate="+Global.reminderTemplate);
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
}
