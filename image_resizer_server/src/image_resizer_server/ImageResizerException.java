package image_resizer_server;

/**
 *
 * @author wrent
 */
class ImageResizerException extends Exception {
    private String message;
    
    public ImageResizerException(String message) {
        this.message = message;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
}
