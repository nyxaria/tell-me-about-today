package UI;

import Modules.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * Created by gedr on 26/03/2017.
 */
public class UTaskList extends UPanel {

    public ArrayList<UCheckBox> checkboxes;


    public UTaskList() {
        setOpaque(false);
    }

    public void setList() {
        removeAll();
        ArrayList<String> reminders = Main.activeDay.reminders;
        checkboxes = new ArrayList<UCheckBox>();
        //setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(U.tertiary);
        FlowLayout layout = (FlowLayout) getLayout();
        layout.setVgap(0);

        int wrapTextWidth = getWidth() - 30;
        for(String reminder : reminders) {
            UWrap wrap = new UWrap(new BorderLayout(), Color.BLACK, 5, 0.6F, 14, true, true, true, true);
            String reminderFormatted = reminder.substring(1, reminder.length()).trim();
            while(reminderFormatted.endsWith("\n") || reminderFormatted.endsWith(" ")) {
                reminderFormatted = reminderFormatted.substring(0, reminderFormatted.length() - 1);
            }

            UTextField content = new UTextField(reminderFormatted, 11, U.theme == U.Theme.Light ? Color.white : U.text, new Color(0, 0, 0, 0));

            wrap.add(content, BorderLayout.CENTER);
            UCheckBox checkbox = new UCheckBox();

            checkbox.checked = reminder.charAt(0) == 'y';
            checkbox.content = content;

            checkbox.setPreferredSize(new Dimension(16, 16));
            checkbox.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
            checkboxes.add(checkbox);
            checkbox.reminderIndex = checkboxes.indexOf(checkbox);
            content.reminderIndex = checkbox.reminderIndex;
            JPanel checkBoxWrap = new JPanel(new GridBagLayout());
            checkBoxWrap.setOpaque(false);
            checkBoxWrap.add(checkbox);
            content.setPreferredSize(new Dimension(getWidth() - checkBoxWrap.getPreferredSize().width, content.getPreferredSize().height));

            content.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    Main.activeDay.reminders.add(content.reminderIndex, (checkbox.checked ? "y" : "n") + content.getText());
                    Main.activeDay.reminders.remove(content.reminderIndex + 1);
                }
            });

            wrap.add(checkBoxWrap, BorderLayout.WEST);
            wrap.setPreferredSize(new Dimension(getWidth(), content.getPreferredSize().height + 13));
            wrap.listPane = this;
            content.wrap = wrap;
            wrap.setOpaque(false);
            wrap.setOpacity(0);
            wrap.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 12));
            wrap.setBackground(new Color(48, 48, 52, 0));
            add(wrap);

            Runnable doAssist = () -> {
                checkbox.mouseReleased(null);
                checkbox.mouseReleased(null);
            };
            SwingUtilities.invokeLater(doAssist);

        }

    }
}
