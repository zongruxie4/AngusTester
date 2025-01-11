package cloud.xcan.sdf.core.angustester.interfaces.version.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.domain.version.SoftwareVersionStatus;
import cloud.xcan.sdf.core.angustester.interfaces.version.facade.dto.SoftwareVersionAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.version.facade.dto.SoftwareVersionFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.version.facade.dto.SoftwareVersionReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.version.facade.dto.SoftwareVersionSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.version.facade.dto.SoftwareVersionUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.version.facade.vo.SoftwareVersionDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.version.facade.vo.SoftwareVersionVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;

public interface SoftwareVersionFacade {

  IdKey<Long, Object> add(SoftwareVersionAddDto dto);

  void update(SoftwareVersionUpdateDto dto);

  IdKey<Long, Object> replace(SoftwareVersionReplaceDto dto);

  void status(Long id, SoftwareVersionStatus status);

  void merge(Long formId, Long toId);

  void delete(Collection<Long> ids);

  SoftwareVersionDetailVo detail(Long id);

  PageResult<SoftwareVersionVo> list(SoftwareVersionFindDto dto);

  PageResult<SoftwareVersionVo> search(SoftwareVersionSearchDto dto);

}
