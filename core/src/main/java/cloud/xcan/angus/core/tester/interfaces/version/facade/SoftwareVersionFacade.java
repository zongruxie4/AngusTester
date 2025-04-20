package cloud.xcan.angus.core.tester.interfaces.version.facade;

import cloud.xcan.angus.core.tester.domain.version.SoftwareVersionStatus;
import cloud.xcan.angus.core.tester.interfaces.version.facade.dto.SoftwareVersionAddDto;
import cloud.xcan.angus.core.tester.interfaces.version.facade.dto.SoftwareVersionFindDto;
import cloud.xcan.angus.core.tester.interfaces.version.facade.dto.SoftwareVersionReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.version.facade.dto.SoftwareVersionSearchDto;
import cloud.xcan.angus.core.tester.interfaces.version.facade.dto.SoftwareVersionUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.version.facade.vo.SoftwareVersionDetailVo;
import cloud.xcan.angus.core.tester.interfaces.version.facade.vo.SoftwareVersionVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
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
