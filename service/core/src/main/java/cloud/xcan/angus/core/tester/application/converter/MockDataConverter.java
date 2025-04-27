package cloud.xcan.angus.core.tester.application.converter;

import cloud.xcan.angus.model.element.mock.data.MockData;
import cloud.xcan.angus.model.script.AngusScript;
import cloud.xcan.angus.model.script.configuration.Configuration;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.model.script.info.Info;
import cloud.xcan.angus.model.script.pipeline.Task;

public class MockDataConverter {

  public static AngusScript toAngusScript(String plugin, MockData mockData,
      Configuration configuration) {
    return new AngusScript().setType(ScriptType.MOCK_DATA)
        .setPlugin(plugin)
        .setInfo(Info.newBuilder().name(mockData.getName()).build())
        .setConfiguration(configuration.setIterations(mockData.getSettings().getRows()))
        .setTask(Task.newBuilder().mockData(mockData).build());
  }
}
