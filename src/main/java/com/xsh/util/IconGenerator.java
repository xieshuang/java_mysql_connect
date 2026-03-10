package com.xsh.util;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;

import java.awt.image.BufferedImage;

public class IconGenerator {

    public static BufferedImage createAppIcon() {
        int size = 64;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2d = image.createGraphics();
        
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.setColor(new java.awt.Color(66, 133, 244));
        g2d.fillRoundRect(4, 4, 56, 56, 12, 12);
        
        g2d.setColor(java.awt.Color.WHITE);
        g2d.fillRoundRect(10, 14, 44, 8, 4, 4);
        g2d.fillRoundRect(10, 26, 44, 8, 4, 4);
        g2d.fillRoundRect(10, 38, 30, 8, 4, 4);
        
        g2d.setColor(new java.awt.Color(52, 168, 83));
        g2d.fillRect(38, 36, 16, 12);
        
        g2d.setColor(new java.awt.Color(251, 188, 4));
        g2d.fillRect(38, 36, 16, 4);
        
        g2d.dispose();
        return image;
    }

    public static javafx.scene.image.Image createFxImage() {
        BufferedImage bufferedImage = createAppIcon();
        WritableImage image = new WritableImage(bufferedImage.getWidth(), bufferedImage.getHeight());
        javafx.scene.image.PixelWriter pw = image.getPixelWriter();
        for (int y = 0; y < bufferedImage.getHeight(); y++) {
            for (int x = 0; x < bufferedImage.getWidth(); x++) {
                pw.setArgb(x, y, bufferedImage.getRGB(x, y));
            }
        }
        return image;
    }
}
