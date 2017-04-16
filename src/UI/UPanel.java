package UI;

import org.jdesktop.swingx.graphics.GraphicsUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.ImageObserver;
import java.awt.image.Kernel;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gedr on 18/03/2017.
 */
public class UPanel extends JPanel {
    public Color to, from;
    public boolean left, right, bottom, top;
    public float opacity = 1f;

    private static enum Position {
        TOP,
        TOP_LEFT,
        LEFT,
        BOTTOM_LEFT,
        BOTTOM,
        BOTTOM_RIGHT,
        RIGHT,
        TOP_RIGHT;

        private Position() {
        }
    }
    private static final Map<Double, Map<Position, BufferedImage>> CACHE = new HashMap();
    private Color shadowColor;
    private int shadowSize;
    private float shadowOpacity;
    private int cornerSize;
    private boolean showTopShadow;
    private boolean showLeftShadow;
    private boolean showBottomShadow;
    private boolean showRightShadow;

    public UPanel(BorderLayout layout) {
        super(layout);
    }
    public UPanel() {
        super();
    }

    public void setOpacity(float f) {
//        shadowOpacity = f*0.6f;
//        for(Component c : getComponents()) {
//            System.out.println(c.getBackground() + " " + f);
//
//            c.setBackground(new Color(c.getBackground().getRed(),c.getBackground().getGreen(),c.getBackground().getBlue(),c.getBackground().getAlpha()*f));
//            c.setForeground(new Color(c.getForeground().getRed(),c.getForeground().getGreen(),c.getForeground().getBlue(),c.getForeground().getAlpha()*f));
//        }
//        setBackground(new Color(getBackground().getRed(),getBackground().getGreen(),getBackground().getBlue(),getBackground().getAlpha()*f));
//        setForeground(new Color(getForeground().getRed(),getForeground().getGreen(),getForeground().getBlue(),getForeground().getAlpha()*f));
        repaint();
        this.opacity = f;
    }

    HashMap<Position, BufferedImage> images;
    public UPanel(BorderLayout layout, Color shadowColor, int shadowSize, float shadowOpacity, int cornerSize, boolean showTopShadow, boolean showLeftShadow, boolean showBottomShadow, boolean showRightShadow) {
        super(layout);
        this.shadowColor = shadowColor;
        this.shadowSize = shadowSize;
        this.shadowOpacity = shadowOpacity;
        this.cornerSize = cornerSize;
        this.showTopShadow = showTopShadow;
        this.showLeftShadow = showLeftShadow;
        this.showBottomShadow = showBottomShadow;
        this.showRightShadow = showRightShadow;

        images = new HashMap();
        int rectWidth = this.cornerSize + 1;
        java.awt.geom.RoundRectangle2D.Double rect = new java.awt.geom.RoundRectangle2D.Double(0.0D, 0.0D, (double)rectWidth, (double)rectWidth, (double)this.cornerSize, (double)this.cornerSize);
        int imageWidth = rectWidth + this.shadowSize * 2;
        BufferedImage image = GraphicsUtilities.createCompatibleTranslucentImage(imageWidth, imageWidth);
        Graphics2D buffer = (Graphics2D)image.getGraphics();
        buffer.setPaint(new Color(this.shadowColor.getRed(), this.shadowColor.getGreen(), this.shadowColor.getBlue(), (int)(this.shadowOpacity * 255.0F)));
        buffer.translate(this.shadowSize, this.shadowSize);
        buffer.fill(rect);
        buffer.dispose();
        float blurry = 1.0F / (float)(this.shadowSize * this.shadowSize);
        float[] blurKernel = new float[this.shadowSize * this.shadowSize];

        for(int blur = 0; blur < blurKernel.length; ++blur) {
            blurKernel[blur] = blurry;
        }

        ConvolveOp var16 = new ConvolveOp(new Kernel(this.shadowSize, this.shadowSize, blurKernel));
        BufferedImage targetImage = GraphicsUtilities.createCompatibleTranslucentImage(imageWidth, imageWidth);
        ((Graphics2D)targetImage.getGraphics()).drawImage(image, var16, -(this.shadowSize / 2), -(this.shadowSize / 2));
        byte x = 1;
        byte y = 1;
        int w = this.shadowSize;
        int h = this.shadowSize;
        ((Map)images).put(Position.TOP_LEFT, this.getSubImage(targetImage, x, y, w, h));
        x = 1;
        w = this.shadowSize;
        byte var20 = 1;
        ((Map)images).put(Position.LEFT, this.getSubImage(targetImage, x, h, w, var20));
        x = 1;
        w = this.shadowSize;
        h = this.shadowSize;
        ((Map)images).put(Position.BOTTOM_LEFT, this.getSubImage(targetImage, x, rectWidth, w, h));
        int var17 = this.cornerSize + 1;
        byte var19 = 1;
        h = this.shadowSize;
        ((Map)images).put(Position.BOTTOM, this.getSubImage(targetImage, var17, rectWidth, var19, h));
        w = this.shadowSize;
        h = this.shadowSize;
        ((Map)images).put(Position.BOTTOM_RIGHT, this.getSubImage(targetImage, rectWidth, rectWidth, w, h));
        int var18 = this.cornerSize + 1;
        w = this.shadowSize;
        var20 = 1;
        ((Map)images).put(Position.RIGHT, this.getSubImage(targetImage, rectWidth, var18, w, var20));
        y = 1;
        w = this.shadowSize;
        h = this.shadowSize;
        ((Map)images).put(Position.TOP_RIGHT, this.getSubImage(targetImage, rectWidth, y, w, h));
        var17 = this.shadowSize;
        y = 1;
        var19 = 1;
        h = this.shadowSize;
        ((Map)images).put(Position.TOP, this.getSubImage(targetImage, var17, y, var19, h));
        image.flush();
        CACHE.put(Double.valueOf((double)this.shadowSize + (double)this.shadowColor.hashCode() * 0.3D + (double)this.shadowOpacity * 0.12D), (Map<Position, BufferedImage>) images);

    }

    @Override
    protected void paintComponent(Graphics g) {
        ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

        super.paintComponent(g);
        Component c = this; Graphics graphics = g; int x = 1; int y = 1; int width = getWidth()-2; int height = getHeight()-2;
        Graphics2D g2 = (Graphics2D)graphics.create();
        byte shadowOffset = 2;
        Point topLeftShadowPoint = null;
        if(this.showLeftShadow || this.showTopShadow) {
            topLeftShadowPoint = new Point();
            if(this.showLeftShadow && !this.showTopShadow) {
                topLeftShadowPoint.setLocation(x, y + shadowOffset);
            } else if(this.showLeftShadow && this.showTopShadow) {
                topLeftShadowPoint.setLocation(x, y);
            } else if(!this.showLeftShadow && this.showTopShadow) {
                topLeftShadowPoint.setLocation(x + this.shadowSize, y);
            }
        }

        Point bottomLeftShadowPoint = null;
        if(this.showLeftShadow || this.showBottomShadow) {
            bottomLeftShadowPoint = new Point();
            if(this.showLeftShadow && !this.showBottomShadow) {
                bottomLeftShadowPoint.setLocation(x, y + height - this.shadowSize - this.shadowSize);
            } else if(this.showLeftShadow && this.showBottomShadow) {
                bottomLeftShadowPoint.setLocation(x, y + height - this.shadowSize);
            } else if(!this.showLeftShadow && this.showBottomShadow) {
                bottomLeftShadowPoint.setLocation(x + this.shadowSize, y + height - this.shadowSize);
            }
        }

        Point bottomRightShadowPoint = null;
        if(this.showRightShadow || this.showBottomShadow) {
            bottomRightShadowPoint = new Point();
            if(this.showRightShadow && !this.showBottomShadow) {
                bottomRightShadowPoint.setLocation(x + width - this.shadowSize, y + height - this.shadowSize - this.shadowSize);
            } else if(this.showRightShadow && this.showBottomShadow) {
                bottomRightShadowPoint.setLocation(x + width - this.shadowSize, y + height - this.shadowSize);
            } else if(!this.showRightShadow && this.showBottomShadow) {
                bottomRightShadowPoint.setLocation(x + width - this.shadowSize - this.shadowSize, y + height - this.shadowSize);
            }
        }

        Point topRightShadowPoint = null;
        if(this.showRightShadow || this.showTopShadow) {
            topRightShadowPoint = new Point();
            if(this.showRightShadow && !this.showTopShadow) {
                topRightShadowPoint.setLocation(x + width - this.shadowSize, y + shadowOffset);
            } else if(this.showRightShadow && this.showTopShadow) {
                topRightShadowPoint.setLocation(x + width - this.shadowSize, y);
            } else if(!this.showRightShadow && this.showTopShadow) {
                topRightShadowPoint.setLocation(x + width - this.shadowSize - this.shadowSize, y);
            }
        }

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        Rectangle topShadowRect;
        if(this.showLeftShadow) {
            topShadowRect = new Rectangle(x, topLeftShadowPoint.y + this.shadowSize, this.shadowSize, bottomLeftShadowPoint.y - topLeftShadowPoint.y - this.shadowSize);
            g2.drawImage((Image)images.get(Position.LEFT), topShadowRect.x, topShadowRect.y, topShadowRect.width, topShadowRect.height, (ImageObserver)null);
        }

        if(this.showBottomShadow) {
            topShadowRect = new Rectangle(bottomLeftShadowPoint.x + this.shadowSize, y + height - this.shadowSize, bottomRightShadowPoint.x - bottomLeftShadowPoint.x - this.shadowSize, this.shadowSize);
            g2.drawImage((Image)images.get(Position.BOTTOM), topShadowRect.x, topShadowRect.y, topShadowRect.width, topShadowRect.height, (ImageObserver)null);
        }

        if(this.showRightShadow) {
            topShadowRect = new Rectangle(x + width - this.shadowSize, topRightShadowPoint.y + this.shadowSize, this.shadowSize, bottomRightShadowPoint.y - topRightShadowPoint.y - this.shadowSize);
            g2.drawImage((Image)images.get(Position.RIGHT), topShadowRect.x, topShadowRect.y, topShadowRect.width, topShadowRect.height, (ImageObserver)null);
        }

        if(this.showTopShadow) {
            topShadowRect = new Rectangle(topLeftShadowPoint.x + this.shadowSize, y, topRightShadowPoint.x - topLeftShadowPoint.x - this.shadowSize, this.shadowSize);
            g2.drawImage((Image)images.get(Position.TOP), topShadowRect.x, topShadowRect.y, topShadowRect.width, topShadowRect.height, (ImageObserver)null);
        }

        if(this.showLeftShadow || this.showTopShadow) {
            g2.drawImage((Image)images.get(Position.TOP_LEFT), topLeftShadowPoint.x, topLeftShadowPoint.y, (ImageObserver)null);
        }

        if(this.showLeftShadow || this.showBottomShadow) {
            g2.drawImage((Image)images.get(Position.BOTTOM_LEFT), bottomLeftShadowPoint.x, bottomLeftShadowPoint.y, (ImageObserver)null);
        }

        if(this.showRightShadow || this.showBottomShadow) {
            g2.drawImage((Image)images.get(Position.BOTTOM_RIGHT), bottomRightShadowPoint.x, bottomRightShadowPoint.y, (ImageObserver)null);
        }

        if(this.showRightShadow || this.showTopShadow) {
            g2.drawImage((Image)images.get(Position.TOP_RIGHT), topRightShadowPoint.x, topRightShadowPoint.y, (ImageObserver)null);
        }
        if(shadowColor != null) {
            g2.setColor(getBackground());
            g2.fillRect(shadowSize, shadowSize, getWidth() - shadowSize * 2, getHeight() - shadowSize * 2);
        }
       // g2.setColor(Color.red);
        //g2.drawRect(0,0, getWidth(), getHeight());

        g2.dispose();
    }


    private BufferedImage getSubImage(BufferedImage img, int x, int y, int w, int h) {
        BufferedImage ret = GraphicsUtilities.createCompatibleTranslucentImage(w, h);
        Graphics2D g2 = ret.createGraphics();
        g2.drawImage(img, 0, 0, w, h, x, y, x + w, y + h, (ImageObserver)null);
        g2.dispose();
        return ret;
    }

    public Insets getBorderInsets(Component c) {
        int top = this.showTopShadow?this.shadowSize:0;
        int left = this.showLeftShadow?this.shadowSize:0;
        int bottom = this.showBottomShadow?this.shadowSize:0;
        int right = this.showRightShadow?this.shadowSize:0;
        return new Insets(top, left, bottom, right);
    }


//
//        Graphics2D g2 = (Graphics2D) g;
//        g2.setColor(getBackground());
//        g2.fillRect(6,6,getWidth()-3,getHeight()-3);
//
//            GradientPaint paint = new GradientPaint(4, 0, new Color(0, 0, 0, 0), 6, 0, new Color(26, 26,26));
//            g2.setPaint(paint);
//            g2.fillRect(3,3,3,getHeight()-6);
//
//            //g2.fillRect(0,0,getWidth(),getHeight());
//            paint = new GradientPaint(0,0,Color.BLACK, 10,10,new Color(0,0,0,0));
//            g2.setPaint(paint);
//            //g2.fillRect(getWidth()-10,0,10,getHeight());

}
