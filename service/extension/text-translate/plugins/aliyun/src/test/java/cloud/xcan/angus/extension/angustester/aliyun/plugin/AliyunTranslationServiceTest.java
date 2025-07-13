package cloud.xcan.angus.extension.angustester.aliyun.plugin;

import cloud.xcan.angus.extension.angustester.deepseek.api.TranslationService;
import cloud.xcan.angus.extension.angustester.deepseek.plugin.AliyunConfig;
import cloud.xcan.angus.extension.angustester.deepseek.plugin.AliyunTranslationService;
import cloud.xcan.angus.spec.locale.SupportedLanguage;

public class AliyunTranslationServiceTest {

  public static void main(String[] args) throws Exception {
    // Programmatic configuration with custom retry settings
    AliyunConfig config = new AliyunConfig("", "");
    config.setMaxRetries(5); // Allow up to 5 retries
    config.setInitialRetryDelayMs(500); // Start with 500ms delay
    config.setBackoffFactor(1.5); // Linear backoff factor

    TranslationService translator = new AliyunTranslationService(config);

    try {
      String result = translator.translate("Distributed systems design", SupportedLanguage.zh_CN);
      System.out.println("Translation: " + result);
    } catch (Exception e) {
      System.err.println("Translation failed: " + e.getMessage());
    }
  }

}
