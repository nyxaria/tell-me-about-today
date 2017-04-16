package UI;

import Modules.Main;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by gedr on 01/03/2017.
 */

public class UTextArea extends JTextPane {

    private final Style unhighlight;
    private final Style highlight;
    Color textColor = new Color(240, 240, 240);
    Color highlightColor = new Color(168, 168, 168);
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
    Color backgroundColor = new Color(40, 40, 42);

    public UTextArea(String file) {
        setBackground(new Color(0,0,0,0));
        setFont(Global.plain.deriveFont((float) 13));
        undoManager = new UndoManager();

        setBorder(BorderFactory.createEmptyBorder(2, 14, 2, 6));
        document = new DefaultStyledDocument();
        setStyledDocument(document);

        document.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if(Main.activeDay != null) {
                    Main.activeDay.wrap.updateText(getText());
                }

                Runnable doAssist = () -> updated();
                SwingUtilities.invokeLater(doAssist);
            }

            @Override public void removeUpdate(DocumentEvent e) {}
            @Override public void changedUpdate(DocumentEvent e) {}
        });

        document.addUndoableEditListener(undoableListner);
        setSelectionColor(new Color(180, 180, 180, 75));
        setCaretColor(new Color(0, 0, 0, 0));
        setForeground(textColor);


        highlight = addStyle("highlight", null);
        StyleConstants.setForeground(highlight, highlightColor);

        Style highlight2 = addStyle("highlight2", null);
        StyleConstants.setForeground(highlight2, new Color(192, 192, 192));

        unhighlight = addStyle("unhighlight", null);
        StyleConstants.setForeground(unhighlight, textColor);
        StyleConstants.setUnderline(unhighlight, false);

        SimpleAttributeSet underline = new SimpleAttributeSet();
        StyleConstants.setUnderline(underline, true);

        SimpleAttributeSet clear = new SimpleAttributeSet();
        StyleConstants.setUnderline(clear, false);
        ses = Executors.newSingleThreadScheduledExecutor();
        ses.scheduleAtFixedRate((Runnable) () -> {
            if(focus) {
                if(getSelectionStart() - getSelectionEnd() == 0) {
                    document.removeUndoableEditListener(undoableListner);
                    if(highlighted) {
                        getStyledDocument().setCharacterAttributes(lastHighlighted - 1, 1, getStyle("highlight2"), true);
                    } else {
                        getStyledDocument().setCharacterAttributes(lastHighlighted - 1, 1, getStyle("highlight"), true);
                    }
                    highlighted = !highlighted;
                    document.addUndoableEditListener(undoableListner);
                }

            }
        }, 1, 1, TimeUnit.SECONDS);

        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                focus = true;
            }

            @Override
            public void mouseExited(MouseEvent e) {
                focus = false;
            }
        });
        addCaretListener(e -> {
            Runnable doAssist = () -> {
                if(getSelectionStart() - getSelectionEnd() == 0) {

                    document.removeUndoableEditListener(undoableListner);

                    getStyledDocument().setCharacterAttributes(lastHighlighted - 1, 1, getStyle("unhighlight"), true);

                    getStyledDocument().setCharacterAttributes(getCaretPosition() - 1, 1, getStyle("highlight"), true);

                    lastHighlighted = getCaretPosition();
                    highlighted = true;
                    document.addUndoableEditListener(undoableListner);
                } else {
                    document.removeUndoableEditListener(undoableListner);
                    getStyledDocument().setCharacterAttributes(getSelectionStart(), getSelectionEnd() - getSelectionStart(), getStyle("unhighlight"), true);
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
                if(e.getClickCount() == 1)
                    pos = getCaretPosition();
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    int selectionStart = 0, selectionEnd = getText().length();
                    for(int start = pos; start > 0; start --) {
                        if(getText().charAt(start) == '\n' && selectionStart == 0) {
                            selectionStart = start;
                        }
                    }
                    for(int start = pos; start < getText().length(); start ++) {
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

            @Override public void keyTyped(KeyEvent ke) {}

            @Override
            public void keyReleased(KeyEvent ke) {
                if(getText()!=null)
                    Main.activeDay.wrap.updateText(getText());
            }

            @Override public void keyPressed(KeyEvent evt) {}
        });
    }


    public void updated() {
        document.removeUndoableEditListener(undoableListner);

        Style style = addStyle("textColor", null);
        StyleConstants.setForeground(style, getForeground());
        int caretPos = getCaretPosition();
        for(int i = 0; i < getText().length(); i++) {
            getStyledDocument().setCharacterAttributes(i, 1, getStyle("highlight"), true);
            getStyledDocument().setCharacterAttributes(i, 1, getStyle("textColor"), true);
        }
        document.addUndoableEditListener(undoableListner);

        setCaretPosition(caretPos);
        requestFocus();
    }

    public void start() {
        undo.add(getText());

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        opacity = 0;

        Runnable doAssist = () -> {
            setCaretPosition(getText().length());
            executor.scheduleAtFixedRate(() -> {
                if(opacity + .002f > 1f) {
                    opacity = 1f;
                    executor.shutdown();
                } else {
                    opacity += .002f;

                }
                repaint();
            }, 0, 5, TimeUnit.MILLISECONDS);

        };
        SwingUtilities.invokeLater(doAssist);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;


        g.setColor(backgroundColor);
        g.fillRect(0,0,getWidth(),getHeight());
        GradientPaint paint = new GradientPaint(0, 0, new Color(26, 26,26), 5, 0, new Color(0, 0, 0, 0));
        g2.setPaint(paint);
        g2.fillRect(0, 0, 5, getHeight());

        paint = new GradientPaint(getWidth() - 5, 0,new Color(0, 0, 0, 0) , getWidth(), 0, new Color(26, 26,26));
        g2.setPaint(paint);
        g2.fillRect(getWidth() - 5, 0, 5, getHeight());

        ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

        super.paintComponent(g);



    }
}
