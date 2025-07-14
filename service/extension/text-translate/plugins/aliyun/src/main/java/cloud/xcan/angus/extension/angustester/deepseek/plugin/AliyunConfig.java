package cloud.xcan.angus.extension.angustester.deepseek.plugin;

import cloud.xcan.angus.spec.setting.AppSettingHelper.Setting;
import java.util.Properties;

/**
 * Configuration holder for Aliyun translation service
 */
public class AliyunConfig {

  private String apiKey;
  private String apiSecret;
  //private String apiRegionId;

  private String apiEndpoint = "mt.cn-hangzhou.aliyuncs.com";
  private String context = null;
  private int maxRetries = 3;
  private long initialRetryDelayMs = 1000;
  private double backoffFactor = 2.0;
  private int timeoutSeconds = 15;

  public AliyunConfig() {
  }

  public AliyunConfig(String apiKey, String apiSecret) {
    this.apiKey = apiKey;
    this.apiSecret = apiSecret;
  }

  public AliyunConfig(String apiKey, String apiSecret, String apiEndpoint,
      String context, int maxRetries, long initialRetryDelayMs, double backoffFactor,
      int timeoutSeconds) {
    this.apiKey = apiKey;
    this.apiSecret = apiSecret;
    this.apiEndpoint = apiEndpoint;
    this.context = context;
    this.maxRetries = maxRetries;
    this.initialRetryDelayMs = initialRetryDelayMs;
    this.backoffFactor = backoffFactor;
    this.timeoutSeconds = timeoutSeconds;
  }

  // Load configuration from Properties
  public AliyunConfig fromProperties(Setting setting) {
    AliyunConfig config = new AliyunConfig();
    this.apiKey = setting.getString("api.key", "");
    this.apiSecret = setting.getString("api.secret", "");
    this.apiEndpoint = setting.getString("api.endpoint", this.apiEndpoint);

    // Retry configuration
    this.maxRetries = setting.getInt("max.retries", this.maxRetries);
    this.initialRetryDelayMs = setting.getLong("retry.initialDelayMs", this.initialRetryDelayMs);
    this.backoffFactor = setting.getDouble("retry.backoffFactor", this.backoffFactor);
    this.timeoutSeconds = setting.getInt("timeout.seconds", this.timeoutSeconds);
    return config;
  }

  // Getters and Setters
  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public String getApiSecret() {
    return apiSecret;
  }

  public void setApiSecret(String apiSecret) {
    this.apiSecret = apiSecret;
  }

  public String getApiEndpoint() {
    return apiEndpoint;
  }

  public void setApiEndpoint(String apiEndpoint) {
    this.apiEndpoint = apiEndpoint;
  }

  public String getContext() {
    return context;
  }

  public void setContext(String context) {
    this.context = context;
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
