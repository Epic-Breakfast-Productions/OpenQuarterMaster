package com.ebp.openQuarterMaster.lib.core.media;

import com.ebp.openQuarterMaster.lib.core.MainObject;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderClassName = "Builder")
public class Image extends MainObject {
    public static final int IMAGE_SIZE = 250;
    public static final String CONVERTED_IMAGE_FORMAT = "jpg";


    @NonNull
    @NotNull
    @NotBlank
    private String title;

    /**
     * The image format of the data held.
     *
     * Examples:
     * <ul>
     *     <li>jpeg</li>
     *     <li>png</li>
     * </ul>
     *
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

//    public Image(String title, BufferedImage image){
//        BufferedImage resized = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, image.getType());
//
//        CharArrayWriter writer = new CharArrayWriter();
//
//        //TODO:: finish
//
//        ImageIO.write(resized, CONVERTED_IMAGE_FORMAT, new WriterOutwriter);
//        this(title, CONVERTED_IMAGE_FORMAT, os.toByteArray());
//    }

    public String toDataString() {
        StringBuilder sb = new StringBuilder("data:image/");
        sb.append(this.getType());
        sb.append(";base64,");
        sb.append(this.getData());
        //TODO:: research the best way to do this
        return sb.toString();
    }
}
