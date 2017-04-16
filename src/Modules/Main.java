package Modules;

import UI.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by gedr on 14/01/2017
 * A program to record your every day life, aspirations, emotions, ect.
 */

public class Main {

    /**
     * Goals:
     * - Create appealing UI
     * - Save Text/Load Text
     * - Calendar system
     */

    public static JFrame mainFrame;
    public static JPanel mainPane;
    public static boolean isMac;
    private static boolean navBarClicked;
    private static Point mouseCache = new Point();
    private static Point initialPosition;
    private static UTextArea writingZone;
    public static ArrayList<Day> activeDays = new ArrayList<>();
    public static Day activeDay;
    public static TranslucentScrollBar leftScrollPane;
    public static UTaskList taskList;


    public static int wrapTextWidth;
    public static JPanel navBarPane;
    public static int size = 0;
    public static TranslucentScrollBar taskScrollPane;

    public static void main(String args[]) {
        try {
            Global.init();
        } catch(Exception e) {
            e.printStackTrace();
        }
        try {
            initiate();
        } catch(IOException e) {
            e.printStackTrace();
        }

    }


    private static void initiate() throws IOException {
        mainFrame = new JFrame();
        mainFrame.setUndecorated(true);
        mainFrame.setBackground(new Color(0, 0, 0, 0));
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        mainFrame.setBounds((int) (screen.getWidth() / 6), (int) (screen.getHeight() / 7), (int) (screen.getWidth() * 4 / 6), (int) (screen.getHeight() * 5 / 7));


        mainPane = new JPanel();
        mainPane.setBackground(new Color(0,0,0,0));

        mainFrame.add(mainPane);
        mainPane.setLayout(new BorderLayout());

        navBarPane = new JPanel() { //rounded corners
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHints(qualityHints);
                g2.setPaint(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight()/2 + 3, 6, 6);
                g2.fillRect(0, getHeight()/2, getWidth(), getHeight()/2);

                g2.dispose();
            }
        };
        navBarPane.setOpaque(false);
        navBarPane.setLayout(new BorderLayout());
        String alignmentArg = BorderLayout.EAST;
        if(System.getProperty("os.name").toLowerCase().contains("mac")) {
            alignmentArg = BorderLayout.WEST;
            isMac = true;
        }

        UButton exitButton = new UButton("icon:x", U.Size.Small, U.Shape.Square, true);
        exitButton.setBackground(new Color(0,0,0,0));
        exitButton.addMouseListener(new MouseListener() {
            @Override public void mouseClicked(MouseEvent e) { exit(); }
            @Override public void mousePressed(MouseEvent e) {}
            @Override public void mouseReleased(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e) {}
            @Override public void mouseExited(MouseEvent e) {}
        });
        navBarPane.add(exitButton, alignmentArg);

        UButton imageButton = new UButton("icon:image_picture", U.Size.Small, U.Shape.Square, true);
        imageButton.setEnabled(false);

        navBarPane.add(imageButton, alignmentArg.equals(BorderLayout.WEST) ? BorderLayout.EAST : BorderLayout.WEST);

        navBarPane.setBackground(new Color(128, 128, 134));
        navBarPane.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if(navBarClicked) {
                    mainFrame.setLocation((int) (initialPosition.getX() - mouseCache.getX() + e.getXOnScreen()), (int) (initialPosition.getY() - mouseCache.getY() + e.getYOnScreen()));
                }
            }

            @Override public void mouseMoved(MouseEvent e) {}
        });
        navBarPane.addMouseListener(new MouseListener() {
            @Override
            public void mousePressed(MouseEvent e) {
                navBarClicked = true;
                initialPosition = mainFrame.getLocationOnScreen();
                mouseCache = e.getLocationOnScreen();

            }

            @Override public void mouseReleased(MouseEvent e) { navBarClicked = false; }
            @Override public void mouseClicked(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e) {}
            @Override public void mouseExited(MouseEvent e) {}
        });

        JPanel centerWrap = new JPanel(new GridBagLayout());
        ULabel navBarTitle = new ULabel("Tell Me About Today", U.Size.Smaller, U.Shape.Normal, false, U.Style.Plain);
        navBarTitle.setPreferredSize(new Dimension(navBarTitle.getPreferredSize().width, 20));
        navBarTitle.setBorder(BorderFactory.createEmptyBorder(1,0,0,0));
        navBarTitle.inverted(true);
        navBarTitle.setForeground(new Color(240, 240, 240));
        centerWrap.add(navBarTitle);
        navBarPane.add(centerWrap, BorderLayout.CENTER);
        centerWrap.setOpaque(false);

        UPanel centerPane = new UPanel(new BorderLayout());

        JPanel leftPane = new JPanel();
        ((FlowLayout) leftPane.getLayout()).setVgap(0);

        leftPane.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(38, 38, 38)));
        leftPane.setBackground(new Color(78, 78, 83));

        writingZone = new UTextArea("");

        leftPane.setPreferredSize(new Dimension((int) (screen.getWidth() / 7), 300));
        wrapTextWidth = leftPane.getPreferredSize().width - 63;
        String htmlPrefix = "<html><body style='text-align:left;width: ";

        for(int i = 0; i < TimeUnit.DAYS.convert(new Date().getTime() - Global.dateCreated.getTime(), TimeUnit.MILLISECONDS) + 1; i++) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            c.add(Calendar.DATE, -i);  // number of days to add
            UWrap wrap = new UWrap(new BorderLayout(), Color.BLACK, 5, 0.6F, 14, false, true, true, true);

            if(new File(U.data + sdf.format(c.getTime()) + ".txt").exists()) {
                Day day = new Day(sdf.format(c.getTime()), writingZone);
                activeDays.add(day);
                String text = "";
                String[] cont = day.text.split("\n");
                int index = 3;
                if(cont.length > index) {
                    while(text.equals("")) {
                        if(cont.length > index) {
                            text = cont[index++];
                        } else {
                            return;
                        }
                    }
                }
                String titleText = "";
                if(cont.length >= 1) {
                    titleText = cont[1];
                }

                Calendar c1 = Calendar.getInstance();
                c1.add(Calendar.DAY_OF_YEAR, -1);
                Calendar c2 = Calendar.getInstance();

                c2.setTime(c.getTime());

                ULabel title, date;
                if(c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) //yesterday
                        && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)) { //"Yesterday - "
                    date = new ULabel("Yesterday", U.Size.Tiny, U.Shape.Normal, false, U.Style.Plain);
                    date.setVerticalAlignment(SwingConstants.TOP);
                    date.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 8));
                    title = new ULabel(htmlPrefix + (wrapTextWidth - 5) + "px'>" + titleText.trim().replace(".", ".<wbr>"), U.Size.Smaller, U.Shape.Normal, false, U.Style.Plain);
                } else if(format(new Date()).equals(sdf.format(c.getTime()))) { //+"Today - "
                    date = new ULabel("Today", U.Size.Tiny, U.Shape.Normal, false, U.Style.Plain);
                    date.setVerticalAlignment(SwingConstants.TOP);
                    date.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 8));
                    title = new ULabel(htmlPrefix + (wrapTextWidth - 5) + "px'>" + titleText.trim().replace(".", ".<wbr>"), U.Size.Smaller, U.Shape.Normal, false, U.Style.Plain);
                } else {
                    String[] dateData = sdf.format(c.getTime()).split("-");
                    String dateFormatted = (dateData[0].startsWith("0") ? dateData[0].replace("0", "") : dateData[0]) + Day.getDayOfMonthSuffix(Integer.parseInt(dateData[0]) + 1) + " of " + new DateFormatSymbols().getMonths()[Integer.parseInt(dateData[1]) - 1];
                    date = new ULabel(dateFormatted, U.Size.Tiny, U.Shape.Normal, false, U.Style.Plain);
                    date.setVerticalAlignment(SwingConstants.TOP);
                    date.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 7));
                    title = new ULabel(htmlPrefix + (wrapTextWidth - 5) + "px'>" + titleText.trim().replace(".", ".<wbr>"), U.Size.Smaller, U.Shape.Normal, false, U.Style.Plain);
                }

                date.inverted(true);
                title.inverted(true);
                JPanel topWrap = new JPanel(new BorderLayout());
                JPanel titleWrap = new JPanel();

                if(titleText.equals(""))
                    titleWrap.setVisible(false);

                titleWrap.setBorder(BorderFactory.createEmptyBorder(8, 4, 0, 10));
                title.setVerticalAlignment(SwingConstants.BOTTOM);
                title.setBackground(new Color(0,0,0,0));
                titleWrap.add(title);
                topWrap.add(titleWrap, BorderLayout.WEST);
                topWrap.add(date, BorderLayout.EAST);

                topWrap.setOpaque(false);
                wrap.add(topWrap, BorderLayout.NORTH);
                titleWrap.setOpaque(false);
                JPanel contentWrap = new JPanel(new BorderLayout());
                contentWrap.setOpaque(false);
                ULabel content = new ULabel("<html><body style='width: " + (wrapTextWidth) + "px'>" + text.split("\n")[0].replace(".", ".<wbr>"), U.Size.Tiny, U.Shape.Normal, false, U.Style.Plain);

                content.inverted(true);

                contentWrap.add(content, BorderLayout.NORTH);
                contentWrap.setBorder(BorderFactory.createEmptyBorder(0, 6, 5, 3));
                wrap.add(contentWrap, BorderLayout.CENTER);
                wrap.setPreferredSize(new Dimension((int) (screen.getWidth() / 7 - 2 - screen.getWidth()/7%2), title.getPreferredSize().height + content.getPreferredSize().height + 25));
                leftPane.add(wrap);

                MouseListener clickListener = new MouseListener() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if(e.getXOnScreen() > wrap.getLocationOnScreen().getX() + 5 && e.getXOnScreen() < wrap.getLocationOnScreen().getX() + wrap.getWidth() - 5 && e.getYOnScreen() > wrap.getLocationOnScreen().getY() + 5 && e.getYOnScreen() < wrap.getLocationOnScreen().getY() + wrap.getHeight() - 5) {

                            if(activeDay != null) {
                                activeDay.wrap.setBackground(new Color(48, 48, 52));
                                activeDay.text = activeDay.textArea.getText();
                                activeDay.save();
                            }
                            wrap.setBackground(new Color(48, 48, 60));
                            leftScrollPane.repaint();
                            day.textArea.opacity = 0;
                            new Thread(() -> {
                                day.prioritize();
                                day.textArea.start();
                            }).start();
                            activeDays.add(day);
                            activeDay = day;
                            new Thread(() -> {
                                taskList.repaint();
                                taskList.setList();
                            }).start();

                        }
                    }

                    @Override public void mouseClicked(MouseEvent e) {}
                    @Override public void mouseReleased(MouseEvent e) {}
                    @Override public void mouseEntered(MouseEvent e) {}
                    @Override public void mouseExited(MouseEvent e) {}
                };

                content.addMouseListener(clickListener);
                title.addMouseListener(clickListener);
                wrap.addMouseListener(clickListener);
                wrap.leftPane = leftPane;
                wrap.title = title;
                wrap.content = content;
                day.wrap = wrap;

            } else if(sdf.format(c.getTime()).equals(format(new Date()))) {
                ULabel date = new ULabel("Today", U.Size.Tiny, U.Shape.Normal, false, U.Style.Plain);
                date.setVerticalAlignment(SwingConstants.TOP);
                date.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 8));

                ULabel title = new ULabel(htmlPrefix + (wrapTextWidth - 5) + "px'>" + "Add entry", U.Size.Smaller, U.Shape.Normal, false, U.Style.Plain);

                date.inverted(true);
                title.inverted(true);
                JPanel topWrap = new JPanel(new BorderLayout());
                JPanel titleWrap = new JPanel();

                titleWrap.setBorder(BorderFactory.createEmptyBorder(8, 4, 0, 3));
                title.setVerticalAlignment(SwingConstants.BOTTOM);
                titleWrap.add(title);


                JPanel contentWrap = new JPanel(new BorderLayout());
                contentWrap.setOpaque(false);
                ULabel content = new ULabel("<html><body style='width: " + (wrapTextWidth) + "px'>" + "", U.Size.Tiny, U.Shape.Normal, false, U.Style.Plain);
                content.inverted(true);

                contentWrap.add(content, BorderLayout.NORTH);
                contentWrap.setBorder(BorderFactory.createEmptyBorder(0, 6, 5, 0));
                wrap.add(contentWrap, BorderLayout.CENTER);

                topWrap.add(titleWrap, BorderLayout.WEST);
                topWrap.add(date, BorderLayout.EAST);
                content.setVisible(false);
                topWrap.setOpaque(false);
                wrap.add(topWrap, BorderLayout.NORTH);
                titleWrap.setOpaque(false);
                wrap.setPreferredSize(new Dimension((int) (screen.getWidth() / 7 - 2 - screen.getWidth()/7%2), title.getPreferredSize().height +  25));
                leftPane.add(wrap);


                MouseListener clickListener = new MouseListener() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if(e.getXOnScreen() > wrap.getLocationOnScreen().getX() + 5 && e.getXOnScreen() < wrap.getLocationOnScreen().getX() + wrap.getWidth() - 5 && e.getYOnScreen() > wrap.getLocationOnScreen().getY() + 5 && e.getYOnScreen() < wrap.getLocationOnScreen().getY() + wrap.getHeight() - 5) {
                            if(activeDay != null) {
                                activeDay.wrap.setBackground(new Color(48, 48, 52));
                                activeDay.text = activeDay.textArea.getText();
                                activeDay.save();
                            }

                            Day today = new Day(format(new Date()), writingZone);
                            today.textArea.opacity = 0;
                            today.wrap = wrap;
                            wrap.setBackground(new Color(48, 48, 60));
                            leftScrollPane.repaint();
                            activeDays.add(today);
                            activeDay = today;

                            new Thread(() -> {
                                today.prioritize();
                                today.textArea.start();
                                wrap.updateText(today.textArea.getText());

                            }).start();
                            new Thread(() -> {
                                taskList.revalidate();
                                taskList.setList();
                            }).start();
                        }
                    }

                    @Override public void mouseClicked(MouseEvent e) {}
                    @Override public void mouseReleased(MouseEvent e) {}
                    @Override public void mouseEntered(MouseEvent e) {}
                    @Override public void mouseExited(MouseEvent e) {}
                };
                title.addMouseListener(clickListener);
                wrap.addMouseListener(clickListener);

                wrap.title = title;
                wrap.content = content;
                wrap.leftPane = leftPane;
            }
            size += wrap.getPreferredSize().height;

            wrap.setOpaque(false);

//          x create updatable tasks,
//            add task function
//            command line esque
//            settings button in tool bar
//            add daily plan top right

            wrap.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            wrap.setBackground(new Color(48, 48, 52));
            wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
            wrap.setName(sdf.format(c.getTime()));
        }

        UPanel rightPane = new UPanel(new BorderLayout());
        taskList = new UTaskList();
        taskList.setBackground(new Color(88, 88, 93));
        taskList.setOpaque(true);
        taskList.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0, new Color(38, 38, 38)));
        JPanel gridWrap = new JPanel(new BorderLayout());
        JPanel flowWrap = new JPanel(new FlowLayout(FlowLayout.LEFT));
        gridWrap.add(taskList, BorderLayout.NORTH);
        flowWrap.add(taskList);
        flowWrap.setPreferredSize(new Dimension(100,100));
        taskScrollPane = new TranslucentScrollBar(taskList);
        taskScrollPane.setOpaque(true);
        taskScrollPane.setBackground(new Color(88, 88, 93));
        rightPane.add(taskScrollPane, BorderLayout.NORTH);
        rightPane.setBackground(new Color(88, 88, 93));
        rightPane.setPreferredSize(new Dimension((int) (screen.getWidth() / 8), rightPane.getPreferredSize().height));

        leftScrollPane = new TranslucentScrollBar(leftPane);

        leftPane.setPreferredSize(new Dimension((int) screen.getWidth()/7 - 20, size + 1));

        UPanel mainPane = new UPanel(new BorderLayout());

        mainPane.add(writingZone, BorderLayout.CENTER);

        centerPane.add(rightPane, BorderLayout.EAST);
        centerPane.add(mainPane, BorderLayout.CENTER);
        centerPane.add(leftScrollPane, BorderLayout.WEST);

        Main.mainPane.add(navBarPane, BorderLayout.NORTH);
        Main.mainPane.add(centerPane, BorderLayout.CENTER);
        taskList.setPreferredSize(new Dimension(taskList.getPreferredSize().width, 0));
        taskScrollPane.setPreferredSize(new Dimension(taskList.getPreferredSize().width, (mainFrame.getHeight() - navBarPane.getPreferredSize().height)*2/5));
        mainFrame.setVisible(true);

        leftPane.setPreferredSize(new Dimension((int) leftPane.getPreferredSize().getWidth(), size));

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate((Runnable) () -> {
            if(activeDay != null) {
                for(Day day : activeDays) {
                    day.save();
                }
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    //handle force quuitting

    private static void exit() {
        if(activeDay != null) {
            activeDay.text = activeDay.textArea.getText();
            for(Day day : activeDays) {
                day.save();
            }
        }
        mainFrame.setVisible(false);
        System.exit(0);
    }

    public static String format(Date date) {
        return new SimpleDateFormat("dd-MM-yyyy").format(date);
    }
}




