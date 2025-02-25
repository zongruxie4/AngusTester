package cloud.xcan.sdf.core.angustester.application.cmd.func.impl;

import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.FUNC_CASE;
import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.FUNC_CASE_BASELINE;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.sdf.core.angustester.application.converter.FuncBaselineCaseConverter.toBaselineCases;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.core.angustester.application.cmd.activity.ActivityCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.func.FuncBaselineCaseCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.func.FuncBaselineCmd;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncBaselineQuery;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncPlanAuthQuery;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncPlanQuery;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityType;
import cloud.xcan.sdf.core.angustester.domain.func.baseline.FuncBaseline;
import cloud.xcan.sdf.core.angustester.domain.func.baseline.FuncBaselineCase;
import cloud.xcan.sdf.core.angustester.domain.func.baseline.FuncBaselineCaseRepo;
import cloud.xcan.sdf.core.angustester.domain.func.baseline.FuncBaselineRepo;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCase;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseRepo;
import cloud.xcan.sdf.core.angustester.domain.func.plan.FuncPlan;
import cloud.xcan.sdf.core.angustester.domain.func.plan.auth.FuncPlanPermission;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.ProtocolAssert;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.core.utils.CoreUtils;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

@Biz
public class FuncBaselineCmdImpl extends CommCmd<FuncBaseline, Long> implements FuncBaselineCmd {

  @Resource
  private FuncBaselineRepo funcBaselineRepo;

  @Resource
  private FuncBaselineCaseRepo funcBaselineCaseRepo;

  @Resource
  private FuncBaselineCaseCmd funcBaselineCaseCmd;

  @Resource
  private FuncBaselineQuery funcBaselineQuery;

  @Resource
  private FuncCaseRepo funcCaseRepo;

  @Resource
  private FuncPlanQuery funcPlanQuery;

  @Resource
  private FuncPlanAuthQuery funcPlanAuthQuery;

  @Resource
  private ActivityCmd activityCmd;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(FuncBaseline baseline) {
    return new BizTemplate<IdKey<Long, Object>>() {
      FuncPlan planDb;

      @Override
      protected void checkParams() {
        // Check the plan exists
        planDb = funcPlanQuery.checkAndFind(baseline.getPlanId());
        // Check the establish baseline permission
        funcPlanAuthQuery.checkEstablishBaselineAuth(getUserId(), baseline.getPlanId());
        // Check the baseline use cases and the plan are consistent
        funcPlanQuery.checkConsistent(baseline.getPlanId(), baseline.getCaseIds());
      }

      @Override
      protected IdKey<Long, Object> process() {
        baseline.setProjectId(planDb.getProjectId());
        IdKey<Long, Object> idKey = insert(baseline);

        // Save activity
        activityCmd.add(toActivity(FUNC_CASE_BASELINE, baseline, ActivityType.CREATED));
        return idKey;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(FuncBaseline baseline) {
    new BizTemplate<Void>() {
      FuncBaseline baselineDb;

      @Override
      protected void checkParams() {
        // Check the baseline exists
        baselineDb = funcBaselineQuery.checkAndFind(baseline.getId());
        // Check the establish baseline permission
        funcPlanAuthQuery.checkEstablishBaselineAuth(getUserId(), baselineDb.getPlanId());
      }

      @Override
      protected Void process() {
        funcBaselineRepo.save(CoreUtils.copyPropertiesIgnoreNull(baseline, baselineDb));

        // Save activity
        activityCmd.add(toActivity(FUNC_CASE_BASELINE, baselineDb, ActivityType.UPDATED));
        return null;
      }
    }.execute();
  }

  @Override
  public void update0(FuncBaseline baseline) {
    funcBaselineRepo.save(baseline);
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> replace(FuncBaseline baseline) {
    return new BizTemplate<IdKey<Long, Object>>() {
      FuncBaseline baselineDb;

      @Override
      protected void checkParams() {
        if (nonNull(baseline.getId())) {
          // Check the baseline exists
          baselineDb = funcBaselineQuery.checkAndFind(baseline.getId());
          // Check the establish baseline permission
          funcPlanAuthQuery.checkEstablishBaselineAuth(getUserId(), baselineDb.getPlanId());
        }
      }

      @Override
      protected IdKey<Long, Object> process() {
        if (isNull(baselineDb)) {
          return add(baseline);
        }

        baselineDb.setName(baseline.getName()).setDescription(baseline.getDescription());
        funcBaselineRepo.save(baselineDb);

        // Save activity
        activityCmd.add(toActivity(FUNC_CASE_BASELINE, baselineDb, ActivityType.UPDATED));
        return new IdKey<>(baselineDb.getId(), baselineDb.getName());
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void establish(Long id) {
    new BizTemplate<Void>() {
      FuncBaseline baselineDb;

      @Override
      protected void checkParams() {
        // Check the baseline exists
        baselineDb = funcBaselineQuery.checkAndFind(id);
        // Check the baseline has not been established
        ProtocolAssert.assertTrue(isNull(baselineDb.getEstablishedFlag())
            || !baselineDb.getEstablishedFlag(), "Baseline has been established");
        // Check the baseline use case cannot be empty
        ProtocolAssert.assertTrue(isNotEmpty(baselineDb.getCaseIds()),
            "The baseline case cannot be empty");
        // Check the establish baseline permission
        funcPlanAuthQuery.checkEstablishBaselineAuth(getUserId(), baselineDb.getPlanId());
      }

      @Override
      protected Void process() {
        List<FuncCase> casesDb = funcCaseRepo.findAllByIdIn(baselineDb.getCaseIds());
        if (isNotEmpty(casesDb)) {
          List<FuncBaselineCase> baselineCases = toBaselineCases(id, casesDb);
          funcBaselineCaseCmd.establishBaseline(baselineDb.getCaseIds(), baselineCases);

          baselineDb.setEstablishedFlag(true);
          funcBaselineRepo.save(baselineDb);

          // Save establish baseline activity for case
          activityCmd.addAll(toActivities(FUNC_CASE, casesDb, ActivityType.ESTABLISH_BASELINE));
        }
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Collection<Long> ids) {
    new BizTemplate<Void>() {
      List<FuncBaseline> baselinesDb;

      @Override
      protected void checkParams() {
        // Check the baseline exists
        baselinesDb = funcBaselineQuery.checkAndFind(ids);
        // Check the establish baseline permission
        funcPlanAuthQuery.batchCheckPermission(baselinesDb.stream().map(FuncBaseline::getPlanId)
            .collect(Collectors.toSet()), FuncPlanPermission.ESTABLISH_BASELINE);
      }

      @Override
      protected Void process() {
        funcBaselineRepo.deleteByIdIn(ids);
        funcBaselineCaseRepo.deleteByBaselineIdIn(ids);

        // Save delete baseline activity
        activityCmd.addAll(toActivities(FUNC_CASE_BASELINE, baselinesDb, ActivityType.DELETED));
        return null;
      }
    }.execute();
  }

  @Override
  protected BaseRepository<FuncBaseline, Long> getRepository() {
    return funcBaselineRepo;
  }
}
