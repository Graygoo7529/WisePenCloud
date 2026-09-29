package com.oriole.wisepen.document.util;

import com.oriole.wisepen.document.config.DocumentProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.AttributedString;

/** 第一页 rawText 的无水印排版图，不尝试还原 PDF 版式。 */
@Component
@RequiredArgsConstructor
public class DocumentPreviewImageGenerator {

    private final DocumentProperties properties;

    public void generatePreviewImage(String rawText, Path output) throws IOException, FontFormatException {
        Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 26);
        if (properties.getCjkRegularFontPath() != null && !properties.getCjkRegularFontPath().isBlank()) {
            try (var input = Files.newInputStream(Path.of(properties.getCjkRegularFontPath()))) {
                font = Font.createFont(Font.TRUETYPE_FONT, input).deriveFont(26f);
            }
        }
        String displayText = rawText.replace("\t", "    ");
        // 只检查实际绘制的字符；换行由布局处理。无合适字体时按首图缺失处理。
        if (font.canDisplayUpTo(displayText.replaceAll("\\R", "")) >= 0) throw new IOException("第一页文本缺少可用字体");
        BufferedImage image = new BufferedImage(900, 1273, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
            graphics.setColor(Color.BLACK);
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            float y = 50;
            for (String paragraph : displayText.split("\\R")) {
                if (y >= image.getHeight() - 50) break;
                if (paragraph.isBlank()) {
                    y += 32;
                    continue;
                }
                AttributedString attributed = new AttributedString(paragraph);
                attributed.addAttribute(TextAttribute.FONT, font);
                LineBreakMeasurer measurer = new LineBreakMeasurer(attributed.getIterator(), graphics.getFontRenderContext());
                while (measurer.getPosition() < paragraph.length()) {
                    TextLayout line = measurer.nextLayout(image.getWidth() - 100);
                    y += line.getAscent();
                    if (y > image.getHeight() - 50) break;
                    line.draw(graphics, 50, y);
                    y += line.getDescent() + line.getLeading() + 6;
                }
            }
        } finally {
            graphics.dispose();
        }
        if (!ImageIO.write(image, "png", output.toFile())) throw new IOException("无法生成 PNG 预览图");
    }
}
