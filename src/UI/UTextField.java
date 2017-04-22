package UI;

import Modules.Main;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by gedr on 05/04/2017.
 */
public class UTextField extends JTextPane {

    public static AttributeSet strike;
    int lineCount = 0;
    public UWrap wrap;

    int maxNumberOfCharacters = 110;
    public int reminderIndex;

    public UTextField(String s, int sizeInt, Color textColor, Color borderColor) {

        DefaultStyledDocument document = new DefaultStyledDocument() {
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

        if(U.theme == U.Theme.Light) {
            setSelectedTextColor(Color.white);
        } else {
            setSelectedTextColor(U.text);
        }

        StyleContext sc = StyleContext.getDefaultStyleContext();
        UTextField.strike = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, new Color(255, 255, 255));
        UTextField.strike = sc.addAttribute(SimpleAttributeSet.EMPTY, "strike-color", new Color(180, 180, 180, 215));

        setBackground(U.tertiary);

        setFont(Global.plain.deriveFont((float) sizeInt));
        setSelectionColor(new Color(180, 180, 180, 160));
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

        document.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if(wrap.listPane != null && wrap.listPane.expandingHeight==0) {
                    wrap.listPane.expandingHeight = 2;
                    for(Component comp : wrap.listPane.getComponents()) {
                        wrap.listPane.expandingHeight += comp.getPreferredSize().height;
                    }
                    System.out.println(wrap.listPane.expandingHeight);
                }
                resize();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                resize();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                resize();
                if(wrap != null && wrap.listPane != null) {
                    wrap.listPane.expandingHeight = 2;
                    for(Component comp : wrap.listPane.getComponents()) {
                        wrap.listPane.expandingHeight += comp.getPreferredSize().height;
                    }
                    System.out.println(wrap.listPane.expandingHeight);

                    if(wrap.opacity == 0) {
                        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

                        Runnable r = () -> {
                            if(wrap.opacity + .01f > 1f) {
                                wrap.setOpacity(1f);
                                executor.shutdown();
                            } else {
                                wrap.setOpacity(wrap.opacity + .01f);
                            }
                        };
                        executor.scheduleAtFixedRate(r, 0, 30, TimeUnit.MILLISECONDS);
                    }

                }
            }
        });

    }

    protected void resize() {
        if(lineCount != countLines() && countLines() != 0) {
            int delta = countLines() - lineCount;
            lineCount = countLines();
            wrap.setPreferredSize(new Dimension(wrap.getWidth(), wrap.getPreferredSize().height + (14 * delta)));


            //if(lineCount == 1) wrap.setPreferredSize(new Dimension(wrap.getWidth(), 14));
            if(wrap.listPane != null) {
                wrap.listPane.expandingHeight += delta * 14;
                wrap.listPane.setPreferredSize(new Dimension(wrap.listPane.getWidth(), wrap.listPane.expandingHeight));
//                wrap.listPane.getParent().repaint();
//                wrap.listPane.getParent().revalidate();
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
            //setText(getText().substring(0, getText().length() - 1));
            return lineCount;
        }

        if(getText().charAt(getText().length()-1) == '\n')
            lineCount++;
        return lineCount;

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