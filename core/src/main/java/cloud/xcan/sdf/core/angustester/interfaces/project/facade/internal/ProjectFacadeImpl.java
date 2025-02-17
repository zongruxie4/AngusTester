package cloud.xcan.sdf.core.angustester.interfaces.project.facade.internal;

import static cloud.xcan.sdf.core.angustester.interfaces.project.facade.internal.assembler.ProjectAssembler.addDtoToDomain;
import static cloud.xcan.sdf.core.angustester.interfaces.project.facade.internal.assembler.ProjectAssembler.getSearchCriteria;
import static cloud.xcan.sdf.core.angustester.interfaces.project.facade.internal.assembler.ProjectAssembler.getSpecification;
import static cloud.xcan.sdf.core.angustester.interfaces.project.facade.internal.assembler.ProjectAssembler.replaceDtoToDomain;
import static cloud.xcan.sdf.core.angustester.interfaces.project.facade.internal.assembler.ProjectAssembler.toDetailVo;
import static cloud.xcan.sdf.core.angustester.interfaces.project.facade.internal.assembler.ProjectAssembler.updateDtoToDomain;
import static cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.commonlink.user.UserBase;
import cloud.xcan.sdf.api.commonlink.user.UserInfo;
import cloud.xcan.sdf.core.angustester.application.cmd.project.ProjectCmd;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectSearch;
import cloud.xcan.sdf.core.angustester.domain.ExampleDataType;
import cloud.xcan.sdf.core.angustester.domain.project.Project;
import cloud.xcan.sdf.core.angustester.domain.project.ProjectType;
import cloud.xcan.sdf.core.angustester.interfaces.project.facade.ProjectFacade;
import cloud.xcan.sdf.core.angustester.interfaces.project.facade.dto.ProjectAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.project.facade.dto.ProjectFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.project.facade.dto.ProjectReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.project.facade.dto.ProjectSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.project.facade.dto.ProjectUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.project.facade.internal.assembler.ProjectAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.project.facade.vo.ProjectDetailVo;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ProjectFacadeImpl implements ProjectFacade {

  @Resource
  private ProjectCmd projectCmd;

  @Resource
  private ProjectQuery projectQuery;

  @Resource
  private ProjectSearch projectSearch;

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
    return projects.stream().map(ProjectAssembler::toDetailVo).collect(Collectors.toList());
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
            .setFullname(x.getFullname()).setAvatar(x.getAvatar()))
        .collect(Collectors.toList());
  }

  @Override
  @NameJoin
  public PageResult<ProjectDetailVo> list(ProjectFindDto dto) {
    Page<Project> nodePage = projectQuery.find(getSpecification(dto), dto.tranPage());
    return buildVoPageResult(nodePage, ProjectAssembler::toDetailVo);
  }

  @Override
  @NameJoin
  public PageResult<ProjectDetailVo> search(ProjectSearchDto dto) {
    Page<Project> nodePage = projectSearch.search(getSearchCriteria(dto),
        dto.tranPage(), Project.class, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(nodePage, ProjectAssembler::toDetailVo);
  }
}
