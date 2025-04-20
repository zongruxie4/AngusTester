package cloud.xcan.angus.extension.angustester.plugin;


import cloud.xcan.angus.plugin.core.Plugin;
import cloud.xcan.angus.plugin.core.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenapiImportPlugin extends Plugin {

  public final Logger log = LoggerFactory.getLogger(OpenapiImportPlugin.class);

  public OpenapiImportPlugin(PluginWrapper wrapper) {
    super(wrapper);
  }

  @Override
  public void start() {
    log.info("OpenapiImportPlugin is started");
  }

  @Override
  public void stop() {
    log.info("OpenapiImportPlugin is stopped");
  }

}
