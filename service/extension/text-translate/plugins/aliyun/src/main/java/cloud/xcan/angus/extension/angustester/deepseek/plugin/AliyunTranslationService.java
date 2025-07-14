package cloud.xcan.angus.extension.angustester.deepseek.plugin;

import cloud.xcan.angus.extension.angustester.deepseek.api.TranslationService;
import cloud.xcan.angus.plugin.api.Extension;
import cloud.xcan.angus.spec.locale.SupportedLanguage;
import com.aliyun.alimt20181012.Client;
import com.aliyun.alimt20181012.models.TranslateRequest;
import com.aliyun.alimt20181012.models.TranslateResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DeepSeek translation service with robust retry mechanism
 */
@Extension
public class AliyunTranslationService implements TranslationService {

  private static final Logger log = LoggerFactory.getLogger(
      AliyunTranslationService.class.getName());
  private static final String DEFAULT_CONFIG_FILE = "translation.properties";

  private static final SupportedLanguage DEFAULT_SOURCE_LANGUAGE = SupportedLanguage.en;

  private AliyunConfig config;
  private Client client;
  private final ObjectMapper jsonMapper = new ObjectMapper();

  // SPI-compatible constructor (uses properties file)
  public AliyunTranslationService() throws Exception {
    configureClient(loadConfigFromProperties());
  }

  // Programmatic configuration constructor
  public AliyunTranslationService(AliyunConfig config) throws Exception {
    if (config.getApiKey() == null || config.getApiKey().isBlank()) {
      throw new IllegalArgumentException("API key must be provided");
    }
    configureClient(config);
  }

  private void configureClient(AliyunConfig config) throws Exception {
    this.config = config;
    this.client = createClient(this.config);
  }

  // Load configuration from properties file
  private AliyunConfig loadConfigFromProperties() {
    try (var input = getClass().getClassLoader().getResourceAsStream(DEFAULT_CONFIG_FILE)) {
      if (input == null) {
        throw new IllegalStateException("Configuration file not found: " + DEFAULT_CONFIG_FILE);
      }

      Properties prop = new Properties();
      prop.load(input);
      return AliyunConfig.fromProperties(prop);
    } catch (Exception e) {
      throw new RuntimeException("Failed to load configuration", e);
    }
  }

  @Override
  public String translate(String text, SupportedLanguage targetLanguage) {
    return translate(text, DEFAULT_SOURCE_LANGUAGE, targetLanguage);
  }

  @Override
  public String translate(String text, SupportedLanguage sourceLanguage,
      SupportedLanguage targetLanguage) {
    // Create retryable translation task
    Callable<String> translationTask = () -> {
      com.aliyun.alimt20181012.models.TranslateRequest request = buildRequest(text,
          sourceLanguage, targetLanguage);
      return executeRequest(request);
    };

    // Execute with retry logic
    try {
      return executeWithRetry(translationTask);
    } catch (Exception e) {
      throw new RuntimeException("Translation failed after " + config.getMaxRetries() + " attempts",
          e);
    }
  }

  /**
   * Executes a task with exponential backoff retry logic
   */
  private <T> T executeWithRetry(Callable<T> task) throws Exception {
    int attempt = 0;
    Exception lastError = null;
    long delay = config.getInitialRetryDelayMs();

    while (attempt <= config.getMaxRetries()) {
      try {
        return task.call();
      } catch (Exception ex) {
        lastError = ex;
        attempt++;

        if (attempt > config.getMaxRetries()) {
          log.warn("Translation attempt {} failed", attempt);
          break;
        }

        // Log warning with retry information
        log.warn("Translation attempt {} failed. Retrying in {} ms. Error: {}",
            attempt, delay, ex.getMessage());

        // Apply jitter to avoid thundering herd problem
        long jitter = (long) (ThreadLocalRandom.current().nextDouble(0.8, 1.2) * delay);
        Thread.sleep(jitter);

        // Increase delay for next attempt
        delay = (long) (delay * config.getBackoffFactor());
      }
    }

    throw new Exception("Max retries exceeded", lastError);
  }

  private com.aliyun.alimt20181012.models.TranslateRequest buildRequest(String text,
      SupportedLanguage sourceLanguage, SupportedLanguage targetLanguage) {
    return new com.aliyun.alimt20181012.models.TranslateRequest()
        .setFormatType("text")
        .setTargetLanguage(safeLanguage(targetLanguage))
        .setSourceLanguage(safeLanguage(sourceLanguage))
        .setSourceText(text)
        .setScene("description");
  }

  private String executeRequest(TranslateRequest request) throws Exception {
    RuntimeOptions runtime = new RuntimeOptions();
    TranslateResponse response;
    try {
      response = client.translateWithOptions(request, runtime);
    } catch (TeaException error) {
      throw new RetryableException("API returned retryable error: " + error.message);
    } catch (Exception _error) {
      TeaException error = new TeaException(_error.getMessage(), _error);
      throw new RetryableException("API returned retryable error: " + error.message);
    }
    return response.getBody().getData().getTranslated();
  }

  public void setConfig(AliyunConfig config) throws Exception {
    configureClient(config);
  }

  private Client createClient(AliyunConfig config) throws Exception {
    Config aliConfig = new Config();
    aliConfig.endpoint = config.getApiEndpoint();
    aliConfig.accessKeyId = config.getApiKey();
    aliConfig.accessKeySecret = config.getApiSecret();
    return new Client(aliConfig);
  }

  private String safeLanguage(SupportedLanguage language) {
    return SupportedLanguage.zh_CN.equals(language) ? "zh" : language.getValue();
  }

  /**
   * Custom exception for retryable errors
   */
  private static class RetryableException extends RuntimeException {

    public RetryableException(String message) {
      super(message);
    }
  }
}
