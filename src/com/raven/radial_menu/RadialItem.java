package com.raven.radial_menu;

import java.awt.Color;
import javax.swing.Icon;

public class RadialItem {

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public RadialItem(Icon icon, Color color) {
        this.icon = icon;
        this.color = color;
    }

    public RadialItem() {
    }

    private Icon icon;
    private Color color;
}
