package cloud.xcan.angus.core.tester.application.cmd.script.impl;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import org.springframework.stereotype.Service;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.script.ScriptTagCmd;
import cloud.xcan.angus.core.tester.domain.script.tag.ScriptTag;
import cloud.xcan.angus.core.tester.domain.script.tag.ScriptTagRepo;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of script tag command operations.
 *
 * <p>This class provides functionality for managing script tags, including adding,
 * replacing, cloning, and deleting tags associated with scripts.</p>
 *
 * <p>It handles tag operations in a batch manner for better performance and
 * maintains consistency with the script lifecycle management.</p>
 */
@Service
public class ScriptTagCmdImpl extends CommCmd<ScriptTag, Long> implements
    ScriptTagCmd {

  @Resource
  private ScriptTagRepo scriptTagRepo;

  /**
   * Adds multiple tags to a script.
   *
   * <p>This method creates new tag associations for the specified script.
   * If the tags collection is empty, the method returns without performing any operations.</p>
   *
   * @param scriptId the ID of the script to add tags to
   * @param tags     collection of tag names to add
   */
  @Override
  public void add(Long scriptId, Collection<String> tags) {
    if (isEmpty(tags)) {
      return;
    }

    List<ScriptTag> scriptTags = tags.stream()
        .map(tagName -> new ScriptTag().setScriptId(scriptId).setName(tagName))
        .toList();
    batchInsert0(scriptTags);
  }

  /**
   * Replaces all existing tags for a script with new ones.
   *
   * <p>This method first removes all existing tags for the script, then adds
   * the new tags. This ensures a clean replacement without duplicate tags.</p>
   *
   * @param scriptId the ID of the script to replace tags for
   * @param tags     collection of new tag names to replace existing tags
   */
  @Override
  public void replace(Long scriptId, Collection<String> tags) {
    scriptTagRepo.deleteByScriptId(scriptId);
    add(scriptId, tags);
  }

  /**
   * Clones tags from one script to another.
   *
   * <p>This method copies all tags from the source script to the target script.
   * It's typically used during script cloning operations to maintain tag consistency.</p>
   *
   * @param sourceScriptId the ID of the source script to copy tags from
   * @param targetScriptId the ID of the target script to copy tags to
   */
  @Override
  public void clone(Long sourceScriptId, Long targetScriptId) {
    Set<String> tags = scriptTagRepo.findByScriptId(sourceScriptId).stream()
        .map(ScriptTag::getName)
        .collect(Collectors.toSet());
    add(targetScriptId, tags);
  }

  /**
   * Deletes all tags for multiple scripts.
   *
   * <p>This method is typically used during bulk operations such as script deletion
   * or cleanup to remove all associated tags efficiently.</p>
   *
   * @param scriptIds collection of script IDs whose tags should be deleted
   */
  @Override
  public void deleteByScriptIdIn(Collection<Long> scriptIds) {
    scriptTagRepo.deleteByScriptIdIn(scriptIds);
  }

  /**
   * Returns the repository instance for this command.
   *
   * @return the script tag repository
   */
  @Override
  protected BaseRepository<ScriptTag, Long> getRepository() {
    return scriptTagRepo;
  }
}
