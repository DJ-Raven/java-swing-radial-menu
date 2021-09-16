package com.raven.radial_menu;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;
import org.jdesktop.animation.timing.TimingTargetAdapter;

public class RadialMenu extends JComponent {

    public Color getColorHover() {
        return colorHover;
    }

    public void setColorHover(Color colorHover) {
        this.colorHover = colorHover;
    }

    public int getButtonSize() {
        return buttonSize;
    }

    public void setButtonSize(int buttonSize) {
        this.buttonSize = buttonSize;
    }

    public int getItemSize() {
        return itemSize;
    }

    public void setItemSize(int itemSize) {
        this.itemSize = itemSize;
    }

    private int buttonSize = 50;
    private int itemSize = 35;
    private float animateSize = 0f;
    private Animator animator;
    private boolean showing;
    private boolean mouseOver;
    private Color colorHover;
    private final List<EventRadialMenu> events = new ArrayList<>();
    private final List<RadialItem> items = new ArrayList<>();

    public RadialMenu() {
        setBackground(new Color(20, 176, 211));
        setForeground(new Color(250, 250, 250));
        colorHover = new Color(42, 205, 241);
        TimingTarget target = new TimingTargetAdapter() {
            @Override
            public void timingEvent(float fraction) {
                if (showing) {
                    animateSize = 1f - fraction;
                } else {
                    animateSize = fraction;
                }
                repaint();
            }

            @Override
            public void end() {
                showing = !showing;
            }

        };
        animator = new Animator(500, target);
        animator.setResolution(0);
        animator.setAcceleration(0.5f);
        animator.setDeceleration(0.5f);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                if (SwingUtilities.isLeftMouseButton(me)) {
                    mouseOver = isMouseOverMenu(me);
                    if (mouseOver) {
                        if (!animator.isRunning()) {
                            animator.start();
                        }
                    } else {
                        int index = isMouseOverItem(me);
                        if (index >= 0) {
                            runEvent(index);
                        }
                    }
                }
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent me) {
                boolean over = isMouseOverMenu(me);
                if (over || isMouseOverItem(me) >= 0) {
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
                if (over != mouseOver) {
                    mouseOver = over;
                    repaint();
                }
            }
        });
    }

    @Override
    public void paint(Graphics grphcs) {
        Graphics2D g2 = (Graphics2D) grphcs;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (!items.isEmpty()) {
            int width = getWidth();
            int height = getHeight();
            int size = (int) ((Math.min(width, height) / 2) - (itemSize / 1.5f));
            int centerX = width / 2;
            int centerY = height / 2;
            float anglePerItem = 360 / items.size();
            float currentAnimate = animateSize;
            for (int i = 0; i < items.size(); i++) {
                RadialItem item = items.get(i);
                float angle = 90 + i * anglePerItem;
                Point location = toLocation(angle, size * currentAnimate);
                g2.setColor(item.getColor());
                int itemX = centerX + location.x - itemSize / 2;
                int itemY = centerY - location.y - itemSize / 2;
                g2.fillOval(itemX, itemY, itemSize, itemSize);
                //  Create Icon
                int iconX = itemX + ((itemSize - item.getIcon().getIconWidth()) / 2);
                int iconY = itemY + ((itemSize - item.getIcon().getIconHeight()) / 2);
                g2.drawImage(toImage(item.getIcon()), iconX, iconY, null);
            }
        }
        createMenuButton(g2);
        g2.dispose();
    }

    private void createMenuButton(Graphics2D g2) {
        int width = getWidth();
        int height = getHeight();
        int x = (width - buttonSize) / 2;
        int y = (height - buttonSize) / 2;
        if (mouseOver) {
            g2.setColor(colorHover);
        } else {
            g2.setColor(getBackground());
        }
        g2.fillOval(x, y, buttonSize, buttonSize);
        int stroke = 3;
        g2.setColor(getForeground());
        int lineSize = (int) (buttonSize - (buttonSize * 0.5f));    //  50% of button sise
        int lineSpace = lineSize / 3;
        int lineX = (width - lineSize) / 2;
        int lineY = height / 2;
        g2.setStroke(new BasicStroke(stroke));
        int startY = lineY - lineSpace;
        int endY = lineY + lineSpace;
        double space = animateSize * (endY - startY);
        startY += space;
        endY -= space;
        g2.drawLine(lineX, lineY - lineSpace, lineX + lineSize, startY);
        g2.drawLine(lineX, lineY + lineSpace, lineX + lineSize, endY);
        float alpha = 1f - animateSize;
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2.drawLine(lineX, lineY, lineX + lineSize, lineY);
    }

    private Point toLocation(float angle, double size) {
        int x = (int) (Math.cos(Math.toRadians(angle)) * size);
        int y = (int) (Math.sin(Math.toRadians(angle)) * size);
        return new Point(x, y);
    }

    private boolean isMouseOverMenu(MouseEvent me) {
        int width = getWidth();
        int height = getHeight();
        int x = (width - buttonSize) / 2;
        int y = (height - buttonSize) / 2;
        Shape s = new Ellipse2D.Double(x, y, buttonSize, buttonSize);
        return s.contains(me.getPoint());
    }

    private int isMouseOverItem(MouseEvent me) {
        int index = -1;
        if (showing) {
            int width = getWidth();
            int height = getHeight();
            int centerX = width / 2;
            int centerY = height / 2;
            float anglePerItem = 360 / items.size();
            int size = (int) ((Math.min(width, height) / 2) - (itemSize / 1.5f));
            for (int i = 0; i < items.size(); i++) {
                float angle = 90 + i * anglePerItem;
                Point location = toLocation(angle, size * 1f);
                int itemX = centerX + location.x - itemSize / 2;
                int itemY = centerY - location.y - itemSize / 2;
                Shape shape = new Ellipse2D.Double(itemX, itemY, itemSize, itemSize);
                if (shape.contains(me.getPoint())) {
                    return i;
                }
            }
        }
        return index;
    }

    private void runEvent(int index) {
        for (EventRadialMenu event : events) {
            event.menuSelected(index);
        }
    }

    public void addItem(RadialItem item) {
        items.add(item);
    }

    public Image toImage(Icon icon) {
        return ((ImageIcon) icon).getImage();
    }

    public void setShowMenu(boolean show) {
        if (showing != show) {
            if (!animator.isRunning()) {
                animator.start();
            }
        }
    }

    public void addEvent(EventRadialMenu event) {
        this.events.add(event);
    }
}
