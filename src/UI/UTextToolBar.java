package UI;

import Modules.Main;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UTextToolBar extends UPanel implements MouseListener, MouseMotionListener {

    private final int parentWidth;
    private final int parentHeight;
    private boolean navBarClicked = false;
    private Point mouseCache;

    private float idleOpacity = 0.4f;
    public UTextToolBar() {
        //super(new BorderLayout());

        int width = 66;
        setSize(width, 48);
        setOpacity(idleOpacity);

        parentWidth = (int) (Main.screen.width * ((2f / 3) - (1f / 8) - (1f / 7)) - 13);
        parentHeight = (int) (Main.screen.getHeight() * 5f / 7) - Main.navBarPane.getPreferredSize().height + 1;
        setBounds(parentWidth - getWidth() - 5, parentHeight - getHeight() - 5, getWidth(), getHeight());

        UPanel verticalWrap = new UPanel();
        verticalWrap.setLayout(new BoxLayout(verticalWrap, BoxLayout.Y_AXIS));

        //aligners

        UPanel textAlignmentWrap = new UPanel();
        textAlignmentWrap.setLayout(new BoxLayout(textAlignmentWrap, BoxLayout.X_AXIS));

        UButton alignLeft = new UButton("icon:image_left-alignment", U.Size.Small, U.Shape.Square, true);
        UButton alignCenter = new UButton("icon:image_center-alignment", U.Size.Small, U.Shape.Square, true);
        UButton alignRight = new UButton("icon:image_right-alignment", U.Size.Small, U.Shape.Square, true);

        alignLeft.setMaximumSize(new Dimension(20, 18));
        alignCenter.setMaximumSize(new Dimension(20, 18));
        alignRight.setMaximumSize(new Dimension(20, 18));

        textAlignmentWrap.add(Box.createRigidArea(new Dimension(4, 15)));
        textAlignmentWrap.add(alignLeft);
        textAlignmentWrap.add(Box.createRigidArea(new Dimension(1, 15)));
        textAlignmentWrap.add(alignCenter);
        textAlignmentWrap.add(Box.createRigidArea(new Dimension(1, 15)));
        textAlignmentWrap.add(alignRight);

        alignCenter.setName("center");
        alignLeft.setName("left");
        alignRight.setName("right");

        alignCenter.addMouseListener(alignmentListener);
        alignRight.addMouseListener(alignmentListener);
        alignLeft.addMouseListener(alignmentListener);


        //modifiers

        UPanel textModifiersWrap = new UPanel();
        textModifiersWrap.setLayout(new BoxLayout(textModifiersWrap, BoxLayout.X_AXIS));

        UButton boldText = new UButton("icon:image_bold-text", U.Size.Small, U.Shape.Square, true);
        UButton italicizeText = new UButton("icon:image_italicize-text", U.Size.Small, U.Shape.Square, true);
        UButton underlineText = new UButton("icon:image_underline-text", U.Size.Small, U.Shape.Square, true);

        boldText.setMaximumSize(new Dimension(20, 18));
        italicizeText.setMaximumSize(new Dimension(20, 18));
        underlineText.setMaximumSize(new Dimension(20, 18));

        textModifiersWrap.add(Box.createRigidArea(new Dimension(4, 15)));
        textModifiersWrap.add(boldText);
        textModifiersWrap.add(Box.createRigidArea(new Dimension(1, 15)));
        textModifiersWrap.add(italicizeText);
        textModifiersWrap.add(Box.createRigidArea(new Dimension(1, 15)));
        textModifiersWrap.add(underlineText);

        boldText.setName("bold");
        italicizeText.setName("italicize");
        underlineText.setName("underline");

        boldText.addMouseListener(modifierListener);
        italicizeText.addMouseListener(modifierListener);
        underlineText.addMouseListener(modifierListener);

        verticalWrap.add(textAlignmentWrap);
        verticalWrap.add(Box.createRigidArea(new Dimension(10, 4)));
        verticalWrap.add(textModifiersWrap);

        add(verticalWrap, BorderLayout.CENTER);

        verticalWrap.setOpaque(false);
        textAlignmentWrap.setOpaque(false);
        textModifiersWrap.setOpaque(false);
        setOpaque(false);

        verticalWrap.setBackground(U.transparent);
        textAlignmentWrap.setBackground(U.transparent);
        textModifiersWrap.setBackground(U.transparent);

        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseListeners(this);


    }

    MouseAdapter alignmentListener = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            StyledDocument doc = Main.writingZone.getStyledDocument();
            SimpleAttributeSet center = new SimpleAttributeSet();
            switch(((Component) e.getSource()).getName()) {
                case "center":
                    StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
                    break;
                case "left":
                    StyleConstants.setAlignment(center, StyleConstants.ALIGN_LEFT);
                    break;
                case "right":
                    StyleConstants.setAlignment(center, StyleConstants.ALIGN_RIGHT);

            }
            doc.setParagraphAttributes(Main.writingZone.getCaretPosition(), 1, center, false);

        }
    };

    MouseAdapter modifierListener = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            StyledDocument doc = Main.writingZone.getStyledDocument();
            int oldCaretPos = Main.writingZone.getCaretPosition();

            int start = Main.writingZone.getSelectionStart();
            int end = Main.writingZone.getSelectionEnd();
            for(int i = start + (end == start ? -1 : 0); i < end; i++) {
                AttributeSet set = doc.getCharacterElement(i).getAttributes();
                SimpleAttributeSet modifier = new SimpleAttributeSet();
                modifier.addAttributes(set);
                switch(((Component) e.getSource()).getName()) {
                    case "bold":
                        StyleConstants.setBold(modifier, !StyleConstants.isBold(modifier));
                        break;
                    case "underline":
                        StyleConstants.setUnderline(modifier, !StyleConstants.isUnderline(modifier));
                        break;
                    case "italicize":
                        StyleConstants.setItalic(modifier, !StyleConstants.isItalic(modifier));
                        break;
                }
                Main.writingZone.setCaretPosition(i);

                doc.setCharacterAttributes(i, 1, modifier, true);
            }
            Main.writingZone.setCaretPosition(oldCaretPos);

            Main.writingZone.setSelectionStart(start);
            Main.writingZone.setSelectionEnd(end);

        }
    };

    private void addMouseListeners(Component parent) {
        if(parent instanceof UPanel) {
            for(Component child : ((UPanel) parent).getComponents()) {
                child.addMouseListener(this);
                child.addMouseMotionListener(this);
                addMouseListeners(child);
            }
        } else {
            parent.addMouseMotionListener(this);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if(navBarClicked) {
            setLocation((int) (getX() - (mouseCache.getX() - e.getX())), (int) (getY() - (mouseCache.getY() - e.getY())));
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if(e.getSource() instanceof UPanel) {
            navBarClicked = true;
            mouseCache = getMousePosition();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        navBarClicked = false;
        if(getX() + getWidth() - 4 < 0) {
            setLocation(0, getY());
        }
        if(getX() + 4 > parentWidth) {
            setLocation(parentWidth - getWidth(), getY());
        }
        if(getY() + getHeight() - 4 < 0) {
            setLocation(getX(), 0);
        }
        if(getY() + 4 > parentHeight) {
            setLocation(getX(), parentHeight - getHeight());
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    ScheduledExecutorService executor;

    @Override
    public void mouseEntered(MouseEvent e) {
        if(navBarClicked) return;

        if(executor != null) executor.shutdown();
        executor = Executors.newScheduledThreadPool(1);
        Runnable r = () -> {
            if(opacity + .01f > 1f) {
                setOpacity(1f);
                executor.shutdown();
            } else {
                setOpacity(opacity + .01f);
            }

        };
        executor.scheduleAtFixedRate(r, 0, 8, TimeUnit.MILLISECONDS);

    }

    @Override
    public void mouseExited(MouseEvent e) {
        if(navBarClicked) return;

        if(executor != null) executor.shutdown();
        executor = Executors.newScheduledThreadPool(1);

        Runnable r = () -> {
            if(opacity - .01f < idleOpacity) {
                setOpacity(idleOpacity);
                executor.shutdown();
            } else {
                setOpacity(opacity - .01f);
            }

        };
        executor.scheduleAtFixedRate(r, 0, 7, TimeUnit.MILLISECONDS);
    }

    @Override
    public void mouseMoved(MouseEvent e) {}

    @Override
    protected void paintComponent(Graphics g) {

        ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity > 1f ? 1f : opacity));

        super.paintComponent(g);
    }

}
