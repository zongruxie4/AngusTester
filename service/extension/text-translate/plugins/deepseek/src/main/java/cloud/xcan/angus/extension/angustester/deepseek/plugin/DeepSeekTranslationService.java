package cloud.xcan.angus.extension.angustester.deepseek.plugin;

import cloud.xcan.angus.extension.angustester.deepseek.api.RetryableException;
import cloud.xcan.angus.extension.angustester.deepseek.api.TranslationService;
import cloud.xcan.angus.extension.angustester.deepseek.api.TranslationServiceProvider;
import cloud.xcan.angus.plugin.api.Extension;
import cloud.xcan.angus.spec.locale.MessageHolder;
import cloud.xcan.angus.spec.locale.SupportedLanguage;
import cloud.xcan.angus.spec.setting.AppSettingHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DeepSeek translation service with robust retry mechanism
 */
@Extension
public class DeepSeekTranslationService implements TranslationService {

  private static final Logger log = LoggerFactory.getLogger(
      DeepSeekTranslationService.class.getName());
  private static final String DEFAULT_CONFIG_FILE = "translation.properties";

  private DeepSeekConfig config;
  private final ObjectMapper jsonMapper = new ObjectMapper();

  // SPI-compatible constructor (uses properties file)
  public DeepSeekTranslationService() {
    this.config = loadConfigFromProperties();
  }

  // Programmatic configuration constructor
  public DeepSeekTranslationService(DeepSeekConfig config) {
    if (config.getApiKey() == null || config.getApiKey().isBlank()) {
      throw new IllegalArgumentException("API key must be provided");
    }
    this.config = config;
  }

  // Load configuration from properties file
  private DeepSeekConfig loadConfigFromProperties() {
    return new DeepSeekConfig().fromProperties(
        AppSettingHelper.getSetting(DEFAULT_CONFIG_FILE, DeepSeekTranslationService.class));
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
      HttpRequest request = buildRequest(text,
          MessageHolder.message(sourceLanguage.getMessageKey(), Locale.ENGLISH),
          MessageHolder.message(targetLanguage.getMessageKey(), Locale.ENGLISH));
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

  @Override
  public TranslationServiceProvider getProvider() {
    return TranslationServiceProvider.DeepSeek;
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

  private HttpRequest buildRequest(String text, String sourceLanguage, String targetLanguage) {
    JSONObject requestBody = new JSONObject();
    requestBody.put("model", "deepseek-chat");

    JSONArray messages = new JSONArray();
    JSONObject systemMessage = new JSONObject();
    systemMessage.put("role", "system");
    String prompt = config.getPromptTemplate().replace("{sourceLanguage}", targetLanguage);
    prompt = prompt.replace("{targetLanguage}", targetLanguage);
    systemMessage.put("content", prompt);
    messages.put(systemMessage);

    JSONObject userMessage = new JSONObject();
    userMessage.put("role", "user");
    userMessage.put("content", text);
    messages.put(userMessage);

    requestBody.put("messages", messages);

    return HttpRequest.newBuilder()
        .uri(URI.create(config.getApiEndpoint()))
        .header("Authorization", "Bearer " + config.getApiKey())
        .header("Content-Type", "application/json")
        .timeout(Duration.ofSeconds(config.getTimeoutSeconds()))
        .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
        .build();
  }

  private String executeRequest(HttpRequest request) throws Exception {
    HttpClient client = HttpClient.newHttpClient();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    // Check for server errors (5xx) or rate limits (429)
    int statusCode = response.statusCode();
    if (statusCode >= 500 || statusCode == 429) {
      log.error("Translation failed, status={}, response={}", statusCode, response.body());
      throw new RetryableException("API returned retryable status: " + statusCode);
    }

    if (statusCode != 200) {
      log.error("Translation failed, status={}, response={}", statusCode, response.body());
      throw new RuntimeException("API request failed: " + response.body());
    }

    return parseResponse(response.body());
  }

  private String parseResponse(String jsonBody) throws Exception {
    JsonNode root = jsonMapper.readTree(jsonBody);
    return root.path("choices")
        .path(0)
        .path("message")
        .path("content")
        .asText();
  }

  public void setConfig(DeepSeekConfig config) {
    this.config = config;
  }

}
