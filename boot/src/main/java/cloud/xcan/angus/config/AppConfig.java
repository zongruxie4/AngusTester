package cloud.xcan.angus.config;

import cloud.xcan.angus.extraction.DatasetExtractor;
import cloud.xcan.angus.extraction.DefaultDatasetExtractor;
import cloud.xcan.angus.extraction.DefaultVariableExtractor;
import cloud.xcan.angus.extraction.VariableExtractor;
import cloud.xcan.angus.spec.thread.delay.DelayOrderQueueManager;
import cloud.xcan.jmock.core.parser.MockFunctionDocParser;
import cloud.xcan.jmock.core.parser.replacer.DefaultMockExpressionReplacer;
import cloud.xcan.jmock.core.parser.replacer.DefaultMockTextReplacer;
import feign.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

  @Bean
  @ConditionalOnMissingBean
  public Logger.Level feignLoggerLevel() {
    return Logger.Level.FULL;
  }

  @Bean
  public DelayOrderQueueManager delayOrderQueueManager() {
    return new DelayOrderQueueManager();
  }

  @Bean("defaultMockExpressionReplacer")
  public DefaultMockExpressionReplacer defaultMockExpressionReplacer() {
    return new DefaultMockExpressionReplacer();
  }

  @Bean("defaultMockTextReplacer")
  public DefaultMockTextReplacer defaultMockTextReplacer() {
    return new DefaultMockTextReplacer();
  }

  @Bean("mockFunctionDocParser")
  public MockFunctionDocParser mockFunctionDocParser(){
    return new MockFunctionDocParser();
  }

  @Bean("defaultVariableExtractor")
  public VariableExtractor variableExtractor() {
    return new DefaultVariableExtractor();
  }

  @Bean("defaultDatasetExtractor")
  public DatasetExtractor datasetExtractor() {
    return new DefaultDatasetExtractor();
  }

}
