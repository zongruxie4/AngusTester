package cloud.xcan.angus.extension.angustester.deepseek.api;

import cloud.xcan.angus.plugin.api.ExtensionPoint;
import cloud.xcan.angus.spec.locale.SupportedLanguage;

/**
 * Translation service interface based on SPI mechanism
 */
public interface TranslationService extends ExtensionPoint {

  SupportedLanguage DEFAULT_SOURCE_LANGUAGE = SupportedLanguage.en;

  String translate(String text, SupportedLanguage targetLanguage);

  String translate(String text, SupportedLanguage sourceLanguage, SupportedLanguage targetLanguage);

  TranslationServiceProvider getProvider();

}
