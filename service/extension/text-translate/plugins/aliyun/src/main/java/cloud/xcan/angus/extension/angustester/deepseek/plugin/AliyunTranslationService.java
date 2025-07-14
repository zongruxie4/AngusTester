package cloud.xcan.angus.extension.angustester.deepseek.plugin;

import static java.util.Objects.nonNull;

import cloud.xcan.angus.extension.angustester.deepseek.api.RetryableException;
import cloud.xcan.angus.extension.angustester.deepseek.api.TranslationService;
import cloud.xcan.angus.extension.angustester.deepseek.api.TranslationServiceProvider;
import cloud.xcan.angus.plugin.api.Extension;
import cloud.xcan.angus.spec.locale.SupportedLanguage;
import cloud.xcan.angus.spec.setting.AppSettingHelper;
import cloud.xcan.angus.spec.setting.AppSettingHelper.Setting;
import com.aliyun.alimt20181012.Client;
import com.aliyun.alimt20181012.models.TranslateRequest;
import com.aliyun.alimt20181012.models.TranslateResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aliyun translation service with robust retry mechanism.
 * <p>
 * Unable to recognize complex formats, such as Markdown and json format data.
 */
@Extension
public class AliyunTranslationService implements TranslationService {

  private static final Logger log = LoggerFactory.getLogger(
      AliyunTranslationService.class.getName());
  private static final String DEFAULT_CONFIG_FILE = "aliyun-translation.properties";

  private AliyunConfig config;
  private Setting settings;

  private Client client;

  // SPI-compatible constructor (uses properties file)
  public AliyunTranslationService() throws Exception {
    loadConfig();
  }

  // Programmatic configuration constructor
  public AliyunTranslationService(AliyunConfig config) throws Exception {
    if (config.getApiKey() == null || config.getApiKey().isBlank()) {
      throw new IllegalArgumentException("API key must be provided");
    }
    this.config = config;
    this.client = createClient(config);
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
      throw new RuntimeException("Translation failed after "
          + config.getMaxRetries() + " attempts", e);
    }
  }

  @Override
  public TranslationServiceProvider getProvider() {
    return TranslationServiceProvider.Aliyun;
  }

  // Load configuration from properties file and envs
  @Override
  public void loadConfig() throws Exception {
    if (this.settings == null) {
      this.settings = AppSettingHelper.getSetting(DEFAULT_CONFIG_FILE,
          AliyunTranslationService.class);
    }
    this.config = new AliyunConfig().fromProperties(settings);
    this.client = createClient(this.config);
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
      log.error("Translation failed, status={}, response={}", error.code, error.message);
      throw new RetryableException("API returned retryable error: " + error.message);
    } catch (Exception _error) {
      log.error("Translation failed, error={}", _error.getMessage());
      TeaException error = new TeaException(_error.getMessage(), _error);
      throw new RetryableException("API returned retryable error: " + error.message);
    }

    if (nonNull(response.getBody()) && response.getBody().code != 200) {
      log.error("Translation failed, error={}", response.getBody().getMessage());
      throw new IllegalStateException(response.getBody().getMessage());
    }

    return response.getBody().getData().getTranslated();
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

}
