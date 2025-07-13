package cloud.xcan.angus.extension.angustester.deepseek.plugin;


import cloud.xcan.angus.plugin.core.Plugin;
import cloud.xcan.angus.plugin.core.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeepSeekTextTranslatePlugin extends Plugin {

  public final Logger log = LoggerFactory.getLogger(DeepSeekTextTranslatePlugin.class);

  public DeepSeekTextTranslatePlugin(PluginWrapper wrapper) {
    super(wrapper);
  }

  @Override
  public void start() {
    log.info("DeepSeekTextTranslatePlugin is started");
  }

  @Override
  public void stop() {
    log.info("DeepSeekTextTranslatePlugin is stopped");
  }

}
