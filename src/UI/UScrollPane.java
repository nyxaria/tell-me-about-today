package UI;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

/**
 * Created by gedr on 27/02/2017.
 */
public class UScrollPane extends JScrollPane {

    public UScrollPane(JComponent component, int vert, int horiz) {
        super(component, vert, horiz);
        getVerticalScrollBar().setUnitIncrement(14);
        getViewport().setOpaque(false);
        //getViewport().setBackground(new Color(48, 48, 108));
        setBorder(null);
        getVerticalScrollBar().setBorder(null);
        getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
        getVerticalScrollBar().setUI(new UScrollBarUI());
    }

    public UScrollPane(int vert, int horiz) {
        super(vert, horiz);
        getVerticalScrollBar().setUnitIncrement(14);
        getViewport().setOpaque(false);
        //getViewport().setBackground(new Color(48, 48, 108));
        setBorder(null);
        getVerticalScrollBar().setBorder(null);
        getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
        getVerticalScrollBar().setUI(new UScrollBarUI());}

}

class UScrollBarUI extends BasicScrollBarUI {

    Color background = new Color(38, 38, 38);

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        g.setColor(new Color(0,0,0,0));
        // g.fillRect(0, 0, (int) trackBounds.getWidth(), (int) trackBounds.getHeight());
        g.setColor(new Color(33,33,33));
        //g.fillRect(0,0,1,(int) trackBounds.getHeight());
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        JButton button = new JButton("zero button");
        Dimension zeroDim = new Dimension(0, 0);
        button.setPreferredSize(zeroDim);
        button.setMinimumSize(zeroDim);
        button.setMaximumSize(zeroDim);
        return button;
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        JButton button = new JButton("zero button");
        Dimension zeroDim = new Dimension(0, 0);
        button.setPreferredSize(zeroDim);
        button.setMinimumSize(zeroDim);
        button.setMaximumSize(zeroDim);
        return button;
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        g2d.setColor(new Color(92, 92, 92, 128));
        g2d.translate(thumbBounds.x, thumbBounds.y);
        g2d.fillRoundRect(2, 2, (int) thumbBounds.getWidth() - 4, (int) thumbBounds.getHeight() - 4, 7, 5);
        g2d.translate(-thumbBounds.x, -thumbBounds.y);
        g2d.dispose();

    }
}
