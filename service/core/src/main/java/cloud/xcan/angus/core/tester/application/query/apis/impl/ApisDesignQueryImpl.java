package cloud.xcan.angus.core.tester.application.query.apis.impl;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceExisted;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.APIS_DESIGN_SERVICE_EXISTED_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.APIS_SERVICE_DESIGN_EXISTED_T;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.apis.ApisDesignQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesAuthQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesSchemaQuery;
import cloud.xcan.angus.core.tester.domain.apis.ApisBasicInfo;
import cloud.xcan.angus.core.tester.domain.apis.design.ApisDesign;
import cloud.xcan.angus.core.tester.domain.apis.design.ApisDesignInfo;
import cloud.xcan.angus.core.tester.domain.apis.design.ApisDesignInfoRepo;
import cloud.xcan.angus.core.tester.domain.apis.design.ApisDesignRepo;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import io.swagger.v3.core.util.Json31;
import io.swagger.v3.oas.models.OpenAPI;
import jakarta.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class ApisDesignQueryImpl implements ApisDesignQuery {

  @Resource
  private ApisDesignRepo apisDesignRepo;

  @Resource
  private ApisDesignInfoRepo apisDesignInfoRepo;

  @Resource
  private ServicesQuery servicesQuery;

  @Resource
  private ServicesSchemaQuery servicesSchemaQuery;

  @Resource
  private ServicesAuthQuery servicesAuthQuery;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private UserManager userManager;

  @Override
  public ApisDesign detail(Long id) {
    return new BizTemplate<ApisDesign>() {
      ApisDesign designDb;

      @Override
      protected void checkParams() {
        // Check the design exists
        designDb = checkAndFind(id);
        // Check the view permissions
        if (designDb.getDesignSource().isSynchronousService()) {
          servicesAuthQuery.checkViewAuth(getUserId(), designDb.getDesignSourceId());
        } else {
          // Check the project permission
          projectMemberQuery.checkMember(getUserId(), designDb.getProjectId());
        }
      }

      @Override
      protected ApisDesign process() {
        // Fetch the latest design content information
        if (designDb.hasLatestContent()) {
          OpenAPI openapi = servicesSchemaQuery.openapiDetail0(designDb.getDesignSourceId(),
              null, false);
          designDb.setLatestOpenapi(Json31.pretty(openapi));
        } else if (isNotEmpty(designDb.getOpenapi())) {
          designDb.setLatestOpenapi(designDb.getOpenapi());
        }

        setServicesName(designDb);
        return designDb;
      }
    }.execute();
  }

  @Override
  public Page<ApisDesignInfo> list(GenericSpecification<ApisDesignInfo> spec,
      PageRequest pageable, Class<ApisBasicInfo> clz) {
    return new BizTemplate<Page<ApisDesignInfo>>() {
      @Override
      protected void checkParams() {
        // Check the project permission
        projectMemberQuery.checkMember(spec.getCriteria());
      }

      @Override
      protected Page<ApisDesignInfo> process() {
        Page<ApisDesignInfo> page = apisDesignInfoRepo.findAll(spec, pageable);
        if (page.hasContent()) {
          setServicesName(page.getContent());
          userManager.setUserNameAndAvatar(page.getContent(), "createdBy");
        }
        return page;
      }
    }.execute();
  }

  @Override
  public List<ApisDesignInfo> findbyIds(HashSet<Long> ids) {
    return apisDesignInfoRepo.findAllById(ids);
  }

  @Override
  public ApisDesign checkAndFind(Long id) {
    return apisDesignRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "ApisDesign"));
  }

  @Override
  public ApisDesignInfo checkAndFindInfo(Long id) {
    return apisDesignInfoRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "ApisDesign"));
  }

  @Override
  public void checkServiceExisted(Long serviceId) {
    ApisDesignInfo design = apisDesignInfoRepo.findByDesignSourceId(serviceId);
    assertResourceExisted(design, APIS_SERVICE_DESIGN_EXISTED_T, new Object[]{design.getName()});
  }

  @Override
  public void setServicesName(ApisDesign design) {
    if (nonNull(design.getDesignSourceId())) {
      Services services = servicesQuery.find0(design.getDesignSourceId());
      if (nonNull(services)) {
        design.setDesignSourceName(services.getName());
      }
    }
  }

  @Override
  public void setServicesName(List<ApisDesignInfo> designs) {
    Set<Long> servicesIds = designs.stream().map(ApisDesignInfo::getDesignSourceId)
        .filter(Objects::nonNull).collect(Collectors.toSet());
    if (!servicesIds.isEmpty()) {
      Map<Long, Services> servicesMap = servicesQuery.find0ByIds(servicesIds).stream()
          .collect(Collectors.toMap(Services::getId, x -> x));
      for (ApisDesignInfo design : designs) {
        design.setDesignSourceName(servicesMap.getOrDefault(design.getDesignSourceId(),
            new Services()).getName());
      }
    }
  }
}
