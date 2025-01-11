package cloud.xcan.sdf.core.angustester.infra.iaas;


import cloud.xcan.sdf.api.message.CommProtocolException;
import cloud.xcan.sdf.core.angustester.domain.node.dns.NodeDomainDns;
import cloud.xcan.sdf.core.utils.GsonUtils;
import com.aliyun.alidns20150109.Client;
import com.aliyun.alidns20150109.models.AddDomainRecordRequest;
import com.aliyun.alidns20150109.models.AddDomainRecordResponse;
import com.aliyun.alidns20150109.models.AddDomainRequest;
import com.aliyun.alidns20150109.models.AddDomainResponse;
import com.aliyun.alidns20150109.models.DeleteDomainRecordRequest;
import com.aliyun.alidns20150109.models.DeleteDomainRecordResponse;
import com.aliyun.alidns20150109.models.UpdateDomainRecordRequest;
import com.aliyun.alidns20150109.models.UpdateDomainRecordResponse;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author wjl
 */
@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "xcan.iaas.aliyun.dns")
public class AliyunDnsClient implements DnsClient {

  private String accessKeyId;

  private String accessKeySecret;

  private int connectTimeout = 5 * 1000;

  private int readTimeout = 2 * 60 * 1000;

  public Client getAliyunClient() throws Exception {
    Config config = new Config().setAccessKeyId(accessKeyId)
        .setAccessKeySecret(accessKeySecret)
        .setConnectTimeout(connectTimeout)
        .setReadTimeout(readTimeout);
    return new Client(config);
  }

  @SneakyThrows
  @Override
  public boolean addDomain(String name) {
    Client client = getAliyunClient();
    try {
      AddDomainRequest request = new AddDomainRequest().setDomainName(name);
      AddDomainResponse response = client.addDomainWithOptions(request, new RuntimeOptions());
      log.info("Add aliyun domain response:{}", GsonUtils.toFormatJson(response));
      return true;
    } catch (Exception e) {
      // Note: Allow local save successfully when failure on aliyun
      log.error("Add aliyun domain exception: {}", e.getMessage());
    }
    return false;
  }

  @SneakyThrows
  @Override
  public String addDomainDns(String domain, NodeDomainDns dns) {
    try {
      Client client = getAliyunClient();
      AddDomainRecordRequest request = new AddDomainRecordRequest()
          .setDomainName(domain)
          .setLine(dns.getLine().getValue().toLowerCase())
          .setRR(dns.getName())
          // MX记录的优先级，取值范围：[1,50]
          // 记录类型为MX记录时，此参数必需，MX 优先级的数值越低，优先级别就越高
          .setPriority(1L)
          .setType(dns.getType().name())
          .setValue(dns.getValue())
          // 云解析DNS版本     取值范围            说明
          // 免费版            [600 - 86400]     单位秒，正整数
          // 个人版            [600 - 86400]     单位秒，正整数
          // 企业标准版         [60 - 86400]     单位秒，正整数 -> 已开通企业标准版本
          // 企业旗舰版         [1 - 86400]      单位秒，正整数
          .setTTL(dns.getTtl().longValue());
      RuntimeOptions runtime = new RuntimeOptions();
      AddDomainRecordResponse response = client.addDomainRecordWithOptions(request, runtime);
      log.info("Add aliyun domain DNS response:{}", GsonUtils.toFormatJson(response));
      return response.getBody().getRecordId();
    } catch (Exception e) {
      // Note: Allow local save successfully when failure on aliyun
      log.error("Add aliyun domain DNS exception: {}", e.getMessage());
      throw CommProtocolException.of(e.getMessage());
    }
  }

  @SneakyThrows
  @Override
  public void updateDomainDns(NodeDomainDns nodeDomainDns) {
    try {
      Client client = getAliyunClient();
      UpdateDomainRecordRequest request = new UpdateDomainRecordRequest()
          .setRecordId(nodeDomainDns.getCloudRecordId())
          .setLine(nodeDomainDns.getLine().getValue().toLowerCase())
          .setRR(nodeDomainDns.getName())
          .setType(nodeDomainDns.getType().name())
          // MX记录的优先级，取值范围：[1,50]
          // 记录类型为MX记录时，此参数必需，MX 优先级的数值越低，优先级别就越高
          .setPriority(1L)
          .setValue(nodeDomainDns.getValue())
          .setTTL(nodeDomainDns.getTtl().longValue());
      RuntimeOptions runtime = new RuntimeOptions();
      UpdateDomainRecordResponse response = client.updateDomainRecordWithOptions(request, runtime);
      log.info("Update aliyun domain DNS response:{}", GsonUtils.toFormatJson(response));
    } catch (Exception e) {
      log.error("Update aliyun domain DNS exception: {}", e.getMessage());
      throw CommProtocolException.of(e.getMessage());
    }
  }

  @SneakyThrows
  @Override
  public void deleteDomainDns(String recordId) {
    try {
      Client client = getAliyunClient();
      DeleteDomainRecordRequest request = new DeleteDomainRecordRequest().setRecordId(recordId);
      RuntimeOptions runtime = new RuntimeOptions();
      DeleteDomainRecordResponse response = client.deleteDomainRecordWithOptions(request, runtime);
      log.info("Delete aliyun domain DNS response:{}", GsonUtils.toFormatJson(response));
    } catch (Exception e) {
      log.info("Delete aliyun domain DNS exception:{}", e.getMessage());
      throw CommProtocolException.of(e.getMessage());
    }
  }

}
