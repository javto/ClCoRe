package image_resizer_server;

/**
 * Exception class for error handling.
 * @author wrent
 */
class ImageResizerException extends Exception {
    private String message;
    
    /**
     * Initializes new exception.
     * @param message exception message
     */
    public ImageResizerException(String message) {
        this.message = message;
    }
    
    /**
     * Returns the exception message.
     * @return exception message.
     */
    @Override
    public String getMessage() {
        return message;
    }
}
