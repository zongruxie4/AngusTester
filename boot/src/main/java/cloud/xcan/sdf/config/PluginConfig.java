/*
 * Copyright (c) 2021   CN=XiaoCan Technology (Beijing) Co.，Ltd
 *
 *        http://www.xcan.company
 *
 * Licensed under the XCBL(XCan Business License) Version 1.0.
 * Detail XCBL license at:
 *
 * http://www.xcan.company/licenses/XCBL-1.0
 */
package cloud.xcan.sdf.config;

import cloud.xcan.sdf.plugin.core.PluginStateListener;
import cloud.xcan.sdf.plugin.spring.SpringPluginManager;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class PluginConfig {

  @Bean
  @ConditionalOnMissingBean
  public SpringPluginManager pluginManager(
      @Autowired(required = false) List<PluginStateListener> listeners) {
    SpringPluginManager springPluginManager = new SpringPluginManager();
    if (ObjectUtils.isNotEmpty(listeners)){
      for (PluginStateListener pluginStateListener : listeners) {
        springPluginManager.addPluginStateListener(pluginStateListener);
      }
    }
    return springPluginManager;
  }

}
