package cloud.xcan.sdf.model.services;

import cloud.xcan.sdf.model.apis.ApisInfo;
import cloud.xcan.sdf.model.script.TestType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ApisTestCount {

  private long totalApisNum;

  private long enabledTestNum;

  private long enabledFuncTestNum;

  private long enabledPerfTestNum;

  private long enabledStabilityTestNum;

  private List<ApisInfo> allApis;

  private Map<TestType, List<Long>> enabledTestApiIds = new HashMap<>();

}
