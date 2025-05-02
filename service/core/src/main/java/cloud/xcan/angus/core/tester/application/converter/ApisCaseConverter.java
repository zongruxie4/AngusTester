package cloud.xcan.angus.core.tester.application.converter;

import static cloud.xcan.angus.core.spring.SpringContextHolder.getBean;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;

import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.apis.cases.ApisCase;
import cloud.xcan.angus.idgen.uid.impl.CachedUidGenerator;
import cloud.xcan.angus.model.element.http.ApisCaseType;
import cloud.xcan.angus.model.element.http.Http;
import io.swagger.v3.oas.models.extension.ApisProtocol;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;

public class ApisCaseConverter {

  public static void setApisInfo(List<ApisCase> cases, Apis apisDb) {
    for (ApisCase case0 : cases) {
      case0.setProjectId(apisDb.getProjectId());
      case0.setServicesId(apisDb.getServiceId());
      case0.setApisDeleted(apisDb.getDeleted());
    }
  }

  public static ApisCase setReplaceInfo(ApisCase caseDb, Apis apisDb, ApisCase case0) {
    return caseDb.setId(case0.getId())
        .setName(case0.getName())
        .setDescription(case0.getDescription())
        .setApisId(apisDb.getId())
        .setApisServiceId(apisDb.getServiceId())
        .setProjectId(apisDb.getProjectId())
        //.setEnabled(case0.getEnabled())
        //.setType(case0.getType())
        .setProtocol(case0.getProtocol())
        .setMethod(case0.getMethod())
        .setEndpoint(case0.getEndpoint())
        .setCurrentServer(case0.getCurrentServer())
        .setParameters(case0.getParameters())
        .setRequestBody(case0.getRequestBody())
        .setAuthentication(case0.getAuthentication())
        .setAssertions(case0.getAssertions())
        .setDatasetActionOnEOF(
            nullSafe(case0.getDatasetActionOnEOF(), apisDb.getDatasetActionOnEOF()))
        .setDatasetSharingMode(
            nullSafe(case0.getDatasetSharingMode(), apisDb.getDatasetSharingMode()));
  }

  public static ApisCase toCloneCase(ApisCase caseDb) {
    return new ApisCase().setId(getBean(CachedUidGenerator.class).getUID())
        .setProjectId(caseDb.getProjectId())
        .setServicesId(caseDb.getServicesId())
        .setName(caseDb.getName())
        .setDescription(caseDb.getDescription())
        .setApisId(caseDb.getApisId())
        .setEnabled(caseDb.getEnabled())
        .setType(ApisCaseType.USER_DEFINED)
        .setProtocol(caseDb.getProtocol())
        .setMethod(caseDb.getMethod())
        .setEndpoint(caseDb.getEndpoint())
        .setCurrentServer(caseDb.getCurrentServer())
        .setParameters(caseDb.getParameters())
        .setRequestBody(caseDb.getRequestBody())
        .setAuthentication(caseDb.getAuthentication())
        .setAssertions(caseDb.getAssertions())
        .setApisDeleted(caseDb.getApisDeleted())
        .setExecTestNum(0)
        .setExecTestFailureNum(0);
  }

  public static ApisCase httpToFuncCase(Apis apis, Http case0) {
    return new ApisCase()
        .setId(nullSafe(case0.getCaseId(), getBean(CachedUidGenerator.class).getUID()))
        .setProjectId(apis.getProjectId())
        .setServicesId(apis.getServiceId())
        .setName(case0.getName())
        .setDescription(case0.getDescription())
        .setApisId(apis.getId())
        .setEnabled(case0.isEnabled())
        .setType(case0.getCaseType())
        .setProtocol(ApisProtocol.http)
        .setMethod(apis.getMethod())
        .setEndpoint(apis.getEndpoint())
        .setCurrentServer(isNotEmpty(apis.getCurrentServer())
            && apis.getCurrentServer().isValidUrl() ? apis.getCurrentServer()
            : isNotEmpty(apis.getAvailableServers()) ? apis.getAvailableServers().stream()
                .filter(Server::isValidUrl).findFirst().orElse(null) : null)
        .setParameters(apis.getParameters())
        .setRequestBody(apis.getRequestBody())
        .setAuthentication(apis.getAuthentication())
        .setAssertions(case0.getAssertions())
        .setApisDeleted(apis.getDeleted())
        .setExecTestNum(0)
        .setExecTestFailureNum(0);
  }
}
