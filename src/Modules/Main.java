package Modules;

import UI.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static UI.U.*;

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
    public static JPanel navBarPane;
    public static JPanel leftPane;
    public static UTextArea writingZone;
    public static UTaskList taskList;
    public static TranslucentScrollBar leftScrollPane;
    public static TranslucentScrollBar taskScrollPane;
    public static TranslucentScrollBar rightScrollPane;


    public static boolean isMac;
    private static boolean navBarClicked;
    private static boolean settingsView;
    public static int wrapTextWidth;
    public static int size = 0;
    public static Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    private static Point mouseCache = new Point();
    private static Point initialPosition;

    public static ArrayList<Day> activeDays = new ArrayList<>();
    public static Day activeDay;
    public static UPanel rightPane;
    public static ArrayList<UWrap> activeWraps = new ArrayList<>();
    public static ULabel navBarTitle;

    private static JFrame settingsFrame;
    public static boolean settingUp;
    public static boolean scrollingSettings;
    public static SettingsPane settingsPane;

    public static void main(String args[]) {
        UIManager.put("List.lockToPositionOnScroll", Boolean.FALSE);
        try {
            Global.init();
            initiate();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    private static void initiate() throws IOException {
        mainFrame = new JFrame();
        mainFrame.setUndecorated(true);
        mainFrame.setBackground(transparent);
        mainFrame.setBounds((int) (screen.getWidth() / 6), (int) (screen.getHeight() / 7), (int) (screen.getWidth() * 4 / 6), (int) (screen.getHeight() * 5 / 7));


        mainPane = new JPanel();
        mainPane.setBackground(transparent);

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

        UButton exitButton = new UButton("icon:x", Size.Small, U.Shape.Square, true);
        exitButton.setBackground(transparent);
        exitButton.addMouseListener(new MouseListener() {
            @Override public void mouseClicked(MouseEvent e) { exit(); }
            @Override public void mousePressed(MouseEvent e) {}
            @Override public void mouseReleased(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e) {}
            @Override public void mouseExited(MouseEvent e) {}
        });
        navBarPane.add(exitButton, alignmentArg);

        JPanel toolbarWrap = new JPanel();
        //toolbarWrap.setLayout(new BoxLayout(toolbarWrap, BoxLayout.X_AXIS));
        ((FlowLayout) toolbarWrap.getLayout()).setVgap(0);
        ((FlowLayout) toolbarWrap.getLayout()).setHgap(0);

        UButton settingsButton = new UButton("icon:settings", Size.Small, U.Shape.Square, true);
        settingsButton.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(!scrollingSettings) {
                    if(settingsView) {
                        rightScrollPane.scrollTo(0f);
                        settingsView = false;
                        settingsPane.updateSettings();
                    } else {
                        //settingsPane.resize();
                        rightScrollPane.scrollTo(1f);
                        settingsPane.active(true);
                        settingsView = true;
                    }
                }
            }

            @Override public void mousePressed(MouseEvent e) {}
            @Override public void mouseReleased(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e) {}
            @Override public void mouseExited(MouseEvent e) {}
        });

        UButton imageButton = new UButton("icon:image_picture", Size.Small, U.Shape.Square, true);
        imageButton.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(U.theme == Theme.Dark) {
                    U.light();
                } else {
                    U.dark();
                }

                Global.writeSettings();
            }

            @Override public void mousePressed(MouseEvent e) {}
            @Override public void mouseReleased(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e) {}
            @Override public void mouseExited(MouseEvent e) {}
        });

        imageButton.setPreferredSize(new Dimension(20,20));
        settingsButton.setPreferredSize(new Dimension(20,20));

        imageButton.setOpaque(false);
        settingsButton.setOpaque(false);
        toolbarWrap.setOpaque(false);

        toolbarWrap.add(imageButton);
        toolbarWrap.add(settingsButton);
        navBarPane.add(toolbarWrap, alignmentArg.equals(BorderLayout.WEST) ? BorderLayout.EAST : BorderLayout.WEST);

        navBarPane.setBackground(toolbar);
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
        navBarTitle = new ULabel("Tell Me About Today", Size.Smaller, U.Shape.Normal, false, Style.Plain);
        navBarTitle.setPreferredSize(new Dimension(navBarTitle.getPreferredSize().width, 20));
        navBarTitle.setBorder(BorderFactory.createEmptyBorder(1,0,0,0));
        navBarTitle.inverted(true);
        navBarTitle.setForeground(text);
        centerWrap.add(navBarTitle);
        navBarPane.add(centerWrap, BorderLayout.CENTER);
        centerWrap.setOpaque(false);

        UPanel centerPane = new UPanel(new BorderLayout());

        writingZone = new UTextArea();

        if(U.theme == Theme.Dark)
            U.dark();
        else
            U.light();


        leftPane = new JPanel();
        ((FlowLayout) leftPane.getLayout()).setVgap(0);
        leftPane.setBackground(tertiary);


        leftPane.setPreferredSize(new Dimension((int) (screen.getWidth() / 7), 300));
        wrapTextWidth = leftPane.getPreferredSize().width - 63;

        populateLeftPane();

        settingsPane = new SettingsPane();

        rightPane = new UPanel(new BorderLayout());
        taskList = new UTaskList();
        taskList.setBackground(tertiary);
        taskList.setOpaque(true);
        taskList.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0, border));
        JPanel gridWrap = new JPanel(new BorderLayout());
        JPanel flowWrap = new JPanel(new FlowLayout(FlowLayout.LEFT));
        gridWrap.add(taskList, BorderLayout.NORTH);
        rightPane.add(settingsPane, BorderLayout.SOUTH);

        flowWrap.add(taskList);
        flowWrap.setPreferredSize(new Dimension(100,100));
        taskScrollPane = new TranslucentScrollBar(taskList, true);
        taskScrollPane.setOpaque(true);
        rightPane.add(taskScrollPane, BorderLayout.NORTH);
        rightPane.setBackground(tertiary);

        leftScrollPane = new TranslucentScrollBar(leftPane,true);

        leftPane.setPreferredSize(new Dimension((int) screen.getWidth()/7 - 20, size + 1));

        UPanel mainPane = new UPanel();
        mainPane.setLayout(new OverlayLayout(mainPane));



        mainPane.add(new TranslucentScrollBar(writingZone, true));



        rightScrollPane = new TranslucentScrollBar(rightPane, false);
        rightPane.setPreferredSize(new Dimension((int) (screen.getWidth() / 8), mainFrame.getHeight() - navBarPane.getPreferredSize().height + settingsPane.getPreferredSize().height));
        centerPane.add(rightScrollPane, BorderLayout.EAST);
        centerPane.add(mainPane, BorderLayout.CENTER);
        centerPane.add(leftScrollPane, BorderLayout.WEST);

        Main.mainPane.add(navBarPane, BorderLayout.NORTH);
        Main.mainPane.add(centerPane, BorderLayout.CENTER);
        taskList.setPreferredSize(new Dimension(taskList.getPreferredSize().width, 0));
        taskScrollPane.setPreferredSize(new Dimension(taskList.getPreferredSize().width, (mainFrame.getHeight() - navBarPane.getPreferredSize().height)));
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

    private static void populateLeftPane() {
        String htmlPrefix = "<html><body style='text-align:left;width: ";

        for(int i = 0; i < TimeUnit.DAYS.convert(new Date().getTime() - Global.dateCreated.getTime(), TimeUnit.MILLISECONDS) + 1; i++) {

            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            c.add(Calendar.DATE, -i);  // number of days to add
            UWrap wrap = new UWrap(new BorderLayout(), Color.BLACK, 5, 0.6F, 14, false, true, true, true);
            activeWraps.add(wrap);
            if(new File(data + new SimpleDateFormat("dd-MM-yyyy").format(c.getTime()) + ".txt").exists()) {
                Day day = new Day(format(c.getTime()), writingZone);
                activeDays.add(day);
                String text = "";
                String[] cont = day.plainText.split("\n");
                int index = 2;
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
                    date = new ULabel("Yesterday", Size.Tiny, U.Shape.Normal, false, Style.Plain);
                    date.setVerticalAlignment(SwingConstants.TOP);
                    date.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 8));
                    title = new ULabel(htmlPrefix + (wrapTextWidth - 5) + "px'>" + titleText.trim().replace(".", ".<wbr>"), Size.Smaller, U.Shape.Normal, false, Style.Plain);
                } else if(format(new Date()).equals(format(c.getTime()))) { //+"Today - "
                    date = new ULabel("Today", Size.Tiny, U.Shape.Normal, false, Style.Plain);
                    date.setVerticalAlignment(SwingConstants.TOP);
                    date.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 8));
                    title = new ULabel(htmlPrefix + (wrapTextWidth - 5) + "px'>" + titleText.trim().replace(".", ".<wbr>"), Size.Smaller, U.Shape.Normal, false, Style.Plain);
                } else {
                    String[] dateData = format(c.getTime()).split("-");
                    String dateFormatted = new DateFormatSymbols().getMonths()[Integer.parseInt(dateData[1]) - 1] + " " + (dateData[0].startsWith("0") ? dateData[0].replace("0", "") : dateData[0]) + Day.getDayOfMonthSuffix(Integer.parseInt(dateData[0]) + 1) ;
                    date = new ULabel(dateFormatted, Size.Tiny, U.Shape.Normal, false, Style.Plain);
                    date.setVerticalAlignment(SwingConstants.TOP);
                    date.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 7));
                    title = new ULabel(htmlPrefix + (wrapTextWidth - 5) + "px'>" + titleText.trim().replace(".", ".<wbr>"), Size.Smaller, U.Shape.Normal, false, Style.Plain);
                }
                wrap.date = date;
                JPanel topWrap = new JPanel(new BorderLayout());
                JPanel titleWrap = new JPanel();

                if(titleText.equals(""))
                    titleWrap.setVisible(false);

                titleWrap.setBorder(BorderFactory.createEmptyBorder(8, 4, 0, 10));
                title.setVerticalAlignment(SwingConstants.BOTTOM);
                title.setBackground(transparent);
                titleWrap.add(title);
                topWrap.add(titleWrap, BorderLayout.WEST);
                topWrap.add(date, BorderLayout.EAST);

                topWrap.setOpaque(false);
                wrap.add(topWrap, BorderLayout.NORTH);
                titleWrap.setOpaque(false);
                JPanel contentWrap = new JPanel(new BorderLayout());
                contentWrap.setOpaque(false);
                ULabel content = new ULabel("<html><body style='width: " + (wrapTextWidth) + "px'>" + text.split("\n")[0].replace(".", ".<wbr>"), Size.Tiny, U.Shape.Normal, false, Style.Plain);

                contentWrap.add(content, BorderLayout.NORTH);
                contentWrap.setBorder(BorderFactory.createEmptyBorder(0, 6, 5, 3));
                wrap.add(contentWrap, BorderLayout.CENTER);
                wrap.setPreferredSize(new Dimension((int) (screen.getWidth() / 7 - 2 - screen.getWidth()/7%2), title.getPreferredSize().height + content.getPreferredSize().height + 25));

                leftPane.add(wrap);

                boolean inverted = U.theme == Theme.Dark;
                title.inverted(inverted);
                content.inverted(inverted);
                date.inverted(inverted);

                MouseListener clickListener = new MouseListener() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if(e.getXOnScreen() > wrap.getLocationOnScreen().getX() + 5 && e.getXOnScreen() < wrap.getLocationOnScreen().getX() + wrap.getWidth() - 5 && e.getYOnScreen() > wrap.getLocationOnScreen().getY() + 5 && e.getYOnScreen() < wrap.getLocationOnScreen().getY() + wrap.getHeight() - 5) {
                            if(settingUp) return;
                            if(wrap.getBackground().equals(accent)) return;
                            settingUp = true;
                            if(activeDay != null) {
                                activeDay.wrap.setBackground(secondary);
                                activeDay.text = activeDay.textArea.getStyledText();
                                activeDay.plainText = activeDay.textArea.getText();
                                activeDay.save();
                            }
                            wrap.setBackground(accent);
                            leftScrollPane.repaint();
                            day.textArea.opacity = 0;
                            new Thread(() -> {
                                activeDay = day;
                                taskList.repaint();
                                day.prioritize();
                                day.textArea.start();

                                Main.taskList.setList();
                                Main.taskList.repaint();
                            }).start();
                            activeDays.add(day);

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

            } else if(new SimpleDateFormat("dd-MM-yyyy").format(c.getTime()).equals(new SimpleDateFormat("dd-MM-yyyy").format(new Date()))) {
                ULabel date = new ULabel("Today", Size.Tiny, U.Shape.Normal, false, Style.Plain);
                wrap.date = date;
                date.setVerticalAlignment(SwingConstants.TOP);
                date.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 8));

                ULabel title = new ULabel(htmlPrefix + (wrapTextWidth - 5) + "px'>" + "Add entry", Size.Smaller, U.Shape.Normal, false, Style.Plain);

                JPanel topWrap = new JPanel(new BorderLayout());
                JPanel titleWrap = new JPanel();

                titleWrap.setBorder(BorderFactory.createEmptyBorder(8, 4, 0, 10));
                title.setVerticalAlignment(SwingConstants.BOTTOM);
                titleWrap.add(title);


                JPanel contentWrap = new JPanel(new BorderLayout());
                contentWrap.setOpaque(false);
                ULabel content = new ULabel("<html><body style='width: " + (wrapTextWidth) + "px'>" + "", Size.Tiny, U.Shape.Normal, false, Style.Plain);

                contentWrap.add(content, BorderLayout.NORTH);
                contentWrap.setBorder(BorderFactory.createEmptyBorder(0, 6, 5, 0));
                wrap.add(contentWrap, BorderLayout.CENTER);

                topWrap.add(titleWrap, BorderLayout.WEST);
                topWrap.add(date, BorderLayout.EAST);
                content.setVisible(false);
                topWrap.setOpaque(false);
                wrap.add(topWrap, BorderLayout.NORTH);
                titleWrap.setOpaque(false);
                wrap.setPreferredSize(new Dimension((int) (screen.getWidth() / 7 - 2 - screen.getWidth()/7%2), title.getPreferredSize().height +  24));
                leftPane.add(wrap);

                boolean inverted = U.theme == Theme.Dark;
                title.inverted(inverted);
                content.inverted(inverted);
                date.inverted(inverted);

                MouseListener clickListener = new MouseListener() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if(e.getXOnScreen() > wrap.getLocationOnScreen().getX() + 5 && e.getXOnScreen() < wrap.getLocationOnScreen().getX() + wrap.getWidth() - 5 && e.getYOnScreen() > wrap.getLocationOnScreen().getY() + 5 && e.getYOnScreen() < wrap.getLocationOnScreen().getY() + wrap.getHeight() - 5) {
                            if(settingUp) return;
                            settingUp = true;
                            if(activeDay != null) {
                                activeDay.wrap.setBackground(secondary);
                                activeDay.text = activeDay.textArea.getStyledText();
                                activeDay.plainText = activeDay.textArea.getText();
                                activeDay.save();
                            }

                            Day today = new Day(format(new Date()), writingZone);
                            today.textArea.opacity = 0;
                            today.wrap = wrap;
                            wrap.setBackground(accent);
                            leftScrollPane.repaint();
                            activeDays.add(today);
                            activeDay = today;

                            new Thread(() -> {
                                today.prioritize();
                                today.textArea.start();
                                wrap.updateText(today.textArea.getText());
                                taskList.revalidate();
                                taskList.repaint();
                                taskList.setList();
                                settingUp = false;

                            }).start();
                            new Thread(() -> {

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
            //          x add task function
            //          x settings button in tool bar
            //            add daily plan top right
            //            add basic formatting

            wrap.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            wrap.setBackground(secondary);
            wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
            wrap.setName(format(c.getTime()));
        }
    }

    private static void exit() {
        if(activeDay != null) {
            activeDay.text = activeDay.textArea.getStyledText();
            activeDay.plainText = activeDay.textArea.getText();
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




