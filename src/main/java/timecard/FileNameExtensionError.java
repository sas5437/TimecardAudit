package timecard;

public class FileNameExtensionError extends Exception {
  static final String message = "Filename must have extension ";
    
  public FileNameExtensionError(String extension) {
    super(message + extension);

  }
}
