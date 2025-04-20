package cloud.xcan.angus.core.tester.application.cmd.mock;

import cloud.xcan.angus.core.tester.domain.mock.data.MockFuncData;
import cloud.xcan.angus.core.tester.domain.mock.data.MockFuncRequest;
import cloud.xcan.angus.core.tester.domain.mock.data.MockTextData;
import cloud.xcan.angus.core.tester.domain.mock.data.MockTextRequest;
import cloud.xcan.angus.model.element.mock.data.MockData;
import cloud.xcan.angus.model.script.configuration.Configuration;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.List;

public interface MockDataCmd {

  List<Object> mockFunc(String function, int iterations);

  List<MockFuncData> mockFuncInBatch(List<MockFuncRequest> functions);

  List<String> mockText(String text, int iterations);

  List<MockTextData> mockTextInBatch(List<MockTextRequest> texts);

  IdKey<Long, Object> dataScriptAdd(Long scriptId, Long projectId, String plugin,
      Configuration configuration, MockData mockData);

  String dataScriptView(String plugin, Configuration configuration, MockData mockData);

  IdKey<Long, Object> dataGen(Long scriptId, Long projectId, String plugin,
      Configuration configuration, MockData mockData);
}




