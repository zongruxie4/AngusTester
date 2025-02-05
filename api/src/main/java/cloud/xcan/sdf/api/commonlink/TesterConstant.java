package cloud.xcan.sdf.api.commonlink;

import cloud.xcan.sdf.spec.annotations.Unmodifiable;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface TesterConstant {

  String ANGUS_API_DOC_VERSION = "1.0.0";
  String DOMAIN = "http://www.xcan.cloud";

  String TASK_BID_KEY = "task";
  String NODE_HOSTNAME_BID_KEY = "nodeHostname";
  String FUNC_CASE_BID_KEY = "funcCase";

  String MOCK_SERVICE_CLOUD_DOMAIN = "angusmock.cloud";
  String MOCK_SERVICE_CLOUD_DOMAIN_SUFFIX = ".angusmock.cloud";

  int MOCK_EXCEPTION_SALF_LENGTH = 200;

  /**
   * Samples
   */
  List<String> SAMPLE_SERVICES_FILES = List.of("OpenapiPetstore.yaml", "AngusTesterSample.yaml");
  List<String> SAMPLE_SCRIPT_FILES = List.of(
      "TcpFunctionality.yaml", "TcpPerformance.yaml",
      "SmtpFunctionality.yaml", "SmtpPerformance.yaml",
      "MailFunctionality.yaml", "MailPerformance.yaml",
      "LdapFunctionality.yaml", "LdapPerformance.yaml",
      "WebSocketFunctionality.yaml", "WebSocketPerformance.yaml",
      "JdbcFunctionality.yaml", "JdbcPerformance.yaml",
      "HttpFunctionality.yaml", "HttpPerformance.yaml", "HttpBenchmark.yaml");
  String SAMPLE_MOCK_APIS_FILE = "MockApis.yaml";

  /**
   * AngusTester
   */
  String ANGUSTESTER_OPEN = "ANGUSTESTER_OPEN";
  String ANGUSTESTER_ADMIN = "ANGUSTESTER_ADMIN";
  String ANGUSTESTER_USER = "ANGUSTESTER_USER";
  String ANGUSTESTER_GUEST = "ANGUSTESTER_GUEST";

  String ANGUSTESTER_PRODUCT_CODE = "AngusTester";
  String LCS_DIR = "lcs" + File.separator;

  int MAX_API_PERF_CONCURRENCY = 1000000;
  int MAX_API_NUM = 2000000;
  int MAX_PROJECT_API_NUM = 20000;
  int MAX_API_CASE_NUM = 100;

  String CLONE_NAME_SUFFIX = "Copy";

  int MAX_TAGS_NUM = 5;

  int MAX_TEST_TYPE = 3;
  int MAX_ACTIVITY_LENGTH = 1600;
  int MAX_TASK_REMARK_NUM = 30;

  int MAX_OPT_CASE_NUM = 10000;

  int MAX_SCE_MONITOR_HISTORY_NUM = 2000;

  String USER_DEFAULT_DIR = "undefined";

  String UNARCHIVED_API_NAME_PREFIX = "api";

  @Unmodifiable
  int MAX_DIR_LEVEL = 3;

  long MAX_DATASET_REVIEW_ROWS = 10000;
  long DEFAULT_DATASET_REVIEW_ROWS = 20;

  long MAX_MOCK_ROWS = 100000;
  int MAX_MOCK_FUNC_LENGTH = 8 * 1024;
  long MAX_MOCK_FUNC_ITERATIONS = MAX_MOCK_ROWS;
  int MAX_MOCK_TEXT_LENGTH = 200 * 1024;
  long MAX_MOCK_TEXT_ITERATIONS = 100;

  int MAX_SYNC_OPENAPI_NUM = 10;

  int MAX_COMMENT_TARGET_NUM = 200;

  int WEEKLY_WORKING_HOURS = 8;

  double DEFAULT_DAILY_WORKLOAD = 8d;

  String IMPORT_OPENAPI_DIR = "importApis" + File.separator + "openapi" + File.separator;
  String IMPORT_POSTMAN_DIR = "importApis" + File.separator + "postman" + File.separator;
  String EXPORT_OPENAPI_DIR = "exportApis" + File.separator;

  String EXPORT_SUMMARY_DIR = "exportSummary" + File.separator;
  String EXPORT_ANALYSIS_DIR = "exportAnalysis" + File.separator;

  String IMPORT_MOCK_SERVICE_DIR = "importMockService" + File.separator;
  String EXPORT_MOCK_SERVICE_DIR = "exportMockService" + File.separator;

  // @DoInFuture("Move purchase logic to IAAS proxy service(Multi-cloud management)")
  /*******************阿里云节点购买参数*******************/
  //最大允许100个
  int BATCH_INSTANCE_SIZE = 50;

  String AGENT_STARTED_MESSAGE = "Agent service started";

  //9个备选区域列表
  Map<String, Map<String, String>> ALIYUN_REGION_IDS = new HashMap<>() {
    {
      put("cn-beijing", Map.of("VSWITCH_ID", "vsw-2zepgonatg5zkhmo198ru",
          "SECURITY_GROUP_ID", "sg-2zef2h0z4i07l8xdb8rq"));
      put("cn-hangzhou", Map.of("VSWITCH_ID", "vsw-bp19jcv956rssqrbwg5z",
          "SECURITY_GROUP_ID", "sg-2zef2h0z4i07l8xdb8rq"));
      put("cn-shanghai", Map.of("VSWITCH_ID", "vsw-uf6w4cxjj8i15cqprndoh",
          "SECURITY_GROUP_ID", "sg-uf69nie2g9g7g8m81us1"));
      /*put("cn-shenzhen", Map.of("VSWITCH_ID", "vsw-wz9u3zlhh8n0pg1es0pir",
          "SECURITY_GROUP_ID", "sg-wz9eybmi432exm4y1syn"));*/
      put("cn-guangzhou", Map.of("VSWITCH_ID", "vsw-7xvyj8btxma5cpftnpikq",
          "SECURITY_GROUP_ID", "sg-7xvbvl6tjwj2y5m66xvc"));
      put("cn-chengdu", Map.of("VSWITCH_ID", "vsw-2vcoarcpfvfw3ermav9qf",
          "SECURITY_GROUP_ID", "sg-2vcimx0of1tw06xtjgj5"));
      put("cn-wulanchabu", Map.of("VSWITCH_ID", "vsw-0jlizrxjf2uzqjsq79j5w",
          "SECURITY_GROUP_ID", "sg-0jl36s6iid5sbuw5r7o3"));
      put("cn-huhehaote", Map.of("VSWITCH_ID", "vsw-hp3i8s7n80vmzl444esc5",
          "SECURITY_GROUP_ID", "sg-hp334l72lb5pcj2iydt3"));
      put("cn-hongkong", Map.of("VSWITCH_ID", "vsw-j6c6nicxftw6bana2w8t3",
          "SECURITY_GROUP_ID", "sg-j6cfmpon4i7z83uzj29j"));
    }
  };

  //资源类型:instance：ECS实例；disk：云盘；reservedinstance：预留实例券；ddh：专有宿主机
  String ALIYUN_RESOURCE_TYPE = "instance";
  //要查询的资源类型：Zone：可用区；IoOptimized：I/O优化；InstanceType：实例规格；SystemDisk：系统盘；DataDisk：数据盘；Network：网络类型；ddh：专有宿主机
  String ALIYUN_DESTINATION_RESOURCE = "InstanceType";
  //实例付费类型(InstanceChargeType)：PrePaid：包年包月；PostPaid：按量付费
  String ALIYUN_INSTANCE_CHARGE_TYPE = "PostPaid";
  //网络计费类型(InternetChargeType)：PayByBandwidth：按固定带宽计费；PayByTraffic：按使用流量计费
  String ALIYUN_BANDWIDTH_CHARGE_TYPE = "PayByTraffic";

  //实例规格
  String INSTANCE_TYPE_MINI = "ecs.s6-c1m1.small"; // 测试购买节点时用
  String INSTANCE_TYPE_4C_8G = "ecs.c7.xlarge";
  String INSTANCE_TYPE_8C_16G = "ecs.c7.2xlarge";
  String INSTANCE_TYPE_16C_32G = "ecs.c7.4xlarge";
  String INSTANCE_TYPE_32C_64G = "ecs.c7.8xlarge";
  String INSTANCE_TYPE_64C_128G = "ecs.c7.16xlarge";

  //虚拟机参数
  String INSTANCE_IMAGE_ID = "centos_7_6_x64_20G_alibase_20211130.vhd";
  //String SECURITY_GROUP_ID = "sg-8vba3awvq3fzn7sfu7vo";
  //String VSWITCH_ID = "vsw-8vb2znrqe7lvezhve457j";
  String DEFAULT_SYSTEMDISK_CATEGORY = "cloud_essd"; // 200GB IOPS:4200
  //String DEFAULT_SYSDISK_SIZE = "200"; // GB
  //String DEFAULT_INTERNET_MAX_BANDWIDTH_IN = "100"; //MB
  //String DEFAULT_INTERNET_MAX_BANDWIDTH_OUT = "100"; //MB

  String NODE_PASSD_SALT = "QN2NI2WF2.";
  /*******************阿里云节点购买参数*******************/

}
