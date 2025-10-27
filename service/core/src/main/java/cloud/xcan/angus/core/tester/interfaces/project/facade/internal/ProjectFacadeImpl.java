package cloud.xcan.angus.core.tester.interfaces.project.facade.internal;

import static cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.angus.core.tester.interfaces.project.facade.internal.assembler.ProjectAssembler.addDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.project.facade.internal.assembler.ProjectAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.project.facade.internal.assembler.ProjectAssembler.replaceDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.project.facade.internal.assembler.ProjectAssembler.toDetailVo;
import static cloud.xcan.angus.core.tester.interfaces.project.facade.internal.assembler.ProjectAssembler.updateDtoToDomain;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.api.commonlink.user.UserBase;
import cloud.xcan.angus.api.commonlink.user.UserInfo;
import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.project.ProjectCmd;
import cloud.xcan.angus.core.tester.application.query.project.ProjectQuery;
import cloud.xcan.angus.core.tester.domain.ExampleDataType;
import cloud.xcan.angus.core.tester.domain.project.Project;
import cloud.xcan.angus.core.tester.domain.project.ProjectType;
import cloud.xcan.angus.core.tester.interfaces.project.facade.ProjectFacade;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.ProjectAddDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.ProjectFindDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.ProjectReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.ProjectUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.internal.assembler.ProjectAssembler;
import cloud.xcan.angus.core.tester.interfaces.project.facade.vo.ProjectDetailVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ProjectFacadeImpl implements ProjectFacade {

  @Resource
  private ProjectCmd projectCmd;

  @Resource
  private ProjectQuery projectQuery;

  @Override
  public IdKey<Long, Object> add(ProjectAddDto dto) {
    return projectCmd.add(addDtoToDomain(dto));
  }

  @Override
  public void update(ProjectUpdateDto dto) {
    projectCmd.update(updateDtoToDomain(dto));
  }

  @Override
  public IdKey<Long, Object> replace(ProjectReplaceDto dto) {
    return projectCmd.replace(replaceDtoToDomain(dto));
  }

  @Override
  public IdKey<Long, Object> importExample(String name, ProjectType type,
      Set<ExampleDataType> dataTypes) {
    return projectCmd.importExample(name, type, dataTypes);
  }

  @Override
  public void delete(Long id) {
    projectCmd.delete(id);
  }

  @Override
  //@NameJoin -> NOOP
  public List<ProjectDetailVo> userJoined(Long userId, String name) {
    List<Project> projects = projectQuery.userJoined(userId, name);
    return projects.stream().map(ProjectAssembler::toDetailVo).toList();
  }

  @Override
  @NameJoin
  public ProjectDetailVo detail(Long id) {
    return toDetailVo(projectQuery.detail(id));
  }

  @Override
  public List<UserInfo> userMember(Long id) {
    List<UserBase> users = projectQuery.userMember(id);
    return users.stream().map(x -> new UserInfo().setId(x.getId())
            .setFullName(x.getFullName()).setAvatar(x.getAvatar())).toList();
  }

  @Override
  @NameJoin
  public PageResult<ProjectDetailVo> list(ProjectFindDto dto) {
    Page<Project> page = projectQuery.list(getSpecification(dto), dto.tranPage(),
        dto.fullTextSearch, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, ProjectAssembler::toDetailVo);
  }

}
