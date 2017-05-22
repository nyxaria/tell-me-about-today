package UI;

import Modules.Main;
import rtf.AdvancedRTFDocument;
import rtf.AdvancedRTFEditorKit;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.*;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by gedr on 01/03/2017.
 */

public class UTextArea extends JTextPane {

    private Style unhighlight;
    private Style highlight;
    private Style highlight2;

    int lastHighlighted;
    private boolean highlighted;
    ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
    private boolean focus = false;
    ArrayList<String> redo = new ArrayList();
    public ArrayList<String> undo = new ArrayList<>();
    StyledDocument document;
    protected UndoManager undoManager = new UndoManager();

    UndoableEditListener undoableListner = e -> undoManager.addEdit(e.getEdit());
    public boolean initialising;
    public float opacity = 1f;
    public static float shadowOpacity = 0.8f;

    public UTextArea() {
        setBackground(U.transparent);
        setFont(Global.plain.deriveFont((float) 13));
        undoManager = new UndoManager();
        setBorder(BorderFactory.createEmptyBorder(2, 14, 2, 6));
        document = new AdvancedRTFDocument();

        setStyledDocument(document);
        putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        setEditorKit(new AdvancedRTFEditorKit());

        document.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if(Main.activeDay != null) {
                    if(getText() != null && Main.activeDay != null) try {
                        Main.activeDay.wrap.updateText(getDocument().getText(0, getDocument().getLength()));
                    } catch(BadLocationException ex) {
                        ex.printStackTrace();
                    }
                }

                    Runnable doAssist = () -> updated();
                    SwingUtilities.invokeLater(doAssist);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {}

            @Override
            public void changedUpdate(DocumentEvent e) {}
        });

        document.addUndoableEditListener(undoableListner);
        setSelectionColor(new Color(180, 180, 180, 75));
        setCaretColor(new Color(0, 0, 0, 0));
        setForeground(U.text);

        setUpStyles();

        ses = Executors.newSingleThreadScheduledExecutor();
        ses.scheduleAtFixedRate((Runnable) () -> {
            if(focus) {
                if(getSelectionStart() == getSelectionEnd()) {
                    document.removeUndoableEditListener(undoableListner);
                    if(highlighted) {
                        SimpleAttributeSet temp = new SimpleAttributeSet();
                        AttributeSet old = getStyledDocument().getCharacterElement(lastHighlighted - 1).getAttributes();
                        temp.addAttributes(old);
                        temp.addAttributes(highlight2);
                        getStyledDocument().setCharacterAttributes(lastHighlighted - 1, 1, temp, true);
                    } else {
                        SimpleAttributeSet temp = new SimpleAttributeSet();
                        AttributeSet old = getStyledDocument().getCharacterElement(lastHighlighted - 1).getAttributes();
                        temp.addAttributes(old);
                        temp.addAttributes(highlight);
                        getStyledDocument().setCharacterAttributes(lastHighlighted - 1, 1, temp, true);
                    }
                    highlighted = !highlighted;
                    document.addUndoableEditListener(undoableListner);
                }

            }
        }, 1, 1, TimeUnit.SECONDS);

        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                focus = true;
            }

            @Override
            public void focusLost(FocusEvent e) {
                focus = false;
            }
        });
        addCaretListener(e -> {
            Runnable doAssist = () -> {
                if(getSelectionStart() - getSelectionEnd() == 0) {

                    document.removeUndoableEditListener(undoableListner);

                    SimpleAttributeSet temp = new SimpleAttributeSet();
                    AttributeSet old = getStyledDocument().getCharacterElement(lastHighlighted - 1).getAttributes();
                    temp.addAttributes(old);
                    temp.addAttributes(unhighlight);

                    getStyledDocument().setCharacterAttributes(lastHighlighted - 1, 1, temp, true);

                    temp = new SimpleAttributeSet();
                    AttributeSet newChar = getStyledDocument().getCharacterElement(getCaretPosition() - 1).getAttributes();
                    temp.addAttributes(newChar);
                    temp.addAttributes(highlight);

                    getStyledDocument().setCharacterAttributes(getCaretPosition() - 1, 1, temp, true);

                    lastHighlighted = getCaretPosition();
                    highlighted = true;
                    document.addUndoableEditListener(undoableListner);
                } else {
                    document.removeUndoableEditListener(undoableListner);
                    for(int i = getSelectionStart(); i < getSelectionEnd(); i++) {
                        SimpleAttributeSet temp = new SimpleAttributeSet();
                        AttributeSet old = getStyledDocument().getCharacterElement(i).getAttributes();
                        temp.addAttributes(old);
                        temp.addAttributes(unhighlight);
                        getStyledDocument().setCharacterAttributes(i, 1, temp, true);
                    }
                    document.addUndoableEditListener(undoableListner);
                }

            };
            SwingUtilities.invokeLater(doAssist);

        });

        InputMap im = getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Undo");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Redo");

        addMouseListener(new MouseAdapter() {
            int pos = 0;

            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 1) pos = getCaretPosition();
                if(e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    int selectionStart = 0, selectionEnd = getText().length();
                    for(int start = pos; start > 0; start--) {
                        if(getText().charAt(start) == '\n' && selectionStart == 0) {
                            selectionStart = start;
                        }
                    }
                    for(int start = pos; start < getText().length(); start++) {
                        if(getText().charAt(start) == '\n' && selectionEnd == getText().length()) {
                            selectionEnd = start;
                        }
                    }
                    setSelectionStart((selectionStart != 0 ? selectionStart + 1 : 0));
                    setSelectionEnd(selectionEnd);
                }
            }

        });

        am.put("Undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if(!undoManager.canUndo()) return;
                try {
                    undoManager.undo();
                } catch(CannotRedoException cre) {
                    cre.printStackTrace();
                }

            }
        });
        am.put("Redo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!undoManager.canRedo()) return;
                try {
                    undoManager.redo();
                } catch(CannotRedoException cre) {
                    cre.printStackTrace();
                }
            }
        });

        addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent ke) {}

            @Override
            public void keyReleased(KeyEvent ke) {
                if(getText() != null && Main.activeDay != null) try {
                    Main.activeDay.wrap.updateText(getDocument().getText(0, getDocument().getLength()));
                } catch(BadLocationException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void keyPressed(KeyEvent evt) {}
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                actionModifier("italicize");
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_B, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                actionModifier("bold");
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_U, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                actionModifier("underline");
            }
        });
    }

    public void actionModifier(String mod) {
        String oldName = Main.writingZone.getName();
        Main.writingZone.setName(mod);
        MouseEvent me = new MouseEvent(Main.writingZone, 0, 0, 0, 100, 100, 1, false);
        Main.textToolBar.modifierListener.mousePressed(me);
        Main.writingZone.setName(oldName);
    }
    public void setUpStyles() {
        int inverted = U.theme == U.Theme.Dark ? 0 : 168;
        highlight = addStyle("highlight", null);
        int val = Math.abs(inverted - 158);
        StyleConstants.setForeground(highlight, new Color(val, val, val));

        highlight2 = addStyle("highlight2", null);
        val = Math.abs(inverted - 188);
        if(val != 188) val += 65;
        StyleConstants.setForeground(highlight2, new Color(val, val, val));

        unhighlight = addStyle("unhighlight", null);
        StyleConstants.setForeground(unhighlight, U.text);
    }

    public void updated() {
        document.removeUndoableEditListener(undoableListner);

        Style style = addStyle("textColor", null);
        StyleConstants.setForeground(style, getForeground());
        int selectionStart = getSelectionStart();
        int selectionEnd = getSelectionEnd();
        int caretPos = getCaretPosition();
        boolean next = false;
        SimpleAttributeSet prevAttSet = null;

        for(int i = 1; i < getDocument().getLength() + 1; i++) {
            AttributeSet old = getStyledDocument().getCharacterElement(i).getAttributes();
            SimpleAttributeSet set = new SimpleAttributeSet();
            set.addAttributes(old);
            getStyledDocument().setCharacterAttributes(i, 1, getStyle("highlight"), true);

            getStyledDocument().setCharacterAttributes(i, 1, set, true);

        }
        document.addUndoableEditListener(undoableListner);

        setCaretPosition(caretPos);
        setSelectionStart(selectionStart);
        setSelectionEnd(selectionEnd);
        requestFocus();
    }

    public void loadText(String text) {
        StringReader builder = new StringReader(text);
        try {
            getEditorKit().read(builder, getDocument(), 0);
        } catch(IOException | BadLocationException e) {
            e.printStackTrace();
        }
    }

    public String getStyledText() {
        if(getDocument().getLength() > 0) {
            SimpleAttributeSet temp = new SimpleAttributeSet();
            AttributeSet old = getStyledDocument().getCharacterElement(lastHighlighted - 1).getAttributes();
            temp.addAttributes(old);
            temp.addAttributes(unhighlight);

            getStyledDocument().setCharacterAttributes(lastHighlighted - 1, 1, temp, true);

            Writer writer = new StringWriter();
            try {
                (getEditorKit()).write(writer, getDocument(), 0, getDocument().getLength());
                writer.close();
            } catch(IOException | BadLocationException e) {
                e.printStackTrace();
            }
            String output = writer.toString();
            return output;
        } else {
            return "";
        }
    }

    public void start() {
        undo.add(getText());

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        opacity = 0;
        Main.taskScrollPane.activeColor = new Color(Main.taskScrollPane.activeColor.getRed(), Main.taskScrollPane.activeColor.getGreen(), Main.taskScrollPane.activeColor.getBlue(), 0);
        Main.taskScrollPane.repaint();
        Runnable doAssist = () -> {
            setCaretPosition(getDocument().getLength());
            executor.scheduleAtFixedRate(() -> {
                if(opacity + .03f > 1f) {
                    opacity = 1f;
                    executor.shutdown();
                    Main.taskScrollPane.activeColor = TranslucentScrollBar.unactive;
                    Main.taskScrollPane.scrollWidth = 0;



                } else {
                    opacity += .03f;
                }
                repaint();
            }, 0, 100, TimeUnit.MILLISECONDS);
        };
        SwingUtilities.invokeLater(doAssist);


        Main.taskList.revalidate();
        Main.taskList.repaint();
        Main.taskList.setList();
        Main.settingUp = false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g.setColor(U.primary);
        g.fillRect(0, 0, getWidth(), getHeight());
        GradientPaint paint = new GradientPaint(0, 0, new Color(26, 26, 26, (int) (255 * shadowOpacity)), 5, 0, new Color(0, 0, 0, 0));
        g2.setPaint(paint);
        g2.fillRect(0, 0, 5, getHeight());

        paint = new GradientPaint(getWidth() - 5, 0, new Color(0, 0, 0, 0), getWidth(), 0, new Color(26, 26, 26, (int) (255 * shadowOpacity)));
        g2.setPaint(paint);
        g2.fillRect(getWidth() - 5, 0, 5, getHeight());

        ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

        super.paintComponent(g);
    }
}
