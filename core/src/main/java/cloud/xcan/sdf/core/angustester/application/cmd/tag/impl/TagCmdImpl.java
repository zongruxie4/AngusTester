package cloud.xcan.sdf.core.angustester.application.cmd.tag.impl;

import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.TAG;
import static cloud.xcan.sdf.api.commonlink.TesterConstant.SAMPLE_TAG_FILE;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.activityParams;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.CREATED;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.DELETED;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.UPDATED;
import static cloud.xcan.sdf.core.angustester.infra.util.AngusTesterUtils.parseSample;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getDefaultLanguage;

import cloud.xcan.sdf.core.angustester.application.cmd.activity.ActivityCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.tag.TagCmd;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectQuery;
import cloud.xcan.sdf.core.angustester.application.query.tag.TagQuery;
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
import cloud.xcan.sdf.spec.experimental.IdKey;
import com.fasterxml.jackson.core.type.TypeReference;
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

  /**
   * Note: When API calls that are not user-action, tenant and user information must be injected
   * into the PrincipalContext.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> importExample(Long projectId) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        URL resourceUrl = this.getClass().getResource("/samples/tag/"
            + getDefaultLanguage().getValue() + "/" + SAMPLE_TAG_FILE);
        List<Tag> tags = parseSample(Objects.requireNonNull(resourceUrl),
            new TypeReference<List<Tag>>() {
            }, SAMPLE_TAG_FILE);
        for (Tag tag : tags) {
          tag.setProjectId(projectId);
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
