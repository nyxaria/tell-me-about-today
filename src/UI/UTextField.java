package UI;

import Modules.Main;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UTextField extends JTextPane {

    public static AttributeSet strike;
    public final DefaultStyledDocument document;
    int lineCount = 0;
    public UWrap wrap;

    int maxNumberOfCharacters = 130;
    public int reminderIndex;
    public boolean isAddReminder;
    public JPanel borderWrap;
    boolean once = true;
    public boolean hover;
    public boolean inverted;
    public UButton deleteButton;

    public UTextField(String s, float fontSize, Color textColor, Color borderColor) {

        document = new DefaultStyledDocument() {
            @Override
            public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                if((getLength() + str.length()) <= maxNumberOfCharacters) {
                    super.insertString(offs, str, a);
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        };

        setEditorKit(new StyledEditorKit() {
            public ViewFactory getViewFactory() {
                return new StrikeViewFactory();
            }
        });

        setStyledDocument(document);
        setText(s);
        lineCount = countLines();
        setBorder(BorderFactory.createLineBorder(borderColor));
        setForeground(textColor);
        setDisabledTextColor(textColor);

        StyleContext sc = StyleContext.getDefaultStyleContext();
        UTextField.strike = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, new Color(255, 255, 255));

        if(U.theme == U.Theme.Light) {
            setSelectedTextColor(Color.white);
            setSelectionColor(new Color(220, 220, 220, 135));
            UTextField.strike = sc.addAttribute(SimpleAttributeSet.EMPTY, "strike-color", new Color(110, 110, 110, 205));
        } else {
            setSelectedTextColor(U.text);
            setSelectionColor(new Color(180, 180, 180, 135));
            UTextField.strike = sc.addAttribute(SimpleAttributeSet.EMPTY, "strike-color", new Color(180, 180, 180, 215));
        }

        setBackground(U.tertiary);
        putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, false);
        getDocument().putProperty("ZOOM_FACTOR", fontSize + 1);
        setFont(Global.plain.deriveFont(fontSize));
        DefaultCaret dc = new DefaultCaret() {
            @Override
            public void paint(Graphics g) {

                if(isVisible()) {

                    JTextComponent comp = getComponent();
                    if(comp == null) {
                        return;
                    }

                    Rectangle r;
                    try {
                        r = comp.modelToView(getDot());
                        if(r == null) {
                            return;
                        }
                    } catch(BadLocationException e) {
                        return;
                    }
                    if(isVisible()) {
                        if(inverted) {
                            if(U.theme == U.Theme.Dark) g.setColor(new Color(50, 50, 60, 150));
                            else g.setColor(new Color(220, 220, 220, 150));
                        } else g.setColor(new Color(220, 220, 220, 150));

                        g.fillRect(r.x, r.y + 2, 1, r.height - 4);
                    }
                }
            }
        };

        dc.setBlinkRate(600);
        setCaret(dc);

        addStyle("normal", null);

        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) { resize(); }

            @Override
            public void keyPressed(KeyEvent e) {
                resize();
                if(deleteButton != null) deleteButton.repaint();
            }

            @Override
            public void keyReleased(KeyEvent e) { resize(); }
        });

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
            }
        });
        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if(!hover) return;
                if(getText().equals("add reminder") && wrap.opacity == .35f) {
                    wrap.setOpacity(1f);
                    setText("");
                    Main.taskScrollPane.repaint();
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if(isAddReminder && getText().trim().equals("")) {
                    Style style = addStyle("opacityText", null);
                    wrap.setOpacity(.35f);

                    try {
                        getStyledDocument().insertString(0, "add reminder", style);
                    } catch(BadLocationException ex) {}

                    updated();
                    Main.taskScrollPane.repaint();

                } else if(isAddReminder) {
                    isAddReminder = false;

                    Main.taskList.addField("nadd reminder");
                    Main.activeDay.reminders.add("nadd reminder");

                    wrap.listPane.expandingHeight += 25;
                    wrap.listPane.setPreferredSize(new Dimension(wrap.listPane.getWidth(), wrap.listPane.expandingHeight));
                    Main.taskScrollPane.repaint();
                }
            }
        });

        document.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {

                resize();
                if(deleteButton != null) deleteButton.repaint();

            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                resize();
                if(deleteButton != null) deleteButton.repaint();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

    }

    @Override
    protected void paintComponent(Graphics g) {
        final Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        super.paintComponent(g2d);
        Main.rightScrollPane.repaint();
    }

    protected void resize() {
        if((lineCount != countLines()) && countLines() != 0 || once) {
            if(wrap.getPreferredSize().height <= 0) {
                wrap.setPreferredSize(new Dimension(wrap.getPreferredSize().width, 29));
                lineCount = 1;

                SwingUtilities.invokeLater(() -> {
                    resize();
                    setVisible(false);
                    setVisible(true);
                });

            }

            int delta = countLines() - lineCount;

            wrap.setPreferredSize(new Dimension(wrap.listPane.getWidth(), wrap.getPreferredSize().height + (14 * delta)));
            wrap.repaint();

            if(wrap.listPane != null) {
                wrap.listPane.expandingHeight += delta * (lineCount == 1 && countLines() == 2 ? 14 : 14);
                wrap.listPane.setPreferredSize(new Dimension(wrap.listPane.getWidth(), wrap.listPane.expandingHeight));
                if(wrap.listPane instanceof SettingsPane) {
                    wrap.listPane.getParent().setPreferredSize(new Dimension(Main.screen.width / 8, wrap.listPane.getParent().getHeight() + delta * (lineCount == 1 && countLines() == 2 ? 14 : 14)));

                    if(!once) Main.rightScrollPane.scrollTo(1f);
                    else once = false;
                    wrap.listPane.getParent().revalidate();
                    wrap.listPane.revalidate();
                }
            }
            lineCount = countLines();

        }
        if(Main.taskScrollPane != null) Main.taskScrollPane.getVerticalScrollBar().repaint();
        if(deleteButton != null) deleteButton.repaint();
    }

    private int countLines() {
        int totalCharacters = getText().length();
        int lineCount = (totalCharacters == 0) ? 1 : 0;

        int width = getFontMetrics(getFont()).stringWidth(getText());
        if(getWidth() != 0 && (width / (getWidth() - 4)) <= 1) {
            return width / (getWidth() - 4) + 1;
        }

        int offset = totalCharacters - 1;
        try {
            while(offset > 0) {
                offset = Utilities.getRowStart(this, offset) - 1;
                lineCount++;
            }

        } catch(BadLocationException e) {
            return lineCount;
        }

        if(getText().length() != 0 && getText().charAt(getText().length() - 1) == '\n') lineCount++;
        return lineCount;

    }

    @Override
    public void updateUI() {
        super.updateUI();
        putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
    }

    public void updated() {
        Style style = addStyle("textColor", null);
        StyleConstants.setForeground(style, new Color(255, 255, 255, (int) (255 * wrap.opacity)));

        for(int i = 0; i < getText().length(); i++) {
            getStyledDocument().setCharacterAttributes(i, 1, getStyle("textColor"), true);
        }
    }

    public void init() {
        Runnable doAssist = () -> {
            if(once && !isAddReminder) {
                maxNumberOfCharacters++;
                setText(getText() + " ");
                setText(getText().substring(0, getText().length() - 1));
                maxNumberOfCharacters--;
                wrap.checkbox.mouseReleased(null);
                wrap.checkbox.mouseReleased(null);
                once = false;
            }

        };
        SwingUtilities.invokeLater(doAssist);
        if(wrap != null && wrap.listPane != null) {
            wrap.listPane.expandingHeight = 2;
            for(Component comp : wrap.listPane.getComponents()) {
                wrap.listPane.expandingHeight += comp.getPreferredSize().height;
            }

            if(wrap.opacity == 0) {
                ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

                Runnable r = () -> {
                    if(!Main.settingUpSettings) {
                        if(wrap.opacity + .01f > 1f) {
                            wrap.setOpacity(1f);
                            executor.shutdown();

                        } else if(getText().equals("add reminder") && wrap.opacity >= .35f) {
                            wrap.setOpacity(.35f);
                            executor.shutdown();

                        } else {

                            wrap.setOpacity(wrap.opacity + .01f);
                        }
                    }

                };
                executor.scheduleAtFixedRate(r, 0, 17, TimeUnit.MILLISECONDS);

            }

        }
        resize();
    }
}

class StrikeViewFactory implements ViewFactory {
    public View create(Element elem) {
        String kind = elem.getName();
        if(kind != null) {
            if(kind.equals(AbstractDocument.ContentElementName)) {

                return new StrikeLabelView(elem);
            } else if(kind.equals(AbstractDocument.ParagraphElementName)) {
                return new ParagraphView(elem);
            } else if(kind.equals(AbstractDocument.SectionElementName)) {
                return new BoxView(elem, View.Y_AXIS);

            } else if(kind.equals(StyleConstants.ComponentElementName)) {
                return new ComponentView(elem);
            } else if(kind.equals(StyleConstants.IconElementName)) {
                return new IconView(elem);
            }

        }
        return new LabelView(elem);
    }
}

class StrikeLabelView extends LabelView {

    public StrikeLabelView(Element elem) {
        super(elem);
    }

    public void paint(Graphics g, Shape allocation) {
        super.paint(g, allocation);
        paintStrikeLine(g, allocation);

    }

    public void paintStrikeLine(Graphics g, Shape a) {
        Color c = (Color) getElement().getAttributes().getAttribute("strike-color");
        if(c != null) {
            int y = a.getBounds().y + a.getBounds().height - (int) getGlyphPainter().getDescent(this);

            y = y - (int) (getGlyphPainter().getAscent(this) * 0.3f);
            int x = (int) a.getBounds().getX();
            int xx = (int) (a.getBounds().getX() + a.getBounds().getWidth());

            Color old = g.getColor();
            g.setColor(c);
            g.drawLine(x, y, xx, y);
            g.setColor(old);
        }
    }
}