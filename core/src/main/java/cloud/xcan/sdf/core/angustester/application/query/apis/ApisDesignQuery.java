package cloud.xcan.sdf.core.angustester.application.query.apis;

import cloud.xcan.sdf.core.angustester.domain.apis.ApisBasicInfo;
import cloud.xcan.sdf.core.angustester.domain.apis.design.ApisDesign;
import cloud.xcan.sdf.core.angustester.domain.apis.design.ApisDesignInfo;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
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

  void setServicesName(ApisDesign design);

  void setServicesName(List<ApisDesignInfo> designs);

}
