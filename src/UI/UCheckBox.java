package UI;

import Modules.Main;

import javax.swing.*;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Created by gedr on 01/04/2017.
 */
public class UCheckBox extends JComponent implements MouseListener {
    public boolean checked;
    private boolean mouseDown;

    private Color pressedColor = new Color(60, 60, 65, 0);
    private Color backgroundColor = new Color(80, 80, 85, 0);
    private Color lineColor = new Color(200, 200, 210);
    public int reminderIndex;
    public UTextField content;
    public UWrap wrap;
    private boolean inverted;

    public UCheckBox() {
        addMouseListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        Global.prettify(g2);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int inset = 4;

        if(mouseDown) g2.setColor(backgroundColor);
        else g2.setColor(pressedColor);

        g2.fillRect(inset, inset, getWidth() - inset * 2, getHeight() - inset * 2);

        g2.setColor(lineColor.darker());
        //draw box
        g2.drawLine(inset, inset, getWidth() - inset, inset);
        g2.drawLine(getWidth() - inset, inset, getWidth() - inset, getHeight() - inset);
        g2.drawLine(getWidth() - inset, getHeight() - inset, inset, getHeight() - inset);
        g2.drawLine(inset, getHeight() - inset, inset, inset);

        //check
        if(checked) {
            if(inverted) g2.setColor(U.theme == U.Theme.Dark ? new Color(58,58,58) : new Color(210, 210, 210));
            else g2.setColor(U.theme == U.Theme.Light ? new Color(255, 255, 255) : new Color(210, 210, 210));
            g2.drawLine(inset + inset / 4 + 1, getHeight() - inset - 4, inset + inset / 2 + 1, getHeight() - inset - 2);
            g2.drawLine(inset + inset / 2 + 1, getHeight() - inset - 2, getWidth() - inset * 3 / 2 + inset / 3 + 1, inset + inset / 3 - inset * 2 / 3 + 1);

            //            g2.fillRect(inset + 1, inset + 1, getWidth() - inset*2 - 2, getHeight() - inset*2 - 2);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        mouseDown = true;
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        checked = !checked;
        repaint();

        if(content == null || content.getText().equals("add reminder") || content.isAddReminder) return;
        Main.activeDay.reminders.add(reminderIndex, (checked ? "y" : "n") + content.getText());
        Main.activeDay.reminders.remove(reminderIndex + 1);
        mouseDown = false;
        StyledDocument doc = content.getStyledDocument();

        if(checked) {
            doc.setCharacterAttributes(0, content.getText().length(), content.strike, false);
            content.setEnabled(false);
        } else {
            doc.setCharacterAttributes(0, content.getText().length(), doc.getStyle("normal"), true);
            content.setEnabled(true);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {
        mouseDown = false;
        repaint();
    }

    public void inverted(boolean b) {
        inverted = b;
    }
}
