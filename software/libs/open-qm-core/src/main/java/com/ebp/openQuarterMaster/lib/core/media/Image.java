package com.ebp.openQuarterMaster.lib.core.media;

import com.ebp.openQuarterMaster.lib.core.MainObject;
import com.ebp.openQuarterMaster.lib.core.validation.annotations.ValidBase64;
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
    public static String resizeGetBytes(BufferedImage image) {
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
        return new String(data);
    }

    @NonNull
    @NotNull
    @NotBlank
    private String title;

    private String description;

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
    @NonNull
    @NotNull
    @NotBlank
    private String type;

    /**
     * The base-64 encoded data that makes up the image.
     * <p>
     */
    @NonNull
    @NotNull
    @NotBlank
    @ValidBase64
    private String data;

    public Image(String title, BufferedImage image) {
        this(title, null, CONVERTED_IMAGE_FORMAT, resizeGetBytes(image));
    }

    public Image(String title, String description, BufferedImage image) {
        this(title, description, CONVERTED_IMAGE_FORMAT, resizeGetBytes(image));
    }

    public String toDataString() {
        return String.format("data:image/%s;base64,%s", this.getType(), this.getData());
    }

    public Image setDataWithImage(BufferedImage image) {
        this.setData(resizeGetBytes(image));
        return this;
    }

    public int getDataLength() {
        if (this.getData() != null) {
            return this.getData().length();
        }
        return 0;
    }
}
