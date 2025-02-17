package cloud.xcan.sdf.core.angustester.interfaces.project.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.commonlink.user.UserInfo;
import cloud.xcan.sdf.core.angustester.domain.ExampleDataType;
import cloud.xcan.sdf.core.angustester.domain.project.ProjectType;
import cloud.xcan.sdf.core.angustester.interfaces.project.facade.dto.ProjectAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.project.facade.dto.ProjectFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.project.facade.dto.ProjectReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.project.facade.dto.ProjectSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.project.facade.dto.ProjectUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.project.facade.vo.ProjectDetailVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.List;
import java.util.Set;

public interface ProjectFacade {

  IdKey<Long, Object> add(ProjectAddDto dto);

  void update(ProjectUpdateDto dto);

  IdKey<Long, Object> replace(ProjectReplaceDto dto);

  IdKey<Long, Object> importExample(String name, ProjectType type, Set<ExampleDataType> dataTypes);

  void delete(Long id);

  List<ProjectDetailVo> userJoined(Long userId, String name);

  ProjectDetailVo detail(Long id);

  List<UserInfo> userMember(Long id);

  PageResult<ProjectDetailVo> list(ProjectFindDto dto);

  PageResult<ProjectDetailVo> search(ProjectSearchDto dto);

}
