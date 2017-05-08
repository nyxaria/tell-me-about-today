package UI;

import Modules.Main;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
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



        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ULabel.cogImage = ImageIO.read(Main.class.getResource("/res/FEZ-04-512.png"));
                    ULabel.cogImage = createOutlineImage(ULabel.imageToBufferedImage(ULabel.cogImage));
                    ULabel.cogImage = Thumbnails.of(ULabel.imageToBufferedImage(ULabel.cogImage)).size(15, 15).asBufferedImage();
                    Main.navBarPane.repaint();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        initSettings();
    }

    public static void initSettings() {
        if(!(new File(U.data).exists())) {
            new File(U.root).mkdirs();
            new File(U.data).mkdirs();
            try(PrintWriter out = new PrintWriter(U.settings)) {
                out.println("created=" + new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
                out.println("reminderTemplate=nWorkout`nMeditate`nRead for 30 minutes`nWrite diary entry and write about your day`nSpeak to one stranger`nChores`nadd reminder");
                out.println("theme=Light");
                out.println("titleTemplate=Title");
                out.println("dateTemplate=day, ddsuffix month year");
                out.println("textTemplate=What did you get up to today?");
                out.println("dateFormat=dd-MM-yyyy");
            } catch(FileNotFoundException e) {
                e.printStackTrace();
            }
            dateCreated = new Date();
            reminderTemplate = "nWorkout`nMeditate`nRead for 30 minutes`nWrite diary entry and write about your day`nSpeak to one stranger`nChores`nadd reminder";
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
        for(int i = 0; i < string.length() - key.length(); i++) {
            if(string.substring(i, i + key.length()).equals(key)) {
                count++;
            }
        }
        return count;
    }

    public static Area getOutline(BufferedImage bi) {
        GeneralPath gp = new GeneralPath();
        boolean cont = false;

        for(int xx = 0; xx < bi.getWidth(); xx++) {
            for(int yy = 0; yy < bi.getHeight(); yy++) {
                if(bi.getRGB(xx, yy) != 0) {
                    if(cont) {
                        gp.lineTo(xx, yy);
                        gp.lineTo(xx, yy + 1);
                        gp.lineTo(xx + 1, yy + 1);
                        gp.lineTo(xx + 1, yy);
                        gp.lineTo(xx, yy);
                    } else {
                        gp.moveTo(xx, yy);
                    }
                    cont = true;
                } else {
                    cont = false;
                }
            }
            cont = false;
        }

        gp.closePath();
        return new Area(gp);
    }

    public static BufferedImage createOutlineImage(BufferedImage image) throws Exception {
        Area area = getOutline(image);
        final BufferedImage result = new BufferedImage(18 * 8, 18 * 8, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        prettify(g);

        AffineTransform old = g.getTransform();
        AffineTransform tr2 = new AffineTransform(old);

        tr2.scale(18f / area.getBounds().getWidth() * 8, 18f / area.getBounds().getHeight() * 8);

        g.setTransform(tr2);

        g.setColor(new Color(60,60,60,100));

        g.setClip(area);

        g.setStroke(new BasicStroke(1));
        g.fillRect(0, 0, image.getWidth(), image.getHeight());

        g.setClip(0,0,image.getWidth(),image.getHeight());
        g.setColor(Color.gray);
        g.setStroke(new BasicStroke(1));

        g.draw(area);
        return result;
    }
}
