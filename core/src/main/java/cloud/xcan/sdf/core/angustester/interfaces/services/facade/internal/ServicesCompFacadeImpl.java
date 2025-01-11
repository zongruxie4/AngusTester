package cloud.xcan.sdf.core.angustester.interfaces.services.facade.internal;

import cloud.xcan.sdf.core.angustester.application.cmd.services.ServicesCompCmd;
import cloud.xcan.sdf.core.angustester.application.query.services.ServicesCompQuery;
import cloud.xcan.sdf.core.angustester.domain.services.comp.ServicesComp;
import cloud.xcan.sdf.core.angustester.domain.services.comp.ServicesCompType;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.ServicesCompFacade;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.internal.assembler.ServicesCompAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.comp.ServicesCompDetailVo;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
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
    return Objects.isNull(comp) ? null : ServicesCompAssembler.toDetailVo(comp);
  }

  @NameJoin
  @Override
  public List<ServicesCompDetailVo> listByType(Long serviceId, Set<ServicesCompType> types,
      Set<String> keys, Boolean ignoreModel) {
    List<ServicesComp> comps = servicesCompQuery.listByType(serviceId, types, keys);
    return CollectionUtils.isEmpty(comps) ? null
        : comps.stream().map(x -> ServicesCompAssembler.toDetailVo(x, ignoreModel))
            .collect(Collectors.toList());
  }

  @NameJoin
  @Override
  public List<ServicesCompDetailVo> listByRef(Long serviceId, Set<String> refs,
      Boolean ignoreModel) {
    List<ServicesComp> comps = servicesCompQuery.listByRef(serviceId, refs);
    return CollectionUtils.isEmpty(comps) ? null :
        comps.stream().map(x -> ServicesCompAssembler.toDetailVo(x, ignoreModel))
            .collect(Collectors.toList());
  }

  @NameJoin
  @Override
  public List<ServicesCompDetailVo> listAll(Long serviceId, Boolean ignoreModel) {
    List<ServicesComp> comps = servicesCompQuery.listAll(serviceId);
    return CollectionUtils.isEmpty(comps) ? null :
        comps.stream().map(x -> ServicesCompAssembler.toDetailVo(x, ignoreModel))
            .collect(Collectors.toList());
  }

}
