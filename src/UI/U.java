package UI;

import Modules.Main;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;

/**
 * Created by gedr on 19/02/2017.
 */
public class U {
    public static final String root = "tellmeabouttoday/";
    public static final String data = root + "data/";
    public static final String settings = root + "settings.txt";

    public enum Theme {Dark, Light}

    public static Theme theme = Theme.Dark;

    public enum Size {Tiny, Smaller, Small, Normal, Big, Medium, Bigger}

    public enum Style {Plain, Itallic, Bold}

    public enum Shape {Normal, Square}

    public static Color primary;
    public static Color secondary;
    public static Color tertiary;
    public static Color accent = new Color(48, 48, 60);
    public static Color toolbar = new Color(128, 128, 134);
    public static Color text = new Color(240, 240, 240);
    public static Color transparent = new Color(0, 0, 0, 0);
    public static Color border = new Color(38, 38, 38);

    public static void dark() {
        theme = Theme.Dark;
        primary = new Color(40, 40, 42);
        tertiary = new Color(88, 88, 93); // new Color(78, 78, 83);
        secondary = new Color(48, 48, 52);
        text = new Color(240, 240, 240);
        accent = new Color(48, 48, 60);
        toolbar = new Color(128,128,135);

        Main.navBarPane.setBackground(toolbar);
        Main.navBarTitle.inverted(true);

        UTextArea.shadowOpacity = 0.8f;
        Main.writingZone.setForeground(text);
        Main.writingZone.setUpStyles();
        Main.writingZone.updated();
        Main.textToolBar.setBackground(new Color(200,200,200));


        TranslucentScrollBar.dragging = new Color(150, 150, 150, 150);
        TranslucentScrollBar.hover = new Color(150, 150, 150, 140);
        TranslucentScrollBar.unactive = new Color(130, 130, 130, 55);


        if(Main.leftPane != null) {
            Main.leftPane.setBackground(tertiary);
            Main.rightPane.setBackground(tertiary);
            Main.rightScrollPane.setBackground(tertiary);
            Main.taskList.setBackground(tertiary);
            if(Main.taskList.checkboxes != null) for(UCheckBox check : Main.taskList.checkboxes) {
                check.content.setBackground(tertiary);
                if(check.content.wrap.opacity==1f)
                    check.content.setForeground(text);
                check.content.setSelectedTextColor(text);
                StyleContext sc = StyleContext.getDefaultStyleContext();
                UTextField.strike = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, text);
                UTextField.strike = sc.addAttribute(SimpleAttributeSet.EMPTY, "strike-color", new Color(180, 180, 180, 225));

                check.mouseReleased(null);
                check.mouseReleased(null);
            }

        }
        if(Main.settingsPane!=null) {
            for(JComponent comp : Main.settingsPane.dataFields) {
                if(comp instanceof UTextField) {
                    ((UTextField) comp).wrap.setBackground(new Color(240, 240, 240));
                    comp.setBackground(new Color(240, 240, 240));
                    ((UTextField) comp).wrap.title.inverted(false);
                    comp.setForeground(new Color(58, 58, 58));
                    ((UTextField) comp).setDisabledTextColor(comp.getForeground());
                } else if(comp instanceof UCheckBox) {
                    ((UCheckBox) comp).wrap.setBackground(new Color(240, 240, 240));
                    ((UCheckBox) comp).wrap.title.inverted(false);
                }
            }
            Main.settingsPane.setBackground(new Color(240,240,240));
        }

            for(UWrap wrap : Main.activeWraps) {
            wrap.setBackground(secondary);
            if(wrap.content != null) {
                wrap.content.inverted(true);
                wrap.title.inverted(true);
                wrap.date.inverted(true);
            }
        }

        if(Main.activeDay != null) Main.activeDay.wrap.setBackground(accent);
        if(Main.mainFrame.getMostRecentFocusOwner() != null) {
            if (Main.mainFrame.getMostRecentFocusOwner() instanceof UTextField) {
                if (!((UTextField) Main.mainFrame.getMostRecentFocusOwner()).isAddReminder) {
                    Main.mainFrame.getMostRecentFocusOwner().requestFocus();
                }
            } else {
                Main.mainFrame.getMostRecentFocusOwner().requestFocus();
            }
        }


    }

    public static void light() {
        theme = Theme.Light;
        primary = new Color(255 - 40, 255 - 40, 255 - 42);
        secondary = new Color(255 - 48, 255 - 48, 255 - 52);
        tertiary = new Color(255 - 88, 255 - 88, 255 - 93); // new Color(78, 78, 83);
        text = new Color(10, 10, 10);
        accent = new Color(255 - 48, 255 - 48, 255 - 38);
        toolbar = new Color(230,230,230);

        Main.navBarPane.setBackground(toolbar);
        Main.navBarTitle.inverted(false);

        UTextArea.shadowOpacity = 0.5f;
        Main.writingZone.setForeground(text);
        Main.writingZone.setUpStyles();
        Main.writingZone.updated();
        Main.textToolBar.setBackground(new Color(80,80,90));


        TranslucentScrollBar.dragging = new Color(100,100,100,180);
        TranslucentScrollBar.hover = new Color(100,100,100,160);
        TranslucentScrollBar.unactive = new Color(100,100,100,90);

        if(Main.leftPane != null) {
            Main.leftPane.setBackground(tertiary);
            Main.rightPane.setBackground(tertiary);
            Main.rightScrollPane.setBackground(tertiary);
            Main.taskList.setBackground(tertiary);
            if(Main.taskList != null && Main.taskList.checkboxes != null) for(UCheckBox check : Main.taskList.checkboxes) {
                check.content.setBackground(tertiary);
                if(check.content.wrap.opacity==1f)
                    check.content.setForeground(new Color(255, 255, 255));
                check.content.setSelectedTextColor(new Color(255,255,255));
                check.content.setSelectionColor(new Color(220,220,220,135));
                StyleContext sc = StyleContext.getDefaultStyleContext();
                UTextField.strike = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, new Color(255, 255, 255));
                UTextField.strike = sc.addAttribute(SimpleAttributeSet.EMPTY, "strike-color", new Color(110,110,110, 205));

                check.mouseReleased(null);
                check.mouseReleased(null);
            }
        }
        for(UWrap wrap : Main.activeWraps) {
            wrap.setBackground(secondary);
            if(wrap.content != null) {
                wrap.content.inverted(false);
                wrap.title.inverted(false);
                wrap.date.inverted(false);
            }
        }

        if(Main.settingsPane!=null) {
            for(JComponent comp : Main.settingsPane.dataFields) {
                if(comp instanceof UTextField) {
                    ((UTextField) comp).wrap.setBackground(new Color(48, 48, 48));
                    ((UTextField) comp).wrap.title.inverted(true);
                    comp.setForeground(new Color(240, 240, 240));
                    ((UTextField) comp).setDisabledTextColor(comp.getForeground());
                    comp.setBackground(new Color(48, 48, 48));
                } else if(comp instanceof UCheckBox) {
                    ((UCheckBox) comp).wrap.setBackground(new Color(48, 48, 48));
                    ((UCheckBox) comp).wrap.title.inverted(true);
                }
            }
            Main.settingsPane.setBackground(new Color(48, 48, 48));
        }

        if(Main.activeDay != null) Main.activeDay.wrap.setBackground(accent);
        theme = Theme.Light;
        if(Main.mainFrame.getMostRecentFocusOwner() != null) {
            if (Main.mainFrame.getMostRecentFocusOwner() instanceof UTextField) {
                if (!((UTextField) Main.mainFrame.getMostRecentFocusOwner()).isAddReminder) {
                    Main.mainFrame.getMostRecentFocusOwner().requestFocus();
                }
            } else {
                Main.mainFrame.getMostRecentFocusOwner().requestFocus();
            }
        }
    }

}
