package UI;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TranslucentScrollBar extends JScrollPane {


    Color dragging = new Color(170, 170, 170, 150), hover = new Color(150, 150, 150, 150), unactive = new Color(140, 140, 140, 50);

    private long timeOutMs = 1000;

    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    Color activeColor = unactive;
    private long lastInteraction = 0;
    private boolean forceActive;
    public boolean resizing;

    Runnable r = () -> {
        if(System.currentTimeMillis() - lastInteraction > timeOutMs && activeColor.getAlpha() >= unactive.getAlpha() && !forceActive) {
            activeColor = new Color(activeColor.getRed(), activeColor.getGreen(), activeColor.getBlue(), activeColor.getAlpha() - 1);
            getVerticalScrollBar().repaint();
        }
        if(activeColor.getAlpha() == unactive.getAlpha())
            activeColor = unactive;
    };

    public TranslucentScrollBar(JPanel pane) {
        super(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        setViewportView(pane);
        setComponentZOrder(getVerticalScrollBar(), 0);
        getVerticalScrollBar().setUnitIncrement(9);

        setComponentZOrder(getViewport(), 1);
        getVerticalScrollBar().setOpaque(false);

        executor.scheduleAtFixedRate(r, 0, 5, TimeUnit.MILLISECONDS);

        getVerticalScrollBar().addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                lastInteraction = System.currentTimeMillis();
                activeColor = dragging;
            }

            @Override
            public void mousePressed(MouseEvent e) {
                lastInteraction = System.currentTimeMillis();
                activeColor = dragging;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                lastInteraction = System.currentTimeMillis();
                activeColor = hover;
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                lastInteraction = System.currentTimeMillis();
                activeColor = hover;
                forceActive = true;
            }

            @Override
            public void mouseExited(MouseEvent e) {
                lastInteraction = System.currentTimeMillis();
                activeColor = hover;
                forceActive = false;
            }
        });

        getViewport().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if(!resizing) {
                    activeColor = dragging;
                    lastInteraction = System.currentTimeMillis();
                } else {
                    resizing = false;
                }
            }
        });

        setLayout(new ScrollPaneLayout() {
            @Override
            public void layoutContainer(Container parent) {
                JScrollPane scrollPane = (JScrollPane) parent;

                Rectangle availR = scrollPane.getBounds();
                availR.x = availR.y = 0;

                Insets insets = parent.getInsets();
                availR.x = insets.left;
                availR.y = insets.top;
                availR.width -= insets.left + insets.right;
                availR.height -= insets.top + insets.bottom;

                Rectangle vsbR = new Rectangle();
                vsbR.width = 12;
                vsbR.height = availR.height;
                vsbR.x = availR.x + availR.width - vsbR.width;
                vsbR.y = availR.y;

                if(viewport != null) {
                    viewport.setBounds(availR);
                }
                if(vsb != null) {
                    vsb.setVisible(true);
                    vsb.setBounds(vsbR);
                }
            }
        });
        getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            private final Dimension d = new Dimension();

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return new JButton() {
                    @Override
                    public Dimension getPreferredSize() {
                        return d;
                    }
                };
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return new JButton() {
                    @Override
                    public Dimension getPreferredSize() {
                        return d;
                    }
                };
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle r) {}

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color color = null;
                JScrollBar sb = (JScrollBar) c;
                int width = 0;
                if(!sb.isEnabled() || r.width > r.height) {
                    return;
                } else if(isDragging) {
                    width = 1;
                } else if(isThumbRollover()) {
                    width = 1;
                }
                g2.setPaint(activeColor);
                g2.fillRoundRect(r.x + 3 - width, r.y + 1, r.width - 6 + width, r.height - 2, 6 + width, 5);
                g2.dispose();
            }

            @Override
            protected void setThumbBounds(int x, int y, int width, int height) {
                super.setThumbBounds(x, y, width, height);
                scrollbar.repaint();
            }
        });

        setBorder(BorderFactory.createEmptyBorder());
        setOpaque(false);
    }
}