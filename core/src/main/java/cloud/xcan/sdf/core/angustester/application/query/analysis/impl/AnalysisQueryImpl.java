package cloud.xcan.sdf.core.angustester.application.query.analysis.impl;

import static cloud.xcan.sdf.core.angustester.application.converter.AnalysisConverter.getAnalysisCreatorResourcesFilter;
import static cloud.xcan.sdf.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getTenantId;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.isNull;

import cloud.xcan.sdf.api.commonlink.TesterConstant;
import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.api.manager.UserManager;
import cloud.xcan.sdf.api.message.http.ResourceExisted;
import cloud.xcan.sdf.api.message.http.ResourceNotFound;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.analysis.AnalysisQuery;
import cloud.xcan.sdf.core.angustester.application.query.analysis.AnalysisSnapshotQuery;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncCaseQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectMemberQuery;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskQuery;
import cloud.xcan.sdf.core.angustester.domain.analysis.Analysis;
import cloud.xcan.sdf.core.angustester.domain.analysis.AnalysisRepo;
import cloud.xcan.sdf.core.angustester.domain.analysis.AnalysisResource;
import cloud.xcan.sdf.core.angustester.domain.analysis.snapshot.AnalysisSnapshot;
import cloud.xcan.sdf.core.angustester.domain.task.count.AbstractOverview;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.core.utils.SpringAppDirUtils;
import cloud.xcan.sdf.spec.utils.FileUtils;
import cloud.xcan.sdf.spec.utils.JsonUtils;
import java.io.File;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class AnalysisQueryImpl implements AnalysisQuery {

  @Resource
  private AnalysisRepo analysisRepo;

  @Resource
  private AnalysisSnapshotQuery analysisSnapshotQuery;

  @Resource
  private TaskQuery taskQuery;

  @Resource
  private FuncCaseQuery funcCaseQuery;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private UserManager userManager;

  @Override
  public Analysis detail(Long id) {
    return new BizTemplate<Analysis>() {
      Analysis analysisDb;

      @Override
      protected void checkParams() {
        analysisDb = checkAndFind(id);
      }

      @Override
      protected Analysis process() {
        AnalysisSnapshot snapshot = null;
        if (analysisDb.getDatasource().isSnapshotData()) {
          snapshot = analysisSnapshotQuery.checkAndFindByAnalysisId(analysisDb.getId());
          analysisDb.setDataObj(parseSnapshotDataString(analysisDb, snapshot.getData()));
        }
        if (isNull(snapshot)) {
          analysisDb.setDataObj(getSnapshotData(analysisDb));
        }
        return analysisDb;
      }
    }.execute();
  }

  @Override
  public Page<Analysis> find(GenericSpecification<Analysis> spec, PageRequest pageable) {
    return new BizTemplate<Page<Analysis>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(spec.getCriterias());
      }

      @Override
      protected Page<Analysis> process() {
        return analysisRepo.findAll(spec, pageable);
      }
    }.execute();
  }

  @Override
  public File overviewExport(Long id) {
    return new BizTemplate<File>() {
      Analysis analysisDb;

      @Override
      protected void checkParams() {
        analysisDb = detail(id);
      }

      @SneakyThrows
      @Override
      protected File process() {
        String fileName = analysisDb.getName() + "-" + System.currentTimeMillis() + ".xlsx";
        String filePath = new SpringAppDirUtils().getTmpDir() + TesterConstant.EXPORT_ANALYSIS_DIR
            + getTenantId() + File.separator + fileName;
        File file = new File(filePath);
        FileUtils.forceMkdirParent(file);
        analysisDb.getDataObj().writeExcelFile(file);
        return file;
      }
    }.execute();
  }

  @Override
  public void checkExits(Long projectId, String name) {
    long count = analysisRepo.countByProjectIdAndName(projectId, name);
    if (count > 0) {
      throw ResourceExisted.of(name, "Analysis");
    }
  }

  @Override
  public Analysis checkAndFind(Long id) {
    return analysisRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "Analysis"));
  }

  @Override
  public List<Analysis> checkAndFind(Collection<Long> ids) {
    List<Analysis> analyses = analysisRepo.findAllById(ids);
    assertResourceNotFound(isNotEmpty(analyses), ids.iterator().next(), "Report");
    if (ids.size() != analyses.size()) {
      for (Analysis analysis : analyses) {
        assertResourceNotFound(ids.contains(analysis.getId()), analysis.getId(), "Report");
      }
    }
    return analyses;
  }

  @Override
  public AbstractOverview getSnapshotData(Analysis analysis) {
    AbstractOverview data = null;
    if (analysis.getResource().isTask()) {
      data = taskQuery.assembleTaskAnalysisSnapshot(analysis);
    }
    if (analysis.getResource().isCase()) {
      data = funcCaseQuery.assembleCaseAnalysisSnapshot(analysis);
    }
    return data;
  }

  @Override
  public String getSnapshotDataString(Analysis analysis) {
    return JsonUtils.toJson(getSnapshotData(analysis));
  }

  @Override
  public AbstractOverview parseSnapshotDataString(Analysis analysis, String snapshot) {
    if (isEmpty(snapshot)) {
      return null;
    }
    AbstractOverview data = null;
    if (analysis.getResource().isTask()) {
      data = taskQuery.parseTaskAnalysisSnapshot(analysis.getTemplate(), snapshot);
    }
    if (analysis.getResource().isCase()) {
      data = funcCaseQuery.parseCaseAnalysisSnapshot(analysis.getTemplate(), snapshot);
    }
    return data;
  }

  @Override
  public List<Analysis> getAnalysisCreatedSummaries(AnalysisResource resource, Long projectId,
      Long sprintId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      AuthObjectType creatorOrgType, Long creatorOrgId) {
    // Find all when condition is null, else find by condition
    Set<Long> creatorIds = Objects.isNull(creatorOrgType) ? null
        : userManager.getUserIdByOrgType0(creatorOrgType, creatorOrgId);
    Set<SearchCriteria> allFilters = getAnalysisCreatorResourcesFilter(
        resource, projectId, sprintId, createdDateStart, createdDateEnd, creatorIds);
    return analysisRepo.findAllByFilters(allFilters);
  }

}
