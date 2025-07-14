package cloud.xcan.angus.extension.angustester.deepseek.plugin;


import cloud.xcan.angus.plugin.core.Plugin;
import cloud.xcan.angus.plugin.core.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AliyunTextTranslatePlugin extends Plugin {

  public final Logger log = LoggerFactory.getLogger(AliyunTextTranslatePlugin.class);

  public AliyunTextTranslatePlugin(PluginWrapper wrapper) {
    super(wrapper);
  }

  @Override
  public void start() {
    log.info("AliyunTextTranslatePlugin is started");
  }

  @Override
  public void stop() {
    log.info("AliyunTextTranslatePlugin is stopped");
  }

}
