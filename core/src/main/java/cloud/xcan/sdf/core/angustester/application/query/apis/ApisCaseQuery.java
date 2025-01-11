package cloud.xcan.sdf.core.angustester.application.query.apis;

import cloud.xcan.angus.model.element.http.ApisCaseType;
import cloud.xcan.sdf.core.angustester.domain.apis.Apis;
import cloud.xcan.sdf.core.angustester.domain.apis.cases.ApisCase;
import cloud.xcan.sdf.core.angustester.domain.apis.cases.ApisCaseInfo;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApisCaseQuery {

  ApisCase detail(Long id);

  Page<ApisCaseInfo> list(GenericSpecification<ApisCaseInfo> spec, Pageable pageable);

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

  void setSafeCloneName(ApisCase funcCase);

}
