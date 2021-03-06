package no.uib.mitraq.util;

/**
 * Enumerator for supported export plot file types.
 *
 * @author Harald Barsnes
 */
public enum ImageType {

    /**
     * Supported image types.
     */
    JPEG(".jpg"), TIFF(".tiff"), PNG(".png"), PDF(".pdf"), SVG(".svg");
    /**
     * The image file extension.
     */
    private String extension;

    /**
     * Constructor setting the image type extension.
     *
     * @param extension
     */
    ImageType(String extension) {
        this.extension = extension;
    }

    /**
     * Returns the extension.
     *
     * @return the extension
     */
    public String getExtension() {
        return this.extension;
    }
}
