package cloud.xcan.angus.extension.angustester.deepseek.plugin;

import cloud.xcan.angus.extension.angustester.deepseek.api.TranslationService;
import cloud.xcan.angus.spec.locale.SupportedLanguage;

public class DeepSeekTranslationServiceTest {

  public static void main(String[] args) {
    // Programmatic configuration with custom retry settings
    DeepSeekConfig config = new DeepSeekConfig("");
    config.setMaxRetries(5); // Allow up to 5 retries
    config.setInitialRetryDelayMs(500); // Start with 500ms delay
    config.setBackoffFactor(1.5); // Linear backoff factor

    TranslationService translator = new DeepSeekTranslationService(config);

    try {
      String result = translator.translate("Distributed systems design", SupportedLanguage.zh_CN);
      System.out.println("Translation: " + result);
    } catch (Exception e) {
      System.err.println("Translation failed: " + e.getMessage());
    }
  }

}
