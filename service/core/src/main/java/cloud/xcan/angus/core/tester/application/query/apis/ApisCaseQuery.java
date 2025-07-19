package cloud.xcan.angus.core.tester.application.query.apis;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.apis.cases.ApisCase;
import cloud.xcan.angus.core.tester.domain.apis.cases.ApisCaseInfo;
import cloud.xcan.angus.model.element.http.ApisCaseType;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ApisCaseQuery {

  ApisCase detail(Long id);

  Page<ApisCaseInfo> list(GenericSpecification<ApisCaseInfo> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match);

  List<ApisCase> findByApisId(Long apisId);

  List<ApisCase> findByServicesIdAndType(Long servicesId, ApisCaseType apisCaseType);

  Long countByServiceId(Long serviceId);

  void checkCaseNameExists(Apis apisDb, List<ApisCase> cases);

  void checkAndSafeUpdateNameExists(Apis apisDb, List<ApisCase> cases);

  ApisCase checkAndFind(Long id);

  ApisCaseInfo checkAndFindInfo(Long id);

  List<ApisCase> checkAndFind(Collection<Long> ids);

  List<ApisCaseInfo> checkAndFindInfo(Collection<Long> ids);

  void checkExistedCaseType(Long apisId, List<ApisCase> cases);

  void checkUpdateNameExists(Long planId, String name, Long id);

  void checkCaseQuota(int incr, Long apisId);

  void setInfoApisNameAndDeleted(List<ApisCaseInfo> cases);

  void setApisAndServiceInfo(List<ApisCase> cases);

  Map<String, String> findCaseAllRef(ApisCase caseDb);

  void setAndGetRefAuthentication(ApisCase case0);

  void setSafeCloneName(ApisCase funcCase);

}
