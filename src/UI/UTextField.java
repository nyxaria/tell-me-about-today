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

/**
 * Created by gedr on 05/04/2017.
 */
public class UTextField extends JTextPane {

    public static AttributeSet strike;
    public final DefaultStyledDocument document;
    int lineCount = 0;
    public UWrap wrap;

    int maxNumberOfCharacters = 110;
    public int reminderIndex;
    public boolean isAddReminder;
    public JPanel borderWrap;
    boolean once = true;
    public boolean hover;

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
                return new NewViewFactory();
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
        putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        getDocument().putProperty("ZOOM_FACTOR", fontSize);
        setFont(Global.plain.deriveFont(fontSize));
        DefaultCaret dc = new DefaultCaret() {
            @Override
            public void paint(Graphics g) {

                if(isVisible()) {

                    JTextComponent comp = getComponent();
                    if(comp == null) {
                        return;
                    }

                    Rectangle r = null;
                    try {
                        r = comp.modelToView(getDot());
                        if(r == null) {
                            return;
                        }
                    } catch(BadLocationException e) {
                        return;
                    }
                    if(isVisible()) {
                        g.setColor(new Color(180, 180, 180, 75));
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
            public void keyPressed(KeyEvent e) { resize(); }

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
                    Main.taskScrollPane.repaint();
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if(isAddReminder && getText().equals("add reminder")) {
                    wrap.setOpacity(.35f);
                    Main.taskScrollPane.repaint();
                } else if(isAddReminder) {
                    borderWrap.setVisible(true);
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
                if(wrap.listPane != null && wrap.listPane.expandingHeight == 0 && getText().length() != 0) {
                    wrap.listPane.expandingHeight = 1;
                    for(Component comp : wrap.listPane.getComponents()) {
                        wrap.listPane.expandingHeight += comp.getPreferredSize().height;
                    }
                }
                resize();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                resize();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                Runnable doAssist = () -> {
                    if(once && !isAddReminder) {
                        maxNumberOfCharacters++;
                        wrap.setVisible(true);
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
                            if(wrap.opacity + .01f > 1f) {
                                wrap.setOpacity(1f);
                                executor.shutdown();

                            } else if(getText().equals("add reminder") && wrap.opacity >= .35f) {
                                wrap.setOpacity(.35f);
                                executor.shutdown();

                            } else {

                                wrap.setOpacity(wrap.opacity + .01f);
                            }

                        };
                        executor.scheduleAtFixedRate(r, 0, 17, TimeUnit.MILLISECONDS);

                    }

                }
            }
        });

    }

    @Override
    protected void paintComponent(Graphics g) {
        final Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        super.paintComponent(g2d);
    }

    protected void resize() {
        if(lineCount != countLines() && countLines() != 0) {
            int delta = countLines() - lineCount;
            lineCount = countLines();

            wrap.setPreferredSize(new Dimension(wrap.getWidth(), wrap.getPreferredSize().height + (14 * delta)));

            if(wrap.listPane != null) {
                wrap.listPane.expandingHeight += delta * 14;
                wrap.listPane.setPreferredSize(new Dimension(wrap.listPane.getWidth(), wrap.listPane.expandingHeight));
                if(wrap.listPane instanceof SettingsPane) {
                    wrap.listPane.getParent().setPreferredSize(new Dimension(Main.screen.width / 8, wrap.listPane.getParent().getHeight() + delta * 14));
                    Main.rightScrollPane.scrollTo(1f);
                }
            }
        }
        if(Main.taskScrollPane != null) Main.taskScrollPane.getVerticalScrollBar().repaint();
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
}

class NewViewFactory implements ViewFactory {
    public View create(Element elem) {
        String kind = elem.getName();
        if(kind != null) {
            if(kind.equals(AbstractDocument.ContentElementName)) {

                return new MyLabelView(elem);
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

        // default to text display
        return new LabelView(elem);
    }
}

class MyLabelView extends LabelView {

    public MyLabelView(Element elem) {
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
            int x1 = (int) a.getBounds().getX();
            int x2 = (int) (a.getBounds().getX() + a.getBounds().getWidth());

            Color old = g.getColor();
            g.setColor(c);
            g.drawLine(x1, y, x2, y);
            g.setColor(old);
        }
    }
}