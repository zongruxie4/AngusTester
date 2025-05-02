package cloud.xcan.angus.core.tester.interfaces.services.facade.internal;

import static cloud.xcan.angus.core.tester.interfaces.services.facade.internal.assembler.ServicesCompAssembler.toDetailVo;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;

import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.services.ServicesCompCmd;
import cloud.xcan.angus.core.tester.application.query.services.ServicesCompQuery;
import cloud.xcan.angus.core.tester.domain.services.comp.ServicesComp;
import cloud.xcan.angus.core.tester.domain.services.comp.ServicesCompType;
import cloud.xcan.angus.core.tester.interfaces.services.facade.ServicesCompFacade;
import cloud.xcan.angus.core.tester.interfaces.services.facade.vo.comp.ServicesCompDetailVo;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ServicesCompFacadeImpl implements ServicesCompFacade {

  @Resource
  private ServicesCompCmd servicesCompCmd;

  @Resource
  private ServicesCompQuery servicesCompQuery;

  @Override
  public IdKey<Long, Object> replace(Long serviceId, ServicesCompType type, String key,
      String component) {
    return servicesCompCmd.replace(serviceId, type, key, component);
  }

  @Override
  public void deleteByType(Long serviceId, ServicesCompType type, Set<String> keys) {
    servicesCompCmd.deleteByType(serviceId, type, keys);
  }

  @Override
  public void deleteByRef(Long serviceId, Set<String> refs) {
    servicesCompCmd.deleteByRef(serviceId, refs);
  }

  @Override
  public void deleteAll(Long serviceId) {
    servicesCompCmd.deleteAll(serviceId);
  }

  @NameJoin
  @Override
  public ServicesCompDetailVo detailByRef(Long serviceId, String ref) {
    ServicesComp comp = servicesCompQuery.detailByRef(serviceId, ref);
    return isNull(comp) ? null : toDetailVo(comp);
  }

  @NameJoin
  @Override
  public List<ServicesCompDetailVo> listByType(Long serviceId, Set<ServicesCompType> types,
      Set<String> keys, Boolean ignoreModel) {
    List<ServicesComp> comps = servicesCompQuery.listByType(serviceId, types, keys);
    return isEmpty(comps) ? null : comps.stream()
        .map(x -> toDetailVo(x, ignoreModel)).collect(Collectors.toList());
  }

  @NameJoin
  @Override
  public List<ServicesCompDetailVo> listByRef(Long serviceId, Set<String> refs,
      Boolean ignoreModel) {
    List<ServicesComp> comps = servicesCompQuery.listByRef(serviceId, refs);
    return isEmpty(comps) ? null : comps.stream()
        .map(x -> toDetailVo(x, ignoreModel)).collect(Collectors.toList());
  }

  @NameJoin
  @Override
  public List<ServicesCompDetailVo> listAll(Long serviceId, Boolean ignoreModel) {
    List<ServicesComp> comps = servicesCompQuery.listAll(serviceId);
    return isEmpty(comps) ? null : comps.stream()
        .map(x -> toDetailVo(x, ignoreModel)).collect(Collectors.toList());
  }

}
