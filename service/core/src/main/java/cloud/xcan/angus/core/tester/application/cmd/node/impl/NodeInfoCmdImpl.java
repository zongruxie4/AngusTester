package cloud.xcan.angus.core.tester.application.cmd.node.impl;

import static cloud.xcan.angus.core.spring.env.AbstractEnvLoader.appDir;
import static cloud.xcan.angus.core.spring.env.EnvKeys.TESTER_APIS_SERVER_URL;
import static cloud.xcan.angus.core.spring.env.EnvKeys.TESTER_HOST;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.spec.experimental.BizConstant.AuthKey.SIGN2P_TOKEN_CLIENT_SCOPE;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getTenantName;
import static cloud.xcan.angus.spec.utils.NetworkUtils.getValidIpv4;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.agent.AgentCommandType;
import cloud.xcan.angus.agent.message.runner.RunnerKillDto;
import cloud.xcan.angus.api.commonlink.client.Client2pSignupBiz;
import cloud.xcan.angus.api.commonlink.node.AgentInstallCmd;
import cloud.xcan.angus.api.commonlink.node.NodeInstallConfig;
import cloud.xcan.angus.api.gm.client.ClientSignInnerRemote;
import cloud.xcan.angus.api.gm.client.ClientSignPubRemote;
import cloud.xcan.angus.api.gm.client.dto.AuthClientSignInDto;
import cloud.xcan.angus.api.gm.client.dto.AuthClientSignupDto;
import cloud.xcan.angus.api.gm.client.vo.AuthClientSignVo;
import cloud.xcan.angus.api.gm.client.vo.AuthClientSignupVo;
import cloud.xcan.angus.api.manager.TenantManager;
import cloud.xcan.angus.api.pojo.auth.AgentAuth;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.spring.boot.ApplicationInfo;
import cloud.xcan.angus.core.spring.env.EnvHelper;
import cloud.xcan.angus.core.tester.application.cmd.node.NodeInfoCmd;
import cloud.xcan.angus.core.tester.application.query.node.NodeInfoQuery;
import cloud.xcan.angus.core.tester.domain.node.info.NodeInfo;
import cloud.xcan.angus.core.tester.domain.node.info.NodeInfoRepo;
import cloud.xcan.angus.core.tester.infra.config.NodeAgentCmdConfig.NodeAgentCmdProperties;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.NodeRunnerKillDto;
import cloud.xcan.angus.remote.message.SysException;
import cloud.xcan.angus.remoting.common.message.ReplyMessage;
import cloud.xcan.angus.remoting.common.router.ChannelRouter;
import cloud.xcan.angus.spec.properties.repo.PropertiesRepo;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.transaction.annotation.Transactional;

/**
 * Command implementation for node information management.
 * <p>
 * Provides methods for updating, deleting, initializing, and managing agent authentication and runner processes for node information.
 * <p>
 * Ensures distributed coordination, agent installation, and integration with external services.
 */
@Slf4j
@Biz
public class NodeInfoCmdImpl implements NodeInfoCmd {

  @Resource
  private NodeInfoRepo nodeInfoRepo;

  @Resource
  private NodeInfoQuery nodeInfoQuery;

  @Resource
  private NodeInfoCmd nodeInfoCmd;

  @Resource
  private NodeAgentCmdProperties agentProperties;

  @Resource
  private ClientSignInnerRemote clientSignInnerRemote;

  @Resource
  private ClientSignPubRemote clientSignPubRemote;

  @Resource
  private DiscoveryClient discoveryClient;

  @Resource
  private TenantManager tenantManager;

  @Resource
  private ApplicationInfo appInfo;

  /**
   * Update node information.
   * <p>
   * Saves the node information to the repository.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update0(NodeInfo nodeInfo){
    nodeInfoRepo.save(nodeInfo);
  }

  /**
   * Delete node information by IDs.
   * <p>
   * Removes node information records from the repository.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Collection<Long> ids) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        nodeInfoRepo.deleteByIdIn(ids);
        return null;
      }
    }.execute();
  }

  /**
   * Generate agent installation command for a node.
   * <p>
   * Initializes node info, saves agent authentication, and returns installation command.
   */
  @Override
  public AgentInstallCmd agentInstallCmd(Long nodeId) {
    return new BizTemplate<AgentInstallCmd>(false) {
      final Long tenantId = getOptTenantId();

      @Override
      protected AgentInstallCmd process() {
        NodeInfo nodeInfo = nodeInfoCmd.initAgentNodeInfo(tenantId, nodeId);
        nodeInfoCmd.saveAgentAuthInfo(tenantId, nodeId, nodeInfo);
        return getAgentInstallCmd(nodeInfo);
      }

      private AgentInstallCmd getAgentInstallCmd(NodeInfo nodeInfo) {
        return new AgentInstallCmd()
            .setLinuxInstallScriptName(agentProperties.getLinuxInstallScriptName())
            .setLinuxDownloadInstallScriptCmd(buildDownloadLinuxInstallCmd())
            .setLinuxRunInstallScriptCmd(buildLinuxRunInstallCmd(nodeInfo))
            .setLinuxOnlineInstallCmd(buildLinuxOnlineInstallCmd(nodeInfo))
            .setLinuxOfflineInstallSteps(buildLinuxOfflineInstallSteps(nodeInfo))
            .setWindowsInstallScriptName(agentProperties.getWindowsInstallScriptName())
            .setWindowsDownloadInstallScriptCmd(buildDownloadWindowsInstallCmd())
            .setWindowsRunInstallScriptCmd(buildWindowsRunInstallCmd(nodeInfo))
            .setWindowsOnlineInstallCmd(buildWindowsOnlineInstallCmd(nodeInfo))
            .setWindowsOfflineInstallSteps(buildWindowsOfflineInstallSteps(nodeInfo))
            .setInstallConfig(new NodeInstallConfig()
                .setTenantId(tenantId).setDeviceId(nodeInfo.getId())
                .setServerCtrlUrlPrefix(agentProperties.getServerCtrlUrlPrefix())
                .setCtrlAccessToken(nodeInfo.getAgentAuth().getAccessToken())
            );
      }



      /**
       * curl -s http://192.168.0.102:1805/filepxy/pubapi/v1/object/download/install-agent.sh?id=96204965327929444 -o install-agent.sh --retry 3 -m 120
       */
      private String buildDownloadLinuxInstallCmd() {
        return "curl -s " + getLinuxInstallScriptUrl() + " -o " + agentProperties
            .getLinuxInstallScriptName()
            + " --retry 3 -m 120 --connect-timeout 10 "; // -m : --max-time SECONDS  Maximum time allowed for the transfer
        // --connect-timeout : SECONDS  Maximum time allowed for connection
      }

      private String getLinuxInstallScriptUrl() {
        return agentProperties.getInstallScriptUrlPrefix() + "/"
            + agentProperties.getLinuxInstallScriptName() + "?fid=" + agentProperties
            .getLinuxInstallScriptId();
      }

      /**
       * ./install-agent.sh 1.0.0 99360265199419407 'http://localhost:6808/openapi2p/v1/ctrl' a0694ac2-e5e7-4eda-a29d-afd447b6c5e5 1 1
       */
      private String buildLinuxRunInstallCmd(NodeInfo nodeInfo) {
        return "./" + agentProperties.getLinuxInstallScriptName() + " "
            + agentProperties.getAgentVersion() + " " + agentProperties.getAgentFileId() + " \\\n '"
            + agentProperties.getServerCtrlUrlPrefix() + "' \\\n"
            + nodeInfo.getAgentAuth().getAccessToken() + " \\\n"
            + tenantId + " " + nodeId;
      }

      /**
       * curl -s http://192.168.0.102:1805/storage/pubapi/v1/object/download/install-agent.sh?id=96204965327929444 | bash -s 1.0.0 99360265199419407 'http://localhost:6808/openapi2p/v1/ctrl' a0694ac2-e5e7-4eda-a29d-afd447b6c5e5 1 1
       */
      private String buildLinuxOnlineInstallCmd(NodeInfo nodeInfo) {
        return "curl -s \"" + getLinuxInstallScriptUrl() + "\" | bash -s "
            + agentProperties.getAgentVersion() + " " + agentProperties.getAgentFileId() + " \\\n '"
            + agentProperties.getServerCtrlUrlPrefix() + "' \\\n"
            + nodeInfo.getAgentAuth().getAccessToken() + " \\\n"
            + tenantId + " " + nodeId;
      }

      /**
       *  Linux或者Mac系统自动安装步骤(必须以root用户执行脚本)：
       *  <pre>
       *  安装方式1：
       *    curl -s http://192.168.0.102:1805/filepxy/pubapi/v1/object/download/install-agent.sh?id=96204965327929444 | bash -s 1.0.0 99360265199419407 'http://localhost:6808/openapi2p/v1/ctrl' a0694ac2-e5e7-4eda-a29d-afd447b6c5e5 1 1
       *  安装方式2：
       *    1).下载自动安装脚本：<a href="http://192.168.0.102:1805/filepxy/pubapi/v1/object/download/install-agent.sh?id=96204965327929444">install-agent.sh</a>
       *    2).将install-agent.sh复制到自定义的安装目录，运行脚本安装：./install-agent.sh 1.0.0 99360265199419407 http://192.168.1.7:1834/openapi2p/v1/ctrl a0694ac2-e5e7-4eda-a29d-afd447b6c5e5 1 1
       *
       *  // I18N
       *  // 安装方式1：\n  {0}\n安装方式2：\n  1).下载自动安装脚本：{1}\n  2).将{2}复制到自定义的安装目录，运行脚本安装：{3}
       *  // Method 1:\n {0}\nMethod 2:\n 1).Download the installation script: {1}\n 2).Copy {2} to a custom installation directory and run the script to install : {3}
       *  </pre>
       */
      private Map<String, String> buildLinuxOfflineInstallSteps(NodeInfo nodeInfo) {
        Map<String, String> stepMessages = new HashMap<>();
        stepMessages.put("installScriptUrl", getLinuxInstallScriptUrl());
        stepMessages.put("onlineInstallCmd", buildLinuxOnlineInstallCmd(nodeInfo));
        stepMessages.put("installScriptName", agentProperties.getLinuxInstallScriptName());
        stepMessages.put("runInstallCmd", buildLinuxRunInstallCmd(nodeInfo));
        return stepMessages;
      }

      private String buildWindowsOnlineInstallCmd(NodeInfo nodeInfo) {
        return "/bin/sh -c \"$(curl -s " + getWindowsInstallScriptUrl() + ") "
            + agentProperties.getAgentVersion() + " "
            + agentProperties.getAgentFileId() + " "
            + agentProperties.getServerCtrlUrlPrefix() + " "
            + nodeInfo.getAgentAuth().getAccessToken() + " "
            + tenantId + " "
            + nodeId + "\"";
      }

      private String buildDownloadWindowsInstallCmd() {
        return "curl -s " + getLinuxInstallScriptUrl() + " -o " + agentProperties
            .getLinuxInstallScriptName() + " --retry 3 -m 120";
      }

      private String getWindowsInstallScriptUrl() {
        return agentProperties.getInstallScriptUrlPrefix() + "/"
            + agentProperties.getWindowsInstallScriptName() + "?fid=" + agentProperties
            .getWindowsInstallScriptId();
      }

      /**
       * ./install-agent.sh 1.0.0 99360265199419407 'http://localhost:6808/openapi2p/v1/ctrl' a0694ac2-e5e7-4eda-a29d-afd447b6c5e5 1 1
       */
      private String buildWindowsRunInstallCmd(NodeInfo nodeInfo) {
        return "call " + agentProperties.getWindowsInstallScriptName() + " "
            + agentProperties.getAgentVersion() + " "
            + agentProperties.getAgentFileId() + " '"
            + agentProperties.getServerCtrlUrlPrefix() + "' "
            + nodeInfo.getAgentAuth().getAccessToken() + " "
            + tenantId + " "
            + nodeId;
      }

      private Map<String, String> buildWindowsOfflineInstallSteps(NodeInfo nodeInfo) {
        Map<String, String> stepMessages = new HashMap<>();
        stepMessages.put("installScriptUrl", getWindowsInstallScriptUrl());
        stepMessages.put("onlineInstallCmd", buildWindowsOnlineInstallCmd(nodeInfo));
        stepMessages.put("installScriptName", agentProperties.getWindowsInstallScriptName());
        stepMessages.put("runInstallCmd", buildWindowsRunInstallCmd(nodeInfo));
        return stepMessages;
      }
    }.execute();
  }



  /**
   * Clear runner processes on a list of nodes.
   * <p>
   * Sends clear command to agent nodes via local controller.
   */
  @Override
  public void clearRunner(List<NodeInfo> nodeInfos) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        for (NodeInfo node : nodeInfos) {
          // Push local controller
          ChannelRouter router = nodeInfoQuery.getLocalChannelRouter(node.getId(),
              node.getTenantId());
          if (nonNull(router)) {
            try {
              // Send clear command to node agent
              ReplyMessage replyMessage = nodeInfoQuery.pushAgentMessage(
                  AgentCommandType.RUNNER_CLEAR, null, router);
              log.info("Controller handle to clear runner on agent, nodeId: {}, reply message: {}",
                  node.getId(), replyMessage);
            } catch (Exception e) {
              log.error("Clear runner on agent exception", e);
            }
          }
        }
        return null;
      }
    }.execute();
  }

  /**
   * Kill runner process on a node by DTO.
   * <p>
   * Sends kill command to agent node or broadcasts to remote controllers.
   */
  @Override
  public Boolean runnerProcessKill(NodeRunnerKillDto dto) {
    return new BizTemplate<Boolean>() {
      NodeInfo nodeInfoDb;

      @Override
      protected void checkParams() {
        nodeInfoDb = nodeInfoQuery.checkAndFind(dto.getNodeId());
      }

      @Override
      protected Boolean process() {
        log.info("Controller handle to kill runner process, nodeId: {}, pid: {}", dto.getNodeId(),
            dto.getPid());
        // Push local controller
        ChannelRouter agentRouter = nodeInfoQuery.getLocalChannelRouter(dto.getNodeId(),
            nodeInfoDb.getTenantId());
        RunnerKillDto killDto = new RunnerKillDto().setPid(dto.getPid().toString());
        if (nonNull(agentRouter)) {
          try {
            return nodeInfoQuery.pushKillRunnerProcessCmd2Agent(killDto, agentRouter).success;
          } catch (Exception e) {
            log.error("Controller [isLocalRouter={}] kill runner process exception: {}",
                true, e.getMessage());
          }
          return null;
        }

        // Push remote controller
        if (dto.isBroadcast()) {
          List<ServiceInstance> instances = discoveryClient.getInstances(appInfo.getArtifactId());
          if (isNotEmpty(instances)) {
            for (ServiceInstance instance : instances) {
              // Exclude current controller
              if (!instance.getInstanceId().equals(appInfo.getInstanceId())) {
                String remoteStartUrl =
                    "http://" + instance.getInstanceId() + KILL_RUNNER_PROCESS_ENDPOINT;
                dto.setBroadcast(false);
                Boolean killed = nodeInfoQuery.broadcastKillRunnerRemoteCtrl(dto, remoteStartUrl);
                if (nonNull(killed)) {
                  return killed;
                }
              }
            }
          }
        }
        return null;
      }
    }.execute();
  }

  /**
   * Configure agent authentication for the main node.
   * <p>
   * Generates and saves agent authentication tokens and updates configuration files.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void configureAgentAuth() throws Exception {
    Long ownerTenantId = tenantManager.checkAndFindOwnerTenant().getId();
    String ip = EnvHelper.getString(TESTER_HOST, getValidIpv4());
    NodeInfo nodeInfo = nodeInfoQuery.findTenantNode(ownerTenantId, ip);
    if (isNull(nodeInfo)) {
      throw SysException.of("Application main node info not found");
    }

    AgentAuth agentAuth = nodeInfo.getAgentAuth();
    if (isNull(agentAuth)) {
      agentAuth = genOpen2pAuthToken(ownerTenantId, nodeInfo.getId());
      nodeInfo.setAgentAuth(agentAuth);
      update0(nodeInfo);
    }

    PropertiesRepo.of(appDir.getConfDir(), "remoting.properties")
        .save("remoting.ctrlUrlPrefix",
            EnvHelper.getString(TESTER_APIS_SERVER_URL + "/openapi2p/v1/ctrl/discovery"))
        .save("remoting.ctrlAccessToken", agentAuth.getAccessToken())
        .saveToDisk();

    PropertiesRepo.of(appDir.getConfDir(), "agent.properties")
        .save("angusagent.serverIp", ip)
        .save("angusagent.principal.tenantId", ownerTenantId.toString())
        .save("angusagent.principal.deviceId", nodeInfo.getId().toString())
        .saveToDisk();
  }

  /**
   * Initialize agent node information.
   * <p>
   * Creates or updates node info with tenant and node IDs.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public NodeInfo initAgentNodeInfo(Long tenantId, Long nodeId) {
    Optional<NodeInfo> nodeInfo = nodeInfoRepo.findById(nodeId);
    if (nodeInfo.isPresent() && nonNull(nodeInfo.get().getAgentAuth())) {
      return nodeInfo.get();
    }
    NodeInfo initNodeInfo = new NodeInfo();
    if (nodeInfo.isPresent()) {
      initNodeInfo = nodeInfo.get();
    }
    initNodeInfo.setTenantId(tenantId);
    initNodeInfo.setId(nodeId);
    initNodeInfo.setLastModifiedDate(LocalDateTime.now());
    nodeInfoRepo.save(initNodeInfo);
    return initNodeInfo;
  }

  /**
   * Save agent authentication information for a node.
   * <p>
   * Generates and saves agent authentication tokens for the node.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void saveAgentAuthInfo(Long tenantId, Long nodeId, NodeInfo nodeInfo) {
    nodeInfo.setAgentAuth(genOpen2pAuthToken(tenantId, nodeId));
    nodeInfoRepo.save(nodeInfo);
  }

  /**
   * Generate openapi2p client for applyTenantId
   */
  private AgentAuth genOpen2pAuthToken(Long tenantId, Long nodeId) {
    AuthClientSignupDto signupDto = new AuthClientSignupDto()
        .setSignupBiz(Client2pSignupBiz.AGENT)
        .setTenantId(tenantId)
        .setTenantName(getTenantName())
        .setResourceId(nodeId);
    AuthClientSignupVo signupVo = clientSignInnerRemote.signupByDoor(signupDto).orElseContentThrow();

    AuthClientSignInDto signInDto = new AuthClientSignInDto()
        .setClientId(signupVo.getClientId())
        .setClientSecret(signupVo.getClientSecret())
        .setScope(SIGN2P_TOKEN_CLIENT_SCOPE);
    AuthClientSignVo signInVo = clientSignPubRemote.signin(signInDto).orElseContentThrow();

    return new AgentAuth().setClientId(signupVo.getClientId())
        .setClientSecret(signupVo.getClientSecret())
        .setAccessToken(signInVo.getAccessToken());
  }
}
