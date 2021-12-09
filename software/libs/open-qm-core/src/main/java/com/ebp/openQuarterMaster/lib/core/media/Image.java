package com.ebp.openQuarterMaster.lib.core.media;

import com.ebp.openQuarterMaster.lib.core.MainObject;
import lombok.*;
import org.apache.commons.io.output.WriterOutputStream;

import javax.imageio.ImageIO;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 *
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderClassName = "Builder")
public class Image extends MainObject {
    public static final int IMAGE_SIZE = 250;
    public static final String CONVERTED_IMAGE_FORMAT = "jpg";
    private static final Base64.Encoder BASE_64_ENCODED = Base64.getEncoder();

    public static BufferedImage resize(BufferedImage inputImage) {
        // creates output image
        BufferedImage outputImage = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, inputImage.getType());

        // scales the input image to the output image
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(inputImage, 0, 0, IMAGE_SIZE, IMAGE_SIZE, null);
        g2d.dispose();

        return outputImage;
    }

    public static char[] resizeGetBytes(BufferedImage image) {
        BufferedImage resized = resize(image);

        ByteBuffer byteBuffer;
        {
            CharBuffer charBuffer;
            try (
                    CharArrayWriter writer = new CharArrayWriter();
                    WriterOutputStream os = new WriterOutputStream(writer, StandardCharsets.UTF_8);
            ) {
                ImageIO.write(resized, CONVERTED_IMAGE_FORMAT, os);
                os.flush();

                charBuffer = CharBuffer.wrap(writer.toCharArray());
            } catch (IOException e) {
                throw new IllegalStateException("Somehow failed to write in-memory.", e);
            }
            byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
        }
        byteBuffer = BASE_64_ENCODED.encode(byteBuffer);
        return byteBuffer.asCharBuffer().array();
    }

    @NonNull
    @NotNull
    @NotBlank
    private String title;

    /**
     * The image format of the data held.
     * <p>
     * Examples:
     * <ul>
     *     <li>jpeg</li>
     *     <li>png</li>
     * </ul>
     * <p>
     * TODO:: validator
     */
    private String type;

    /**
     * The base-64 encoded data that makes up the image.
     * <p>
     * TODO:: validator for valid base64
     */
    @NonNull
    @NotNull
    @NotBlank
    private char[] data;

    public Image(String title, BufferedImage image) {
        this(title, CONVERTED_IMAGE_FORMAT, resizeGetBytes(image));
    }

    public String toDataString() {
        //TODO:: research the best way to do this
        StringBuilder sb = new StringBuilder("data:image/");
        sb.append(this.getType());
        sb.append(";base64,");
        sb.append(this.getData());
        return sb.toString();
    }
}
