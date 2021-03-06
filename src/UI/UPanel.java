package UI;


import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gedr on 18/03/2017.
 */
public class UPanel extends JPanel {
    public float opacity = 1f;
    public int expandingHeight;
    public boolean transparent;

    private enum Position {
        TOP,
        TOP_LEFT,
        LEFT,
        BOTTOM_LEFT,
        BOTTOM,
        BOTTOM_RIGHT,
        RIGHT,
        TOP_RIGHT;

        Position() {}
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
        repaint();
        this.opacity = f;
        process(this);
    }

    public void setSoftOpacity(float f) {
        repaint();
        this.opacity = f;
    }

    void process(UPanel parent) {
        for (Component child : parent.getComponents()) {
            if(child instanceof UPanel) {
                UPanel pane = (UPanel) child;
                pane.setOpacity(opacity);
                process(pane);
            }
        }
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
        CACHE.put(Double.valueOf((double)this.shadowSize + (double)this.shadowColor.hashCode() * 0.3D + (double)this.shadowOpacity * 0.12D), images);

    }

    @Override
    protected void paintComponent(Graphics g) {
        if(!transparent)
        if(shadowColor != null) {
            g.setColor(getBackground());
            g.fillRect(shadowSize+1, shadowSize+1, getWidth() - shadowSize * 2-2, getHeight() - shadowSize * 2-2);
        } else {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity > 1f ? 1f : opacity));

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
            g2.drawImage(images.get(Position.LEFT), topShadowRect.x, topShadowRect.y, topShadowRect.width, topShadowRect.height, null);
        }

        if(this.showBottomShadow) {
            topShadowRect = new Rectangle(bottomLeftShadowPoint.x + this.shadowSize, y + height - this.shadowSize, bottomRightShadowPoint.x - bottomLeftShadowPoint.x - this.shadowSize, this.shadowSize);
            g2.drawImage(images.get(Position.BOTTOM), topShadowRect.x, topShadowRect.y, topShadowRect.width, topShadowRect.height, null);
        }

        if(this.showRightShadow) {
            topShadowRect = new Rectangle(x + width - this.shadowSize, topRightShadowPoint.y + this.shadowSize, this.shadowSize, bottomRightShadowPoint.y - topRightShadowPoint.y - this.shadowSize);
            g2.drawImage(images.get(Position.RIGHT), topShadowRect.x, topShadowRect.y, topShadowRect.width, topShadowRect.height, null);
        }

        if(this.showTopShadow) {
            topShadowRect = new Rectangle(topLeftShadowPoint.x + this.shadowSize, y, topRightShadowPoint.x - topLeftShadowPoint.x - this.shadowSize, this.shadowSize);
            g2.drawImage(images.get(Position.TOP), topShadowRect.x, topShadowRect.y, topShadowRect.width, topShadowRect.height, null);
        }

        if(this.showLeftShadow || this.showTopShadow) {
            g2.drawImage(images.get(Position.TOP_LEFT), topLeftShadowPoint.x, topLeftShadowPoint.y, null);
        }

        if(this.showLeftShadow || this.showBottomShadow) {
            g2.drawImage(images.get(Position.BOTTOM_LEFT), bottomLeftShadowPoint.x, bottomLeftShadowPoint.y, null);
        }

        if(this.showRightShadow || this.showBottomShadow) {
            g2.drawImage(images.get(Position.BOTTOM_RIGHT), bottomRightShadowPoint.x, bottomRightShadowPoint.y, null);
        }

        if(this.showRightShadow || this.showTopShadow) {
            g2.drawImage(images.get(Position.TOP_RIGHT), topRightShadowPoint.x, topRightShadowPoint.y, null);
        }

        g2.dispose();
    }


    private BufferedImage getSubImage(BufferedImage img, int x, int y, int w, int h) {
        BufferedImage ret = GraphicsUtilities.createCompatibleTranslucentImage(w, h);
        Graphics2D g2 = ret.createGraphics();
        g2.drawImage(img, 0, 0, w, h, x, y, x + w, y + h, null);
        g2.dispose();
        return ret;
    }


}
