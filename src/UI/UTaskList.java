package UI;

import Modules.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class UTaskList extends UPanel {

    public ArrayList<UCheckBox> checkboxes;
    private UWrap wrap;

    public UTaskList() {
        setOpaque(false);
    }

    public void setList() {
        removeAll();
        ArrayList<String> reminders = Main.activeDay.reminders;
        checkboxes = new ArrayList<>();
        setBackground(U.tertiary);
        FlowLayout layout = new FlowLayout();
        layout.setVgap(0);
        setOpacity(0f);
        setLayout(layout);
        for(int i = 0; i < reminders.size(); i++) //foreach throws concurrentmodificationexception for some reason - something to do with implementation of lambda?
            addField(reminders.get(i));
        //


        SwingUtilities.invokeLater(() -> {
            new java.util.Timer().schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    expandingHeight = 1;
                    for(Component comp : wrap.listPane.getComponents()) {
                        expandingHeight += comp.getPreferredSize().height;
                    }
                    setPreferredSize(new Dimension(wrap.listPane.getWidth(), wrap.listPane.expandingHeight));
                    Main.taskScrollPane.repaint();
                }
            }, 400);
        });

        Main.settingUp = false;

    }

    public void addField(String reminder) {

        String text = reminder.substring(1, reminder.length()).trim();
        while(text.endsWith("\n") || text.endsWith(" ")) {
            text = text.substring(0, text.length() - 1);
        }

        wrap = new UWrap(new BorderLayout(), Color.BLACK, 5, 0.6F, 14, true, true, true, true);

        UTextField content = new UTextField(text, 11, U.theme == U.Theme.Light ? Color.white : U.text, new Color(0, 0, 0, 0));

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
            public void keyReleased(KeyEvent e) {
                Main.activeDay.reminders.add(content.reminderIndex, (checkbox.checked ? "y" : "n") + content.getText());
                Main.activeDay.reminders.remove(content.reminderIndex + 1);
            }

        });

        wrap.add(checkBoxWrap, BorderLayout.WEST);
        wrap.setPreferredSize(new Dimension((int) getPreferredSize().getWidth(), content.getPreferredSize().height + 13));
        wrap.listPane = this;
        content.wrap = wrap;
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 12));
        wrap.setBackground(new Color(48, 48, 52, 0));

        wrap.setOpacity(0);
        wrap.checkbox = checkbox;
        JPanel layer = new JPanel();
        layer.setLayout(new OverlayLayout(layer));
        layer.setOpaque(false);

        JPanel borderWrap = new JPanel(new BorderLayout());
        JPanel northWrap = new JPanel(new BorderLayout());

        UButton deleteButton = new UButton("icon:x_transparent", U.Size.Normal, U.Shape.Square);
        borderWrap.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 4));
        content.addMouseListener(deleteButton.getMouseListeners()[0]);
        northWrap.add(deleteButton, BorderLayout.NORTH);
        borderWrap.add(northWrap, BorderLayout.EAST);
        northWrap.setOpaque(false);
        borderWrap.setOpaque(false);

        content.deleteButton = deleteButton;

        deleteButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                Main.activeDay.reminders.remove(checkbox.reminderIndex);

                for(int i = checkbox.reminderIndex + 1; i < checkboxes.size(); i++) {
                    checkboxes.get(i).reminderIndex--;
                }
                checkboxes.remove(checkbox);

                remove(layer);
                repaint();
                Main.activeDay.save();
            }
        });

        layer.add(borderWrap);

        content.borderWrap = borderWrap;
        layer.add(wrap);

        if(reminder.trim().equals("nadd reminder")) {
            content.isAddReminder = true;
            borderWrap.setVisible(false);
            wrap.setVisible(true);
        }
        add(layer);
        SwingUtilities.invokeLater(content::init);

    }
}
