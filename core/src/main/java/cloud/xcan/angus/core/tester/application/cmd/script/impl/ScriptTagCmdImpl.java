package cloud.xcan.angus.core.tester.application.cmd.script.impl;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.core.biz.Biz;
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

@Biz
public class ScriptTagCmdImpl extends CommCmd<ScriptTag, Long> implements
    ScriptTagCmd {

  @Resource
  private ScriptTagRepo scriptTagTargetRepo;

  @Override
  public void add(Long id, Collection<String> tags) {
    if (isEmpty(tags)) {
      return;
    }

    List<ScriptTag> tagTargets = tags.stream().map(x ->
        new ScriptTag().setScriptId(id).setName(x))
        .collect(Collectors.toList());
    batchInsert0(tagTargets);
  }

  @Override
  public void replace(Long id, Collection<String> tags) {
    scriptTagTargetRepo.deleteByScriptId(id);
    add(id, tags);
  }

  @Override
  public void clone(Long id, Long newId) {
    Set<String> tags = scriptTagTargetRepo.findByScriptId(id).stream()
        .map(ScriptTag::getName).collect(Collectors.toSet());
    add(newId, tags);
  }

  @Override
  public void deleteByScriptIdIn(Collection<Long> ids) {
    scriptTagTargetRepo.deleteByScriptIdIn(ids);
  }

  @Override
  protected BaseRepository<ScriptTag, Long> getRepository() {
    return scriptTagTargetRepo;
  }

}
