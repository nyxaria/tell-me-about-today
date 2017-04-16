package UI;

import Modules.Main;

import javax.swing.*;
import java.awt.*;

public class UWrap extends UPanel {

    public ULabel title, content;
    public JPanel leftPane;
    int prevHeight = getPreferredSize().height;
    public JPanel listPane;
    private int delta = 0;

    public UWrap(BorderLayout layout, Color shadowColor, int shadowSize, float shadowOpacity, int cornerSize, boolean showTopShadow, boolean showLeftShadow, boolean showBottomShadow, boolean showRightShadow) {
        super(layout, shadowColor, shadowSize, shadowOpacity, cornerSize, showTopShadow, showLeftShadow, showBottomShadow, showRightShadow);
    }

    int titlePrevHeight = 0;
    public void changeTitle(String string) {
        if(string.contains("\n")) {
            if(!title.getParent().isVisible()) {
                title.getParent().setVisible(true);
            }
            title.setText("<html><body style='text-align:left;width: " + (Main.wrapTextWidth - 5) + "px'>" + string.split("\n")[1].trim().replace(".", ".<wbr>"));
            Main.leftScrollPane.getVerticalScrollBar().repaint();
            if(titlePrevHeight != title.getHeight()) {
                delta += title.getHeight() - titlePrevHeight;
            }
            titlePrevHeight = title.getHeight();
        }

    }

    int contentPrevHeight = 0;
    public void changeContent(String text) {
        if(!content.isVisible())
            content.setVisible(true);
        if(contentPrevHeight == 0) {
            contentPrevHeight = content.getHeight();
        }

        String[] cont = text.split("\n");
        text = "";
        int index = 3;
        if(cont.length > index) {
            while(text.equals("")) {
                text = cont[index++];
            }
        }

        content.setText("<html><body style='width: " + (Main.wrapTextWidth) + "px'>" +  text.replace(".", ".<wbr>"));
        Main.leftScrollPane.getVerticalScrollBar().repaint();
        if(contentPrevHeight != content.getHeight()) {
            delta += content.getHeight() - contentPrevHeight;
        }
        contentPrevHeight = content.getHeight();

    }

    public void updateText(String text) {
        if(Main.activeDay.textArea.initialising)
            return;
        changeTitle(text);
        changeContent(text);
        Main.leftScrollPane.resizing = true;
        Main.leftScrollPane.repaint();

        setPreferredSize(new Dimension((int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 7), title.getPreferredSize().height + content.getPreferredSize().height + 25));
        setMaximumSize(getPreferredSize());
        if(prevHeight != getPreferredSize().height && prevHeight != 0) {
            delta = getPreferredSize().height - prevHeight;
            Main.size += delta;
            leftPane.setPreferredSize(new Dimension(leftPane.getPreferredSize().width, (Main.size  >= Main.mainFrame.getHeight() - Main.navBarPane.getHeight() ? Main.size : Main.mainFrame.getHeight() - Main.navBarPane.getHeight() )));
            leftPane.repaint();
            leftPane.revalidate();
            Main.leftScrollPane.resizing = true;
            Main.leftScrollPane.repaint();
        }
        prevHeight = getPreferredSize().height;

    }
}
