package cloud.xcan.angus.core.tester.interfaces.project.facade;

import cloud.xcan.angus.api.commonlink.user.UserInfo;
import cloud.xcan.angus.core.tester.domain.ExampleDataType;
import cloud.xcan.angus.core.tester.domain.project.ProjectType;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.ProjectAddDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.ProjectFindDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.ProjectImportDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.ProjectReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.ProjectUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.vo.ProjectDetailVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Set;
import org.springframework.http.ResponseEntity;

public interface ProjectFacade {

  IdKey<Long, Object> add(ProjectAddDto dto);

  void update(ProjectUpdateDto dto);

  IdKey<Long, Object> replace(ProjectReplaceDto dto);

  IdKey<Long, Object> importExample(String name, ProjectType type, Set<ExampleDataType> dataTypes);

  IdKey<Long, Object> imports(ProjectImportDto dto);

  void delete(Long id);

  List<ProjectDetailVo> userJoined(Long userId, String name);

  ProjectDetailVo detail(Long id);

  List<UserInfo> userMember(Long id);

  PageResult<ProjectDetailVo> list(ProjectFindDto dto);

  ResponseEntity<org.springframework.core.io.Resource> export(Long projectId, String format,
      jakarta.servlet.http.HttpServletResponse response);
}
