package cloud.xcan.sdf.core.angustester.application.cmd.analysis.impl;

import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.FUNC_CASE_ANALYSIS;
import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.TASK_ANALYSIS;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.sdf.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.core.utils.CoreUtils.copyPropertiesIgnoreNull;
import static cloud.xcan.sdf.core.utils.CoreUtils.copyPropertiesIgnoreTenantAuditing;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.sdf.core.angustester.application.cmd.activity.ActivityCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.analysis.AnalysisCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.analysis.AnalysisSnapshotCmd;
import cloud.xcan.sdf.core.angustester.application.query.analysis.AnalysisQuery;
import cloud.xcan.sdf.core.angustester.application.query.analysis.AnalysisSnapshotQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectMemberQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectQuery;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityType;
import cloud.xcan.sdf.core.angustester.domain.analysis.Analysis;
import cloud.xcan.sdf.core.angustester.domain.analysis.AnalysisRepo;
import cloud.xcan.sdf.core.angustester.domain.analysis.snapshot.AnalysisSnapshot;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import java.util.List;
import javax.annotation.Resource;
import javax.transaction.Transactional;

@Biz
public class AnalysisCmdImpl extends CommCmd<Analysis, Long> implements AnalysisCmd {

  @Resource
  private AnalysisRepo analysisRepo;

  @Resource
  private AnalysisQuery analysisQuery;

  @Resource
  private AnalysisSnapshotCmd analysisSnapshotCmd;

  @Resource
  private AnalysisSnapshotQuery analysisSnapshotQuery;

  @Resource
  private ProjectQuery projectQuery;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private ActivityCmd activityCmd;

  @Transactional(rollbackOn = Exception.class)
  @Override
  public IdKey<Long, Object> add(Analysis analysis) {
    return new BizTemplate<IdKey<Long, Object>>() {
      @Override
      protected void checkParams() {
        // Check the project exists
        projectQuery.checkAndFind(analysis.getProjectId());
        // Check the project member
        projectMemberQuery.checkMember(getUserId(), analysis.getProjectId());
        // Check the name exists
        analysisQuery.checkExits(analysis.getProjectId(), analysis.getName());
        // Check the plan or sprint id
        assertTrue(!analysis.isAnalysisPlan() || nonNull(analysis.getPlanId()),
            "Analysis plan or sprint id is required");
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Save analysis
        IdKey<Long, Object> idKey = insert(analysis);

        // Generate snapshot data
        if (analysis.getDatasource().isSnapshotData()) {
          AnalysisSnapshot snapshot = new AnalysisSnapshot();
          snapshot.setAnalysisId(idKey.getId());
          snapshot.setData(analysisQuery.getSnapshotDataString(analysis));
          analysisSnapshotCmd.add0(snapshot);
        }

        // Save activity
        activityCmd.add(toActivity(analysis.getResource().isTask()
            ? TASK_ANALYSIS : FUNC_CASE_ANALYSIS, analysis, ActivityType.CREATED));
        return idKey;
      }
    }.execute();
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public void update(Analysis analysis) {
    new BizTemplate<Void>() {
      Analysis analysisDb;

      @Override
      protected void checkParams() {
        // Check the analysis exists
        analysisDb = analysisQuery.checkAndFind(analysis.getId());
        // Check the analysis exists
        if (isNotEmpty(analysis.getName()) && !analysisDb.getName().equals(analysis.getName())) {
          analysisQuery.checkExits(analysisDb.getProjectId(), analysis.getName());
        }
      }

      @Override
      protected Void process() {
        boolean modified = (
            (isNull(analysis.getDatasource()) && analysisDb.getDatasource().isSnapshotData())
                || (nonNull(analysis.getDatasource()) && analysis.getDatasource().isSnapshotData())
        ) && analysis.notSameConfigAs(analysisDb);

        analysisRepo.save(copyPropertiesIgnoreNull(analysis, analysisDb));

        if (modified) {
          AnalysisSnapshot as = analysisSnapshotQuery.findByAnalysisId(analysisDb.getId());
          analysisSnapshotCmd.replace0(as, analysis, analysisDb);
        }

        activityCmd.add(toActivity(analysisDb.getResource().isTask()
            ? TASK_ANALYSIS : FUNC_CASE_ANALYSIS, analysisDb, ActivityType.UPDATED));
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public IdKey<Long, Object> replace(Analysis analysis) {
    return new BizTemplate<IdKey<Long, Object>>() {
      Analysis analysisDb;

      @Override
      protected void checkParams() {
        if (nonNull(analysis.getId())) {
          // Check the analysis exists
          analysisDb = analysisQuery.checkAndFind(analysis.getId());
          // Check the analysis name exists
          if (isNotEmpty(analysis.getName()) && !analysisDb.getName().equals(analysis.getName())) {
            analysisQuery.checkExits(analysisDb.getProjectId(), analysis.getName());
          }
        }
      }

      @Override
      protected IdKey<Long, Object> process() {
        if (isNull(analysis.getId())) {
          return add(analysis);
        }

        boolean modified = (
            (isNull(analysis.getDatasource()) && analysisDb.getDatasource().isSnapshotData())
                || (nonNull(analysis.getDatasource()) && analysis.getDatasource().isSnapshotData())
        ) && analysis.notSameConfigAs(analysisDb);

        analysisRepo.save(copyPropertiesIgnoreTenantAuditing(analysis, analysisDb,
            "projectId", "resource", "template"));

        if (modified) {
          AnalysisSnapshot as = analysisSnapshotQuery.checkAndFindByAnalysisId(analysisDb.getId());
          analysisSnapshotCmd.replace0(as, analysis, analysisDb);
        }

        activityCmd.add(toActivity(analysisDb.getResource().isTask()
            ? TASK_ANALYSIS : FUNC_CASE_ANALYSIS, analysisDb, ActivityType.UPDATED));

        return new IdKey<Long, Object>().setId(analysisDb.getId()).setKey(analysisDb.getName());
      }
    }.execute();
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public void refresh(Long id) {
    new BizTemplate<Void>() {
      Analysis analysisDb;

      @Override
      protected void checkParams() {
        // Check the analysis exists
        analysisDb = analysisQuery.checkAndFind(id);
        // Check the required snapshot
        assertTrue(analysisDb.getDatasource().isSnapshotData(), "Analysis is not snapshot");
      }

      @Override
      protected Void process() {
        AnalysisSnapshot as = analysisSnapshotQuery.findByAnalysisId(analysisDb.getId());
        if (nonNull(as)) {
          as.setData(analysisQuery.getSnapshotDataString(analysisDb));
          analysisSnapshotCmd.update0(as);
        } else {
          AnalysisSnapshot snapshot = new AnalysisSnapshot();
          snapshot.setAnalysisId(analysisDb.getId());
          snapshot.setData(analysisQuery.getSnapshotDataString(analysisDb));
          analysisSnapshotCmd.add0(snapshot);
        }
        activityCmd.add(toActivity(analysisDb.getResource().isTask()
            ? TASK_ANALYSIS : FUNC_CASE_ANALYSIS, analysisDb, ActivityType.UPDATED));
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public void delete(Collection<Long> ids) {
    new BizTemplate<Void>() {
      List<Analysis> analysisDb;

      @Override
      protected void checkParams() {
        analysisDb = analysisQuery.checkAndFind(ids);
      }

      @Override
      protected Void process() {
        analysisRepo.deleteByIdIn(ids);
        analysisSnapshotCmd.deleteByAnalysisId(ids);

        activityCmd.addAll(toActivities(analysisDb.get(0).getResource().isTask()
            ? TASK_ANALYSIS : FUNC_CASE_ANALYSIS, analysisDb, ActivityType.DELETED));
        return null;
      }
    }.execute();
  }

  @Override
  protected BaseRepository<Analysis, Long> getRepository() {
    return this.analysisRepo;
  }
}
