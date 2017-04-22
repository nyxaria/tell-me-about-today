package UI;

import Modules.Main;

import javax.swing.*;
import java.awt.*;

public class SettingsPane extends UPanel {
    private final String[] settings = {"Default Reminders", "Title Template", "Text Template", "American Date Format"};
    private UCheckBox dateCheckbox;

    public SettingsPane() {
        super();
        setLayout(new FlowLayout());
        setOpaque(false);
        setBackground(U.transparent);

        ((FlowLayout) getLayout()).setVgap(0);
        ((FlowLayout) getLayout()).setHgap(0);
        String htmlPrefix = "<html><body style='text-align:left;width: ";
        int size = 0;

        int wrapTextWidth = getWidth() * 3 / 5 - 30;
        for(String setting : settings) {
            UWrap wrap = new UWrap(new BorderLayout(), Color.BLACK, 5, 0.6F, 14, true, true, true, true);
            wrap.setBackground(new Color(48, 48, 48));

            ULabel description = new ULabel(htmlPrefix + (wrapTextWidth - 5) + setting + "px'>" + setting, U.Size.Tiny, U.Shape.Normal, false, U.Style.Plain);
            description.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
            description.inverted(true);

            String defaultVal = "";
            switch(setting) {
                case "Default Reminders":
                    defaultVal = Global.reminderTemplate.substring(1).replaceAll("`n", "\n");
                    break;
                case "Title Template":
                    defaultVal = Global.titleTemplate;
                    break;
                case "Text Template":
                    defaultVal = Global.textTemplate;
                    break;
                case "American Date Format":
                    defaultVal = Global.americanFormat ? "y" : "n";
                    UCheckBox checkbox = new UCheckBox();

                    checkbox.checked = defaultVal.charAt(0) == 'y';

                    checkbox.setPreferredSize(new Dimension(16, 16));
                    checkbox.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
                    dateCheckbox = checkbox;
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
            }
            UTextField content = new UTextField(defaultVal, 11, U.theme == U.Theme.Light ? Color.white : U.text, new Color(0, 0, 0, 0));

            if(!setting.equals("American Date Format")) {
                JPanel descWrap = new JPanel(new GridBagLayout());
                descWrap.add(description);
                descWrap.setOpaque(false);
                wrap.add(descWrap, BorderLayout.NORTH);

                wrap.add(content, BorderLayout.CENTER);
                int lineCount = Global.countOccurrences(defaultVal, "\n");
                content.lineCount = lineCount;
                content.setPreferredSize(new Dimension(getWidth(), 14 * lineCount));
                content.setFont(Global.plain.deriveFont((float) 11));
                wrap.opacity = 1f;
                content.wrap = wrap;
                content.wrap.listPane = this;
                content.setBackground(new Color(48, 48, 48));
                content.maxNumberOfCharacters = 300;
            } else {
                content.setPreferredSize(new Dimension(0, 0));
            }
            wrap.setPreferredSize(new Dimension((int) (Main.screen.getWidth() / 8) + 16, 30 + (int) content.getPreferredSize().getHeight()));
            wrap.setOpaque(false);
            wrap.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
            add(wrap);
            size += (int) wrap.getPreferredSize().getHeight();
        }
        setPreferredSize(new Dimension(getPreferredSize().width, size));

        when clicked again save settings

        //create form add at end of right pane et c
    }
}
