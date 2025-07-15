package cloud.xcan.angus.extension.angustester.deepseek.api;

/**
 * Custom exception for retryable errors
 */
public class RetryableException extends RuntimeException {

  public RetryableException(String message) {
    super(message);
  }
}
