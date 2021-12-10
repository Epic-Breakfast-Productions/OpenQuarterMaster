package com.ebp.openQuarterMaster.lib.core.media;

import com.ebp.openQuarterMaster.lib.core.MainObject;
import lombok.*;

import javax.imageio.ImageIO;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    /**
     * The size of the images to keep in pixels
     */
    public static final int IMAGE_SIZE = 500;
    /**
     * The format to keep the images in
     */
    public static final String CONVERTED_IMAGE_FORMAT = "jpg";
    /**
     * Base64 encoder to use to encode image data for storage and eventual presentation.
     */
    private static final Base64.Encoder BASE_64_ENCODED = Base64.getEncoder();

    /**
     * Resizes the given image to what should be held in the object. Resizes image to {@link #IMAGE_SIZE} square.
     * <p>
     * Called in {@link #Image(String, BufferedImage)}.
     *
     * @param inputImage The image to resize
     * @return The resized image
     */
    public static BufferedImage resize(BufferedImage inputImage) {
        // creates output image
        BufferedImage outputImage = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, inputImage.getType());

        // scales the input image to the output image
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(inputImage, 0, 0, IMAGE_SIZE, IMAGE_SIZE, null);
        g2d.dispose();

        return outputImage;
    }

    /**
     * Resizes the image and returns the base64 representation of the resized image.
     *
     * @param image The image to resize, and get the data for
     * @return The image data in base64
     */
    public static char[] resizeGetBytes(BufferedImage image) {
        BufferedImage resized = resize(image);

        byte[] data;
        try (
                ByteArrayOutputStream os = new ByteArrayOutputStream()
        ) {
            ImageIO.write(resized, CONVERTED_IMAGE_FORMAT, os);
            os.flush();
            data = os.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Somehow failed to write in-memory.", e);
        }
        data = BASE_64_ENCODED.encode(data);
        return new String(data).toCharArray();
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

    public Image setDataWithImage(BufferedImage image) {
        this.setData(resizeGetBytes(image));
        return this;
    }

    public int getDataLength() {
        if (this.getData() != null) {
            return this.getData().length;
        }
        return 0;
    }
}
