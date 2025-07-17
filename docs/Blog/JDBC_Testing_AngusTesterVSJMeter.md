# AngusTester 和 JMeter JDBC 测试结果比较

本次测试主要目的是对比 AngusTester 和 JMeter 对 JDBC 协议性能表现。

## 测试环境

以下测试 AngusTester 和 JMeter 使用相同环境。注意：本次测试是在同一台 PC
机上进行，如果想测试特定配置下数据库的准确性能，需要将测试机和 MySQL服务器分开部署，或者使用更高配置的测试服务器。

### 软件

- 数据库：Percona Server MySQL 5.7.34-37

- MySQL驱动：mysql-connector-j-8.0.31.jar

- Docker：19.03.11 (使用 Docker 部署 MySQL)

- 测试工具

    - AngusTester 1.0.0
    - Apache JMeter 4.0

### 系统

MacBookPro16 PC

- Processors: 1
- Cores: 8 \* 2 Intel Core i9 2.3 GHz
- Memory: 32 GB

### MySQL 配置

```conf
character_set_server = utf8mb4
skip-host-cache
skip-name-resolve
datadir=/var/lib/mysql
socket=/var/lib/mysql/mysql.sock
secure-file-priv=/var/lib/mysql-files
user=mysql
max_allowed_packet=100M
max_connections=2000   # 最大连接数

# Disabling symbolic-links is recommended to prevent assorted security risks
symbolic-links=0

log-error=/var/log/mysql/error.log
pid-file=/var/run/mysqld/mysqld.pid
```

### 测试表

```sql
CREATE TABLE `user`  (
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NULL DEFAULT NULL,
  INDEX `idx_username`(`username`) USING BTREE
) ENGINE = InnoDB;
```

## 测试脚本

- JMeter Insert 脚本 (JDBC_Testing_Insert_AngusTesterVSJMeter.jmx)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2" properties="4.0" jmeter="4.0 r1823414">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="Jdbc prepared update performance testing" enabled="true">
      <stringProp name="TestPlan.comments"></stringProp>
      <boolProp name="TestPlan.functional_mode">false</boolProp>
      <boolProp name="TestPlan.tearDown_on_shutdown">true</boolProp>
      <boolProp name="TestPlan.serialize_threadgroups">false</boolProp>
      <elementProp name="TestPlan.user_defined_variables" elementType="Arguments" guiclass="ArgumentsPanel" testclass="Arguments" testname="User Defined Variables" enabled="true">
        <collectionProp name="Arguments.arguments"/>
      </elementProp>
      <stringProp name="TestPlan.user_define_classpath"></stringProp>
    </TestPlan>
    <hashTree>
      <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="Thread Group" enabled="true">
        <stringProp name="ThreadGroup.on_sample_error">continue</stringProp>
        <elementProp name="ThreadGroup.main_controller" elementType="LoopController" guiclass="LoopControlPanel" testclass="LoopController" testname="Loop Controller" enabled="true">
          <boolProp name="LoopController.continue_forever">false</boolProp>
          <intProp name="LoopController.loops">-1</intProp>
        </elementProp>
        <stringProp name="ThreadGroup.num_threads">1<!-- 1/10/50/100/200/500/1000/2000 --></stringProp>
        <stringProp name="ThreadGroup.ramp_time"></stringProp>
        <boolProp name="ThreadGroup.scheduler">true</boolProp>
        <stringProp name="ThreadGroup.duration">60</stringProp>
        <stringProp name="ThreadGroup.delay"></stringProp>
      </ThreadGroup>
      <hashTree>
        <JDBCDataSource guiclass="TestBeanGUI" testclass="JDBCDataSource" testname="JDBC Connection Configuration" enabled="true">
          <boolProp name="autocommit">true</boolProp>
          <stringProp name="checkQuery"></stringProp>
          <stringProp name="connectionAge">5000</stringProp>
          <stringProp name="dataSource">test</stringProp>
          <stringProp name="dbUrl">jdbc:mysql://localhost:3306/xcan_mockdata_sample</stringProp>
          <stringProp name="driver">com.mysql.cj.jdbc.Driver</stringProp>
          <boolProp name="keepAlive">true</boolProp>
          <stringProp name="password">123456</stringProp>
          <stringProp name="poolMax">0</stringProp>
          <stringProp name="timeout">100000</stringProp>
          <stringProp name="transactionIsolation">DEFAULT</stringProp>
          <stringProp name="trimInterval">200000</stringProp>
          <stringProp name="username">sample</stringProp>
        </JDBCDataSource>
        <hashTree/>
        <JDBCSampler guiclass="TestBeanGUI" testclass="JDBCSampler" testname="JDBC Request" enabled="true">
          <stringProp name="dataSource">test</stringProp>
          <stringProp name="query">INSERT INTO `user` (username, password) VALUES (?, ?)</stringProp>
          <stringProp name="queryArguments">${__RandomString(16,0123456789abcdefghijklmnopqrstuvwxyz,)},${__RandomString(32,0123456789abcdefghijklmnopqrstuvwxyz,)}</stringProp>
          <stringProp name="queryArgumentsTypes">VARCHAR,VARCHAR</stringProp>
          <stringProp name="queryTimeout">60</stringProp>
          <stringProp name="queryType">Prepared Update Statement</stringProp>
          <stringProp name="resultSetHandler">Store as String</stringProp>
          <stringProp name="resultVariable"></stringProp>
          <stringProp name="variableNames"></stringProp>
        </JDBCSampler>
        <hashTree/>
      </hashTree>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
```

运行脚本：

```bash
./jmeter -n -t ~/scripts/JDBC_Testing_Insert_AngusTesterVSJMeter.jmx
```

- AngusTester Insert 脚本 (JDBC_Testing_Insert_AngusTesterVSJMeter.yaml)

```yaml
specification: angus/1.0.0
info:
  name: Jdbc prepared update performance testing
  description: This is an example of jdbc insert statement testing.
type: TEST_PERFORMANCE
plugin: Jdbc
configuration:
  duration: 60s
  thread:
    threads: 1 # 1/10/50/100/200/500/1000/2000
  priority: 1000
task:
  arguments:
    jdbcSetting:
      type: MYSQL
      driverClassName: com.mysql.cj.jdbc.Driver
      jdbcUrl: jdbc:mysql://localhost:3306/xcan_mockdata_sample
      username: sample
      password: 123456
    ignoreAssertions: true
  pipelines:
    - target: JDBC
      name: UpdateUser
      description: Save user by prepared update statement
      enabled: true
      type: PREPARED_UPDATE
      sql: "INSERT INTO `user` (username, password) VALUES (?, ?)"
      timeoutInSecond: 60
      arguments:
        - type: varchar
          value: "@String(16)" # Mock数据函数
          inout: IN
        - type: varchar
          value: "@String(32)" # Mock数据函数
          inout: IN
```

运行脚本：

```bash
./startup-runner.sh -s ~/scripts/JDBC_Testing_Insert_AngusTesterVSJMeter.yaml -e 1001
```

- JMeter Select 脚本 (JDBC_Testing_Select_AngusTesterVSJMeter.jmx)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2" properties="4.0" jmeter="4.0 r1823414">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="Jdbc prepared select performance testing" enabled="true">
      <stringProp name="TestPlan.comments"></stringProp>
      <boolProp name="TestPlan.functional_mode">false</boolProp>
      <boolProp name="TestPlan.tearDown_on_shutdown">true</boolProp>
      <boolProp name="TestPlan.serialize_threadgroups">false</boolProp>
      <elementProp name="TestPlan.user_defined_variables" elementType="Arguments" guiclass="ArgumentsPanel" testclass="Arguments" testname="User Defined Variables" enabled="true">
        <collectionProp name="Arguments.arguments"/>
      </elementProp>
      <stringProp name="TestPlan.user_define_classpath"></stringProp>
    </TestPlan>
    <hashTree>
      <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="Thread Group" enabled="true">
        <stringProp name="ThreadGroup.on_sample_error">continue</stringProp>
        <elementProp name="ThreadGroup.main_controller" elementType="LoopController" guiclass="LoopControlPanel" testclass="LoopController" testname="Loop Controller" enabled="true">
          <boolProp name="LoopController.continue_forever">false</boolProp>
          <intProp name="LoopController.loops">-1</intProp>
        </elementProp>
        <stringProp name="ThreadGroup.num_threads">1<!-- 1/10/50/100/200/500/1000/2000 --></stringProp>
        <stringProp name="ThreadGroup.ramp_time"></stringProp>
        <boolProp name="ThreadGroup.scheduler">true</boolProp>
        <stringProp name="ThreadGroup.duration">60</stringProp>
        <stringProp name="ThreadGroup.delay"></stringProp>
      </ThreadGroup>
      <hashTree>
        <JDBCDataSource guiclass="TestBeanGUI" testclass="JDBCDataSource" testname="JDBC Connection Configuration" enabled="true">
          <boolProp name="autocommit">true</boolProp>
          <stringProp name="checkQuery"></stringProp>
          <stringProp name="connectionAge">5000</stringProp>
          <stringProp name="dataSource">test</stringProp>
          <stringProp name="dbUrl">jdbc:mysql://localhost:3306/xcan_mockdata_sample</stringProp>
          <stringProp name="driver">com.mysql.cj.jdbc.Driver</stringProp>
          <boolProp name="keepAlive">true</boolProp>
          <stringProp name="password">123456</stringProp>
          <stringProp name="poolMax">0</stringProp>
          <stringProp name="timeout">100000</stringProp>
          <stringProp name="transactionIsolation">DEFAULT</stringProp>
          <stringProp name="trimInterval">200000</stringProp>
          <stringProp name="username">sample</stringProp>
        </JDBCDataSource>
        <hashTree/>
        <JDBCSampler guiclass="TestBeanGUI" testclass="JDBCSampler" testname="JDBC Request" enabled="true">
          <stringProp name="dataSource">test</stringProp>
          <stringProp name="query">SELECT * FROM `user` WHERE username = &apos;FDgcM6u7xwbhkdCnz&apos;</stringProp>
          <stringProp name="queryArguments"></stringProp>
          <stringProp name="queryArgumentsTypes"></stringProp>
          <stringProp name="queryTimeout">60</stringProp>
          <stringProp name="queryType">Prepared Select Statement</stringProp>
          <stringProp name="resultSetHandler">Store as String</stringProp>
          <stringProp name="resultVariable"></stringProp>
          <stringProp name="variableNames"></stringProp>
        </JDBCSampler>
        <hashTree/>
      </hashTree>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
```

运行脚本：

```bash
./jmeter -n -t ~/scripts/JDBC_Testing_Select_AngusTesterVSJMeter.jmx
```

- AngusTester Select 脚本 (JDBC_Testing_Select_AngusTesterVSJMeter.yaml)

```yaml
specification: angus/1.0.0
info:
  name: Jdbc prepared select performance testing
  description: This is an example of jdbc prepared select statement testing.
type: TEST_PERFORMANCE
plugin: Jdbc
configuration:
  duration: 60s
  thread:
    threads: 1 # 1/10/50/100/200/500/1000/2000
  priority: 1000
task:
  arguments:
    ignoreAssertions: true
    jdbcSetting:
      type: MYSQL
      driverClassName: com.mysql.cj.jdbc.Driver
      jdbcUrl: jdbc:mysql://localhost:3306/xcan_mockdata_sample
      username: sample
      password: 123456
  pipelines:
    - target: JDBC
      name: QueryUser
      description: Query user by prepared select statement.
      enabled: true
      type: PREPARED_SELECT
      sql: SELECT * FROM `user` WHERE username = 'FDgcM6u7xwbhkdCnz'
      maxResultRows: 1
      timeoutInSecond: 60
```

运行脚本：

```bash
./startup-runner.sh -s ~/scripts/JDBC_Testing_Select_AngusTesterVSJMeter.yaml -e 1002
```

## 测试结果

### Insert 测试

- 1 个线程运行 60 秒测试结果

JMeter 测试结果：

```bash
Starting the test @ Fri Nov 24 15:37:31 CST 2023 (1700811451582)
Waiting for possible Shutdown/StopTestNow/Heapdump message on port 4445
summary +      1 in 00:00:01 =    1.6/s Avg:   560 Min:   560 Max:   560 Err:     0 (0.00%) Active: 1 Started: 1 Finished: 0
summary +  16697 in 00:00:27 =  611.7/s Avg:     1 Min:     1 Max:    38 Err:     0 (0.00%) Active: 1 Started: 1 Finished: 0
summary =  16698 in 00:00:28 =  598.0/s Avg:     1 Min:     1 Max:   560 Err:     0 (0.00%)
summary +  17045 in 00:00:30 =  568.2/s Avg:     1 Min:     1 Max:    83 Err:     0 (0.00%) Active: 1 Started: 1 Finished: 0
summary =  33743 in 00:00:58 =  582.6/s Avg:     1 Min:     1 Max:   560 Err:     0 (0.00%)
summary +   1080 in 00:00:02 =  504.7/s Avg:     1 Min:     1 Max:    12 Err:     0 (0.00%) Active: 0 Started: 1 Finished: 1
summary =  34823 in 00:01:00 =  579.8/s Avg:     1 Min:     1 Max:   560 Err:     0 (0.00%)
Tidying up ...    @ Fri Nov 24 15:38:32 CST 2023 (1700811512140)
... end of run
```

AngusTester 测试结果：

```bash
===========================================================================================================================
  *** Task Summary Report @ Jdbc TEST_PERFORMANCE @ 2023-11-24 17:42:25 ***
===========================================================================================================================
  Exec ID: 1001                                 Run Mode: LOCAL               Memory: 86.11MBM(Used), 9.15GBM(Free)
  Bytes: 0.0B(Recv), 2.0MB(Write)               Iterations: 39560             Cpu: 5.42%(Proc), 460.27%(Sys)
  Date: 11/24 05:41:24 to 11/24 05:42:24        Duration: 60 Second           Threads: 0/1  terminated
+-------+---------+------+-----+------+-----+-----+-----+-----+------+-------+---------+--------+--------+--------+---------+
| Name  | Samples | Mean | Min | Max  | P50 | P75 | P90 | P99 | P999 | Trans | Trans/s | Errors | Error% | Recv/s | Write/s |
+-------+---------+------+-----+------+-----+-----+-----+-----+------+-------+---------+--------+--------+--------+---------+
| Total | 39560   | 1.5  | 1.0 | 19.0 | 1.0 | 2.0 | 2.0 | 2.0 | 4.0  | 39560 | 659.32  | 0      | 0.0    | 0.0B   | 34.13KB |
+-------+---------+------+-----+------+-----+-----+-----+-----+------+-------+---------+--------+--------+--------+---------+
  *** Sampling Result: SUCCESS
===========================================================================================================================
```

- 10 个线程运行 60 秒测试结果

JMeter 测试结果：

```bash
Starting the test @ Fri Nov 24 17:53:18 CST 2023 (1700819598435)
Waiting for possible Shutdown/StopTestNow/Heapdump message on port 4445
summary +  23829 in 00:00:11 = 2142.7/s Avg:     4 Min:     1 Max:   597 Err:     0 (0.00%) Active: 10 Started: 10 Finished: 0
summary +  61228 in 00:00:30 = 2040.9/s Avg:     4 Min:     2 Max:    45 Err:     0 (0.00%) Active: 10 Started: 10 Finished: 0
summary =  85057 in 00:00:41 = 2068.5/s Avg:     4 Min:     1 Max:   597 Err:     0 (0.00%)
summary +  34121 in 00:00:19 = 1801.2/s Avg:     5 Min:     2 Max:   127 Err:     0 (0.00%) Active: 0 Started: 10 Finished: 10
summary = 119178 in 00:01:00 = 1984.2/s Avg:     4 Min:     1 Max:   597 Err:     0 (0.00%)
Tidying up ...    @ Fri Nov 24 17:54:18 CST 2023 (1700819658945)
... end of run
```

AngusTester 测试结果：

```bash
===========================================================================================================================
  *** Task Summary Report @ Jdbc TEST_PERFORMANCE @ 2023-11-24 17:56:08 ***
===========================================================================================================================
  Exec ID: 1001                                 Run Mode: LOCAL               Memory: 101.2MBM(Used), 9.14GBM(Free)
  Bytes: 0.0B(Recv), 6.36MB(Write)              Iterations: 125744            Cpu: 30.14%(Proc), 797.93%(Sys)
  Date: 11/24 05:55:08 to 11/24 05:56:08        Duration: 60 Second           Threads: 0/10  terminated
+-------+---------+------+-----+--------+-----+-----+-----+------+------+--------+---------+--------+--------+--------+----------+
| Name  | Samples | Mean | Min | Max    | P50 | P75 | P90 | P99  | P999 | Trans  | Trans/s | Errors | Error% | Recv/s | Write/s  |
+-------+---------+------+-----+--------+-----+-----+-----+------+------+--------+---------+--------+--------+--------+----------+
| Total | 125744  | 4.75 | 1.0 | 1005.0 | 4.0 | 5.0 | 7.0 | 13.0 | 29.0 | 125744 | 2095.66 | 0      | 0.0    | 0.0B   | 108.47KB |
+-------+---------+------+-----+--------+-----+-----+-----+------+------+--------+---------+--------+--------+--------+----------+
  *** Sampling Result: SUCCESS
===========================================================================================================================
```

- 50 个线程运行 60 秒测试结果

JMeter 测试结果：

```bash
Starting the test @ Fri Nov 24 17:57:49 CST 2023 (1700819869164)
Waiting for possible Shutdown/StopTestNow/Heapdump message on port 4445
summary +  34999 in 00:00:10 = 3342.5/s Avg:    14 Min:     2 Max:   768 Err:     0 (0.00%) Active: 50 Started: 50 Finished: 0
summary +  85981 in 00:00:30 = 2866.0/s Avg:    17 Min:     3 Max:  1051 Err:     0 (0.00%) Active: 50 Started: 50 Finished: 0
summary = 120980 in 00:00:40 = 2989.3/s Avg:    16 Min:     2 Max:  1051 Err:     0 (0.00%)
summary +  50604 in 00:00:20 = 2546.9/s Avg:    19 Min:     3 Max:   379 Err:     0 (0.00%) Active: 0 Started: 50 Finished: 50
summary = 171584 in 00:01:00 = 2843.6/s Avg:    17 Min:     2 Max:  1051 Err:     0 (0.00%)
Tidying up ...    @ Fri Nov 24 17:58:49 CST 2023 (1700819929870)
... end of run
```

AngusTester 测试结果：

```bash
===========================================================================================================================
  *** Task Summary Report @ Jdbc TEST_PERFORMANCE @ 2023-11-24 18:00:47 ***
===========================================================================================================================
  Exec ID: 1001                                 Run Mode: LOCAL               Memory: 106.99MBM(Used), 9.13GBM(Free)
  Bytes: 0.0B(Recv), 9.68MB(Write)              Iterations: 191423            Cpu: 65.26%(Proc), 813.71%(Sys)
  Date: 11/24 05:59:47 to 11/24 06:00:47        Duration: 60 Second           Threads: 0/50  terminated
+-------+---------+-------+-----+-------+------+------+------+------+------+--------+---------+--------+--------+--------+----------+
| Name  | Samples | Mean  | Min | Max   | P50  | P75  | P90  | P99  | P999 | Trans  | Trans/s | Errors | Error% | Recv/s | Write/s  |
+-------+---------+-------+-----+-------+------+------+------+------+------+--------+---------+--------+--------+--------+----------+
| Total | 191423  | 15.65 | 2.0 | 908.0 | 12.0 | 19.0 | 30.0 | 57.0 | 92.0 | 191423 | 3190.28 | 0      | 0.0    | 0.0B   | 165.12KB |
+-------+---------+-------+-----+-------+------+------+------+------+------+--------+---------+--------+--------+--------+----------+
  *** Sampling Result: SUCCESS
===========================================================================================================================
```

- 100 个线程运行 60 秒测试结果

JMeter 测试结果：

```bash
Starting the test @ Fri Nov 24 18:01:45 CST 2023 (1700820105701)
Waiting for possible Shutdown/StopTestNow/Heapdump message on port 4445
summary +  47439 in 00:00:14 = 3417.5/s Avg:    29 Min:     2 Max:  1275 Err:     0 (0.00%) Active: 100 Started: 100 Finished: 0
summary +  78715 in 00:00:30 = 2623.7/s Avg:    38 Min:     3 Max:  1032 Err:     0 (0.00%) Active: 100 Started: 100 Finished: 0
summary = 126154 in 00:00:44 = 2874.8/s Avg:    34 Min:     2 Max:  1275 Err:     0 (0.00%)
summary +  38058 in 00:00:16 = 2347.5/s Avg:    42 Min:     3 Max:   315 Err:     0 (0.00%) Active: 0 Started: 100 Finished: 100
summary = 164212 in 00:01:00 = 2732.5/s Avg:    36 Min:     2 Max:  1275 Err:     0 (0.00%)
Tidying up ...    @ Fri Nov 24 18:02:46 CST 2023 (1700820166215)
... end of run
```

AngusTester 测试结果：

```bash
===========================================================================================================================
  *** Task Summary Report @ Jdbc TEST_PERFORMANCE @ 2023-11-24 18:09:05 ***
===========================================================================================================================
  Exec ID: 1001                                 Run Mode: LOCAL               Memory: 108.9MBM(Used), 9.13GBM(Free)
  Bytes: 0.0B(Recv), 10.74MB(Write)             Iterations: 212575            Cpu: 54.37%(Proc), 1201.13%(Sys)
  Date: 11/24 06:08:04 to 11/24 06:09:04        Duration: 60 Second           Threads: 0/100  terminated
+-------+---------+------+-----+-------+------+------+------+-------+-------+--------+---------+--------+--------+--------+----------+
| Name  | Samples | Mean | Min | Max   | P50  | P75  | P90  | P99   | P999  | Trans  | Trans/s | Errors | Error% | Recv/s | Write/s  |
+-------+---------+------+-----+-------+------+------+------+-------+-------+--------+---------+--------+--------+--------+----------+
| Total | 212575  | 28.2 | 1.0 | 305.0 | 21.0 | 37.0 | 58.0 | 110.0 | 168.0 | 212575 | 3542.8  | 0      | 0.0    | 0.0B   | 183.37KB |
+-------+---------+------+-----+-------+------+------+------+-------+-------+--------+---------+--------+--------+--------+----------+
  *** Sampling Result: SUCCESS
===========================================================================================================================
```

- 200 个线程运行 60 秒测试结果

JMeter 测试结果：

```bash
Starting the test @ Fri Nov 24 18:10:29 CST 2023 (1700820629673)
Waiting for possible Shutdown/StopTestNow/Heapdump message on port 4445
summary +      1 in 00:00:03 =    0.4/s Avg:  2570 Min:  2570 Max:  2570 Err:     0 (0.00%) Active: 200 Started: 200 Finished: 0
summary +  68344 in 00:00:27 = 2528.4/s Avg:    83 Min:     3 Max:  3374 Err:     0 (0.00%) Active: 200 Started: 200 Finished: 0
summary =  68345 in 00:00:30 = 2299.4/s Avg:    83 Min:     3 Max:  3374 Err:     0 (0.00%)
summary +  85912 in 00:00:30 = 2877.5/s Avg:    71 Min:     2 Max:  1187 Err:     0 (0.00%) Active: 200 Started: 200 Finished: 0
summary = 154257 in 00:01:00 = 2589.1/s Avg:    76 Min:     2 Max:  3374 Err:     0 (0.00%)
summary +   1702 in 00:00:01 = 2676.1/s Avg:    71 Min:     4 Max:   379 Err:     0 (0.00%) Active: 0 Started: 200 Finished: 200
summary = 155959 in 00:01:00 = 2590.0/s Avg:    76 Min:     2 Max:  3374 Err:     0 (0.00%)
Tidying up ...    @ Fri Nov 24 18:11:30 CST 2023 (1700820690663)
... end of run
```

AngusTester 测试结果：

```bash
===========================================================================================================================
  *** Task Summary Report @ Jdbc TEST_PERFORMANCE @ 2023-11-24 18:14:26 ***
===========================================================================================================================
  Exec ID: 1001                                 Run Mode: LOCAL               Memory: 116.88MBM(Used), 9.12GBM(Free)
  Bytes: 0.0B(Recv), 9.08MB(Write)              Iterations: 179738            Cpu: 61.37%(Proc), 1245.63%(Sys)
  Date: 11/24 06:13:25 to 11/24 06:14:25        Duration: 60 Second           Threads: 0/200  terminated
+-------+---------+-------+-----+-------+------+------+-------+-------+-------+--------+---------+--------+--------+--------+----------+
| Name  | Samples | Mean  | Min | Max   | P50  | P75  | P90   | P99   | P999  | Trans  | Trans/s | Errors | Error% | Recv/s | Write/s  |
+-------+---------+-------+-----+-------+------+------+-------+-------+-------+--------+---------+--------+--------+--------+----------+
| Total | 179738  | 66.72 | 2.0 | 670.0 | 50.0 | 89.0 | 141.0 | 270.0 | 398.0 | 179738 | 2995.53 | 0      | 0.0    | 0.0B   | 155.04KB |
+-------+---------+-------+-----+-------+------+------+-------+-------+-------+--------+---------+--------+--------+--------+----------+
  *** Sampling Result: SUCCESS
===========================================================================================================================
```

- 500 个线程运行 60 秒测试结果

JMeter 测试结果：

```bash
Starting the test @ Fri Nov 24 18:15:41 CST 2023 (1700820941390)
Waiting for possible Shutdown/StopTestNow/Heapdump message on port 4445
summary +  30277 in 00:00:18 = 1661.8/s Avg:   294 Min:     3 Max:  5927 Err:     0 (0.00%) Active: 500 Started: 500 Finished: 0
summary +  64740 in 00:00:30 = 2157.9/s Avg:   232 Min:     3 Max:  2950 Err:     0 (0.00%) Active: 500 Started: 500 Finished: 0
summary =  95017 in 00:00:48 = 1970.5/s Avg:   252 Min:     3 Max:  5927 Err:     0 (0.00%)
summary +  21322 in 00:00:12 = 1759.8/s Avg:   281 Min:     3 Max:  3268 Err:     0 (0.00%) Active: 0 Started: 500 Finished: 500
summary = 116339 in 00:01:00 = 1928.2/s Avg:   257 Min:     3 Max:  5927 Err:     0 (0.00%)
Tidying up ...    @ Fri Nov 24 18:16:42 CST 2023 (1700821002123)
... end of run
```

AngusTester 测试结果：

```bash
===========================================================================================================================
  *** Task Summary Report @ Jdbc TEST_PERFORMANCE @ 2023-11-24 18:18:24 ***
===========================================================================================================================
  Exec ID: 1001                                 Run Mode: LOCAL               Memory: 277.65MBM(Used), 8.96GBM(Free)
  Bytes: 0.0B(Recv), 7.36MB(Write)              Iterations: 145556            Cpu: 74.65%(Proc), 1175.65%(Sys)
  Date: 11/24 06:17:24 to 11/24 06:18:24        Duration: 60 Second           Threads: 0/500  terminated
+-------+---------+--------+-----+--------+-------+-------+-------+--------+---------+--------+---------+--------+--------+--------+----------+
| Name  | Samples | Mean   | Min | Max    | P50   | P75   | P90   | P99    | P999    | Trans  | Trans/s | Errors | Error% | Recv/s | Write/s  |
+-------+---------+--------+-----+--------+-------+-------+-------+--------+---------+--------+---------+--------+--------+--------+----------+
| Total | 145556  | 206.02 | 2.0 | 3642.0 | 140.0 | 258.0 | 434.0 | 1165.0 | 2002.89 | 145556 | 2425.69 | 0      | 0.0    | 0.0B   | 125.55KB |
+-------+---------+--------+-----+--------+-------+-------+-------+--------+---------+--------+---------+--------+--------+--------+----------+
  *** Sampling Result: SUCCESS
===========================================================================================================================
```

- 1000 个线程运行 60 秒测试结果

JMeter 测试结果：

```bash
Starting the test @ Fri Nov 24 18:19:04 CST 2023 (1700821144033)
Waiting for possible Shutdown/StopTestNow/Heapdump message on port 4445
summary +  29946 in 00:00:26 = 1170.4/s Avg:   833 Min:     3 Max: 21601 Err:     0 (0.00%) Active: 1000 Started: 1000 Finished: 0
summary +  49145 in 00:00:30 = 1637.7/s Avg:   609 Min:     2 Max: 15572 Err:     0 (0.00%) Active: 1000 Started: 1000 Finished: 0
summary =  79091 in 00:00:56 = 1422.7/s Avg:   694 Min:     2 Max: 21601 Err:     0 (0.00%)
summary +   7287 in 00:00:05 = 1423.0/s Avg:   690 Min:     3 Max:  5303 Err:     0 (0.00%) Active: 0 Started: 1000 Finished: 1000
summary =  86378 in 00:01:01 = 1422.7/s Avg:   694 Min:     2 Max: 21601 Err:     0 (0.00%)
Tidying up ...    @ Fri Nov 24 18:20:05 CST 2023 (1700821205132)
... end of run
```

AngusTester 测试结果：

```bash
===========================================================================================================================
  *** Task Summary Report @ Jdbc TEST_PERFORMANCE @ 2023-11-24 18:23:03 ***
===========================================================================================================================
  Exec ID: 1001                                 Run Mode: LOCAL               Memory: 152.27MBM(Used), 9.09GBM(Free)
  Bytes: 0.0B(Recv), 4.9MB(Write)               Iterations: 96963             Cpu: 139.98%(Proc), 1146.34%(Sys)
  Date: 11/24 06:22:03 to 11/24 06:23:03        Duration: 60 Second           Threads: 0/1000  terminated
+-------+---------+--------+-----+---------+-------+-------+--------+---------+---------+-------+---------+--------+--------+--------+---------+
| Name  | Samples | Mean   | Min | Max     | P50   | P75   | P90    | P99     | P999    | Trans | Trans/s | Errors | Error% | Recv/s | Write/s |
+-------+---------+--------+-----+---------+-------+-------+--------+---------+---------+-------+---------+--------+--------+--------+---------+
| Total | 96963   | 616.32 | 2.0 | 22072.0 | 282.0 | 602.0 | 1301.0 | 6299.36 | 9910.36 | 96963 | 1615.81 | 0      | 0.0    | 0.0B   | 83.63KB |
+-------+---------+--------+-----+---------+-------+-------+--------+---------+---------+-------+---------+--------+--------+--------+---------+
  *** Sampling Result: SUCCESS
===========================================================================================================================
```

- 2000 个线程运行 60 秒测试结果

JMeter 测试结果：

```bash
Starting the test @ Fri Nov 24 18:30:50 CST 2023 (1700821850360)
Waiting for possible Shutdown/StopTestNow/Heapdump message on port 4445
summary +    176 in 00:00:09 =   19.1/s Avg:  5352 Min:    65 Max:  9064 Err:    39 (22.16%) Active: 2000 Started: 2000 Finished: 0
summary +  18908 in 00:00:30 =  630.5/s Avg:  2913 Min:     4 Max: 39041 Err:     0 (0.00%) Active: 2000 Started: 2000 Finished: 0
summary =  19084 in 00:00:39 =  486.9/s Avg:  2935 Min:     4 Max: 39041 Err:    39 (0.20%)
summary +  17040 in 00:00:23 =  747.1/s Avg:  3778 Min:     3 Max: 60981 Err:     0 (0.00%) Active: 0 Started: 2000 Finished: 2000
summary =  36124 in 00:01:02 =  582.6/s Avg:  3333 Min:     3 Max: 60981 Err:    39 (0.11%)
Tidying up ...    @ Fri Nov 24 18:31:52 CST 2023 (1700821912816)
... end of run
```

AngusTester 测试结果：

```bash
===========================================================================================================================
  Exec ID: 1001                                 Run Mode: LOCAL               Memory: 352.47MBM(Used), 8.89GBM(Free)
  Bytes: 0.0B(Recv), 2.86MB(Write)              Iterations: 56653             Cpu: 182.89%(Proc), 1318.76%(Sys)
  Date: 11/24 06:33:28 to 11/24 06:34:28        Duration: 60 Second           Threads: 0/2000  terminated
+-------+---------+---------+-----+---------+-------+-------+--------+----------+----------+-------+---------+--------+--------+--------+---------+
| Name  | Samples | Mean    | Min | Max     | P50   | P75   | P90    | P99      | P999     | Trans | Trans/s | Errors | Error% | Recv/s | Write/s |
+-------+---------+---------+-----+---------+-------+-------+--------+----------+----------+-------+---------+--------+--------+--------+---------+
| Total | 56653   | 2083.02 | 3.0 | 60515.0 | 331.0 | 899.0 | 5566.0 | 30802.46 | 49362.16 | 56653 | 944.14  | 3      | 0.01   | 0.0B   | 48.86KB |
+-------+---------+---------+-----+---------+-------+-------+--------+----------+----------+-------+---------+--------+--------+--------+---------+
  *** Sampling Result: SUCCESS
===========================================================================================================================
```

### Select 测试

以下测试 AngusTester 和 JMter 使用同一份表数据。

- 1 个线程运行 60 秒测试结果

JMeter 测试结果：

```bash
Starting the test @ Fri Nov 24 14:19:05 CST 2023 (1700806745751)
Waiting for possible Shutdown/StopTestNow/Heapdump message on port 4445
summary +  22263 in 00:00:24 =  933.3/s Avg:     1 Min:     0 Max:   390 Err:     0 (0.00%) Active: 1 Started: 1 Finished: 0
summary +  29662 in 00:00:30 =  988.8/s Avg:     0 Min:     0 Max:    50 Err:     0 (0.00%) Active: 1 Started: 1 Finished: 0
summary =  51925 in 00:00:54 =  964.2/s Avg:     1 Min:     0 Max:   390 Err:     0 (0.00%)
summary +   6144 in 00:00:06 =  990.6/s Avg:     0 Min:     0 Max:    10 Err:     0 (0.00%) Active: 0 Started: 1 Finished: 1
summary =  58069 in 00:01:00 =  966.9/s Avg:     1 Min:     0 Max:   390 Err:     0 (0.00%)
Tidying up ...    @ Fri Nov 24 14:20:06 CST 2023 (1700806806202)
... end of run
```

AngusTester 测试结果：

```bash
===========================================================================================================================
  *** Task Summary Report @ Jdbc TEST_PERFORMANCE @ 2023-11-24 13:37:21 ***
===========================================================================================================================
  Exec ID: 1002                                 Run Mode: LOCAL               Memory: 82.25MBM(Used), 9.15GBM(Free)
  Bytes: 0.0B(Recv), 0.0B(Write)                Iterations: 62053             Cpu: 7.67%(Proc), 380.15%(Sys)
  Date: 11/24 01:36:21 to 11/24 01:37:21        Duration: 60 Second           Threads: 0/1  terminated
+-------+---------+------+-----+------+-----+-----+-----+-----+------+-------+---------+--------+--------+--------+---------+
| Name  | Samples | Mean | Min | Max  | P50 | P75 | P90 | P99 | P999 | Trans | Trans/s | Errors | Error% | Recv/s | Write/s |
+-------+---------+------+-----+------+-----+-----+-----+-----+------+-------+---------+--------+--------+--------+---------+
| Total | 62053   | 0.96 | 0.0 | 28.0 | 1.0 | 1.0 | 1.0 | 2.0 | 2.0  | 62053 | 1034.2  | 0      | 0.0    | 0.0B   | 0.0B    |
+-------+---------+------+-----+------+-----+-----+-----+-----+------+-------+---------+--------+--------+--------+---------+
  *** Sampling Result: SUCCESS
===========================================================================================================================
```

- 10 个线程运行 60 秒测试结果

JMeter 测试结果：

```bash
Starting the test @ Fri Nov 24 14:13:03 CST 2023 (1700806383302)
Waiting for possible Shutdown/StopTestNow/Heapdump message on port 4445
summary +      1 in 00:00:01 =    1.8/s Avg:   498 Min:   498 Max:   498 Err:     0 (0.00%) Active: 10 Started: 10 Finished: 0
summary + 147477 in 00:00:26 = 5728.6/s Avg:     1 Min:     0 Max:  1008 Err:     0 (0.00%) Active: 10 Started: 10 Finished: 0
summary = 147478 in 00:00:26 = 5608.0/s Avg:     1 Min:     0 Max:  1008 Err:     0 (0.00%)
summary + 188223 in 00:00:30 = 6274.1/s Avg:     1 Min:     0 Max:   241 Err:     0 (0.00%) Active: 10 Started: 10 Finished: 0
summary = 335701 in 00:00:56 = 5962.9/s Avg:     1 Min:     0 Max:  1008 Err:     0 (0.00%)
summary +  20618 in 00:00:04 = 5489.4/s Avg:     1 Min:     1 Max:     7 Err:     0 (0.00%) Active: 0 Started: 10 Finished: 10
summary = 356319 in 00:01:00 = 5933.3/s Avg:     1 Min:     0 Max:  1008 Err:     0 (0.00%)
Tidying up ...    @ Fri Nov 24 14:14:03 CST 2023 (1700806443756)
... end of run
```

AngusTester 测试结果：

```bash
===========================================================================================================================
  *** Task Summary Report @ Jdbc TEST_PERFORMANCE @ 2023-11-24 14:24:47 ***
===========================================================================================================================
  Exec ID: 1002                                 Run Mode: LOCAL               Memory: 87.31MBM(Used), 9.15GBM(Free)
  Bytes: 0.0B(Recv), 0.0B(Write)                Iterations: 400707            Cpu: 334.73%(Proc), 382.61%(Sys)
  Date: 11/24 02:23:46 to 11/24 02:24:46        Duration: 60 Second           Threads: 0/10  terminated
+-------+---------+------+-----+------+-----+-----+-----+-----+------+--------+---------+--------+--------+--------+---------+
| Name  | Samples | Mean | Min | Max  | P50 | P75 | P90 | P99 | P999 | Trans  | Trans/s | Errors | Error% | Recv/s | Write/s |
+-------+---------+------+-----+------+-----+-----+-----+-----+------+--------+---------+--------+--------+--------+---------+
| Total | 400707  | 1.49 | 0.0 | 42.0 | 1.0 | 2.0 | 2.0 | 3.0 | 5.0  | 400707 | 6678.34 | 0      | 0.0    | 0.0B   | 0.0B    |
+-------+---------+------+-----+------+-----+-----+-----+-----+------+--------+---------+--------+--------+--------+---------+
  *** Sampling Result: SUCCESS
===========================================================================================================================
```

- 50 个线程运行 60 秒测试结果

JMeter 测试结果：

```bash
Starting the test @ Fri Nov 24 14:27:12 CST 2023 (1700807232560)
Waiting for possible Shutdown/StopTestNow/Heapdump message on port 4445
summary + 191458 in 00:00:17 = 11239.8/s Avg:     4 Min:     1 Max:   776 Err:     0 (0.00%) Active: 50 Started: 50 Finished: 0
summary + 312263 in 00:00:30 = 10408.8/s Avg:     4 Min:     1 Max:   212 Err:     0 (0.00%) Active: 50 Started: 50 Finished: 0
summary = 503721 in 00:00:47 = 10709.7/s Avg:     4 Min:     1 Max:   776 Err:     0 (0.00%)
summary + 114747 in 00:00:13 = 8807.7/s Avg:     5 Min:     1 Max:   145 Err:     0 (0.00%) Active: 0 Started: 50 Finished: 50
summary = 618468 in 00:01:00 = 10297.0/s Avg:     4 Min:     1 Max:   776 Err:     0 (0.00%)
Tidying up ...    @ Fri Nov 24 14:28:13 CST 2023 (1700807293029)
... end of run
```

AngusTester 测试结果：

```bash
===========================================================================================================================
  *** Task Summary Report @ Jdbc TEST_PERFORMANCE @ 2023-11-24 13:33:43 ***
===========================================================================================================================
  Exec ID: 1002                                 Run Mode: LOCAL               Memory: 211.14MBM(Used), 9.03GBM(Free)
  Bytes: 0.0B(Recv), 0.0B(Write)                Iterations: 636037            Cpu: 120.28%(Proc), 1191.94%(Sys)
  Date: 11/24 01:32:43 to 11/24 01:33:43        Duration: 60 Second           Threads: 0/50  terminated
+-------+---------+------+-----+------+-----+-----+-----+------+------+--------+----------+--------+--------+--------+---------+
| Name  | Samples | Mean | Min | Max  | P50 | P75 | P90 | P99  | P999 | Trans  | Trans/s  | Errors | Error% | Recv/s | Write/s |
+-------+---------+------+-----+------+-----+-----+-----+------+------+--------+----------+--------+--------+--------+---------+
| Total | 636037  | 4.71 | 1.0 | 72.0 | 4.0 | 5.0 | 7.0 | 13.0 | 26.0 | 636037 | 10600.26 | 0      | 0.0    | 0.0B   | 0.0B    |
+-------+---------+------+-----+------+-----+-----+-----+------+------+--------+----------+--------+--------+--------+---------+
  *** Sampling Result: SUCCESS
===========================================================================================================================
```

- 100 个线程运行 60 秒测试结果

JMeter 测试结果：

```bash
Starting the test @ Fri Nov 24 14:32:31 CST 2023 (1700807551943)
Waiting for possible Shutdown/StopTestNow/Heapdump message on port 4445
summary +      1 in 00:00:01 =    0.9/s Avg:  1005 Min:  1005 Max:  1005 Err:     0 (0.00%) Active: 100 Started: 100 Finished: 0
summary + 354652 in 00:00:27 = 13346.3/s Avg:     7 Min:     1 Max:  1047 Err:     0 (0.00%) Active: 100 Started: 100 Finished: 0
summary = 354653 in 00:00:28 = 12819.1/s Avg:     7 Min:     1 Max:  1047 Err:     0 (0.00%)
summary + 297778 in 00:00:30 = 9925.6/s Avg:     9 Min:     1 Max:  2452 Err:     0 (0.00%) Active: 100 Started: 100 Finished: 0
summary = 652431 in 00:00:58 = 11313.6/s Avg:     8 Min:     1 Max:  2452 Err:     0 (0.00%)
summary +  20427 in 00:00:02 = 8423.5/s Avg:    11 Min:     2 Max:   126 Err:     0 (0.00%) Active: 0 Started: 100 Finished: 100
summary = 672858 in 00:01:00 = 11196.9/s Avg:     8 Min:     1 Max:  2452 Err:     0 (0.00%)
Tidying up ...    @ Fri Nov 24 14:33:32 CST 2023 (1700807612428)
... end of run
```

AngusTester 测试结果：

```bash
===========================================================================================================================
  *** Task Summary Report @ Jdbc TEST_PERFORMANCE @ 2023-11-24 13:28:28 ***
===========================================================================================================================
  Exec ID: 1002                                 Run Mode: LOCAL               Memory: 113.04MBM(Used), 9.12GBM(Free)
  Bytes: 0.0B(Recv), 0.0B(Write)                Iterations: 811148            Cpu: 154.38%(Proc), 1271.88%(Sys)
  Date: 11/24 01:27:28 to 11/24 01:28:28        Duration: 60 Second           Threads: 0/100  terminated
+-------+---------+------+-----+--------+-----+-----+------+------+------+--------+----------+--------+--------+--------+---------+
| Name  | Samples | Mean | Min | Max    | P50 | P75 | P90  | P99  | P999 | Trans  | Trans/s  | Errors | Error% | Recv/s | Write/s |
+-------+---------+------+-----+--------+-----+-----+------+------+------+--------+----------+--------+--------+--------+---------+
| Total | 811148  | 7.39 | 1.0 | 1010.0 | 6.0 | 8.0 | 12.0 | 23.0 | 44.0 | 811148 | 13518.68 | 0      | 0.0    | 0.0B   | 0.0B    |
+-------+---------+------+-----+--------+-----+-----+------+------+------+--------+----------+--------+--------+--------+---------+
  *** Sampling Result: SUCCESS
===========================================================================================================================
```

- 200 个线程运行 60 秒测试结果

JMeter 测试结果：

```bash
Starting the test @ Fri Nov 24 14:35:57 CST 2023 (1700807757194)
Waiting for possible Shutdown/StopTestNow/Heapdump message on port 4445
summary +    157 in 00:00:02 =   69.1/s Avg:   784 Min:     4 Max:  2138 Err:     0 (0.00%) Active: 200 Started: 200 Finished: 0
summary + 312258 in 00:00:30 = 10415.9/s Avg:    19 Min:     1 Max:  2669 Err:     0 (0.00%) Active: 200 Started: 200 Finished: 0
summary = 312415 in 00:00:32 = 9687.0/s Avg:    20 Min:     1 Max:  2669 Err:     0 (0.00%)
summary + 301315 in 00:00:28 = 10802.1/s Avg:    18 Min:     1 Max:   899 Err:     0 (0.00%) Active: 0 Started: 200 Finished: 200
summary = 613730 in 00:01:00 = 10204.0/s Avg:    19 Min:     1 Max:  2669 Err:     0 (0.00%)
Tidying up ...    @ Fri Nov 24 14:36:57 CST 2023 (1700807817896)
... end of run
```

AngusTester 测试结果：

```bash
===========================================================================================================================
  *** Task Summary Report @ Jdbc TEST_PERFORMANCE @ 2023-11-24 13:26:41 ***
===========================================================================================================================
  Exec ID: 1002                                 Run Mode: LOCAL               Memory: 129.1MBM(Used), 9.11GBM(Free)
  Bytes: 0.0B(Recv), 0.0B(Write)                Iterations: 932502            Cpu: 213.32%(Proc), 1423.54%(Sys)
  Date: 11/24 01:25:41 to 11/24 01:26:41        Duration: 60 Second           Threads: 0/200  terminated
+-------+---------+-------+-----+-------+------+------+------+------+-------+--------+----------+--------+--------+--------+---------+
| Name  | Samples | Mean  | Min | Max   | P50  | P75  | P90  | P99  | P999  | Trans  | Trans/s  | Errors | Error% | Recv/s | Write/s |
+-------+---------+-------+-----+-------+------+------+------+------+-------+--------+----------+--------+--------+--------+---------+
| Total | 932502  | 12.86 | 1.0 | 383.0 | 10.0 | 14.0 | 23.0 | 59.0 | 134.0 | 932502 | 15540.66 | 0      | 0.0    | 0.0B   | 0.0B    |
+-------+---------+-------+-----+-------+------+------+------+------+-------+--------+----------+--------+--------+--------+---------+
  *** Sampling Result: SUCCESS
===========================================================================================================================
```

- 500 个线程运行 60 秒测试结果

JMeter 测试结果：

```bash
Starting the test @ Fri Nov 24 14:39:23 CST 2023 (1700807963131)
Waiting for possible Shutdown/StopTestNow/Heapdump message on port 4445
summary +  26931 in 00:00:06 = 4162.4/s Avg:   117 Min:     1 Max:  5782 Err:     0 (0.00%) Active: 500 Started: 500 Finished: 0
summary + 330185 in 00:00:30 = 11006.2/s Avg:    44 Min:     1 Max:  2146 Err:     0 (0.00%) Active: 500 Started: 500 Finished: 0
summary = 357116 in 00:00:36 = 9792.0/s Avg:    49 Min:     1 Max:  5782 Err:     0 (0.00%)
summary + 210966 in 00:00:24 = 8725.9/s Avg:    56 Min:     1 Max:  1531 Err:     0 (0.00%) Active: 0 Started: 500 Finished: 500
summary = 568082 in 00:01:01 = 9367.0/s Avg:    52 Min:     1 Max:  5782 Err:     0 (0.00%)
Tidying up ...    @ Fri Nov 24 14:40:24 CST 2023 (1700808024178)
... end of run
```

AngusTester 测试结果：

```bash
===========================================================================================================================
  *** Task Summary Report @ Jdbc TEST_PERFORMANCE @ 2023-11-24 13:00:22 ***
===========================================================================================================================
  Exec ID: 1002                                 Run Mode: LOCAL               Memory: 377.07MBM(Used), 8.87GBM(Free)
  Bytes: 0.0B(Recv), 0.0B(Write)                Iterations: 758963            Cpu: 312.98%(Proc), 1481.01%(Sys)
  Date: 11/24 12:59:21 to 11/24 01:00:21        Duration: 60 Second           Threads: 0/500  terminated
+-------+---------+-------+-----+--------+------+------+------+-------+-------+--------+----------+--------+--------+--------+---------+
| Name  | Samples | Mean  | Min | Max    | P50  | P75  | P90  | P99   | P999  | Trans  | Trans/s  | Errors | Error% | Recv/s | Write/s |
+-------+---------+-------+-----+--------+------+------+------+-------+-------+--------+----------+--------+--------+--------+---------+
| Total | 758963  | 39.51 | 1.0 | 1693.0 | 20.0 | 37.0 | 83.0 | 343.0 | 608.0 | 758963 | 12648.12 | 0      | 0.0    | 0.0B   | 0.0B    |
+-------+---------+-------+-----+--------+------+------+------+-------+-------+--------+----------+--------+--------+--------+---------+
  *** Sampling Result: SUCCESS
===========================================================================================================================
```

- 1000 个线程运行 60 秒测试结果

JMeter 测试结果：

```bash
Starting the test @ Fri Nov 24 14:40:35 CST 2023 (1700808035915)
Waiting for possible Shutdown/StopTestNow/Heapdump message on port 4445
summary + 114773 in 00:00:24 = 4849.3/s Avg:   196 Min:     2 Max: 14426 Err:     0 (0.00%) Active: 1000 Started: 1000 Finished: 0
summary + 208145 in 00:00:30 = 6947.4/s Avg:   146 Min:     1 Max:  3737 Err:     0 (0.00%) Active: 1000 Started: 1000 Finished: 0
summary = 322918 in 00:00:54 = 6021.4/s Avg:   164 Min:     1 Max: 14426 Err:     0 (0.00%)
summary +  48142 in 00:00:07 = 6982.2/s Avg:   139 Min:     2 Max:  2640 Err:     0 (0.00%) Active: 0 Started: 1000 Finished: 1000
summary = 371060 in 00:01:01 = 6130.8/s Avg:   161 Min:     1 Max: 14426 Err:     0 (0.00%)
Tidying up ...    @ Fri Nov 24 14:41:36 CST 2023 (1700808096896)
... end of run
```

AngusTester 测试结果：

```bash
===========================================================================================================================
  *** Task Summary Report @ Jdbc TEST_PERFORMANCE @ 2023-11-24 13:22:12 ***
===========================================================================================================================
  Exec ID: 1002                                 Run Mode: LOCAL               Memory: 248.41MBM(Used), 8.99GBM(Free)
  Bytes: 0.0B(Recv), 0.0B(Write)                Iterations: 752604            Cpu: 519.84%(Proc), 1499.72%(Sys)
  Date: 11/24 01:21:11 to 11/24 01:22:11        Duration: 60 Second           Threads: 0/1000  terminated
+-------+---------+-------+-----+--------+------+------+-------+-------+--------+--------+----------+--------+--------+--------+---------+
| Name  | Samples | Mean  | Min | Max    | P50  | P75  | P90   | P99   | P999   | Trans  | Trans/s  | Errors | Error% | Recv/s | Write/s |
+-------+---------+-------+-----+--------+------+------+-------+-------+--------+--------+----------+--------+--------+--------+---------+
| Total | 752604  | 79.42 | 1.0 | 3352.0 | 33.0 | 65.0 | 181.0 | 810.0 | 1351.0 | 752604 | 12540.06 | 0      | 0.0    | 0.0B   | 0.0B    |
+-------+---------+-------+-----+--------+------+------+-------+-------+--------+--------+----------+--------+--------+--------+---------+
  *** Sampling Result: SUCCESS
===========================================================================================================================
```

- 2000 个线程运行 60 秒测试结果

JMeter 测试结果：

```bash
Starting the test @ Fri Nov 24 14:42:32 CST 2023 (1700808152699)
Waiting for possible Shutdown/StopTestNow/Heapdump message on port 4445
summary + 142530 in 00:00:27 = 5301.9/s Avg:   248 Min:     1 Max: 26573 Err:    22 (0.02%) Active: 2000 Started: 2000 Finished: 0
summary + 124608 in 00:00:30 = 4153.6/s Avg:   597 Min:     2 Max: 47158 Err:     0 (0.00%) Active: 2000 Started: 2000 Finished: 0
summary = 267138 in 00:00:57 = 4696.3/s Avg:   411 Min:     1 Max: 47158 Err:    22 (0.01%)
summary +  15477 in 00:00:05 = 3236.5/s Avg:   608 Min:     3 Max:  8756 Err:     0 (0.00%) Active: 0 Started: 2000 Finished: 2000
summary = 282615 in 00:01:02 = 4583.0/s Avg:   422 Min:     1 Max: 47158 Err:    22 (0.01%)
Tidying up ...    @ Fri Nov 24 14:43:34 CST 2023 (1700808214784)
... end of run
```

AngusTester 测试结果：

```bash
===========================================================================================================================
  *** Task Summary Report @ Jdbc TEST_PERFORMANCE @ 2023-11-24 13:24:44 ***
===========================================================================================================================
  Exec ID: 1002                                 Run Mode: LOCAL               Memory: 611.72MBM(Used), 8.64GBM(Free)
  Bytes: 0.0B(Recv), 0.0B(Write)                Iterations: 401359            Cpu: 550.80%(Proc), 1347.39%(Sys)
  Date: 11/24 01:23:43 to 11/24 01:24:43        Duration: 60 Second           Threads: 0/2000  terminated
+-------+---------+--------+-----+---------+------+-------+-------+--------+--------+--------+---------+--------+--------+--------+---------+
| Name  | Samples | Mean   | Min | Max     | P50  | P75   | P90   | P99    | P999   | Trans  | Trans/s | Errors | Error% | Recv/s | Write/s |
+-------+---------+--------+-----+---------+------+-------+-------+--------+--------+--------+---------+--------+--------+--------+---------+
| Total | 401359  | 296.03 | 1.0 | 13344.0 | 80.0 | 218.0 | 813.0 | 3054.0 | 6045.4 | 401359 | 6685.86 | 0      | 0.0    | 0.0B   | 0.0B    |
+-------+---------+--------+-----+---------+------+-------+-------+--------+--------+--------+---------+--------+--------+--------+---------+
  *** Sampling Result: SUCCESS
===========================================================================================================================
```

## 对比结果

- 相同条件下，不断增加并发线程数，AngusTester 性能都要比 JMeter 高。
    - 在 Insert 测试中，JMeter 50 并发时 QPS 达到最高 2843.6/s，AngusTester 100 并发时 QPS 达到最高
      3542.8，***最大 QPS 提升了 24.59%***；
    - 在 Select 测试中，JMeter 100 并发时 QPS 达到最高 11196.9/s，AngusTester 200 并发时 QPS 达到最高
      15540.66，***最大 QPS 提升了 38.79***%。
- QPS 达到最大后，随着并发线程数继续增加，JMeter 性能下降迅速，AngusTester 性能下降比较平缓，表现平稳。
