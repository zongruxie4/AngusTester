package cloud.xcan.angus.api.commonlink.exec.result;

import cloud.xcan.angus.api.pojo.Progress;
import cloud.xcan.angus.model.apis.ApiStatus;
import cloud.xcan.angus.model.services.ApisTestCount;
import cloud.xcan.angus.spec.http.HttpMethod;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ExecApisResultInfo {

  private Progress progress;

  private ApisTestCount testApis;

  private TestResultCount testResultCount;

  private List<ExecResultSummary> testResultInfos;

  private LinkedHashMap<ApiStatus, Integer> apisByStatus = new LinkedHashMap<>(); // For AngusTester Report

  private LinkedHashMap<HttpMethod, Integer> apisByMethod = new LinkedHashMap<>(); // For AngusTester Report
}
