package UI;

import Modules.Main;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TranslucentScrollBar extends JScrollPane {

    static Color dragging = new Color(150, 150, 150, 150);
    static Color hover = new Color(150, 150, 150, 150);
    static Color unactive = new Color(130, 130, 130, 55);

    private long timeOutMs = 1000;

    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    Color activeColor = unactive;
    private long lastInteraction = 0;
    private boolean forceActive;
    public boolean scrollVisible;
    public int scrollWidth = 0;

    Runnable r = () -> {
        if(System.currentTimeMillis() - lastInteraction > timeOutMs && activeColor.getAlpha() >= unactive.getAlpha() && !forceActive) {
            activeColor = new Color(activeColor.getRed(), activeColor.getGreen(), activeColor.getBlue(), activeColor.getAlpha() - 1);
            getVerticalScrollBar().repaint();
        }
        if(activeColor.getAlpha() == unactive.getAlpha()) {
            activeColor = unactive;
            scrollWidth = 0;
        }
    };

    public TranslucentScrollBar(JComponent pane, boolean scrollVisible) {
        super(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        if(!scrollVisible) {
            setWheelScrollingEnabled(false);
            //setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_NEVER);

        }
        setViewportView(pane);
        setComponentZOrder(getVerticalScrollBar(), 0);
        getVerticalScrollBar().setUnitIncrement(9);

        setComponentZOrder(getViewport(), 1);
        getVerticalScrollBar().setOpaque(false);

        this.scrollVisible = scrollVisible;

        if(scrollVisible) executor.scheduleAtFixedRate(r, 0, 5, TimeUnit.MILLISECONDS);

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

        addMouseWheelListener(e -> {
            activeColor = dragging;
            lastInteraction = System.currentTimeMillis();
        });

        getViewport().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                scrollWidth = 1;
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
                vsbR.width = scrollVisible ? 12 : 0;
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
                if(!scrollVisible) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                JScrollBar sb = (JScrollBar) c;
                if(!sb.isEnabled() || r.width > r.height) {
                    return;
                } else if(isDragging || isThumbRollover()) {
                    scrollWidth = 1;
                }
                g2.setPaint(activeColor);
                g2.fillRoundRect(r.x + 4 - scrollWidth, r.y + 1, r.width - 6 + scrollWidth, r.height - 2, 6 + scrollWidth, 5);
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
    ScheduledExecutorService exec;
    float t = 0;
    public void scrollTo(final float v) {
        Runnable doAssist = () -> {
            exec = Executors.newScheduledThreadPool(1);
            Main.scrollingSettings = true;
            int target = (int) v*(getVerticalScrollBar().getMaximum()-getHeight());
            int start = getVerticalScrollBar().getValue();
            t = 0;
            exec.scheduleAtFixedRate(() -> {
                if(getVerticalScrollBar().getValue() != Math.abs(target-1)) {
                    if(v == 1) {
                        try {
                            SwingUtilities.invokeAndWait(() -> getVerticalScrollBar().setValue((int)((1-Math.pow(Math.E,-.0005*Math.pow(t,.5) + -45*t*t*t))*target)));
                        } catch(InterruptedException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            SwingUtilities.invokeAndWait(() -> getVerticalScrollBar().setValue((int)((Math.pow(Math.E,-.0005*Math.pow(t,.5) + -45*t*t*t))*start)));
                        } catch(InterruptedException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    getVerticalScrollBar().setValue(target);
                    exec.shutdown();
                    Main.scrollingSettings = false;
                }
                t+=.005;
                repaint();
            }, 0, 15, TimeUnit.MILLISECONDS);

        };
        SwingUtilities.invokeLater(doAssist);

    }
}