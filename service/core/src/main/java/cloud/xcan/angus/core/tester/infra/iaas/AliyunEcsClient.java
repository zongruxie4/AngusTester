package cloud.xcan.angus.core.tester.infra.iaas;

import static cloud.xcan.angus.api.commonlink.TesterConstant.ALIYUN_BANDWIDTH_CHARGE_TYPE;
import static cloud.xcan.angus.api.commonlink.TesterConstant.ALIYUN_DESTINATION_RESOURCE;
import static cloud.xcan.angus.api.commonlink.TesterConstant.ALIYUN_INSTANCE_CHARGE_TYPE;
import static cloud.xcan.angus.api.commonlink.TesterConstant.ALIYUN_REGION_IDS;
import static cloud.xcan.angus.api.commonlink.TesterConstant.ALIYUN_RESOURCE_TYPE;
import static cloud.xcan.angus.api.commonlink.TesterConstant.DEFAULT_SYSTEMDISK_CATEGORY;
import static cloud.xcan.angus.api.commonlink.TesterConstant.INSTANCE_IMAGE_ID;
import static cloud.xcan.angus.api.commonlink.TesterConstant.INSTANCE_TYPE_16C_32G;
import static cloud.xcan.angus.api.commonlink.TesterConstant.INSTANCE_TYPE_32C_64G;
import static cloud.xcan.angus.api.commonlink.TesterConstant.INSTANCE_TYPE_4C_8G;
import static cloud.xcan.angus.api.commonlink.TesterConstant.INSTANCE_TYPE_64C_128G;
import static cloud.xcan.angus.api.commonlink.TesterConstant.INSTANCE_TYPE_8C_16G;
import static cloud.xcan.angus.api.commonlink.TesterConstant.INSTANCE_TYPE_MINI;
import static cloud.xcan.angus.api.commonlink.TesterConstant.NODE_HOSTNAME_BID_KEY;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;

import cloud.xcan.angus.api.commonlink.TesterConstant;
import cloud.xcan.angus.api.pojo.node.NodeSpecData;
import cloud.xcan.angus.core.spring.SpringContextHolder;
import cloud.xcan.angus.core.tester.application.converter.NodeConverter;
import cloud.xcan.angus.core.tester.domain.config.node.Node;
import cloud.xcan.angus.core.utils.GsonUtils;
import com.aliyun.ecs20140526.Client;
import com.aliyun.ecs20140526.models.DeleteInstancesRequest;
import com.aliyun.ecs20140526.models.DeleteInstancesResponse;
import com.aliyun.ecs20140526.models.DescribeAvailableResourceRequest;
import com.aliyun.ecs20140526.models.DescribeAvailableResourceResponse;
import com.aliyun.ecs20140526.models.DescribeInstancesRequest;
import com.aliyun.ecs20140526.models.DescribeInstancesResponse;
import com.aliyun.ecs20140526.models.RebootInstancesRequest;
import com.aliyun.ecs20140526.models.RebootInstancesResponse;
import com.aliyun.ecs20140526.models.RunInstancesRequest;
import com.aliyun.ecs20140526.models.RunInstancesRequest.RunInstancesRequestSystemDisk;
import com.aliyun.ecs20140526.models.RunInstancesResponse;
import com.aliyun.ecs20140526.models.StopInstancesRequest;
import com.aliyun.ecs20140526.models.StopInstancesResponse;
import com.aliyun.teaopenapi.models.Config;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "xcan.iaas.aliyun.ecs")
public class AliyunEcsClient implements EcsClient {

  @Value("${spring.profiles.active}")
  private String profile;

  private String accessKeyId;

  private String accessKeySecret;

  private int connectTimeout = 30 * 1000;

  private int readTimeout = 2 * 60 * 1000;

  public Client getAliyunClient(String regionId) throws Exception {
    Config config = new Config().setAccessKeyId(accessKeyId)
        .setAccessKeySecret(accessKeySecret)
        .setConnectTimeout(connectTimeout)
        .setReadTimeout(readTimeout);
    config.setRegionId(regionId);
    config.endpoint = "ecs." + regionId + ".aliyuncs.com";
    return new Client(config);
  }

  @Override
  public String queryMeetResourceSpecRegion(InstanceChargeType chargeType, NodeSpecData spec) {
    String defaultRegionId = null;
    for (String rid : ALIYUN_REGION_IDS.keySet()) {
      if (hasAvailableResource(rid, chargeType, spec)) {
        defaultRegionId = rid;
        break;
      }
    }
    return defaultRegionId;
  }

  @Override
  public Boolean hasAvailableResource(String regionId, InstanceChargeType chargeType,
      NodeSpecData spec) {
    DescribeAvailableResourceRequest resourceRequest = new DescribeAvailableResourceRequest()
        .setRegionId(regionId)
        .setInstanceChargeType(chargeType.getValue())
        .setResourceType(ALIYUN_RESOURCE_TYPE)
        .setDestinationResource(ALIYUN_DESTINATION_RESOURCE)
        .setInstanceType(hostSpecToInstanceType(spec))
        .setSystemDiskCategory(nullSafe(spec.getSysDiskCategory(), DEFAULT_SYSTEMDISK_CATEGORY));
    try {
      Client client = getAliyunClient(regionId);
      DescribeAvailableResourceResponse resourceResponse = client.describeAvailableResource(
          resourceRequest);
      if (Objects.isNull(resourceResponse.getBody().getAvailableZones())
          || isEmpty(resourceResponse.getBody().getAvailableZones().getAvailableZone())) {
        return false;
      }
      long count = resourceResponse.getBody().getAvailableZones().getAvailableZone()
          .stream().filter(drbza -> "WithStock".equals(drbza.getStatusCategory())).count();
      // WithStock：库存充足
      return count >= 1;
    } catch (Exception e) {
      log.error("Query aliyun resources exception, region: {}, exception: {}",
          regionId, e.getMessage());
    }
    return false;
  }

  @Override
  public List<Node> purchaseAndRunInstances(Long orderId, LocalDateTime orderExpiredDate,
      Long tenantId, NodeSpecData spec, String regionId) throws Exception {
    log.info("Purchase and run aliyun instances, orderId={}, orderExpiredDate={}, tenantId={},"
            + " nodeSpec={}, regionId={}, specNum={}", orderId, orderExpiredDate, tenantId,
        spec.getShowLabel(), regionId, spec.getNodeNum());
    // Assembler instances request
    String hostname = genNodeHostName();
    RunInstancesRequest runRequest = getStartNodesRequest(regionId, hostname, orderId, spec);

    // Run instance
    // 和CreateInstance接口相比，RunInstances接口有以下优点：
    // 单次最多可以创建100台实例，避免重复多次调用CreateInstance。
    // 实例创建之后，实例会自动变成Starting状态，然后变成Running状态，不需要再调用StartInstance启动实例。
    // 创建实例时可以指定InternetMaxBandwidthOut为ECS实例分配公网IP，不需要您再调用AllocatePublicIpAddress分配公网IP。
    // 您可以指定AutoReleaseTime参数来设定自动释放时间，不需要再调用ModifyInstanceAutoReleaseTime设置自动释放时间。
    // 您可以指定LaunchTemplateId和LaunchTemplateVersion使用启动模板，可以免除您每次创建实例时都需要填入大量配置参数。
    // 可以指定UniqueSuffix参数批量设置有序的实例名称或主机名称，方便管理与检索。
    // 使用RunInstances创建实例时支持设置Enclave机密计算模式和可信系统模式。
    RunInstancesResponse runInstancesResponse = getAliyunClient(regionId)
        .runInstances(runRequest);
    log.info("Purchase and run aliyun instances, orderId={}, result={}", orderId,
        GsonUtils.toJson(runInstancesResponse));

    // Assembler purchase nodes
    List<String> instanceIds = runInstancesResponse.getBody().getInstanceIdSets()
        .getInstanceIdSet();
    return NodeConverter.initPurchaseNodeInfo(orderId, hostname, orderExpiredDate, tenantId,
        spec, regionId, instanceIds);
  }

  @Override
  public void deleteInstances(String regionId, List<String> instanceIds) throws Exception {
    // 删除节点后弹性网卡也会被删除
    DeleteInstancesResponse response = getAliyunClient(regionId)
        .deleteInstances(new DeleteInstancesRequest().setRegionId(regionId)
            .setInstanceId(instanceIds)
            // 强制释放运行中（Running）的ECS实例
            .setForce(true));
    log.info("Delete aliyun instances, regionId={}, instanceIds={}, result={}", regionId,
        String.join(",", instanceIds), GsonUtils.toJson(response));
  }

  /**
   * Query instance describe
   */
  @Override
  public Map<String, Node> getInstancesDescribe(String regionId, List<String> instances)
      throws Exception {
    DescribeInstancesRequest descRequest = new DescribeInstancesRequest()
        .setRegionId(regionId)
        // 取值可以由多个实例ID组成一个JSON数组，最多支持100个ID，ID之间用半角逗号（,）隔开
        .setInstanceIds(GsonUtils.toJson(instances));
    DescribeInstancesResponse response = getAliyunClient(regionId).describeInstances(descRequest);
    return response.getBody().getInstances().getInstance().stream()
        .map(instance -> {
          Node node = new Node();
          List<String> vpcIps = instance.getVpcAttributes()
              .getPrivateIpAddress().getIpAddress();
          if (isNotEmpty(vpcIps)) {
            node.setIp(vpcIps.get(0));
          }
          List<String> pubIps = instance.getPublicIpAddress().getIpAddress();
          if (isNotEmpty(pubIps)) {
            node.setPublicIp(pubIps.get(0));
          }
          node.setInstanceId(instance.getInstanceId())
              .setInstanceName(instance.getInstanceName())
              .setSync(isNotEmpty(node.getIp()));
          return node;
        }).collect(Collectors.toMap(Node::getInstanceId, x -> x));
  }

  private RunInstancesRequest getStartNodesRequest(String regionId, String hostname, Long orderId,
      NodeSpecData spec) {
    //设置主机信息
    RunInstancesRequest runInstancesRequest = new RunInstancesRequest()
        .setRegionId(regionId)  //实例所属的地域ID
        .setImageId(INSTANCE_IMAGE_ID)  //镜像
        .setInstanceChargeType(ALIYUN_INSTANCE_CHARGE_TYPE) //按量付费
        .setInternetChargeType(ALIYUN_BANDWIDTH_CHARGE_TYPE)  //按使用流量计费
        .setHostName(hostname)//实例主机名称
        .setSecurityGroupId(ALIYUN_REGION_IDS.get(regionId).get("SECURITY_GROUP_ID")) //弹性网卡所属的安全组ID
        .setVSwitchId(ALIYUN_REGION_IDS.get(regionId).get("VSWITCH_ID")) //弹性网卡所属的虚拟交换机ID
        // 长度为8至30个字符，必须同时包含大小写英文字母、数字和特殊符号中的三类字符。特殊符号可以是： ()`~!@#$%^&*-_+=|{}[]:;'<>,.?/
        .setPassword(TesterConstant.NODE_PASSWORD_SALT + orderId)
        .setInstanceType(hostSpecToInstanceType(spec));

    //设置系统盘
    RunInstancesRequestSystemDisk systemDisk = new RunInstancesRequestSystemDisk()
        .setCategory(DEFAULT_SYSTEMDISK_CATEGORY)
        .setSize(String.valueOf(spec.getSysDisk()));
    runInstancesRequest.setSystemDisk(systemDisk);

    //设置带宽
    runInstancesRequest.setInternetMaxBandwidthIn(spec.getNetwork())
        .setInternetMaxBandwidthOut(spec.getNetwork());

    //设置购买数量
    runInstancesRequest.setAmount(spec.getNodeNum().intValue());
    return runInstancesRequest;
  }

  @Override
  public void stopInstances(String regionId, List<String> instanceIds) throws Exception {
    StopInstancesResponse response = getAliyunClient(regionId)
        .stopInstances(new StopInstancesRequest().setRegionId(regionId)
            .setInstanceId(instanceIds));
    log.info("Stop aliyun instances, regionId={}, instanceIds={}, result={}", regionId,
        String.join(",", instanceIds), GsonUtils.toJson(response));
  }

  @Override
  public void restartInstances(String regionId, List<String> instanceIds) throws Exception {
    RebootInstancesResponse response = getAliyunClient(regionId)
        .rebootInstances(new RebootInstancesRequest().setRegionId(regionId)
            .setInstanceId(instanceIds));
    log.info("Reboot aliyun instances, regionId={}, instanceIds={}, result={}", regionId,
        String.join(",", instanceIds), GsonUtils.toJson(response));
  }

  public String hostSpecToInstanceType(NodeSpecData spec) {
    String instanceType = INSTANCE_TYPE_MINI;
    if (!isProd()) {
      return instanceType;
    }
    if (spec.is4C8G()) {
      instanceType = INSTANCE_TYPE_4C_8G;
    } else if (spec.is8C16G()) {
      instanceType = INSTANCE_TYPE_8C_16G;
    } else if (spec.is16C32G()) {
      instanceType = INSTANCE_TYPE_16C_32G;
    } else if (spec.is32C64G()) {
      instanceType = INSTANCE_TYPE_32C_64G;
    } else if (spec.is64C128G()) {
      instanceType = INSTANCE_TYPE_64C_128G;
    }
    return instanceType;
  }

  /**
   * k8s-node-[]-ecshost或k8s-node-[,]-ecshost ->
   * k8s-node-000000-ecshost、k8s-node-000001-ecshost、k8s-node-000002-ecshost
   */
  public String genNodeHostName() {
    return SpringContextHolder.getBidGenerator().getId(NODE_HOSTNAME_BID_KEY) + "[]";
  }

  private boolean isProd() {
    return "prod".equals(profile);
  }
}
