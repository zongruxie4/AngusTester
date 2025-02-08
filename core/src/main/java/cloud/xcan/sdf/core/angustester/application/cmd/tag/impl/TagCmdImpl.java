package cloud.xcan.sdf.core.angustester.application.cmd.tag.impl;

import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.TAG;
import static cloud.xcan.sdf.api.commonlink.TesterConstant.SAMPLE_FUNC_PLAN_FILE;
import static cloud.xcan.sdf.api.commonlink.TesterConstant.SAMPLE_TAG_FILE;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.activityParams;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.sdf.core.angustester.application.converter.FuncCaseConverter.assembleExampleFuncPlan;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.CREATED;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.DELETED;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.UPDATED;
import static cloud.xcan.sdf.core.angustester.infra.util.AngusTesterUtils.parseSample;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getDefaultLanguage;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getOptTenantId;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.isUserAction;

import cloud.xcan.sdf.api.commonlink.user.User;
import cloud.xcan.sdf.api.manager.UserManager;
import cloud.xcan.sdf.core.angustester.application.cmd.activity.ActivityCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.tag.TagCmd;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectQuery;
import cloud.xcan.sdf.core.angustester.application.query.tag.TagQuery;
import cloud.xcan.sdf.core.angustester.domain.data.variables.Variable;
import cloud.xcan.sdf.core.angustester.domain.func.plan.FuncPlan;
import cloud.xcan.sdf.core.angustester.domain.project.Project;
import cloud.xcan.sdf.core.angustester.domain.tag.Tag;
import cloud.xcan.sdf.core.angustester.domain.tag.TagRepo;
import cloud.xcan.sdf.core.angustester.domain.tag.TagTargetRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.ProtocolAssert;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.core.utils.CoreUtils;
import cloud.xcan.sdf.spec.experimental.Assert;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

@Biz
public class TagCmdImpl extends CommCmd<Tag, Long> implements TagCmd {

  @Resource
  private TagRepo tagRepo;

  @Resource
  private TagTargetRepo tagTargetRepo;

  @Resource
  private TagQuery tagQuery;

  @Resource
  private ProjectQuery projectQuery;

  @Resource
  private UserManager userManager;

  @Resource
  private ActivityCmd activityCmd;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> add(Long projectId, Set<String> names) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      @Override
      protected void checkParams() {
        // Check the edit permission
        Project projectDb = projectQuery.checkAndFind(projectId);
        projectQuery.checkEditModulePermission(projectDb);
        // Check the names exists
        tagQuery.checkAddNameExist(projectId, names);
        // Check the quota limit
        tagQuery.checkQuota(names.size());
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        List<Tag> tags = names.stream()
            .map(name -> new Tag().setProjectId(projectId).setName(name))
            .collect(Collectors.toList());
        List<IdKey<Long, Object>> idKeys = batchInsert(tags, "name");

        activityCmd.batchAdd(toActivities(TAG, tags, CREATED, activityParams(tags)));
        return idKeys;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(List<Tag> tags) {
    new BizTemplate<Void>() {
      List<Tag> tagsDb;

      @Override
      protected void checkParams() {
        // Check the tags exists
        tagsDb = tagQuery.checkAndFind(tags.stream().map(Tag::getId).collect(Collectors.toList()));
        Set<Long> projectIds = tagsDb.stream().map(Tag::getProjectId)
            .collect(Collectors.toSet());
        ProtocolAssert.assertTrue(projectIds.size() == 1,
            "Only batch adding tags with one project is allowed");
        Long projectId = projectIds.iterator().next();

        // Check the edit permission
        Project projectDb = projectQuery.checkAndFind(projectId);
        projectQuery.checkEditModulePermission(projectDb);

        // Check the tag names exist
        tagQuery.checkUpdateNameExists(projectId, tags);
      }

      @Override
      protected Void process() {
        batchUpdate0(CoreUtils.batchCopyPropertiesIgnoreNull(tags, tagsDb));

        activityCmd.batchAdd(toActivities(TAG, tagsDb, UPDATED));
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> importExample(Long projectId) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      Project projectDb;

      @Override
      protected void checkParams() {
        // Check the project exists
        projectDb = projectQuery.checkAndFind(projectId);
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        URL resourceUrl = this.getClass().getResource("/samples/tag/"
            + getDefaultLanguage().getValue() + "/" + SAMPLE_TAG_FILE);
        List<Tag> tags = parseSample(Objects.requireNonNull(resourceUrl), SAMPLE_TAG_FILE);

        if (!isUserAction()){
          List<User> users = userManager.findByTenantId(getOptTenantId());
          Assert.assertNotEmpty(users, "Tenant users are empty");
          for (Tag tag : tags) {
            tag.setId(uidGenerator.getUID()).setTenantId(projectDb.getTenantId())
                .setCreatedBy(users.get(0).getId()).setLastModifiedBy(users.get(0).getId());
          }
        }

        return batchInsert(tags, "name");
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Collection<Long> ids) {
    new BizTemplate<Void>() {
      List<Tag> tagsDb;

      @Override
      protected void checkParams() {
        // check tag exist
        tagsDb = tagQuery.checkAndFind(ids);

        // Check the one project is allowed
        Set<Long> projectIds = tagsDb.stream().map(Tag::getProjectId)
            .collect(Collectors.toSet());
        ProtocolAssert.assertTrue(projectIds.size() == 1,
            "Only batch adding modules with one project is allowed");
        Long projectId = projectIds.iterator().next();

        // Check the edit permission
        Project projectDb = projectQuery.checkAndFind(projectId);
        projectQuery.checkEditModulePermission(projectDb);
      }

      @Override
      protected Void process() {
        // Delete relation
        tagTargetRepo.deleteByTagIdIn(ids);

        // Delete tags
        tagRepo.deleteByIdIn(ids);

        activityCmd.batchAdd(toActivities(TAG, tagsDb, DELETED));
        return null;
      }
    }.execute();
  }

  @Override
  protected BaseRepository<Tag, Long> getRepository() {
    return this.tagRepo;
  }
}
