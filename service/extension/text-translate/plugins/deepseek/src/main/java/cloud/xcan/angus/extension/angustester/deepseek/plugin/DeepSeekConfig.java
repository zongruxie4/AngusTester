package cloud.xcan.angus.extension.angustester.deepseek.plugin;

import java.util.Properties;

/**
 * Configuration holder for DeepSeek translation service
 */
public class DeepSeekConfig {

  private String apiKey;
  private String apiEndpoint = "https://api.deepseek.com/chat/completions";
  private String promptTemplate = "Please translate the following to {targetLanguage} with accurate technical terms";
  private int maxRetries = 3;
  private long initialRetryDelayMs = 1000;
  private double backoffFactor = 2.0;
  private int timeoutSeconds = 15;

  public DeepSeekConfig() {
  }

  public DeepSeekConfig(String apiKey) {
    this.apiKey = apiKey;
  }

  public DeepSeekConfig(String apiKey, String apiEndpoint, String promptTemplate, int maxRetries,
      long initialRetryDelayMs, double backoffFactor, int timeoutSeconds) {
    this.apiKey = apiKey;
    this.apiEndpoint = apiEndpoint;
    this.promptTemplate = promptTemplate;
    this.maxRetries = maxRetries;
    this.initialRetryDelayMs = initialRetryDelayMs;
    this.backoffFactor = backoffFactor;
    this.timeoutSeconds = timeoutSeconds;
  }

  // Load configuration from Properties
  public static DeepSeekConfig fromProperties(Properties prop) {
    DeepSeekConfig config = new DeepSeekConfig();
    config.apiKey = prop.getProperty("api.key", "");
    config.apiEndpoint = prop.getProperty("api.endpoint", config.apiEndpoint);
    config.promptTemplate = prop.getProperty("prompt.template", config.promptTemplate);

    // Retry configuration
    config.maxRetries = Integer.parseInt(
        prop.getProperty("retry.max", String.valueOf(config.maxRetries)));
    config.initialRetryDelayMs = Long.parseLong(
        prop.getProperty("retry.initialDelayMs", String.valueOf(config.initialRetryDelayMs)));
    config.backoffFactor = Double.parseDouble(
        prop.getProperty("retry.backoffFactor", String.valueOf(config.backoffFactor)));
    config.timeoutSeconds = Integer.parseInt(
        prop.getProperty("timeout.seconds", String.valueOf(config.timeoutSeconds)));

    return config;
  }

  // Getters and Setters
  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public String getApiEndpoint() {
    return apiEndpoint;
  }

  public void setApiEndpoint(String apiEndpoint) {
    this.apiEndpoint = apiEndpoint;
  }

  public String getPromptTemplate() {
    return promptTemplate;
  }

  public void setPromptTemplate(String promptTemplate) {
    this.promptTemplate = promptTemplate;
  }

  public int getMaxRetries() {
    return maxRetries;
  }

  public void setMaxRetries(int maxRetries) {
    this.maxRetries = maxRetries;
  }

  public long getInitialRetryDelayMs() {
    return initialRetryDelayMs;
  }

  public void setInitialRetryDelayMs(long initialRetryDelayMs) {
    this.initialRetryDelayMs = initialRetryDelayMs;
  }

  public double getBackoffFactor() {
    return backoffFactor;
  }

  public void setBackoffFactor(double backoffFactor) {
    this.backoffFactor = backoffFactor;
  }

  public int getTimeoutSeconds() {
    return timeoutSeconds;
  }

  public void setTimeoutSeconds(int timeoutSeconds) {
    this.timeoutSeconds = timeoutSeconds;
  }
}
