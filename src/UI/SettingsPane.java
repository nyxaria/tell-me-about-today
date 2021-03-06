package UI;

import Modules.Main;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.util.ArrayList;

public class SettingsPane extends UPanel {
    private final String[] settings = {"Default Reminders", "Title Template", "Text Template", "American Date Format"};

    public ArrayList<JComponent> dataFields = new ArrayList<>();

    public SettingsPane() {
        super();
        setLayout(new FlowLayout());
        transparent = true;
        //        setOpaque(false);
        ((FlowLayout) getLayout()).setVgap(0);
        ((FlowLayout) getLayout()).setHgap(0);
        String htmlPrefix = "<html><body style='text-align:left;width: ";
        Color background = U.theme == U.Theme.Light ? new Color(48, 48, 48) : new Color(240, 240, 240);
        setBackground(background);

        int wrapTextWidth = getWidth() * 3 / 5 - 30;

        for(String setting : settings) {
            UWrap wrap = new UWrap(new BorderLayout(), Color.BLACK, 5, 0.6F, 14, true, true, true, true);
            wrap.setBackground(background);

            ULabel description = new ULabel(htmlPrefix + (wrapTextWidth - 5) + setting + "px'>" + setting, U.Size.Tiny, U.Shape.Normal, false, U.Style.Plain);
            description.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
            description.inverted(U.theme == U.Theme.Light);
            wrap.title = description;
            String defaultVal = "";
            switch(setting) {
                case "Default Reminders":
                    defaultVal = Global.reminderTemplate.substring(1, Global.reminderTemplate.lastIndexOf('`')).replaceAll("`n", "\n");
                    break;
                case "Title Template":
                    defaultVal = Global.titleTemplate;
                    break;
                case "Text Template":
                    defaultVal = Global.textTemplate;
                    break;
                case "American Date Format":
                    UCheckBox checkbox = new UCheckBox();

                    checkbox.checked = Global.americanFormat;

                    checkbox.setPreferredSize(new Dimension(16, 16));
                    checkbox.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
                    JPanel checkBoxWrap = new JPanel(new GridBagLayout());
                    checkBoxWrap.setOpaque(false);
                    checkBoxWrap.add(checkbox);
                    description.setBorder(BorderFactory.createEmptyBorder(0, 1, 1, 0));
                    wrap.add(description, BorderLayout.WEST);

                    wrap.add(checkBoxWrap, BorderLayout.EAST);
                    Runnable doAssist = () -> {
                        checkbox.mouseReleased(null);
                        checkbox.mouseReleased(null);
                    };
                    SwingUtilities.invokeLater(doAssist);
                    dataFields.add(checkbox);
                    checkbox.setName(setting);
                    checkbox.wrap = wrap;
                    wrap.title = description;
                    checkbox.inverted(true);

            }
            UTextField content = new UTextField("", 11f, U.theme == U.Theme.Light ? Color.white : new Color(58, 58, 58), new Color(0, 0, 0, 0));

            if(!setting.equals("American Date Format")) {
                content.setContentType("text");
                wrap.listPane = this;
                content.wrap = wrap;
                content.inverted = true;

                try {
                    content.getDocument().insertString(0, defaultVal, null);
                } catch(BadLocationException e) {
                    e.printStackTrace();
                }
                JPanel descWrap = new JPanel(new GridBagLayout());
                descWrap.add(description);
                descWrap.setOpaque(false);
                wrap.add(descWrap, BorderLayout.NORTH);

                wrap.add(content, BorderLayout.CENTER);
                int lineCount = Global.countOccurrences(defaultVal, "\n");
                content.setPreferredSize(new Dimension(getWidth(), lineCount * 16));

                content.lineCount = (!setting.equals("Default Reminders") ? 1 : lineCount + 1);
                wrap.opacity = 1f;
                content.setBackground(background);
                content.maxNumberOfCharacters = 300;
                content.setName(setting);
                dataFields.add(content);
            } else {
                content.setPreferredSize(new Dimension(0, 0));
            }
            wrap.setPreferredSize(new Dimension((int) (Main.screen.getWidth() / 8) + 16, 30 + (int) content.getPreferredSize().getHeight()));
            wrap.setOpaque(false);
            wrap.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
            add(wrap);
            expandingHeight += (int) wrap.getPreferredSize().getHeight();
        }

        setOpacity(0.0f);
        Main.settingUpSettings = false;
        setPreferredSize(new Dimension(Main.screen.width / 8, expandingHeight));

    }

    public void active(boolean b) {
        for(JComponent comp : dataFields) {
            comp.setEnabled(b);
        }
    }


    public void updateSettings() {
        for(JComponent comp : dataFields) {
            comp.setEnabled(false);
            switch(comp.getName()) {
                case "Default Reminders":
                    Global.reminderTemplate = "n" + ((UTextField) comp).getText().trim().replaceAll("\n", "`n") + "`nadd reminder";
                    break;
                case "Title Template":
                    Global.titleTemplate = ((UTextField) comp).getText();
                    break;
                case "Text Template":
                    Global.textTemplate = ((UTextField) comp).getText();
                    break;
                case "American Date Format":
                    Global.americanFormat = ((UCheckBox) comp).checked;
                    if(Global.americanFormat) Global.dateFormat = "MM-dd-yyyy";
                    else Global.dateFormat = "dd-MM-yyyy";
                    break;
            }
        }
        Global.writeSettings();
    }

}
