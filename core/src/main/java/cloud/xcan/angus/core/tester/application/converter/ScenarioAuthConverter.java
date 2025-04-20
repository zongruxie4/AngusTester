package cloud.xcan.angus.core.tester.application.converter;


import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.tester.domain.scenario.auth.ScenarioAuth;
import cloud.xcan.angus.core.tester.domain.scenario.auth.ScenarioPermission;
import cloud.xcan.angus.idgen.UidGenerator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class ScenarioAuthConverter {

  @NotNull
  public static List<ScenarioAuth> toScenarioCreatorAuths(Set<Long> creatorIds, Long scenarioId,
      UidGenerator uidGenerator) {
    return creatorIds.stream()
        .map(creatorId -> new ScenarioAuth().setId(uidGenerator.getUID())
            .setScenarioId(scenarioId)
            .setAuthObjectType(AuthObjectType.USER)
            .setAuthObjectId(creatorId)
            .setAuths(ScenarioPermission.ALL)
            .setCreator(true)).collect(Collectors.toList());
  }

}
