package cloud.xcan.angus.core.tester.application.query.apis;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.apis.ApisBasicInfo;
import cloud.xcan.angus.core.tester.domain.apis.design.ApisDesign;
import cloud.xcan.angus.core.tester.domain.apis.design.ApisDesignInfo;
import java.util.HashSet;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ApisDesignQuery {

  ApisDesign detail(Long id);

  Page<ApisDesignInfo> list(GenericSpecification<ApisDesignInfo> spec, PageRequest pageable,
      Class<ApisBasicInfo> clz);

  List<ApisDesignInfo> findbyIds(HashSet<Long> ids);

  ApisDesign checkAndFind(Long id);

  ApisDesignInfo checkAndFindInfo(Long id);

  void checkServiceExisted(Long serviceId);

  void setServicesName(ApisDesign design);

  void setServicesName(List<ApisDesignInfo> designs);

}
