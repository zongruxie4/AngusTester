<script setup lang="ts">
import { ref, defineAsyncComponent, reactive, onMounted, nextTick, onBeforeUnmount, computed, watch, inject } from 'vue';
import { Form, FormItem, Switch, Collapse, CollapsePanel, Checkbox, CheckboxGroup, Badge, Button } from 'ant-design-vue';
import { Input, Select, SelectEnum, DatePicker, Icon, IconRequired, Arrow, RadioGroup, Tooltip, ShortDuration, Colon } from '@xcan-angus/vue-ui';
import { TESTER, SupportedLanguage, DatabaseType, TransactionIsolation, PoolType } from '@xcan-angus/infra';
import type { Rule } from 'ant-design-vue/es/form';
import elementResizeDetector from 'element-resize-detector';
import { cloneDeep } from 'lodash-es';
import { useI18n } from 'vue-i18n';
const { t } = useI18n();

export interface Props {
  scriptType: string;
  scriptInfo?: Record<string, any>;
  addType?: 'expr'; // expr 体验执行
  isDeep?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  scriptInfo: undefined,
  addType: undefined,
  isDeep: false
});

const projectInfo = inject('projectInfo', ref({ id: '' }));

const LineChart = defineAsyncComponent(() => import('./LineChart.vue'));
const Variables = defineAsyncComponent(() => import('./Variables/index.vue'));
const SelectDataSourceModal = defineAsyncComponent(() => import('./SelectDataSourceModal/index.vue'));

const variablesRef = ref();

const splitTime = (str: string): [string, string] => {
  const number = str.replace(/\D/g, '');
  const unit = str.replace(/\d/g, '');
  return [number, unit];
};

const chartData = reactive({
  xData: [],
  yData: []
});
const configurationtends = ref();
const pluginType = ref();
const pluginSettingExtends = ref();
const configVariables = ref([]);

const modalVisible = ref(false);

const toSelectDataSource = () => {
  modalVisible.value = true;
};

const selectedDataSourceOk = (data) => {
  if (jdbcArgumentsParams.value.jdbcSetting) {
    jdbcArgumentsParams.value.jdbcSetting.type = data.database;
    jdbcArgumentsParams.value.jdbcSetting.jdbcUrl = data.jdbcUrl;
    jdbcArgumentsParams.value.jdbcSetting.username = data.username;
    jdbcArgumentsParams.value.jdbcSetting.password = data.password;
  } else {
    jdbcArgumentsParams.value.jdbcSetting = {
      type: data.database,
      jdbcUrl: data.jdbcUrl,
      username: data.username,
      password: data.password
    };
  }
};

// 变更图表数据
const setEchartsData = () => {
  const { duration, thread } = executParams.value;
  const durationOfSec = +duration;
  const xData = [];
  const yData = [];
  if (!duration || !thread) {
    return;
  }
  if (supercharged.value && stressReliever.value) {
    const { threads, rampUpInterval, rampUpThreads, rampDownInterval, rampDownThreads } = thread;
    if (!rampUpInterval || !rampUpThreads || !rampDownInterval || !rampDownThreads) {
      return;
    }
    const supers = Math.ceil((+threads) / (+rampUpThreads)) - 1; // 升阶段 点个数;
    const superDuration = +rampUpInterval * supers; // 升阶段 使用时长;

    const downs = Math.round((+threads) / (+rampDownThreads) * 10) / 10; // 降阶段 点个数;
    const downDuration = +rampDownInterval * downs; // 降阶段 使用时长;
    const downTimeSec = durationOfSec - downDuration;
    const startTime = 0;
    const intervalNum = 1;

    if (superDuration + downDuration < +durationOfSec) {
      let currentTreads = +rampUpThreads;
      for (let i = 0; i <= durationOfSec; i = i + intervalNum) {
        xData.push(startTime + i);
        if (i <= superDuration) {
          if (i % +rampUpInterval === 0 && i !== 0) {
            currentTreads = currentTreads + (+rampUpThreads);
            if (currentTreads > +threads) {
              currentTreads = +threads;
            }
          }
          yData.push(currentTreads);
        }

        if (i > superDuration && i <= downTimeSec) {
          currentTreads = +threads;
          yData.push(currentTreads);
        }

        if (i > downTimeSec) {
          if (i % +rampDownInterval === 0) {
            currentTreads = currentTreads - (+rampDownThreads);
            if (currentTreads < 0) {
              currentTreads = 0;
            }
          }
          yData.push(currentTreads);
        }
      }
      chartData.xData = xData;
      chartData.yData = yData;
    }
    if (superDuration + downDuration >= +durationOfSec) {
      let currentTreads = +rampUpThreads;
      for (let i = 0; i <= durationOfSec; i = i + intervalNum) {
        xData.push(startTime + i);
        if (i < superDuration) {
          if (i % +rampUpInterval === 0 && i !== 0) {
            currentTreads = currentTreads + (+rampUpThreads);
            if (currentTreads > +threads) {
              currentTreads = +threads;
            }
          }
          yData.push(currentTreads);
        } else if (i === superDuration || !yData.includes(+threads)) {
          currentTreads = +threads;
          yData.push(currentTreads);
        } else if (i > downTimeSec) {
          if (i % +rampDownInterval === 0) {
            currentTreads = currentTreads - (+rampDownThreads);
            if (currentTreads < 0) {
              currentTreads = 0;
            }
          }
          yData.push(currentTreads);
        }
      }
      chartData.xData = xData;
      chartData.yData = yData;
    }
  }

  if (supercharged.value && !stressReliever.value) {
    const { threads, rampUpInterval, rampUpThreads } = thread;
    if (!rampUpInterval || !rampUpThreads) {
      return;
    }
    const supers = Math.ceil((+threads) / (+rampUpThreads)) - 1; // 升阶段;
    const superDuration = +rampUpInterval * supers; // 升阶段 使用时长;
    const startTime = 0;
    const intervalNum = 1;

    let currentTreads = +rampUpThreads;
    for (let i = 0; i <= durationOfSec; i = i + intervalNum) {
      xData.push(startTime + i);
      if (i <= superDuration) {
        if (i % +rampUpInterval === 0 && i !== 0) {
          currentTreads = currentTreads + (+rampUpThreads);
          if (currentTreads > +threads) {
            currentTreads = +threads;
          }
        }
        yData.push(currentTreads);
      } else {
        currentTreads = +threads;
        yData.push(currentTreads);
      }
    }
    chartData.xData = xData;
    chartData.yData = yData;
  }

  if (!supercharged.value && stressReliever.value) {
    const { threads, rampDownInterval, rampDownThreads } = thread;
    if (!rampDownInterval || !rampDownThreads) {
      return;
    }
    const downs = Math.ceil((+threads) / (+rampDownThreads)); // 降阶段 点个数;
    const downDuration = +rampDownInterval * downs; // 降阶段 使用时长;
    const downTimeSec = durationOfSec - downDuration;
    const startTime = 0;
    const intervalNum = 1;
    let currentTreads = +threads;
    if (downTimeSec <= 0) {
      for (let i = 0; i <= durationOfSec; i = i + intervalNum) {
        xData.push(startTime + i);
        if (i % +rampDownInterval === 0 && i !== 0) {
          currentTreads = currentTreads - (+rampDownThreads);
        }
        yData.push(currentTreads);
      }
    } else {
      for (let i = 0; i <= durationOfSec; i = i + intervalNum) {
        xData.push(startTime + i);
        if (i <= downTimeSec) {
          yData.push(currentTreads);
        } else {
          if (i % +rampDownInterval === 0 && i !== 0) {
            currentTreads = currentTreads - (+rampDownThreads);
            if (currentTreads < 0) {
              currentTreads = 0;
            }
          }
          yData.push(currentTreads);
        }
      }
    }
    chartData.xData = xData;
    chartData.yData = yData;
  }
  if (!supercharged.value && !stressReliever.value) {
    const { threads } = thread;
    const startTime = 0;
    const endTime = durationOfSec;
    xData.push(startTime);
    xData.push(endTime);
    yData.push(threads);
    yData.push(threads);
    chartData.xData = xData;
    chartData.yData = yData;
  }
};

const initParams = (isSwitch?: boolean) => {
  if (!props.scriptInfo) {
    return;
  }
  const info = cloneDeep(props.scriptInfo);
  configurationtends.value = {};
  pluginSettingExtends.value = undefined;
  const { configuration, plugin, task, type } = info;
  pluginType.value = plugin;
  if (configuration) {
    const { duration, thread, iterations, startMode, startAtDate, priority, reportInterval, lang, startupTimeout, runnerSetupTimeout, shutdownTimeout, onError, nodeSelectors, variables, ...configOhters } = configuration;
    if (configOhters) {
      configurationtends.value = configOhters;
    }

    if (props.scriptType === type) {
      if (duration) {
        const strs = splitTime(duration);
        executParams.value.duration = strs[0];
        durationUnit.value = strs[1];
      }

      if (iterations) {
        executParams.value.iterations = iterations || '';
      }

      if (thread) {
        const { threads, rampUpInterval, rampUpThreads, rampDownInterval, rampDownThreads, resetAfterRamp } = thread;

        executParams.value.thread.threads = props.scriptType === 'TEST_FUNCTIONALITY' ? '1' : threads;

        if (rampUpInterval && rampUpThreads) {
          supercharged.value = true;
          executParams.value.thread.rampUpInterval = rampUpInterval.match(/\d+/)[0];
          executParams.value.thread.rampUpThreads = rampUpThreads;
        } else {
          supercharged.value = false;
        }

        if (rampDownInterval && rampDownThreads) {
          stressReliever.value = true;
          executParams.value.thread.rampDownInterval = rampDownInterval.match(/\d+/)[0];
          executParams.value.thread.rampDownThreads = rampDownThreads;
        } else {
          stressReliever.value = false;
        }

        executParams.value.thread.resetAfterRamp = typeof resetAfterRamp === 'boolean' ? [resetAfterRamp] : [];
      }
    } else {
      if (props.scriptType !== type && props.scriptType === 'TEST_FUNCTIONALITY') {
        executParams.value.thread.threads = '1';
        executParams.value.iterations = '1';
      }

      if (props.scriptType !== type && props.scriptType === 'TEST_PERFORMANCE') {
        executParams.value.thread.threads = '5000';
        executParams.value.thread.rampUpInterval = '1';
        executParams.value.thread.rampUpThreads = '100';
        executParams.value.thread.resetAfterRamp = [];
        executParams.value.duration = '50';
        durationUnit.value = 'min';
        supercharged.value = true;
      }

      if (props.scriptType !== type && props.scriptType === 'TEST_STABILITY') {
        executParams.value.thread.threads = '200';
        executParams.value.duration = '30';
        durationUnit.value = 'min';
      }
    }

    globalParams.value.startMode = startMode?.value || undefined;
    globalParams.value.startAtDate = startAtDate || undefined;
    globalParams.value.priority = priority || '';
    if (reportInterval) {
      const strs = splitTime(reportInterval);
      globalParams.value.reportInterval = strs[0];
      reportIntervalUnit.value = strs[1];
    }
    globalParams.value.lang = lang || undefined;
    if (shutdownTimeout) {
      const strs = splitTime(shutdownTimeout);
      globalParams.value.shutdownTimeout = strs[0];
      shutdownTimeoutUnit.value = strs[1];
    }

    if (startupTimeout) {
      const strs = splitTime(startupTimeout);
      globalParams.value.startupTimeout = strs[0];
      startupTimeoutUnit.value = strs[1];
    }

    if (runnerSetupTimeout) {
      const strs = splitTime(runnerSetupTimeout);
      globalParams.value.runnerSetupTimeout = strs[0];
      runnerSetupTimeoutUnit.value = strs[1];
    }

    if (onError) {
      const { action, sampleError, sampleErrorNum } = onError;
      globalParams.value.onError.action = action?.value || undefined;
      globalParams.value.onError.sampleError = typeof sampleError === 'boolean' ? sampleError : true;
      globalParams.value.onError.sampleErrorNum = globalParams.value.onError.sampleError ? sampleErrorNum || '20' : '';
    }

    if (nodeSelectors) {
      const { num, strategy, availableNodeIds, appNodeIds } = nodeSelectors;
      globalParams.value.nodeSelectors.num = num || '';
      globalParams.value.nodeSelectors.availableNodeIds = availableNodeIds?.length ? availableNodeIds : [];
      globalParams.value.nodeSelectors.appNodeIds = appNodeIds?.length ? appNodeIds : [];
      if (strategy) {
        const { enabled, maxTaskNum, lastExecuted, specEnabled, cpuSpec, memorySpec, diskSpec, idleRateEnabled, cpuIdleRate, memoryIdleRate, diskIdleRate } = strategy;
        globalParams.value.nodeSelectors.enabled = !!enabled;
        if (enabled) {
          globalParams.value.nodeSelectors.maxTaskNum = maxTaskNum || '';
          globalParams.value.nodeSelectors.lastExecuted = !!lastExecuted;
          globalParams.value.nodeSelectors.idleRateEnabled = !!idleRateEnabled;
          globalParams.value.nodeSelectors.specEnabled = !!specEnabled;
          if (specEnabled) {
            globalParams.value.nodeSelectors.cpuSpec = cpuSpec || '';
            globalParams.value.nodeSelectors.memorySpec = memorySpec ? memorySpec.replace(/GB/g, '') : '';
            globalParams.value.nodeSelectors.diskSpec = diskSpec ? diskSpec.replace(/GB/g, '') : '';
          }
          if (idleRateEnabled) {
            globalParams.value.nodeSelectors.cpuIdleRate = cpuIdleRate ? cpuIdleRate.replace(/%/g, '') : '';
            globalParams.value.nodeSelectors.memoryIdleRate = memoryIdleRate ? memoryIdleRate.replace(/%/g, '') : '';
            globalParams.value.nodeSelectors.diskIdleRate = diskIdleRate ? diskIdleRate.replace(/%/g, '') : '';
          }
        }
      }
    }

    if (variables?.length) {
      configVariables.value = variables;
    }
  } else {
    if (props.scriptType !== type && props.scriptType === 'TEST_FUNCTIONALITY') {
      executParams.value.thread.threads = '1';
      executParams.value.iterations = '1';
    } else {
      executParams.value.iterations = '';
    }

    if (props.scriptType !== type && props.scriptType === 'TEST_PERFORMANCE') {
      executParams.value.thread.threads = '5000';
      executParams.value.thread.rampUpInterval = '1';
      executParams.value.thread.rampUpThreads = '100';
      executParams.value.thread.resetAfterRamp = [];
      executParams.value.duration = '50';
      durationUnit.value = 'min';
      supercharged.value = true;
    }

    if (props.scriptType !== type && props.scriptType === 'TEST_STABILITY') {
      executParams.value.thread.threads = '200';
      executParams.value.duration = '30';
      durationUnit.value = 'min';
    }
  }

  if (plugin === 'Http') {
    if (task) {
      const { arguments: ar } = task;
      if (ar) {
        const { httpSetting, ignoreAssertions, updateTestResult, ...others } = ar;
        httpArgumentsParams.value.ignoreAssertions = getIgnoreAssertions(ignoreAssertions);
        httpArgumentsParams.value.updateTestResult = !!updateTestResult;

        if (httpSetting) {
          const { connectTimeout, readTimeout, retryNum, maxRedirects, ...settingOthers } = httpSetting;
          if (connectTimeout) {
            const strs = splitTime(connectTimeout);
            httpArgumentsParams.value.httpSetting.connectTimeout = strs[0];
            httpSettingReadTimeoutUnit.value = strs[1];
          }

          if (readTimeout) {
            const strs = splitTime(readTimeout);
            httpArgumentsParams.value.httpSetting.readTimeout = strs[0];
            httpSettingReadTimeoutUnit.value = strs[1];
          }

          httpArgumentsParams.value.httpSetting.retryNum = retryNum || '';
          httpArgumentsParams.value.httpSetting.maxRedirects = maxRedirects || '';
          // 保存setting的扩展属性 保存需要带回去
          pluginSettingExtends.value = settingOthers;
        }

        if (others) {
          extendParams.value.extend = [];
          const _keys = Object.keys(others);
          if (_keys?.length) {
            _keys.forEach(key => {
              extendParams.value.extend.push({
                key: key,
                value: others[key]
              });
            });
          } else {
            extendParams.value.extend = [{ key: '', value: '' }];
          }
        }
      }
    }
  }

  if (plugin === 'WebSocket') {
    if (task) {
      const { arguments: ar } = task;
      if (ar) {
        const { webSocketSetting, ignoreAssertions, updateTestResult, ...others } = ar;
        webSocketArgumentsParams.value.ignoreAssertions = getIgnoreAssertions(ignoreAssertions);
        webSocketArgumentsParams.value.updateTestResult = !!updateTestResult;

        if (webSocketSetting) {
          const { connectTimeout, responseTimeout, reconnectionInterval, maxReconnections, ...settingOthers } = webSocketSetting;
          if (connectTimeout) {
            const strs = splitTime(connectTimeout);
            webSocketArgumentsParams.value.webSocketSetting.connectTimeout = strs[0];
            webSocketSettingConnectTimeoutUnit.value = strs[1];
          }

          if (responseTimeout) {
            const strs = splitTime(responseTimeout);
            webSocketArgumentsParams.value.webSocketSetting.responseTimeout = strs[0];
            webSocketSettingResponseTimeoutUnit.value = strs[1];
          }

          if (reconnectionInterval) {
            const strs = splitTime(reconnectionInterval);
            webSocketArgumentsParams.value.webSocketSetting.reconnectionInterval = strs[0];
            reconnectionIntervalUnit.value = strs[1];
          }

          webSocketArgumentsParams.value.webSocketSetting.maxReconnections = maxReconnections || '';

          // 保存setting的扩展属性 保存需要带回去
          pluginSettingExtends.value = settingOthers;
        }

        if (others) {
          extendParams.value.extend = [];
          const _keys = Object.keys(others);
          if (_keys?.length) {
            _keys.forEach(key => {
              extendParams.value.extend.push({
                key: key,
                value: others[key]
              });
            });
          } else {
            extendParams.value.extend = [{ key: '', value: '' }];
          }
        }
      }
    }
  }

  if (plugin === 'Jdbc') {
    if (task) {
      const { arguments: ar } = task;
      if (ar) {
        const { jdbcSetting, ignoreAssertions, updateTestResult, ...others } = ar;
        jdbcArgumentsParams.value.ignoreAssertions = getIgnoreAssertions(ignoreAssertions);
        jdbcArgumentsParams.value.updateTestResult = !!updateTestResult;

        if (jdbcSetting) {
          const { type, driverClassName, isolation, jdbcUrl, username, password, pool, ...settingOthers } = jdbcSetting;

          jdbcArgumentsParams.value.jdbcSetting.type = type;
          jdbcArgumentsParams.value.jdbcSetting.driverClassName = driverClassName || '';
          jdbcArgumentsParams.value.jdbcSetting.jdbcUrl = jdbcUrl || '';
          jdbcArgumentsParams.value.jdbcSetting.isolation = isolation;
          jdbcArgumentsParams.value.jdbcSetting.username = username || '';
          jdbcArgumentsParams.value.jdbcSetting.password = password || '';
          if (pool) {
            isOpenPool.value = true;
            const { name, maximumPoolSize, minimumIdle, maxWaitTimeoutMillis } = pool;
            jdbcArgumentsParams.value.pool.name = name;
            jdbcArgumentsParams.value.pool.maximumPoolSize = maximumPoolSize || '';
            jdbcArgumentsParams.value.pool.maxWaitTimeoutMillis = maxWaitTimeoutMillis || '';
            jdbcArgumentsParams.value.pool.minimumIdle = minimumIdle || '';
          } else {
            isOpenPool.value = false;
          }
          // 保存setting的扩展属性 保存需要带回去
          pluginSettingExtends.value = settingOthers;
        }

        if (others) {
          extendParams.value.extend = [];
          const _keys = Object.keys(others);
          if (_keys?.length) {
            _keys.forEach(key => {
              extendParams.value.extend.push({
                key: key,
                value: others[key]
              });
            });
          } else {
            extendParams.value.extend = [{ key: '', value: '' }];
          }
        }
      }
    }
  }
};

const getIgnoreAssertions = (ignoreAssertions) => {
  return typeof ignoreAssertions === 'boolean' ? ignoreAssertions : props.scriptType !== 'TEST_FUNCTIONALITY';
};

watch(() => props.scriptType, () => {
  resetParam();
  initParams(true);
});

const executFormRef = ref();
const executParams = ref({
  iterations: '', // 迭代次数
  duration: '', // 执行时长
  thread: {
    threads: '1', // 并发线程数 (用户)
    rampUpInterval: '', // 增压间隔
    rampUpThreads: '', // 增压线程数
    rampDownInterval: '', // 减压间隔
    rampDownThreads: '', // 减压线程数
    resetAfterRamp: [] // 调整线程数后是否清除之前采样数据重新度量采样，只有开启了增压或者减压才有
  }
});

const threadsBlur = (e) => {
  executParams.value.thread.threads = e.target.value;
  if (+executParams.value.thread.rampUpThreads > e.target.value) {
    executParams.value.thread.rampUpThreads = '';
  }
  if (+executParams.value.thread.rampUpThreads > e.target.value) {
    executParams.value.thread.rampUpThreads = '';
  }
  setExecParamErrNum(['thread', 'threads']);
};

const iterationsBlur = (e) => {
  executParams.value.iterations = e.target.value;
  setExecParamErrNum('iterations');
};

const rampUpIntervalBlur = (e) => {
  executParams.value.thread.rampUpInterval = e.target.value;
  setExecParamErrNum(['thread', 'rampUpInterval']);
};
const rampUpThreadsBlur = (e) => {
  executParams.value.thread.rampUpThreads = e.target.value;
  setExecParamErrNum(['thread', 'rampUpThreads']);
};
const rampDownIntervalBlur = (e) => {
  executParams.value.thread.rampDownInterval = e.target.value;
  setExecParamErrNum(['thread', 'rampDownInterval']);
};
const rampDownThreadsBlur = (e) => {
  executParams.value.thread.rampDownThreads = e.target.value;
  setExecParamErrNum(['thread', 'rampDownThreads']);
};

const durationUnit = ref('s');
const durationBlur = (value) => {
  const strs = splitTime(value);
  executParams.value.duration = strs[0];
  if (+executParams.value.thread.rampUpInterval > +strs[0]) {
    executParams.value.thread.rampUpInterval = '';
  }
  if (+executParams.value.thread.rampDownInterval > +strs[0]) {
    executParams.value.thread.rampDownInterval = '';
  }
  setExecParamErrNum('duration');
};

const durationChange = (value) => {
  const strs = splitTime(value);
  durationUnit.value = strs[1];
};

const timeUnitMessage = computed(() => {
  switch (durationUnit.value) {
    case 'ms':
      return '毫秒';
    case 's':
      return '秒';
    case 'min':
      return '分钟';
    case 'h':
      return '小时';
    case 'd':
      return '天';
    default:
      return '秒';
  }
});

const supercharged = ref(false); // 开始 默认直压
const stressReliever = ref(false); // 结束 默认直压

const superchargedChange = (value) => {
  supercharged.value = value;
  if (!supercharged.value) {
    executParams.value.thread.rampUpInterval = '';
    executParams.value.thread.rampUpThreads = '';
  }
};

const stressRelieverChange = (value) => {
  stressReliever.value = value;
  if (!stressReliever.value) {
    executParams.value.thread.rampDownInterval = '';
    executParams.value.thread.rampDownThreads = '';
  }
};

const globalFormRef = ref();
const globalParams = ref({
  startMode: '',
  startAtDate: '', // 定时执行时间，当 startMode=TIMING时必须
  startupTimeout: '', // 代理启动Runner程序的超时时间，默认为1分钟（1s~7200s）
  runnerSetupTimeout: '', // Runner程序初始化采样超时时间，默认为1分钟（1s~7200s）
  priority: '',
  nodeSelectors: { // 节点选择
    num: '', // 执行任务所需节点数
    enabled: undefined, // 选择策略
    availableNodeIds: undefined, // 可用指定节点
    appNodeIds: undefined, // 应用节点
    maxTaskNum: '', // 执行节点允许的最大任务数，超过时忽略选择，最大1000
    lastExecuted: undefined, // 选择最后一次执行当前脚本的节点，第一次执行时忽略匹配
    // 按照规格选择节点
    specEnabled: false, // 开启规格选择
    cpuSpec: '', // 最小CPU规格，最大 64
    memorySpec: '', // 最小内存规格，最大 512GB
    diskSpec: '', // 最小磁盘规格，最大 2000GB
    // 按照使用率选择节点
    idleRateEnabled: false, // 开启空闲率选择
    cpuIdleRate: '', // CPU空闲率，最大100%
    memoryIdleRate: '', // 内存空闲率，最大100%
    diskIdleRate: '' // 磁盘空闲率，最大100%
  },
  onError: {
    action: '', // 处理方式（枚举ActionWhenError）：CONTINUE - 继续执行, STOP - 停止 , STOP_NOW - 立即停止
    sampleError: true, // 是否采样错误内容，开启会将错误发送到服务端，默认开启
    sampleErrorNum: '20' // 采样错误内容数，最大200，默认20
  },
  reportInterval: '', // 报告采样结果的间隔。支持范围：1-300秒，默认为5秒
  lang: undefined,
  shutdownTimeout: '' // 停止执行超时时间，如果超过指定超时时间采样未结束，会强制Kill掉执行，可能导致采样错误
});

const startModeChange = (value) => {
  globalParams.value.startMode = value;
  if (value === 'IMMEDIATELY') {
    globalFormRef.value.clearValidate('startAtDate');
  }
};

const shutdownTimeoutUnit = ref('s');
const shutdownTimeoutChange = (value) => {
  const strs = splitTime(value);
  globalParams.value.shutdownTimeout = strs[0];
  shutdownTimeoutUnit.value = strs[1];
};

const reportIntervalUnit = ref('s');
const reportIntervalChange = (value) => {
  const strs = splitTime(value);
  globalParams.value.reportInterval = strs[0];
  reportIntervalUnit.value = strs[1];
};

const startupTimeoutUnit = ref('s');
const startupTimeoutChange = (value) => {
  const strs = splitTime(value);
  globalParams.value.startupTimeout = strs[0];
  startupTimeoutUnit.value = strs[1];
};

const runnerSetupTimeoutUnit = ref('s');
const runnerSetupTimeoutChange = (value) => {
  const strs = splitTime(value);
  globalParams.value.runnerSetupTimeout = strs[0];
  runnerSetupTimeoutUnit.value = strs[1];
};

const httpParamsFormRef = ref();
const httpArgumentsParams = ref({
  ignoreAssertions: true,
  updateTestResult: false,
  httpSetting: {
    connectTimeout: '', // 连接超时时间。指定客户端和服务器建立连接的最长等待时间，默认为3秒
    readTimeout: '', // 读取超时。指定客户端未收到关闭连接的服务器响应的时间，默认为60秒
    retryNum: '', // 请求失败时的重试次数。默认情况下不重试，最多允许6次
    maxRedirects: '' // 请求返回3xx状态时的重定向次数。默认1次，最多允许10次
  }
});

const httpSettingConnectTimeoutUnit = ref('s');
const httpSettingReadTimeoutUnit = ref('s');
const httpConnectTimeoutChange = (value) => {
  const strs = splitTime(value);
  httpArgumentsParams.value.httpSetting.connectTimeout = strs[0];
  httpSettingConnectTimeoutUnit.value = strs[1];
};
const httpReadTimeoutChange = (value) => {
  const strs = splitTime(value);
  httpArgumentsParams.value.httpSetting.readTimeout = strs[0];
  httpSettingReadTimeoutUnit.value = strs[1];
};

const extendParams = ref({
  extend: [{ key: '', value: '' }]
});

const webSocketParamsFormRef = ref();
const webSocketArgumentsParams = ref({
  ignoreAssertions: true,
  updateTestResult: false,
  webSocketSetting: {
    connectTimeout: '', // 连接超时。指定客户端和服务器建立连接的最长等待时间，默认为3秒
    responseTimeout: '', //  响应超时。指定客户端未收到响应消息最大等待时间，默认为60秒
    maxReconnections: '', // 连接中断重连次数，最大100次
    reconnectionInterval: '' // 连接中断后重练间隔。请求失败时的重试间隔，默认为200毫秒，允许的最大时间为30分钟
  }
});
const webSocketSettingConnectTimeoutUnit = ref('s');
const webSocketSettingResponseTimeoutUnit = ref('s');
const reconnectionIntervalUnit = ref('ms');

const webSocletConnectTimeoutChange = (value) => {
  const strs = splitTime(value);
  webSocketArgumentsParams.value.webSocketSetting.connectTimeout = strs[0];
  webSocketSettingConnectTimeoutUnit.value = strs[1];
};
const webSocletResponseTimeoutChange = (value) => {
  const strs = splitTime(value);
  webSocketArgumentsParams.value.webSocketSetting.responseTimeout = strs[0];
  webSocketSettingResponseTimeoutUnit.value = strs[1];
};
const webSocletReconnectionIntervalChange = (value) => {
  const strs = splitTime(value);
  webSocketArgumentsParams.value.webSocketSetting.reconnectionInterval = strs[0];
  reconnectionIntervalUnit.value = strs[1];
};

const jdbcParamsFormRef = ref();
const jdbcArgumentsParams = ref({
  ignoreAssertions: true,
  updateTestResult: false,
  jdbcSetting: {
    type: undefined,
    driverClassName: '',
    jdbcUrl: '',
    username: '',
    password: '',
    isolation: undefined
  },
  pool: {
    name: undefined,
    maximumPoolSize: '',
    maxWaitTimeoutMillis: '',
    minimumIdle: ''
  }
});

const maxWaitTimeoutMillisUnit = ref('ms');
const jdbcMaxWaitTimeoutMillisChange = (value) => {
  const strs = splitTime(value);
  jdbcArgumentsParams.value.pool.maxWaitTimeoutMillis = strs[0];
  maxWaitTimeoutMillisUnit.value = strs[1];
};

const keyValidator = async (_rule: Rule, value: string) => {
  if (!value) {
    return Promise.reject(new Error(t('xcan_exec.enterParameterName')));
  } else {
    if (!extendParams.value) {
      return;
    }
    const keyNames = new Set();
    const hasDuplicates = extendParams.value.extend.some(obj => {
      if (keyNames.has(obj.key)) {
        return true;
      }
      keyNames.add(obj.key);
      return false;
    });
    if (hasDuplicates) {
      return Promise.reject(new Error(t('xcan_exec.parameterNameExists')));
    } else {
      extendParamsRef.value.clearValidate();
      return Promise.resolve();
    }
  }
};

const isOpenPool = ref(false);

const extendParamsRef = ref();
const iterationsValidator = async (_rule: Rule, value: string) => {
  if (!value) {
    if (['MOCK_DATA'].includes(props.scriptType)) {
      return Promise.resolve();
    }

    if (!executParams.value.duration) {
      return Promise.reject(new Error(t('xcan_exec.enterIterationsOrDuration')));
    } else {
      return Promise.resolve();
    }
  } else {
    executFormRef.value.clearValidate('duration');
    if (['TEST_FUNCTIONALITY'].includes(props.scriptType)) {
      if (+value < 1 || +value > 200) {
        return Promise.reject(new Error(t('xcan_exec.functionalTestIterationsRange')));
      }
      return Promise.resolve();
    }

    if (props.addType === 'expr') {
      if (+value < 1 || +value > 100000) {
        return Promise.reject(new Error(t('xcan_exec.experienceTestIterationsRange')));
      }
    } else {
      if (+value < 1 || +value > 100000000000) {
        return Promise.reject(new Error(t('xcan_exec.iterationsRange')));
      }
    }

    return Promise.resolve();
  }
};

const threadsValidator = async (_rule: Rule, value: string) => {
  if (!value) {
    return Promise.reject(new Error(t('xcan_exec.pleaseEnterThreadCount')));
  } else {
    if (props.scriptType === 'TEST_FUNCTIONALITY') {
      return Promise.resolve();
    }

    if (['MOCK_DATA', 'MOCK_APIS'].includes(props.scriptType)) {
      if (+value > 1000 || +value < 1) {
        return Promise.reject(new Error(t('xcan_exec.mockDataTaskThreadRange')));
      }

      return Promise.resolve();
    }

    if (+value > 10000 || +value < 1) {
      return Promise.reject(new Error(t('xcan_exec.taskThreadRange')));
    }

    return Promise.resolve();
  }
};

const threadsMax = computed(() => {
  if (props.scriptType === 'TEST_FUNCTIONALITY') {
    return 1;
  }

  if (['MOCK_DATA', 'MOCK_APIS'].includes(props.scriptType)) {
    return 1000;
  }

  return 10000;
});

const durationValidator = async (_rule: Rule, value: string) => {
  if (!value) {
    if (!executParams.value.iterations) {
      return Promise.reject(new Error(t('xcan_exec.enterIterationsOrDuration')));
    } else {
      return Promise.resolve();
    }
  } else {
    if (props.addType === 'expr') {
      if (durationUnit.value === 'min' && +value > 5) {
        return Promise.reject(new Error(t('xcan_exec.experienceExecutionMaxDuration')));
      }
      if (durationUnit.value === 's' && +value > 300) {
        return Promise.reject(new Error(t('xcan_exec.experienceExecutionMaxDuration')));
      }
    }
    executFormRef.value.clearValidate('iterations');
    return Promise.resolve();
  }
};

const delHttpApisSecurityItem = (index: number) => {
  if (!extendParams.value) {
    return;
  }
  if (extendParams.value.extend.length === 1) {
    extendParams.value.extend = [{ key: '', value: '' }];
    return;
  }
  extendParams.value.extend.splice(index, 1);
};

const addApisSecurityItem = () => {
  if (!extendParams.value || extendParams.value.extend.length === 20) {
    return;
  }
  extendParamsRef.value.validate().then(
    () => {
      extendParams.value.extend.push({ key: '', value: '' });
    }
  ).catch();
};

const isValid = async () => {
  let valid = true;
  const errors = {
    'execut-form': {},
    'global-form': {},
    'http-form': {},
    'websocket-form': {},
    'jdbc-form': {}
  };
  await executFormRef.value.validate().then(
    () => {
      for (const key in execParamErr.value) {
        execParamErr.value[key] = false;
      }
    }
  ).catch(
    (error) => {
      const names = error.errorFields?.map(item => item.name);
      for (let i = 0; i < names.length; i++) {
        const errKey = Array.isArray(names[i]) ? names[i].join('') : names[i];
        execParamErr.value[errKey] = true;
      }
      errors['execut-form'] = error;
      valid = false;
    });
  await globalFormRef.value.validate().then(
    () => {
      for (const key in globalParamErr.value) {
        globalParamErr.value[key] = false;
      }
    }
  ).catch(
    (error) => {
      const names = error.errorFields?.map(item => item.name);
      for (let i = 0; i < names.length; i++) {
        const errKey = Array.isArray(names[i]) ? names[i].join('') : names[i];
        globalParamErr.value[errKey] = true;
      }
      errors['global-form'] = error;
      valid = false;
    });
  if (pluginType.value === 'Http') {
    await httpParamsFormRef.value.validate().then().catch(
      (error) => {
        errors['http-form'] = error;
        valid = false;
      });
  }
  if (pluginType.value === 'WebSocket') {
    await webSocketParamsFormRef.value.validate().then().catch(
      (error) => {
        errors['websocket-form'] = error;
        valid = false;
      });
  }
  if (pluginType.value === 'Jdbc') {
    await jdbcParamsFormRef.value.validate(
    ).then(
      () => {
        for (const key in jdbcParamErr.value) {
          jdbcParamErr.value[key] = false;
        }
      }
    ).catch(
      (error) => {
        const names = error.errorFields?.map(item => item.name);
        for (let i = 0; i < names.length; i++) {
          const errKey = Array.isArray(names[i]) ? names[i].join('') : names[i];
          jdbcParamErr.value[errKey] = true;
        }
        errors['jdbc-form'] = error;
        valid = false;
      });
  }

  return { valid, errors };
};

const getData = () => {
  const _thread: any = {
    threads: executParams.value.thread.threads
  };

  if (props.scriptType !== 'TEST_FUNCTIONALITY') {
    if (+executParams.value.thread.rampUpInterval > 0 && executParams.value.thread.rampUpThreads) {
      _thread.rampUpInterval = executParams.value.thread.rampUpInterval + durationUnit.value;
      _thread.rampUpThreads = executParams.value.thread.rampUpThreads;
    }
    if (+executParams.value.thread.rampDownInterval > 0 && executParams.value.thread.rampDownThreads) {
      _thread.rampDownInterval = executParams.value.thread.rampDownInterval + durationUnit.value;
      _thread.rampDownThreads = executParams.value.thread.rampDownThreads;
    }

    if ((_thread?.rampUpThreads || _thread?.rampDownThreads) && executParams.value.thread.resetAfterRamp?.length) {
      _thread.resetAfterRamp = executParams.value.thread.resetAfterRamp[0];
    }
  }

  const params: any = {
    configuration: {
      ...configurationtends.value,
      thread: _thread
    }
  };

  if (+executParams.value.duration > 0 && props.scriptType !== 'TEST_FUNCTIONALITY' && props.scriptType !== 'MOCK_DATA') {
    params.configuration.duration = executParams.value.duration + durationUnit.value;
  }

  if (executParams.value.iterations) {
    params.configuration.iterations = executParams.value.iterations;
  }

  if (globalParams.value.nodeSelectors.num) {
    params.configuration.lang = globalParams.value.lang;
  }

  if (globalParams.value.startMode) {
    params.configuration.startMode = globalParams.value.startMode;

    if (globalParams.value.startMode === 'TIMING') {
      params.configuration.startAtDate = globalParams.value.startAtDate;
    }
  }

  if (globalParams.value.lang) {
    params.configuration.lang = globalParams.value.lang;
  }

  if (globalParams.value.priority) {
    params.configuration.priority = globalParams.value.priority;
  }

  if (+globalParams.value.reportInterval > 0) {
    params.configuration.reportInterval = globalParams.value.reportInterval + reportIntervalUnit.value;
  }
  if (+globalParams.value.shutdownTimeout > 0) {
    params.configuration.shutdownTimeout = globalParams.value.shutdownTimeout + shutdownTimeoutUnit.value;
  }
  if (+globalParams.value.startupTimeout > 0) {
    params.configuration.startupTimeout = globalParams.value.startupTimeout + startupTimeoutUnit.value;
  }
  if (+globalParams.value.runnerSetupTimeout > 0) {
    params.configuration.runnerSetupTimeout = globalParams.value.runnerSetupTimeout + runnerSetupTimeoutUnit.value;
  }

  if (globalParams.value.nodeSelectors.num) {
    if (params.configuration.nodeSelectors) {
      params.configuration.nodeSelectors.num = globalParams.value.nodeSelectors.num;
    } else {
      params.configuration.nodeSelectors = {
        num: globalParams.value.nodeSelectors.num
      };
    }
  }

  if (globalParams.value.nodeSelectors.availableNodeIds?.length) {
    if (params.configuration.nodeSelectors) {
      params.configuration.nodeSelectors.availableNodeIds = globalParams.value.nodeSelectors.availableNodeIds;
    } else {
      params.configuration.nodeSelectors = {
        availableNodeIds: globalParams.value.nodeSelectors.availableNodeIds
      };
    }
  }

  if (globalParams.value.nodeSelectors.appNodeIds?.length) {
    if (params.configuration.nodeSelectors) {
      params.configuration.nodeSelectors.appNodeIds = globalParams.value.nodeSelectors.appNodeIds;
    } else {
      params.configuration.nodeSelectors = {
        appNodeIds: globalParams.value.nodeSelectors.appNodeIds
      };
    }
  }

  const _enabled = globalParams.value.nodeSelectors.enabled;
  let _strategy: any = null;
  if (_enabled) {
    _strategy = {
      idleRateEnabled: globalParams.value.nodeSelectors.idleRateEnabled,
      lastExecuted: globalParams.value.nodeSelectors.lastExecuted,
      specEnabled: globalParams.value.nodeSelectors.specEnabled
    };

    if (globalParams.value.nodeSelectors.maxTaskNum) {
      _strategy.maxTaskNum = globalParams.value.nodeSelectors.maxTaskNum;
    }

    if (_strategy.idleRateEnabled) {
      if (globalParams.value.nodeSelectors.cpuIdleRate) {
        _strategy.cpuIdleRate = globalParams.value.nodeSelectors.cpuIdleRate;
      }
      if (globalParams.value.nodeSelectors.diskIdleRate) {
        _strategy.diskIdleRate = globalParams.value.nodeSelectors.diskIdleRate;
      }
      if (globalParams.value.nodeSelectors.memoryIdleRate) {
        _strategy.memoryIdleRate = globalParams.value.nodeSelectors.memoryIdleRate;
      }
    }

    if (_strategy.specEnabled) {
      if (globalParams.value.nodeSelectors.cpuSpec) {
        _strategy.cpuSpec = globalParams.value.nodeSelectors.cpuSpec;
      }
      if (globalParams.value.nodeSelectors.diskSpec) {
        _strategy.diskSpec = globalParams.value.nodeSelectors.diskSpec;
      }
      if (globalParams.value.nodeSelectors.memorySpec) {
        _strategy.memorySpec = globalParams.value.nodeSelectors.memorySpec;
      }
    }
  }

  if (_strategy) {
    if (params.configuration.nodeSelectors) {
      params.configuration.nodeSelectors.strategy = _strategy;
    } else {
      params.configuration.nodeSelectors = {
        strategy: _strategy
      };
    }
  }

  let _onError: any = {};
  if (globalParams.value.onError.action) {
    _onError = {
      action: globalParams.value.onError.action
    };
  }

  if (globalParams.value.onError.sampleError) {
    _onError.sampleError = globalParams.value.onError.sampleError;
    _onError.sampleErrorNum = globalParams.value.onError.sampleErrorNum;
  } else {
    _onError.sampleError = globalParams.value.onError.sampleError;
  }

  if (Object.keys(_onError).length) {
    params.configuration.onError = _onError;
  }

  const _arguments: any = {};
  if (pluginType.value === 'Http') {
    const _setting: any = { ...pluginSettingExtends.value };
    if (extendParams.value.extend?.length > 0) {
      extendParams.value.extend.forEach(item => {
        if (item.key && item.value) {
          _arguments[item.key] = item.value;
        }
      });
    }

    _arguments.ignoreAssertions = httpArgumentsParams.value.ignoreAssertions;
    _arguments.updateTestResult = httpArgumentsParams.value.updateTestResult;

    if (+httpArgumentsParams.value.httpSetting.connectTimeout > 0) {
      _setting.connectTimeout = httpArgumentsParams.value.httpSetting.connectTimeout + httpSettingConnectTimeoutUnit.value;
    }
    if (+httpArgumentsParams.value.httpSetting.readTimeout > 0) {
      _setting.readTimeout = httpArgumentsParams.value.httpSetting.readTimeout + httpSettingReadTimeoutUnit.value;
    }
    if (httpArgumentsParams.value.httpSetting.retryNum) {
      _setting.retryNum = httpArgumentsParams.value.httpSetting.retryNum;
    }
    if (httpArgumentsParams.value.httpSetting.maxRedirects) {
      _setting.maxRedirects = httpArgumentsParams.value.httpSetting.maxRedirects;
    }

    if (Object.keys(_setting)?.length) {
      _arguments.httpSetting = _setting;
    }
  }

  if (pluginType.value === 'WebSocket') {
    const _setting: any = { ...pluginSettingExtends.value };
    if (extendParams.value.extend?.length > 0) {
      extendParams.value.extend.forEach(item => {
        if (item.key && item.value) {
          _arguments[item.key] = item.value;
        }
      });
    }

    _arguments.ignoreAssertions = webSocketArgumentsParams.value.ignoreAssertions;
    _arguments.updateTestResult = webSocketArgumentsParams.value.updateTestResult;

    if (+webSocketArgumentsParams.value.webSocketSetting.maxReconnections) {
      _setting.maxReconnections = webSocketArgumentsParams.value.webSocketSetting.maxReconnections;
    }

    if (+webSocketArgumentsParams.value.webSocketSetting.connectTimeout > 0) {
      _setting.connectTimeout = webSocketArgumentsParams.value.webSocketSetting.connectTimeout + webSocketSettingConnectTimeoutUnit.value;
    }
    if (+webSocketArgumentsParams.value.webSocketSetting.responseTimeout > 0) {
      _setting.responseTimeout = webSocketArgumentsParams.value.webSocketSetting.responseTimeout + webSocketSettingResponseTimeoutUnit.value;
    }
    if (+webSocketArgumentsParams.value.webSocketSetting.reconnectionInterval > 0) {
      _setting.reconnectionInterval = webSocketArgumentsParams.value.webSocketSetting.reconnectionInterval + reconnectionIntervalUnit.value;
    }

    if (Object.keys(_setting)?.length) {
      _arguments.webSocketSetting = _setting;
    }
  }

  if (pluginType.value === 'Jdbc') {
    if (extendParams.value.extend?.length > 0) {
      extendParams.value.extend.forEach(item => {
        if (item.key && item.value) {
          _arguments[item.key] = item.value;
        }
      });
    }

    const { ignoreAssertions, updateTestResult, jdbcSetting, pool } = jdbcArgumentsParams.value;
    const { type, driverClassName, jdbcUrl, username, password, isolation } = jdbcSetting;
    const { name, maximumPoolSize, minimumIdle, maxWaitTimeoutMillis } = pool;
    _arguments.ignoreAssertions = ignoreAssertions;
    _arguments.updateTestResult = updateTestResult;
    _arguments.jdbcSetting = {
      ...pluginSettingExtends.value,
      type: type,
      driverClassName: driverClassName,
      jdbcUrl: jdbcUrl,
      username: username,
      pool: {
        name: name,
        maximumPoolSize: maximumPoolSize,
        minimumIdle: minimumIdle
      }
    };

    if (password) {
      _arguments.jdbcSetting.password = password;
    }

    if (isolation) {
      _arguments.jdbcSetting.isolation = isolation;
    }

    if (maxWaitTimeoutMillis) {
      _arguments.jdbcSetting.pool.maxWaitTimeoutMillis = maxWaitTimeoutMillis;
    }
  }

  if (props.scriptType !== 'MOCK_DATA' && Object.keys(_arguments)?.length) {
    params.arguments = _arguments;
  }

  const _variables = variablesRef.value?.getData?.();
  if (_variables) {
    params.updateVariableByIteration = _variables.updateVariableByIteration;
    params.configuration.variables = _variables.variables;
  }

  return params;
};

const nodeParamsOpen = ref(true);
const openNodeParams = () => {
  nodeParamsOpen.value = !nodeParamsOpen.value;
};

const errorParamsOpen = ref(true);
const openErrorParams = () => {
  errorParamsOpen.value = !errorParamsOpen.value;
};

const reporteParamsOpen = ref(true);
const openReportParams = () => {
  reporteParamsOpen.value = !reporteParamsOpen.value;
};

const runnerParamsOpen = ref(true);
const openRunnerParams = () => {
  runnerParamsOpen.value = !runnerParamsOpen.value;
};

const stopParamsOpen = ref(true);
const openStopParams = () => {
  stopParamsOpen.value = !stopParamsOpen.value;
};

const httpParamsOpen = ref(true);
const openHttpParams = () => {
  httpParamsOpen.value = !httpParamsOpen.value;
};

const httpExtendedParamsOpen = ref(true);
const openHttpExtendedParams = () => {
  httpExtendedParamsOpen.value = !httpExtendedParamsOpen.value;
};

const jdbcParamsOpen = ref(true);
const openJdbcParams = () => {
  jdbcParamsOpen.value = !jdbcParamsOpen.value;
};

const connectionPoolParamsOpen = ref(true);
const openConnectionPoolParams = () => {
  connectionPoolParamsOpen.value = !connectionPoolParamsOpen.value;
};

const selectProps = {
  style: { width: '80px' }
};

const durationSelectProps = {
  ...selectProps,
  excludes: ({ message }): boolean => {
    return props.addType === 'expr' ? ['ms', 'h', 'd'].includes(message) : ['ms', 'd'].includes(message);
  }
};

const durationMax = computed(() => props.addType === 'expr' ? 300 : 86400);

const reportIntervalSelectProps = {
  ...selectProps,
  excludes: ({ message }): boolean => {
    return ['ms', 'min', 'h', 'd'].includes(message);
  }
};

const startupTimeoutSelectProps = {
  ...selectProps,
  excludes: ({ message }): boolean => {
    return ['ms', 'd'].includes(message);
  }
};

const runnerSetupTimeoutSelectProps = {
  ...selectProps,
  excludes: ({ message }): boolean => {
    return ['ms', 'd'].includes(message);
  }
};

const shutdownTimeoutSelectProps = {
  ...selectProps,
  excludes: ({ message }): boolean => {
    return [].includes(message);
  }
};

const reconnectionIntervalSelectProps = {
  ...selectProps,
  excludes: ({ message }): boolean => {
    return ['h', 'd'].includes(message);
  }
};

const maxWaitTimeoutMillisSelectProps = {
  ...selectProps,
  excludes: ({ message }): boolean => {
    return ['s', 'min', 'h', 'd'].includes(message);
  }
};

let isFirstLoad = true;
watch(() => props.scriptInfo, () => {
  if (!isFirstLoad) {
    resetParam();
    isFirstLoad = false;
  }

  initParams(false);
}, {
  immediate: true,
  deep: props.isDeep
});

const resetParam = () => {
  executParams.value = {
    iterations: '',
    duration: '',
    thread: {
      threads: '',
      rampUpInterval: '',
      rampUpThreads: '',
      rampDownInterval: '',
      rampDownThreads: '',
      resetAfterRamp: null
    }
  };
  globalParams.value = {
    startMode: '',
    startAtDate: '',
    priority: '',
    nodeSelectors: {
      num: '',
      enabled: undefined,
      availableNodeIds: undefined,
      appNodeIds: undefined,
      maxTaskNum: '',
      lastExecuted: undefined,
      specEnabled: false,
      cpuSpec: '',
      memorySpec: '',
      diskSpec: '',
      idleRateEnabled: false,
      cpuIdleRate: '',
      memoryIdleRate: '',
      diskIdleRate: ''
    },
    onError: {
      action: '',
      sampleError: false,
      sampleErrorNum: ''
    },
    reportInterval: '',
    lang: undefined,
    shutdownTimeout: '',
    runnerSetupTimeout: '',
    startupTimeout: ''
  };
  httpArgumentsParams.value = {
    ignoreAssertions: undefined,
    updateTestResult: false,
    httpSetting: {
      connectTimeout: '',
      readTimeout: '',
      retryNum: '',
      maxRedirects: ''
    }
  };
  webSocketArgumentsParams.value = {
    ignoreAssertions: undefined,
    updateTestResult: false,
    webSocketSetting: {
      connectTimeout: '',
      responseTimeout: '',
      maxReconnections: '',
      reconnectionInterval: ''
    }
  };
  jdbcArgumentsParams.value = {
    ignoreAssertions: undefined,
    updateTestResult: false,
    jdbcSetting: {
      type: undefined,
      driverClassName: '',
      jdbcUrl: '',
      username: '',
      password: '',
      isolation: undefined
    },
    pool: {
      name: undefined,
      maximumPoolSize: '',
      maxWaitTimeoutMillis: '',
      minimumIdle: ''
    }
  };
  configVariables.value = [];
  durationUnit.value = 's';
  shutdownTimeoutUnit.value = 's';
  httpSettingConnectTimeoutUnit.value = 's';
  httpSettingReadTimeoutUnit.value = 's';
  webSocketSettingConnectTimeoutUnit.value = 's';
  webSocketSettingResponseTimeoutUnit.value = 's';
  maxWaitTimeoutMillisUnit.value = 'ms';
  reconnectionIntervalUnit.value = 'ms';
  supercharged.value = false;
  stressReliever.value = false;
};

watch(() => executParams.value, () => {
  setEchartsData();
}, {
  deep: true
});

const activeKey = ref<string[]>(['exec']);
const setActiveKey = (key: string) => {
  if (activeKey.value.includes(key)) {
    activeKey.value = activeKey.value.filter(item => item !== key);
  } else {
    activeKey.value.push(key);
  }
};

const openExecutParames = () => {
  if (!activeKey.value.includes('exec')) {
    setActiveKey('exec');
  }
};

const openGlobalParames = () => {
  if (!activeKey.value.includes('advanced')) {
    setActiveKey('advanced');
    nodeParamsOpen.value = true;
    errorParamsOpen.value = true;
    reporteParamsOpen.value = true;
    stopParamsOpen.value = true;
  }
};

const openPulginParames = () => {
  if (!activeKey.value.includes('plugin')) {
    setActiveKey('plugin');
    httpParamsOpen.value = true;
    jdbcParamsOpen.value = true;
    connectionPoolParamsOpen.value = true;
  }
};

const superchargedRef = ref();
const stressRelieverRef = ref();
let erd = elementResizeDetector({ strategy: 'scroll' });
const superchargedRefHeight = ref(20);
const stressRelieverRefHeight = ref(20);

watch(() => supercharged.value, (newValue) => {
  if (['MOCK_DATA', 'TEST_FUNCTIONALITY'].includes(props.scriptType)) {
    return;
  }
  setEchartsData();
  if (!newValue) {
    superchargedRefHeight.value = 20;
    return;
  }

  nextTick(() => {
    erd.listenTo(superchargedRef.value, () => {
      superchargedRefHeight.value = superchargedRef.value.clientHeight + 8;
    });
  });
}, {
  immediate: true
});

watch(() => stressReliever.value, (newValue) => {
  if (['MOCK_DATA', 'TEST_FUNCTIONALITY'].includes(props.scriptType)) {
    return;
  }
  setEchartsData();
  if (!newValue) {
    stressRelieverRefHeight.value = 20;
    return;
  }

  nextTick(() => {
    erd.listenTo(stressRelieverRef.value, () => {
      stressRelieverRefHeight.value = stressRelieverRef.value.clientHeight + 8;
    });
  });
}, {
  immediate: true
});

onBeforeUnmount(() => {
  if (superchargedRef.value) {
    erd.removeListener(superchargedRef.value, null);
  }

  erd = null;
});

onMounted(() => {
  setEchartsData();
});

const updateRsetAfterRamp = (_value: boolean[]) => {
  const value = _value.length === 1 ? _value[0] : _value[1];
  const _resetAfterRamp = executParams.value.thread.resetAfterRamp;
  if (_resetAfterRamp?.length) {
    if (_resetAfterRamp[0] === value) {
      executParams.value.thread.resetAfterRamp = [];
    } else {
      executParams.value.thread.resetAfterRamp = [value];
    }
  } else {
    executParams.value.thread.resetAfterRamp = [value];
  }
};

const execParamErr = ref({
  threadthreads: false,
  duration: false,
  iterations: false,
  threadrampUpInterval: false,
  threadrampUpThreads: false,
  threadrampDownInterval: false,
  threadrampDownThreads: false
});

const setExecParamErrNum = (name: string | string[]) => {
  const key = Array.isArray(name) ? name.join('') : name;
  executFormRef.value.validate([name]).then(() => {
    execParamErrNum.value = 0;
    execParamErr.value[key] = false;
  }).catch(() => {
    execParamErr.value[key] = true;
  });
};

const globalParamErr = ref({
  startAtDate: false
});

const setGlobalParamErrNum = (name: string | string[]) => {
  const key = Array.isArray(name) ? name.join('') : name;
  globalFormRef.value.validate().then(() => {
    globalParamErr.value[key] = false;
  }).catch(() => {
    globalParamErr.value[key] = true;
  });
};

const startAtDateChange = (value) => {
  globalParams.value.startAtDate = value;
  globalParamErr.value.startAtDate = false;
  setGlobalParamErrNum('startAtDate');
};

const jdbcParamErr = ref({
  jdbcSettingtype: false,
  jdbcSettingdriverClassName: false,
  jdbcSettingjdbcUrl: false,
  jdbcSettingusername: false,
  poolname: false,
  poolmaximumPoolSize: false,
  poolminimumIdle: false
});

const setPluginParamErrNum = (name: string | string[]) => {
  const key = Array.isArray(name) ? name.join('') : name;
  jdbcParamsFormRef.value.validate([name]).then(() => {
    jdbcParamErr.value[key] = false;
  }).catch(() => {
    jdbcParamErr.value[key] = true;
  });
};

const openPoolChange = (value) => {
  isOpenPool.value = value;
  if (!value) {
    jdbcParamsFormRef.value.clearValidate([['pool', 'name'], ['pool', 'maximumPoolSize'], ['pool', 'minimumIdle']]);
    jdbcParamErr.value.poolname = false;
    jdbcParamErr.value.poolmaximumPoolSize = false;
    jdbcParamErr.value.poolminimumIdle = false;
  }
};

const execParamErrNum = computed(() => {
  return Object.values(execParamErr.value).filter(value => value).length || 0;
});

const globalParamErrNum = computed(() => {
  return Object.values(globalParamErr.value).filter(value => value).length || 0;
});

const jdbcParamErrNum = computed(() => {
  return Object.values(jdbcParamErr.value).filter(value => value).length || 0;
});

const errTotal = computed(() => {
  return execParamErrNum.value + globalParamErrNum.value + jdbcParamErrNum.value;
});

defineExpose({ isValid, getData, openExecutParames, openGlobalParames, openPulginParames, errTotal });
</script>
<template>
  <Collapse
    v-model:activeKey="activeKey"
    class="!bg-transparent"
    :bordered="false">
    <CollapsePanel
      key="exec"
      :showArrow="false"
      forceRender>
      <template #header>
        <Badge size="small" :count="execParamErrNum">
          <div class="flex items-center text-3 text-text-title font-medium leading-5">
            <Icon icon="icon-zhihangcanshu" class="text-3.5 mr-2" />
            <span>{{ t('xcan_exec.executionParameters') }}</span>
          </div>
        </Badge>
      </template>
      <template #extra>
        <Arrow :open="activeKey.includes('exec')" @click="setActiveKey('exec')" />
      </template>
      <div class="flex text-3">
        <div class="flex flex-1 max-w-150 mr-5 pl-3.5" style="min-width:360px">
          <div class="mr-2.5 leading-7 w-35 flex-none execut-form">
            <div class="h-7 whitespace-nowrap mb-5 thread-threads">
              <IconRequired />{{ t('xcan_exec.concurrencyThreads') }}
            </div>
            <template v-if="!['MOCK_APIS'].includes(props.scriptType)">
              <div class="h-7 whitespace-nowrap mb-5 pl-1.75 iterations">{{ t('xcan_exec.iterations') }}</div>
            </template>
            <template v-if="!['MOCK_DATA', 'TEST_FUNCTIONALITY'].includes(props.scriptType)">
              <div class="h-7 whitespace-nowrap mb-5 pl-1.75 duration">{{ t('xcan_exec.executionDuration') }}</div>
              <div
                class="h-7 whitespace-nowrap thread-rampUpInterval thread-rampUpThreads pl-1.75"
                :style="{ 'margin-bottom': superchargedRefHeight + 'px' }">
                {{ t('xcan_exec.initialRampUp') }}
              </div>
              <div
                class="h-7 whitespace-nowrap thread-rampDownInterval thread-rampDownThreads pl-1.75"
                :style="{ 'margin-bottom': stressRelieverRefHeight + 'px' }">
                {{ t('xcan_exec.finalRampDown') }}
              </div>
              <template v-if="supercharged || stressReliever">
                <div class="h-7 whitespace-nowrap thread-rampDownInterval pl-1.75">
                  {{ t('xcan_exec.resetSamplingResults') }}
                </div>
              </template>
            </template>
          </div>
          <Form
            ref="executFormRef"
            class="flex-1"
            :model="executParams"
            :colon="false"
            layout="horizontal">
            <FormItem
              class="pr-5 relative"
              :name="['thread', 'threads']"
              :rules="{ required: true, validator: threadsValidator, trigger: ['change', 'blur'] }">
              <Input
                :value="executParams.thread.threads"
                dataType="number"
                :min="1"
                :max="threadsMax"
                :disabled="props.scriptType === 'TEST_FUNCTIONALITY'"
                :placeholder="t('xcan_exec.maxThreads')"
                @blur="threadsBlur" />
              <Tooltip
                :title="t('xcan_exec.maxThreadsTooltip')"
                placement="topLeft"
                arrowPointAtCenter
                :overlayStyle="{ 'max-width': '600px' }">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
              </Tooltip>
            </FormItem>
            <template v-if="!['MOCK_APIS'].includes(props.scriptType)">
              <FormItem
                class="pr-5 relative"
                name="iterations"
                :rules="{ required: false, validator: iterationsValidator, trigger: ['change', 'blur'] }">
                <Input
                  :value="executParams.iterations"
                  dataType="number"
                  :min="1"
                  :max="props.addType === 'expr' ? 1000000 : 100000000000"
                  size="small"
                  :placeholder="t('xcan_exec.iterations')"
                  @blur="iterationsBlur" />
                <Tooltip
                  :title="t('xcan_exec.iterationsTooltip')"
                  placement="topLeft"
                  arrowPointAtCenter
                  :overlayStyle="{ 'max-width': '600px' }">
                  <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
                </Tooltip>
              </FormItem>
            </template>
            <template v-if="!['MOCK_DATA', 'TEST_FUNCTIONALITY'].includes(props.scriptType)">
              <FormItem
                class="relative pr-5"
                name="duration"
                :rules="{ required: false, validator: durationValidator, trigger: ['change', 'blur'] }">
                <ShortDuration
                  :value="executParams.duration + durationUnit"
                  size="small"
                  class="w-full"
                  :max="durationMax"
                  :inputProps="{ placeholder: t('xcan_exec.executionDuration') }"
                  :selectProps="durationSelectProps"
                  @blur="durationBlur"
                  @change="durationChange" />
                <Tooltip
                  :title="t('xcan_exec.executionDurationTooltip')"
                  placement="topLeft"
                  arrowPointAtCenter
                  :overlayStyle="{ 'max-width': '600px' }">
                  <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
                </Tooltip>
              </FormItem>
              <FormItem class="h-7 leading-7">
                <RadioGroup
                  :value="supercharged"
                  class="space-x-6 -ml-5 mt-0.5"
                  :disabled="executParams.duration === '0'"
                  :options="[{ value: false, message: t('xcan_exec.directPressure') }, { value: true, message: t('xcan_exec.gradualPressure') }]"
                  @change="superchargedChange">
                </RadioGroup>
              </FormItem>
              <template v-if="supercharged">
                <div ref="superchargedRef" class="flex  flex-wrap -mt-3 supercharged">
                  <div class="flex text-3 space-x-1 mr-2">
                    <div class="h-7 leading-7">每隔</div>
                    <FormItem
                      class="flex-1"
                      :name="['thread', 'rampUpInterval']"
                      :rules="{ required: supercharged, message: t('xcan_exec.rampUpIntervalRule'), trigger: ['change', 'blur'] }">
                      <Input
                        v-model:value="executParams.thread.rampUpInterval"
                        class="w-24"
                        :min="0"
                        :max="+executParams.duration"
                        dataType="number"
                        :placeholder="t('xcan_exec.rampUpInterval')"
                        @blur="rampUpIntervalBlur" />
                    </FormItem>
                    <div class="h-7 leading-7">{{ timeUnitMessage }}</div>
                  </div>
                  <div class="flex text-3 space-x-1">
                    <div class="h-7 leading-7">增加</div>
                    <FormItem
                      class="flex-1"
                      :name="['thread', 'rampUpThreads']"
                      :rules="{ required: supercharged, message: t('xcan_exec.rampUpThreadsRule'), trigger: ['change', 'blur'] }">
                      <Input
                        :value="executParams.thread.rampUpThreads"
                        dataType="number"
                        class="w-24"
                        :min="0"
                        :max="+executParams.thread.threads"
                        size="small"
                        :placeholder="t('xcan_exec.rampUpThreads')"
                        @blur="rampUpThreadsBlur" />
                    </FormItem>
                    <div class="h-7 leading-7 flex items-center">
                      {{ t('xcan_exec.concurrency') }}
                      <Tooltip
                        placement="topLeft"
                        arrowPointAtCenter
                        :overlayStyle="{ 'max-width': '600px' }">
                        <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
                        <template #title>
                          <div class="flex">
                            <div class="mr-2">
                              <div>
                                {{ t('xcan_exec.rampUpInterval') }}
                                <Colon />
                              </div>
                              <div>
                                {{ t('xcan_exec.rampUpThreads') }}
                                <Colon />
                              </div>
                            </div>
                            <div>
                              <div>{{ t('xcan_exec.rampUpIntervalTooltip') }}</div>
                              <div>{{ t('xcan_exec.rampUpThreadsTooltip') }}</div>
                            </div>
                          </div>
                        </template>
                      </Tooltip>
                    </div>
                  </div>
                </div>
              </template>
              <FormItem class="h-7 leading-7">
                <RadioGroup
                  :value="stressReliever"
                  class="space-x-6 -ml-5 mt-0.5"
                  :disabled="executParams.duration === '0'"
                  :options="[{ value: false, message: t('xcan_exec.directReduction') }, { value: true, message: t('xcan_exec.gradualReduction') }]"
                  @change="stressRelieverChange">
                </RadioGroup>
              </FormItem>
              <template v-if="stressReliever">
                <div ref="stressRelieverRef" class="flex  flex-wrap -mt-3 stress-reliever">
                  <div class="flex text-3 space-x-1 mr-2">
                    <div class="h-7 leading-7">每隔</div>
                    <FormItem
                      class="flex-1"
                      :name="['thread', 'rampDownInterval']"
                      :rules="{ required: supercharged, message: t('xcan_exec.rampDownIntervalRule'), trigger: ['change', 'blur'] }">
                      <Input
                        :value="executParams.thread.rampDownInterval"
                        class="w-24"
                        :min="0"
                        :max="+executParams.duration"
                        dataType="number"
                        :placeholder="t('xcan_exec.rampDownInterval')"
                        @blur="rampDownIntervalBlur" />
                    </FormItem>
                    <div class="h-7 leading-7">{{ timeUnitMessage }}</div>
                  </div>
                  <div class="flex text-3 space-x-1">
                    <div class="h-7 leading-7">减少</div>
                    <FormItem
                      class="flex-1"
                      :name="['thread', 'rampDownThreads']"
                      :rules="{ required: stressReliever, message: t('xcan_exec.rampDownThreadsRule'), trigger: ['change', 'blur'] }">
                      <Input
                        :value="executParams.thread.rampDownThreads"
                        dataType="number"
                        class="w-24"
                        :min="0"
                        :max="+executParams.thread.threads"
                        size="small"
                        :placeholder="t('xcan_exec.rampDownThreads')"
                        @blur="rampDownThreadsBlur" />
                    </FormItem>
                    <div class="h-7 leading-7 flex items-center">
                      {{ t('xcan_exec.concurrency') }}
                      <Tooltip
                        placement="topLeft"
                        arrowPointAtCenter
                        :overlayStyle="{ 'max-width': '600px' }">
                        <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
                        <template #title>
                          <div class="flex">
                            <div class="mr-2">
                              <div>
                                {{ t('xcan_exec.rampDownInterval') }}
                                <Colon />
                              </div>
                              <div>
                                {{ t('xcan_exec.rampDownThreads') }}
                                <Colon />
                              </div>
                            </div>
                            <div>
                              <div>{{ t('xcan_exec.rampDownIntervalTooltip') }}</div>
                              <div>{{ t('xcan_exec.rampDownThreadsTooltip') }}</div>
                            </div>
                          </div>
                        </template>
                      </Tooltip>
                    </div>
                  </div>
                </div>
              </template>
              <template v-if="supercharged || stressReliever">
                <FormItem class="h-7 leading-7 pr-5 relative" :name="['thread', 'resetAfterRamp']">
                  <CheckboxGroup :value="executParams.thread.resetAfterRamp" @change="updateRsetAfterRamp($event)">
                    <Checkbox :value="true">{{ t('xcan_exec.yes') }}</Checkbox>
                    <Checkbox :value="false">{{ t('xcan_exec.no') }}</Checkbox>
                  </CheckboxGroup>
                  <Tooltip
                    :title="t('xcan_exec.resetAfterRampTooltip')"
                    placement="topLeft"
                    arrowPointAtCenter
                    :overlayStyle="{ 'max-width': '600px' }">
                    <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute left-26 top-2" />
                  </Tooltip>
                </FormItem>
              </template>
            </template>
          </Form>
        </div>
        <template
          v-if="!['MOCK_DATA', 'TEST_FUNCTIONALITY'].includes(props.scriptType) && (supercharged || stressReliever)">
          <LineChart
            v-if="+chartData.yData?.[1] > 0 && +chartData.yData?.[1] <= 10000"
            class="mt-12 flex-1"
            :unit="timeUnitMessage"
            :xData="chartData.xData"
            :yData="chartData.yData" />
        </template>
      </div>
    </CollapsePanel>
    <CollapsePanel
      key="advanced"
      :showArrow="false"
      forceRender>
      <template #header>
        <Badge size="small" :count="globalParamErrNum">
          <div class="flex items-center text-3 text-text-title font-medium leading-5">
            <Icon icon="icon-quanjupeizhi" class="text-3.5 mr-2" />
            <span>{{ t('xcan_exec.advancedParameters') }}</span>
          </div>
        </Badge>
      </template>
      <template #extra>
        <Arrow :open="activeKey.includes('advanced')" @click="setActiveKey('advanced')" />
      </template>
      <div class="flex max-w-150 pl-3.5 text-3">
        <div class="mr-2.5 leading-7 w-35 flex-none global-form">
          <div class="h-7 whitespace-nowrap mb-5 startMode pl-1.75">
            {{ t('xcan_exec.startMode') }}
          </div>
          <div v-if="globalParams.startMode === 'TIMING'" class="h-7 whitespace-nowrap mb-5 -mt-3 startAtDate"></div>
          <div class="h-7 whitespace-nowrap mb-5 priority pl-1.75">
            {{ t('xcan_exec.priority') }}
          </div>
          <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-1.75 font-medium ">
            {{ t('xcan_exec.executionNodes') }}
            <Arrow
              :open="nodeParamsOpen"
              class="ml-2"
              @click="openNodeParams" />
          </div>
          <div
            :class="nodeParamsOpen ? 'open-params' : 'stop-params'"
            class="overflow-hidden transition-all"
            style="transition-duration: 400ms;">
            <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 nodeSelectors-num">{{ t('xcan_exec.executionNodeCount') }}</div>
            <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 nodeSelectors-availableNodeIds">
              {{ t('xcan_exec.availableExecutionNodes') }}
            </div>
            <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 nodeSelectors-appNodeIds">{{ t('xcan_exec.applicationNodes') }}</div>
            <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 nodeSelectors-enabled">{{ t('xcan_exec.nodeSelectionStrategy') }}</div>
            <div
              :class="globalParams.nodeSelectors.enabled ? 'open-params' : 'stop-params'"
              class="overflow-hidden transition-all"
              style="transition-duration: 400ms;">
              <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 nodeSelectors-maxTaskNum">{{ t('xcan_exec.maxTasksPerNode') }}</div>
              <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 nodeSelectors-lastExecuted">{{ t('xcan_exec.lastExecutedNode') }}</div>
              <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25">{{ t('xcan_exec.minSpecificationNodes') }}</div>
              <div
                v-if="globalParams.nodeSelectors.specEnabled"
                class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 nodeSelectors-specEnabled">
                {{ t('xcan_exec.minSpecification') }}
              </div>
              <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25">{{ t('xcan_exec.minIdleRateNodes') }}</div>
              <div
                v-if="globalParams.nodeSelectors.idleRateEnabled"
                class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 nodeSelectors-idleRateEnabled">
                {{ t('xcan_exec.minIdleRate') }}
              </div>
            </div>
          </div>
          <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-1.75  font-medium">
            {{ t('xcan_exec.errorHandling') }}
            <Arrow
              :open="errorParamsOpen"
              class="ml-2"
              @click="openErrorParams" />
          </div>
          <div
            :class="errorParamsOpen ? 'open-params' : 'stop-params'"
            class="overflow-hidden transition-all"
            style="transition-duration: 400ms;">
            <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 onError-action">
              {{ t('xcan_exec.errorHandlingMethod') }}
            </div>
            <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 onError-sampleError">{{ t('xcan_exec.sampleErrorContent') }}</div>
            <div
              :class="globalParams.onError.sampleError ? 'open-params' : 'stop-params'"
              class="overflow-hidden transition-all"
              style="transition-duration: 400ms;">
              <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 onError-sampleErrorNum">{{ t('xcan_exec.sampleErrorContentCount') }}</div>
            </div>
          </div>
          <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-1.75  font-medium">
            {{ t('xcan_exec.reportConfiguration') }}
            <Arrow
              :open="reporteParamsOpen"
              class="ml-2"
              @click="openReportParams" />
          </div>
          <div
            :class="reporteParamsOpen ? 'open-params' : 'stop-params'"
            class="overflow-hidden transition-all"
            style="transition-duration: 400ms;">
            <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 reportInterval">{{ t('xcan_exec.samplingInterval') }}</div>
            <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 lang">{{ t('xcan_exec.language') }}</div>
          </div>
          <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-1.75  font-medium">
            {{ t('xcan_exec.startupConfiguration') }}
            <Arrow
              :open="runnerParamsOpen"
              class="ml-2"
              @click="openRunnerParams" />
          </div>
          <div
            :class="runnerParamsOpen ? 'open-params' : 'stop-params'"
            class="overflow-hidden transition-all"
            style="transition-duration: 400ms;">
            <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 reportInterval">{{ t('xcan_exec.executor') }}</div>
            <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 lang">{{ t('xcan_exec.samplingInitializationTimeout') }}</div>
          </div>
          <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-1.75  font-medium">
            {{ t('xcan_exec.stopConfiguration') }}
            <Arrow
              :open="stopParamsOpen"
              class="ml-2"
              @click="openStopParams" />
          </div>
          <div
            :class="stopParamsOpen ? 'open-params' : 'stop-params'"
            class="overflow-hidden transition-all"
            style="transition-duration: 400ms;">
            <div class="h-7 whitespace-nowrap flex items-center pl-5.25 shutdownTimeout">{{ t('xcan_exec.stopExecutionTimeout') }}</div>
          </div>
        </div>
        <Form
          ref="globalFormRef"
          class="flex-1"
          :model="globalParams"
          :colon="false"
          layout="horizontal">
          <FormItem class="pr-5 relative" name="startMode">
            <RadioGroup
              :value="globalParams.startMode"
              class="space-x-6 -ml-5 h-7 pt-1"
              enumKey="StartMode"
              @change="startModeChange">
            </RadioGroup>
            <Tooltip
              :title="t('xcan_exec.startModeTooltip')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{ 'max-width': '600px' }">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2.5" />
            </Tooltip>
          </FormItem>
          <FormItem
            v-if="globalParams.startMode === 'TIMING'"
            name="startAtDate"
            class="-mt-3 pr-5 relative"
            :rules="{ required: globalParams.startMode === 'TIMING', message: t('xcan_exec.pleaseEnterScheduledExecutionTime'), trigger: ['change', 'blur'] }">
            <DatePicker
              :value="globalParams.startAtDate"
              showTime
              class="w-full"
              :placeholder="t('xcan_exec.scheduledExecutionTime')"
              @change="startAtDateChange" />
            <Tooltip
              :title="t('xcan_exec.scheduledExecutionTimeTooltip')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{ 'max-width': '600px' }">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
            </Tooltip>
          </FormItem>
          <FormItem name="priority" class="pr-5 relative">
            <Input
              v-model:value="globalParams.priority"
              dataType="number"
              :min="1"
              :max="2147483647"
              :placeholder="t('xcan_exec.priority')" />
            <Tooltip
              :title="t('xcan_exec.priorityTooltip')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{ 'max-width': '600px' }">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
            </Tooltip>
          </FormItem>
          <div class="h-7 whitespace-nowrap mb-5"></div>
          <div
            :class="nodeParamsOpen ? 'open-params' : 'stop-params'"
            class="overflow-hidden transition-all"
            style="transition-duration: 400ms;">
            <FormItem class="pr-5 relative" :name="['nodeSelectors', 'num']">
              <Input
                v-model:value="globalParams.nodeSelectors.num"
                :min="1"
                :max="props.addType === 'expr' ? 1 : 200"
                :placeholder="t('xcan_exec.executionNodeCount')"
                dataType="number" />
              <Tooltip
                :title="t('xcan_exec.executionNodeCountTooltip')"
                placement="topLeft"
                arrowPointAtCenter
                :overlayStyle="{ 'max-width': '600px' }">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
              </Tooltip>
            </FormItem>
            <FormItem class="pr-5 relative" :name="['nodeSelectors', 'availableNodeIds']">
              <Select
                v-model:value="globalParams.nodeSelectors.availableNodeIds"
                :action="`${TESTER}/node?role=EXECUTION&enabled=true&fullTextSearch=true`"
                :fieldNames="{ label: 'name', value: 'id' }"
                mode="multiple"
                :placeholder="t('xcan_exec.availableExecutionNodes')" />
              <Tooltip
                :title="t('xcan_exec.availableExecutionNodesTooltip')"
                placement="topLeft"
                arrowPointAtCenter
                :overlayStyle="{ 'max-width': '600px' }">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
              </Tooltip>
            </FormItem>
            <FormItem class="pr-5 relative" :name="['nodeSelectors', 'appNodeIds']">
              <Select
                v-model:value="globalParams.nodeSelectors.appNodeIds"
                :action="`${TESTER}/node?role=APPLICATION&fullTextSearch=true`"
                :fieldNames="{ label: 'name', value: 'id' }"
                mode="multiple"
                :placeholder="t('xcan_exec.applicationNodes')" />
              <Tooltip
                :title="t('xcan_exec.applicationNodesTooltip')"
                placement="topLeft"
                arrowPointAtCenter
                :overlayStyle="{ 'max-width': '600px' }">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
              </Tooltip>
            </FormItem>
            <FormItem class="h-7 leading-7 pr-5" :name="['nodeSelectors', 'enabled']">
              <Switch
                v-model:checked="globalParams.nodeSelectors.enabled"
                size="small"
                class="w-8" />
              <Tooltip
                :title="t('xcan_exec.nodeSelectionStrategyTooltip')"
                placement="topLeft"
                arrowPointAtCenter
                :overlayStyle="{ 'max-width': '600px' }">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 " />
              </Tooltip>
            </FormItem>
            <div
              :class="globalParams.nodeSelectors.enabled ? 'open-params' : 'stop-params'"
              class="overflow-hidden transition-all"
              style="transition-duration: 400ms;">
              <FormItem class="pr-5 relative" :name="['nodeSelectors', 'maxTaskNum']">
                <Input
                  v-model:value="globalParams.nodeSelectors.maxTaskNum"
                  :min="0"
                  :max="1000"
                  placeholder="t('xcan_exec.maxTasksPerNode')" />
                <Tooltip
                  :title="t('xcan_exec.maxTasksPerNodeTooltip')"
                  placement="topLeft"
                  arrowPointAtCenter
                  :overlayStyle="{ 'max-width': '600px' }">
                  <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
                </Tooltip>
              </FormItem>
              <FormItem class="h-7 leading-7 pr-5 relative" :name="['nodeSelectors', 'lastExecuted']">
                <Switch
                  v-model:checked="globalParams.nodeSelectors.lastExecuted"
                  size="small"
                  class="w-8" />
                <Tooltip
                  :title="t('xcan_exec.lastExecutedNodeTooltip')"
                  placement="topLeft"
                  arrowPointAtCenter
                  :overlayStyle="{ 'max-width': '600px' }">
                  <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
                </Tooltip>
              </FormItem>
              <FormItem class="h-7 leading-7 relative pr-5" :name="['nodeSelectors', 'specEnabled']">
                <Switch
                  v-model:checked="globalParams.nodeSelectors.specEnabled"
                  size="small"
                  class="w-8" />
                <Tooltip
                  :title="t('xcan_exec.enableSpecSelectionTooltip')"
                  placement="topLeft"
                  arrowPointAtCenter
                  :overlayStyle="{ 'max-width': '600px' }">
                  <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
                </Tooltip>
              </FormItem>
              <div v-if="globalParams.nodeSelectors.specEnabled" class="flex pr-1">
                <FormItem label="CPU" class="h-7 leading-7" />
                <FormItem class="h-7" :name="['nodeSelectors', 'cpuSpec']">
                  <Input
                    v-model:value="globalParams.nodeSelectors.cpuSpec"
                    :min="1"
                    :max="16" />
                </FormItem>
                <FormItem label="GB" class="h-7 ml-2 mr-3 leading-7" />
                <FormItem :label="t('xcan_exec.memory')" class="h-7 leading-7" />
                <FormItem class="h-7" :name="['nodeSelectors', 'memorySpec']">
                  <Input
                    v-model:value="globalParams.nodeSelectors.memorySpec"
                    :min="1"
                    :max="512" />
                </FormItem>
                <FormItem label="GB" class="h-7  ml-2 mr-3 leading-7" />
                <FormItem :label="t('xcan_exec.disk')" class="h-7 leading-7" />
                <FormItem class="h-7" :name="['nodeSelectors', 'diskSpec']">
                  <Input
                    v-model:value="globalParams.nodeSelectors.diskSpec"
                    :min="1"
                    :max="2000" />
                </FormItem>
                <FormItem label="GB" class="h-7 ml-2 leading-7" />
                <Tooltip
                  placement="topLeft"
                  arrowPointAtCenter
                  :overlayStyle="{ 'max-width': '600px' }">
                  <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-2 mt-1.75 flex-none" />
                  <template #title>
                    <div>
                      <div>{{ t('xcan_exec.minCpuSpec') }}</div>
                      <div>{{ t('xcan_exec.minMemorySpec') }}</div>
                      <div>{{ t('xcan_exec.minDiskSpec') }}</div>
                    </div>
                  </template>
                </Tooltip>
              </div>
              <FormItem
                class="leading-7 relative pr-5"
                :name="['nodeSelectors', 'idleRateEnabled']"
                :class="globalParams.nodeSelectors.idleRateEnabled ? 'h-7' : 'h-12'">
                <Switch
                  v-model:checked="globalParams.nodeSelectors.idleRateEnabled"
                  size="small"
                  class="w-8" />
                <Tooltip
                  :title="t('xcan_exec.enableIdleRateSelectionTooltip')"
                  placement="topLeft"
                  arrowPointAtCenter
                  :overlayStyle="{ 'max-width': '600px' }">
                  <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
                </Tooltip>
              </FormItem>
              <div v-if="globalParams.nodeSelectors.idleRateEnabled" class="flex pr-1">
                <FormItem label="CPU" class="h-7 leading-7" />
                <FormItem class="h-7 leading-7" :name="['nodeSelectors', 'cpuIdleRate']">
                  <Input
                    v-model:value="globalParams.nodeSelectors.cpuIdleRate"
                    :min="1"
                    :max="100" />
                </FormItem>
                <FormItem label="%" class="h-7 ml-2 mr-5 leading-7" />
                <FormItem label="内存" class="h-7 leading-7" />
                <FormItem class="h-7 leading-7" :name="['nodeSelectors', 'memoryIdleRate']">
                  <Input
                    v-model:value="globalParams.nodeSelectors.memoryIdleRate"
                    :min="1"
                    :max="100" />
                </FormItem>
                <FormItem label="%" class="h-7  ml-2 mr-5 leading-7" />
                <FormItem label="磁盘" class="h-7 leading-7" />
                <FormItem class="h-7 leading-7" :name="['nodeSelectors', 'diskIdleRate']">
                  <Input
                    v-model:value="globalParams.nodeSelectors.diskIdleRate"
                    :min="1"
                    :max="100" />
                </FormItem>
                <FormItem label="%" class="h-7 ml-2 leading-7" />
                <Tooltip
                  placement="topLeft"
                  arrowPointAtCenter
                  :overlayStyle="{ 'max-width': '600px' }">
                  <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-2 mt-1.75 flex-none" />
                  <template #title>
                    <div>
                      <div>{{ t('xcan_exec.cpuIdleRate') }}</div>
                      <div>{{ t('xcan_exec.memoryIdleRate') }}</div>
                      <div>{{ t('xcan_exec.diskIdleRate') }}</div>
                    </div>
                  </template>
                </Tooltip>
              </div>
            </div>
          </div>
          <div class="h-7 whitespace-nowrap mb-5"></div>
          <div
            :class="errorParamsOpen ? 'open-params' : 'stop-params'"
            class="overflow-hidden transition-all"
            style="transition-duration: 400ms;">
            <FormItem class="pr-5 relative" :name="['onError', 'action']">
              <RadioGroup
                v-model:value="globalParams.onError.action"
                class="space-x-6 -ml-5  h-7 pt-1"
                enumKey="ActionWhenError">
              </RadioGroup>
              <Tooltip
                :title="t('xcan_exec.errorHandlingMethodTooltip')"
                placement="topLeft"
                arrowPointAtCenter
                :overlayStyle="{ 'max-width': '600px' }">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2.5" />
              </Tooltip>
            </FormItem>
            <FormItem class="h-7 leading-7 pr-5 relative" :name="['onError', 'sampleError']">
              <Switch
                v-model:checked="globalParams.onError.sampleError"
                size="small"
                class="w-8" />
              <Tooltip
                :title="t('xcan_exec.sampleErrorContentTooltip')"
                placement="topLeft"
                arrowPointAtCenter
                :overlayStyle="{ 'max-width': '600px' }">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
              </Tooltip>
            </FormItem>
            <div
              :class="globalParams.onError.sampleError ? 'open-params' : 'stop-params'"
              class="overflow-hidden transition-all"
              style="transition-duration: 400ms;">
              <FormItem class="pr-5 h-12 relative" :name="['onError', 'sampleErrorNum']">
                <Input
                  v-model:value="globalParams.onError.sampleErrorNum"
                  dataType="number"
                  :min="0"
                  :max="200"
                  size="small"
                  :placeholder="t('xcan_exec.sampleErrorContentCount')" />
                <Tooltip
                  :title="t('xcan_exec.sampleErrorContentCountTooltip')"
                  placement="topLeft"
                  arrowPointAtCenter
                  :overlayStyle="{ 'max-width': '600px' }">
                  <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2.5" />
                </Tooltip>
              </FormItem>
            </div>
          </div>
          <div class="h-7 whitespace-nowrap mb-5 leading-7"></div>
          <div
            :class="reporteParamsOpen ? 'open-params' : 'stop-params'"
            class="overflow-hidden transition-all"
            style="transition-duration: 400ms;">
            <FormItem class="relative pr-5" name="reportInterval">
              <ShortDuration
                :value="globalParams.reportInterval + reportIntervalUnit"
                size="small"
                :inputProps="{ placeholder: t('xcan_exec.samplingInterval') }"
                :selectProps="reportIntervalSelectProps"
                @change="reportIntervalChange" />
              <Tooltip
                :title="t('xcan_exec.samplingIntervalTooltip')"
                placement="topLeft"
                arrowPointAtCenter
                :overlayStyle="{ 'max-width': '600px' }">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
              </Tooltip>
            </FormItem>
            <FormItem name="lang" class="pr-5 relative h-12">
              <SelectEnum
                v-model:value="globalParams.lang"
                :enumKey="SupportedLanguage"
                :placeholder="t('xcan_exec.langPlaceholder')" />
              <Tooltip
                :title="t('xcan_exec.languageTooltip')"
                placement="topLeft"
                arrowPointAtCenter
                :overlayStyle="{ 'max-width': '600px' }">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
              </Tooltip>
            </FormItem>
          </div>
          <div class="h-7 whitespace-nowrap mb-5 leading-7"></div>
          <div
            :class="runnerParamsOpen ? 'open-params' : 'stop-params'"
            class="overflow-hidden transition-all"
            style="transition-duration: 400ms;">
            <FormItem class="relative pr-5" name="startupTimeout">
              <ShortDuration
                :value="globalParams.startupTimeout + startupTimeoutUnit"
                size="small"
                :max="2 * 60 * 60"
                :inputProps="{ placeholder: t('xcan_exec.executor') }"
                :selectProps="startupTimeoutSelectProps"
                @change="startupTimeoutChange" />
              <Tooltip
                :title="t('xcan_exec.executorTooltip')"
                placement="topLeft"
                arrowPointAtCenter
                :overlayStyle="{ 'max-width': '600px' }">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
              </Tooltip>
            </FormItem>
            <FormItem name="lang" class="pr-5 relative h-12">
              <ShortDuration
                :value="globalParams.runnerSetupTimeout + runnerSetupTimeoutUnit"
                size="small"
                :max="2 * 60 * 60"
                :inputProps="{ placeholder: t('xcan_exec.samplingInitializationTimeout') }"
                :selectProps="runnerSetupTimeoutSelectProps"
                @change="runnerSetupTimeoutChange" />
              <Tooltip
                :title="t('xcan_exec.samplingInitializationTimeoutTooltip')"
                placement="topLeft"
                arrowPointAtCenter
                :overlayStyle="{ 'max-width': '600px' }">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
              </Tooltip>
            </FormItem>
          </div>
          <div class="h-7 whitespace-nowrap mb-5"></div>
          <div
            :class="stopParamsOpen ? 'open-params' : 'stop-params'"
            class="overflow-hidden transition-all"
            style="transition-duration: 400ms;">
            <FormItem class="relative pr-5" name="shutdownTimeout">
              <ShortDuration
                :value="globalParams.shutdownTimeout + shutdownTimeoutUnit"
                size="small"
                class="w-full"
                :max="2 * 60 * 60"
                :inputProps="{ placeholder: t('xcan_exec.stopExecutionTimeout') }"
                :selectProps="shutdownTimeoutSelectProps"
                @change="shutdownTimeoutChange" />
              <Tooltip
                :title="t('xcan_exec.stopExecutionTimeoutTooltip')"
                placement="topLeft"
                arrowPointAtCenter
                :overlayStyle="{ 'max-width': '600px' }">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
              </Tooltip>
            </FormItem>
          </div>
        </Form>
      </div>
    </CollapsePanel>
    <template v-if="pluginType">
      <template v-if="!['MOCK_DATA', 'MOCK_APIS'].includes(props.scriptType)">
        <CollapsePanel
          key="plugin"
          :showArrow="false"
          forceRender>
          <template #header>
            <Badge size="small" :count="jdbcParamErrNum">
              <div class="flex items-center text-3 text-text-title font-medium leading-5">
                <Icon icon="icon-chajianpeizhi" class="text-3.5 mr-2" />
                <span>{{ t('xcan_exec.pluginConfiguration') }}</span>
              </div>
            </Badge>
          </template>
          <template #extra>
            <Arrow :open="activeKey.includes('plugin')" @click="setActiveKey('plugin')" />
          </template>
          <template v-if="pluginType === 'Http'">
            <div class="flex pl-3.5 max-w-150 text-3">
              <div class="mr-2.5 leading-7 w-35 flex-none http-form">
                <div class="h-7 whitespace-nowrap mb-5 pl-1.75 ignoreAssertions">{{ t('xcan_exec.ignoreAssertions') }}</div>
                <div class="h-7 whitespace-nowrap mb-5 updateTestResult pl-1.75">
                  {{ t('xcan_exec.updateTestResults') }}
                </div>
                <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-1.75  font-medium">
                  {{  t('xcan_exec.httpParameters') }}
                  <Arrow
                    :open="httpParamsOpen"
                    class="ml-2"
                    @click="openHttpParams" />
                </div>
                <div
                  :class="httpParamsOpen ? 'open-params' : 'stop-params'"
                  class="overflow-hidden transition-all"
                  style="transition-duration: 400ms;">
                  <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 httpSetting-connectTimeout">
                    {{ t('xcan_exec.connectionTimeout') }}
                  </div>
                  <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 httpSetting-readTimeout">
                    {{ t('xcan_exec.readTimeout') }}
                  </div>
                  <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 httpSetting-retryNum">
                    {{ t('xcan_exec.retryCountOnFailure') }}
                  </div>
                  <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 httpSetting-maxRedirects">
                    {{ t('xcan_exec.maxRedirects') }}
                  </div>
                </div>
                <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-1.75  font-medium">
                  {{ t('xcan_exec.extendedParameters') }}
                  <Arrow
                    :open="httpExtendedParamsOpen"
                    class="ml-2"
                    @click="openHttpExtendedParams" />
                </div>
                <div
                  :class="httpExtendedParamsOpen ? 'open-params' : 'stop-params'"
                  class="overflow-hidden transition-all"
                  style="transition-duration: 400ms;">
                  <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25">{{ t('xcan_exec.extendedParameter') }}</div>
                </div>
              </div>
              <div class="flex-1">
                <Form
                  ref="httpParamsFormRef"
                  :model="httpArgumentsParams"
                  :colon="false"
                  layout="horizontal">
                  <FormItem class="h-7 leading-7 pr-5 relative" name="ignoreAssertions">
                    <Switch
                      v-model:checked="httpArgumentsParams.ignoreAssertions"
                      size="small"
                      class="w-8" />
                    <Tooltip
                      :title="t('xcan_exec.ignoreAssertionsTooltip')"
                      placement="topLeft"
                      arrowPointAtCenter
                      :overlayStyle="{ 'max-width': '600px' }">
                      <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
                    </Tooltip>
                  </FormItem>
                  <FormItem class="h-7 leading-7 pr-5 relative" name="updateTestResult">
                    <Switch
                      v-model:checked="httpArgumentsParams.updateTestResult"
                      size="small"
                      class="w-8" />
                    <Tooltip
                      :title="t('xcan_exec.updateTestResultsTooltip')"
                      placement="topLeft"
                      arrowPointAtCenter
                      :overlayStyle="{ 'max-width': '600px' }">
                      <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute top-2 left-10" />
                    </Tooltip>
                  </FormItem>
                  <div class="h-7 whitespace-nowrap mb-5 leading-7"></div>
                  <div
                    :class="httpParamsOpen ? 'open-params' : 'stop-params'"
                    class="overflow-hidden transition-all"
                    style="transition-duration: 400ms;">
                    <FormItem class="relative pr-5" :name="['httpSetting', 'connectTimeout']">
                      <ShortDuration
                        :value="httpArgumentsParams.httpSetting.connectTimeout + httpSettingConnectTimeoutUnit"
                        size="small"
                        class="w-full"
                        placeholder="t('xcan_exec.connectionTimeout')"
                        :max="100000"
                        :selectProps="shutdownTimeoutSelectProps"
                        @change="httpConnectTimeoutChange" />
                      <Tooltip
                        :title="t('xcan_exec.connectionTimeoutTooltip')"
                        placement="topLeft"
                        arrowPointAtCenter
                        :overlayStyle="{ 'max-width': '600px' }">
                        <Icon
                          icon="icon-tishi1"
                          class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
                      </Tooltip>
                    </FormItem>
                    <FormItem class="relative pr-5" :name="['httpSetting', 'readTimeout']">
                      <ShortDuration
                        :value="httpArgumentsParams.httpSetting.readTimeout + httpSettingReadTimeoutUnit"
                        size="small"
                        class="w-full"
                        :placeholder="t('xcan_exec.readTimeoutPlaceholder')"
                        :max="100000"
                        :selectProps="shutdownTimeoutSelectProps"
                        @change="httpReadTimeoutChange" />
                      <Tooltip
                        :title="t('xcan_exec.readTimeoutTooltip')"
                        placement="topLeft"
                        arrowPointAtCenter
                        :overlayStyle="{ 'max-width': '600px' }">
                        <Icon
                          icon="icon-tishi1"
                          class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
                      </Tooltip>
                    </FormItem>
                    <FormItem :name="['httpSetting', 'retryNum']" class="pr-5 relative">
                      <Input
                        v-model:value="httpArgumentsParams.httpSetting.retryNum"
                        dataType="number"
                        :min="0"
                        :max="6"
                        placeholder="t('xcan_exec.retryCountOnFailurePlaceholder')" />
                      <Tooltip
                        :title="t('xcan_exec.retryCountOnFailureTooltip')"
                        placement="topLeft"
                        arrowPointAtCenter
                        :overlayStyle="{ 'max-width': '600px' }">
                        <Icon
                          icon="icon-tishi1"
                          class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
                      </Tooltip>
                    </FormItem>
                    <FormItem :name="['httpSetting', 'maxRedirects']" class="pr-5 relative h-12">
                      <Input
                        v-model:value="httpArgumentsParams.httpSetting.maxRedirects"
                        dataType="number"
                        :min="1"
                        :max="10"
                        placeholder="t('xcan_exec.maxRedirectsPlaceholder')" />
                      <Tooltip
                        :title="t('xcan_exec.maxRedirectsTooltip')"
                        placement="topLeft"
                        arrowPointAtCenter
                        :overlayStyle="{ 'max-width': '600px' }">
                        <Icon
                          icon="icon-tishi1"
                          class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
                      </Tooltip>
                    </FormItem>
                  </div>
                </Form>
                <div
                  :class="httpExtendedParamsOpen ? 'open-params' : 'stop-params'"
                  class="overflow-hidden transition-all"
                  style="transition-duration: 400ms;">
                  <div class="h-7 mb-5"></div>
                  <Form
                    ref="extendParamsRef"
                    :model="extendParams"
                    :colon="false"
                    layout="horizontal">
                    <div class="flex flex-col">
                      <div
                        v-for="(item, index) in extendParams.extend"
                        :key="index"
                        class="flex items-start space-x-1 flex-1">
                        <FormItem
                          class="flex-1"
                          :name="['extend', index, 'key']"
                          :rules="{ required: true, validator: keyValidator }">
                          <Input v-model:value="item.key" placeholder="参数名" />
                        </FormItem>
                        <FormItem
                          class="flex-1"
                          :name="['extend', index, 'value']"
                          :rules="{ required: true, message: '请输入参数值' }">
                          <Input v-model:value="item.value" placeholder="参数值" />
                        </FormItem>
                        <div class="flex items-center w-15 flex-none !ml-2.5 pt-1">
                          <Button
                            style="width:20px;height: 20px;"
                            class="p-0"
                            size="small"
                            @click="delHttpApisSecurityItem(index)">
                            <Icon icon="icon-jian" class="text-3" />
                          </Button>
                          <template
                            v-if="extendParams.extend.length - 1 === index && extendParams.extend.length !== 19">
                            <Button
                              style="width:20px;height: 20px;"
                              class="p-0 ml-1"
                              size="small"
                              @click="addApisSecurityItem">
                              <Icon icon="icon-jia" />
                            </Button>
                          </template>
                        </div>
                      </div>
                    </div>
                  </Form>
                </div>
              </div>
            </div>
          </template>
          <template v-else-if="pluginType === 'WebSocket'">
            <div class="flex pl-3.5 max-w-150 text-3">
              <div class="mr-2.5 leading-7 w-35 flex-none websocket-form">
                <div class="h-7 whitespace-nowrap mb-5 pl-1.75 ignoreAssertions">{{ t('xcan_exec.ignoreAssertions') }}</div>
                <div class="h-7 whitespace-nowrap mb-5 updateTestResult pl-1.75">
                    {{ t('xcan_exec.updateTestResults') }}
                </div>
                <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-1.75  font-medium">
                  {{ t('xcan_exec.webSocketParameters') }}
                  <Arrow
                    :open="httpParamsOpen"
                    class="ml-2"
                    @click="openHttpParams" />
                </div>
                <div
                  :class="httpParamsOpen ? 'open-params' : 'stop-params'"
                  class="overflow-hidden transition-all"
                  style="transition-duration: 400ms;">
                  <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 webSocketSetting-connectTimeout">
                    {{ t('xcan_exec.connectionTimeout') }}
                  </div>
                  <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 webSocketSetting-responseTimeout">
                    {{ t('xcan_exec.responseTimeout') }}
                  </div>
                  <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 webSocketSetting-maxReconnections">
                    {{ t('xcan_exec.maxReconnections') }}
                  </div>
                  <div
                    class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 webSocketSetting-reconnectionInterval">
                    {{ t('xcan_exec.reconnectionInterval') }}
                  </div>
                </div>
                <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-1.75  font-medium">
                  {{ t('xcan_exec.extendedParameters') }}
                  <Arrow
                    :open="httpExtendedParamsOpen"
                    class="ml-2"
                    @click="openHttpExtendedParams" />
                </div>
                <div
                  :class="httpExtendedParamsOpen ? 'open-params' : 'stop-params'"
                  class="overflow-hidden transition-all"
                  style="transition-duration: 400ms;">
                  <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25">{{ t('xcan_exec.extendedParameter') }}</div>
                </div>
              </div>
              <div class="flex-1">
                <Form
                  ref="webSocketParamsFormRef"
                  :model="webSocketArgumentsParams"
                  :colon="false"
                  layout="horizontal">
                  <FormItem class="h-7 leading-7 pr-5 relative" name="ignoreAssertions">
                    <Switch
                      v-model:checked="webSocketArgumentsParams.ignoreAssertions"
                      size="small"
                      class="w-8" />
                    <Tooltip
                      :title="t('xcan_exec.ignoreAssertionsTooltip')"
                      placement="topLeft"
                      arrowPointAtCenter
                      :overlayStyle="{ 'max-width': '600px' }">
                      <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
                    </Tooltip>
                  </FormItem>
                  <FormItem class="h-7 leading-7 pr-5 relative" name="updateTestResult">
                    <Switch
                      v-model:checked="webSocketArgumentsParams.updateTestResult"
                      size="small"
                      class="w-8" />
                    <Tooltip
                      :title="t('xcan_exec.updateTestResultsTooltip')"
                      placement="topLeft"
                      arrowPointAtCenter
                      :overlayStyle="{ 'max-width': '600px' }">
                      <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute top-2 left-10" />
                    </Tooltip>
                  </FormItem>
                  <div class="h-7 whitespace-nowrap mb-5 leading-7"></div>
                  <div
                    :class="httpParamsOpen ? 'open-params' : 'stop-params'"
                    class="overflow-hidden transition-all"
                    style="transition-duration: 400ms;">
                    <FormItem class="relative pr-5" :name="['webSocketSetting', 'connectTimeout']">
                      <ShortDuration
                        :value="webSocketArgumentsParams.webSocketSetting.connectTimeout + webSocketSettingConnectTimeoutUnit"
                        size="small"
                        class="w-full"
                        :placeholder="t('xcan_exec.connectionTimeout')"
                        :max="100000"
                        :selectProps="shutdownTimeoutSelectProps"
                        @change="webSocletConnectTimeoutChange" />
                      <Tooltip
                        :title="t('xcan_exec.connectionTimeoutTooltip')"
                        placement="topLeft"
                        arrowPointAtCenter
                        :overlayStyle="{ 'max-width': '600px' }">
                        <Icon
                          icon="icon-tishi1"
                          class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
                      </Tooltip>
                    </FormItem>
                    <FormItem class="relative pr-5" :name="['webSocketSetting', 'responseTimeout']">
                      <ShortDuration
                        :value="webSocketArgumentsParams.webSocketSetting.responseTimeout + webSocketSettingResponseTimeoutUnit"
                        size="small"
                        class="w-full"
                        placeholder="t('xcan_exec.readTimeoutPlaceholder')"
                        :max="100000"
                        :selectProps="shutdownTimeoutSelectProps"
                        @change="webSocletResponseTimeoutChange" />
                      <Tooltip
                        :title="t('xcan_exec.responseTimeoutTooltip')"
                        placement="topLeft"
                        arrowPointAtCenter
                        :overlayStyle="{ 'max-width': '600px' }">
                        <Icon
                          icon="icon-tishi1"
                          class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
                      </Tooltip>
                    </FormItem>
                    <FormItem :name="['webSocketSetting', 'maxReconnections']" class="pr-5 relative">
                      <Input
                        v-model:value="webSocketArgumentsParams.webSocketSetting.maxReconnections"
                        dataType="number"
                        :min="0"
                        :max="100"
                        :placeholder="t('xcan_exec.maxReconnections')" />
                      <Tooltip
                        :title="t('xcan_exec.maxReconnectionsTooltip')"
                        placement="topLeft"
                        arrowPointAtCenter
                        :overlayStyle="{ 'max-width': '600px' }">
                        <Icon
                          icon="icon-tishi1"
                          class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
                      </Tooltip>
                    </FormItem>
                    <FormItem class="relative pr-5  h-12" :name="['webSocketSetting', 'reconnectionInterval']">
                      <ShortDuration
                        :value="webSocketArgumentsParams.webSocketSetting.reconnectionInterval + reconnectionIntervalUnit"
                        size="small"
                        class="w-full"
                        :placeholder="t('xcan_exec.reconnectionInterval')"
                        :max="0.5 * 60 * 60"
                        :selectProps="reconnectionIntervalSelectProps"
                        @change="webSocletReconnectionIntervalChange" />
                      <Tooltip
                        :title="t('xcan_exec.reconnectionIntervalTooltip')"
                        placement="topLeft"
                        arrowPointAtCenter
                        :overlayStyle="{ 'max-width': '600px' }">
                        <Icon
                          icon="icon-tishi1"
                          class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
                      </Tooltip>
                    </FormItem>
                  </div>
                </Form>
                <div
                  :class="httpExtendedParamsOpen ? 'open-params' : 'stop-params'"
                  class="overflow-hidden transition-all"
                  style="transition-duration: 400ms;">
                  <div class="h-7 mb-5"></div>
                  <Form
                    ref="extendParamsRef"
                    :model="extendParams"
                    :colon="false"
                    layout="horizontal">
                    <div class="flex flex-col">
                      <div
                        v-for="(item, index) in extendParams.extend"
                        :key="index"
                        class="flex items-start space-x-1 flex-1">
                        <FormItem
                          class="flex-1"
                          :name="['extend', index, 'key']"
                          :rules="{ required: true, validator: keyValidator }">
                          <Input v-model:value="item.key" :placeholder="t('xcan_exec.parameterName')" />
                        </FormItem>
                        <FormItem
                          class="flex-1"
                          :name="['extend', index, 'value']"
                          :rules="{ required: true, message: t('xcan_exec.pleaseEnterParameterValue') }">
                          <Input v-model:value="item.value" :placeholder="t('xcan_exec.parameterValue')" />
                        </FormItem>
                        <div class="flex items-center w-15 flex-none !ml-2.5 pt-1">
                          <Button
                            style="width:20px;height: 20px;"
                            class="p-0"
                            size="small"
                            @click="delHttpApisSecurityItem(index)">
                            <Icon icon="icon-jian" class="text-3" />
                          </Button>
                          <template
                            v-if="extendParams.extend.length - 1 === index && extendParams.extend.length !== 19">
                            <Button
                              style="width:20px;height: 20px;"
                              class="p-0 ml-1"
                              size="small"
                              @click="addApisSecurityItem">
                              <Icon icon="icon-jia" />
                            </Button>
                          </template>
                        </div>
                      </div>
                    </div>
                  </Form>
                </div>
              </div>
            </div>
          </template>
          <template v-else-if="pluginType === 'Jdbc'">
            <div class="flex pl-3.5 max-w-150 text-3">
              <div class="mr-2.5 leading-7 w-35 flex-none jdbc-form">
                <div class="h-7 whitespace-nowrap mb-5 pl-1.75 ignoreAssertions">{{ t('xcan_exec.ignoreAssertions') }}</div>
                <div class="h-7 whitespace-nowrap mb-5 updateTestResult pl-1.75">
                  {{ t('xcan_exec.updateTestResults') }}
                </div>
                <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-1.75  font-medium">
                  {{ t('xcan_exec.jdbcParameters') }}
                  <Arrow
                    :open="jdbcParamsOpen"
                    class="ml-2"
                    @click="openJdbcParams" />
                </div>
                <div
                  :class="jdbcParamsOpen ? 'open-params' : 'stop-params'"
                  class="overflow-hidden transition-all"
                  style="transition-duration: 400ms;">
                  <div class="invisible h-7 whitespace-nowrap mb-0 flex items-center pl-3.5 jdbcSetting-type"></div>
                  <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-3.5 jdbcSetting-type">
                    <IconRequired />{{ t('xcan_exec.databaseType') }}
                  </div>
                  <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 jdbcSetting-driverClassName">
                    {{ t('xcan_exec.databaseDriverClassName') }}
                  </div>
                  <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-3.5 jdbcSetting-jdbcUrl">
                    <IconRequired />{{ t('xcan_exec.databaseConnectionURL') }}
                  </div>
                  <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-3.5 jdbcSetting-username">
                    <IconRequired />{{ t('xcan_exec.username') }}
                  </div>
                  <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 jdbcSetting-password">
                    {{ t('xcan_exec.password') }}
                  </div>
                  <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 jdbcSetting-isolation">
                    {{ t('xcan_exec.transactionIsolationLevel') }}
                  </div>
                </div>
                <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-1.75  font-medium">
                  {{ t('xcan_exec.connectionPoolConfiguration') }}
                  <Arrow
                    :open="connectionPoolParamsOpen"
                    class="ml-2"
                    @click="openConnectionPoolParams" />
                </div>
                <div
                  :class="connectionPoolParamsOpen ? 'open-params' : 'stop-params'"
                  class="overflow-hidden transition-all"
                  style="transition-duration: 400ms;">
                  <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25">{{ t('xcan_exec.enableConfiguration') }}</div>
                  <div
                    :class="isOpenPool ? 'open-params' : 'stop-params'"
                    class="overflow-hidden transition-all"
                    style="transition-duration: 400ms;">
                    <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-3.5 pool-name">
                      <IconRequired />{{ t('xcan_exec.connectionPoolType') }}
                    </div>
                    <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-3.5 pool-maximumPoolSize">
                      <IconRequired />{{ t('xcan_exec.connectionPoolSize') }}
                    </div>
                    <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-3.5 pool-minimumIdle">
                      <IconRequired />{{ t('xcan_exec.minIdleConnections') }}
                    </div>
                    <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 pool-maxWaitTimeoutMillis">
                      {{ t('xcan_exec.getConnectionTimeout') }}
                    </div>
                  </div>
                </div>
                <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-1.75  font-medium">
                  {{ t('xcan_exec.extendedParameters') }}
                  <Arrow
                    :open="httpExtendedParamsOpen"
                    class="ml-2"
                    @click="openHttpExtendedParams" />
                </div>
                <div
                  :class="httpExtendedParamsOpen ? 'open-params' : 'stop-params'"
                  class="overflow-hidden transition-all"
                  style="transition-duration: 400ms;">
                  <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25">{{ t('xcan_exec.extendedParameter') }}</div>
                </div>
              </div>
              <div class="flex-1">
                <Form
                  ref="jdbcParamsFormRef"
                  :model="jdbcArgumentsParams"
                  :colon="false"
                  layout="horizontal">
                  <FormItem class="h-7 leading-7 pr-5 relative" name="ignoreAssertions">
                    <Switch
                      v-model:checked="jdbcArgumentsParams.ignoreAssertions"
                      size="small"
                      class="w-8" />
                    <Tooltip
                      :title="t('xcan_exec.ignoreAssertionsTooltip')"
                      placement="topLeft"
                      arrowPointAtCenter
                      :overlayStyle="{ 'max-width': '600px' }">
                      <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
                    </Tooltip>
                  </FormItem>
                  <FormItem class="h-7 leading-7 pr-5 relative" name="updateTestResult">
                    <Switch
                      v-model:checked="jdbcArgumentsParams.updateTestResult"
                      size="small"
                      class="w-8" />
                    <Tooltip
                      :title="t('xcan_exec.updateTestResultsTooltip')"
                      placement="topLeft"
                      arrowPointAtCenter
                      :overlayStyle="{ 'max-width': '600px' }">
                      <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
                    </Tooltip>
                  </FormItem>
                  <div class="h-7 whitespace-nowrap mb-5 leading-7"></div>
                  <div
                    :class="jdbcParamsOpen ? 'open-params' : 'stop-params'"
                    class="overflow-hidden transition-all"
                    style="transition-duration: 400ms;">
                    <FormItem class="pr-5 relative" style="margin-bottom: 12px;">
                      <Button
                        type="link"
                        size="small"
                        class="flex items-center p-0 border-none h-3.5 leading-3.5 space-x-1"
                        @click="toSelectDataSource">
                        <Icon icon="icon-xuanze" class="text-3.5" />
                        <span>{{ t('xcan_exec.selectDataSource') }}</span>
                      </Button>
                    </FormItem>
                    <FormItem
                      class="pr-5 relative"
                      :name="['jdbcSetting', 'type']"
                      :rules="{ required: true, message: t('xcan_exec.pleaseEnterDatabaseType'), trigger: ['change', 'blur'] }">
                      <SelectEnum
                        v-model:value="jdbcArgumentsParams.jdbcSetting.type"
                        :enumKey="DatabaseType"
                        :placeholder="t('xcan_exec.databaseTypePlaceholder')"
                        @change="setPluginParamErrNum(['jdbcSetting', 'type'])" />
                      <Tooltip
                        :title="t('xcan_exec.databaseTypeTooltip')"
                        placement="topLeft"
                        arrowPointAtCenter
                        :overlayStyle="{ 'max-width': '600px' }">
                        <Icon
                          icon="icon-tishi1"
                          class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
                      </Tooltip>
                    </FormItem>
                    <FormItem class="pr-5 relative" :name="['jdbcSetting', 'driverClassName']">
                      <Input
                        v-model:value="jdbcArgumentsParams.jdbcSetting.driverClassName"
                        :maxlength="200"
                        :placeholder="t('xcan_exec.databaseDriverClassNamePlaceholder')"
                        @change="setPluginParamErrNum(['jdbcSetting', 'driverClassName'])" />
                      <Tooltip
                        :title="t('xcam_exec.databaseDriverClassNameTooltip')"
                        placement="topLeft"
                        arrowPointAtCenter
                        :overlayStyle="{ 'max-width': '600px' }">
                        <Icon
                          icon="icon-tishi1"
                          class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
                      </Tooltip>
                    </FormItem>
                    <FormItem
                      class="pr-5 relative"
                      :name="['jdbcSetting', 'jdbcUrl']"
                      :rules="{ required: true, message: t('xcan_exec.pleaseEnterDatabaseConnectionURL'), trigger: ['change', 'blur'] }">
                      <Input
                        v-model:value="jdbcArgumentsParams.jdbcSetting.jdbcUrl"
                        :maxlength="2048"
                        :placeholder="t('xcan_exec.databaseConnectionURLPlaceholder')"
                        @change="setPluginParamErrNum(['jdbcSetting', 'jdbcUrl'])" />
                      <Tooltip
                        :title="t('xcan_exec.databaseConnectionURLTooltip')"
                        placement="topLeft"
                        arrowPointAtCenter
                        :overlayStyle="{ 'max-width': '600px' }">
                        <Icon
                          icon="icon-tishi1"
                          class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
                      </Tooltip>
                    </FormItem>
                    <FormItem
                      class="pr-5"
                      :name="['jdbcSetting', 'username']"
                      :rules="{ required: true, message: t('xcan_exec.pleaseEnterDatabaseUsername'), trigger: ['change', 'blur'] }">
                      <Input
                        v-model:value="jdbcArgumentsParams.jdbcSetting.username"
                        :maxlength="200"
                        :placeholder="t('xcan_exec.databaseUsername')"
                        @change="setPluginParamErrNum(['jdbcSetting', 'username'])" />
                    </FormItem>
                    <FormItem class="pr-5" :name="['jdbcSetting', 'password']">
                      <Input
                        v-model:value="jdbcArgumentsParams.jdbcSetting.password"
                        type="password"
                        :maxlength="1024"
                        :placeholder="t('xcan_exec.databasePassword')" />
                    </FormItem>
                    <FormItem class="pr-5 relative h-12" :name="['jdbcSetting', 'isolation']">
                      <SelectEnum
                        v-model:value="jdbcArgumentsParams.jdbcSetting.isolation"
                        :enumKey="TransactionIsolation"
                        placeholder="t('xcan_exec.transactionIsolationLevel')" />
                      <Tooltip
                        :title="t('xcan_exec.transactionIsolationLevelTooltip')"
                        placement="topLeft"
                        arrowPointAtCenter
                        :overlayStyle="{ 'max-width': '600px' }">
                        <Icon
                          icon="icon-tishi1"
                          class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
                      </Tooltip>
                    </FormItem>
                  </div>
                  <div class="h-7 whitespace-nowrap mb-5 leading-7"></div>
                  <div
                    :class="connectionPoolParamsOpen ? 'open-params' : 'stop-params'"
                    class="overflow-hidden transition-all"
                    style="transition-duration: 400ms;">
                    <FormItem class="h-7 leading-7 pr-5 relative">
                      <Switch
                        :checked="isOpenPool"
                        size="small"
                        class="w-8"
                        @change="openPoolChange" />
                      <Tooltip
                        :title="t('xcan_exec.enableConfigurationTooltip')"
                        placement="topLeft"
                        arrowPointAtCenter
                        :overlayStyle="{ 'max-width': '600px' }">
                        <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
                      </Tooltip>
                    </FormItem>
                    <div
                      :class="isOpenPool ? 'open-params' : 'stop-params'"
                      class="overflow-hidden transition-all"
                      style="transition-duration: 400ms;">
                      <FormItem
                        class="pr-5 read-only:"
                        :name="['pool', 'name']"
                        :rules="{ required: isOpenPool, message: t('xcan_exec.pleaseEnterConnectionPoolType'), trigger: ['change', 'blur'] }">
                        <SelectEnum
                          v-model:value="jdbcArgumentsParams.pool.name"
                          :enumKey="PoolType"
                          :placeholder="t('xcan_exec.connectionPoolType')"
                          @change="setPluginParamErrNum(['pool', 'name'])" />
                        <Tooltip
                          :title="t('xcan_exec.connectionPoolTypeTooltip')"
                          placement="topLeft"
                          arrowPointAtCenter
                          :overlayStyle="{ 'max-width': '600px' }">
                          <Icon
                            icon="icon-tishi1"
                            class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
                        </Tooltip>
                      </FormItem>
                      <FormItem
                        class="relative pr-5"
                        :name="['pool', 'maximumPoolSize']"
                        :rules="{ required: isOpenPool, message: t('xcan_exec.pleaseEnterConnectionPoolSize'), trigger: ['change', 'blur'] }">
                        <Input
                          v-model:value="jdbcArgumentsParams.pool.maximumPoolSize"
                          :min="1"
                          :max="10000"
                          dataType="number"
                          :placeholder="t('xcan_exec.connectionPoolMaximumPoolSizeTooltip')"
                          @change="setPluginParamErrNum(['pool', 'maximumPoolSize'])" />
                        <Tooltip
                          :title="t('xcan_exec.connectionPoolSizeTooltip')"
                          placement="topLeft"
                          arrowPointAtCenter
                          :overlayStyle="{ 'max-width': '600px' }">
                          <Icon
                            icon="icon-tishi1"
                            class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
                        </Tooltip>
                      </FormItem>
                      <FormItem
                        class="relative pr-5"
                        :name="['pool', 'minimumIdle']"
                        :rules="{ required: isOpenPool, message: t('xcan_exec.pleaseEnterMinIdleConnections'), trigger: ['change', 'blur'] }">
                        <Input
                          v-model:value="jdbcArgumentsParams.pool.minimumIdle"
                          dataType="number"
                          :placeholder="t('xcan_exec.minIdleConnections')"
                          @change="setPluginParamErrNum(['pool', 'minimumIdle'])" />
                        <Tooltip
                          :title="t('xcan_exec.minIdleConnectionsTooltip')"
                          placement="topLeft"
                          arrowPointAtCenter
                          :overlayStyle="{ 'max-width': '600px' }">
                          <Icon
                            icon="icon-tishi1"
                            class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
                        </Tooltip>
                      </FormItem>
                      <FormItem class="relative pr-5 h-12" :name="['pool', 'maxWaitTimeoutMillis']">
                        <ShortDuration
                          :value="jdbcArgumentsParams.pool.maxWaitTimeoutMillis + maxWaitTimeoutMillisUnit"
                          size="small"
                          class="w-full"
                          :inputProps="{ placeholder: t('xcan_exec.connectionTimeout') }"
                          :max="2147483.647"
                          :selectProps="maxWaitTimeoutMillisSelectProps"
                          @change="jdbcMaxWaitTimeoutMillisChange" />
                        <Tooltip
                          :title="t('xcan_exec.getConnectionTimeoutTooltip')"
                          placement="topLeft"
                          arrowPointAtCenter
                          :overlayStyle="{ 'max-width': '600px' }">
                          <Icon
                            icon="icon-tishi1"
                            class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
                        </Tooltip>
                      </FormItem>
                    </div>
                  </div>
                  <div class="h-7 whitespace-nowrap mb-5 leading-7"></div>
                </Form>
                <div
                  :class="httpExtendedParamsOpen ? 'open-params' : 'stop-params'"
                  class="overflow-hidden transition-all"
                  style="transition-duration: 400ms;">
                  <Form
                    ref="extendParamsRef"
                    :model="extendParams"
                    :colon="false"
                    layout="horizontal">
                    <div class="flex flex-col">
                      <div
                        v-for="(item, index) in extendParams.extend"
                        :key="index"
                        class="flex items-start space-x-1 flex-1">
                        <FormItem
                          class="flex-1"
                          :name="['extend', index, 'key']"
                          :rules="{ required: true, validator: keyValidator }">
                          <Input v-model:value="item.key" :placeholder="t('xcan_exec.parameterName')" />
                        </FormItem>
                        <FormItem
                          class="flex-1"
                          :name="['extend', index, 'value']"
                          :rules="{ required: true, message: t('xcan_exec.pleaseEnterParameterValue') }">
                          <Input v-model:value="item.value" :placeholder="t('xcan_exec.parameterValue')" />
                        </FormItem>
                        <div class="flex items-center w-15 flex-none !ml-2.5 pt-1">
                          <Button
                            style="width:20px;height: 20px;"
                            class="p-0"
                            size="small"
                            @click="delHttpApisSecurityItem(index)">
                            <Icon icon="icon-jian" class="text-3" />
                          </Button>
                          <template
                            v-if="extendParams.extend.length - 1 === index && extendParams.extend.length !== 19">
                            <Button
                              style="width:20px;height: 20px;"
                              class="p-0 ml-1"
                              size="small"
                              @click="addApisSecurityItem">
                              <Icon icon="icon-jia" />
                            </Button>
                          </template>
                        </div>
                      </div>
                    </div>
                  </Form>
                </div>
              </div>
            </div>
          </template>
          <template v-if="$slots.default">
            <slot></slot>
          </template>
        </CollapsePanel>
      </template>
      <CollapsePanel
        key="variable"
        :showArrow="false"
        forceRender>
        <template #header>
          <div class="flex items-center text-3 text-text-title font-medium leading-5">
            <Icon icon="icon-gonggongbianliang" class="text-3.5 mr-2 -mt-0.5" />
            <span>{{ t('xcan_exec.variables') }}</span>
          </div>
        </template>
        <template #extra>
          <Arrow :open="activeKey.includes('variable')" @click="setActiveKey('variable')" />
        </template>
        <Variables ref="variablesRef" :dataSource="configVariables" />
      </CollapsePanel>
    </template>
  </Collapse>
  <SelectDataSourceModal
    v-model:visible="modalVisible"
    :projectId="projectInfo?.id"
    @ok="selectedDataSourceOk" />
</template>
<style scoped>
@media (width <=1200px) {
  .label-calss {
    margin-bottom: 110px !important;
  }
}

.open-params {
  max-height: 500px;
}

.stop-params {
  max-height: 0;
}

.ant-collapse> :deep(.ant-collapse-item) {
  margin-bottom: 20px;
  border: 0;
}

.ant-collapse> :deep(.ant-collapse-item > .ant-collapse-header) {
  padding: 4px 12px;
  border-bottom: 1px solid var(--border-divider);
}

.ant-collapse> :deep(.ant-collapse-item > .ant-collapse-content > .ant-collapse-content-box) {
  padding: 20px 20px 4px 14px;
}

:deep(.ant-checkbox-group .ant-checkbox-wrapper:hover) {
  border-color: #1890ff;

}

:deep(.ant-checkbox-group .ant-checkbox:hover .ant-checkbox-inner) {
  border-color: #1890ff;
}

:deep(.ant-checkbox-group .ant-checkbox-input:focus + .ant-checkbox-inner) {
  border-color: #1890ff;
  box-shadow: 0 0 0 3px #e6f7ff;
}

:deep(.ant-checkbox-group .ant-checkbox-checked::after) {
  content: '';
  visibility: hidden;
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  animation: antRadioEffect 0.36s ease-in-out;
  border: 1px solid #1890ff;
  border-radius: 50%;
  animation-fill-mode: both;
}

:deep(.ant-checkbox-group .ant-checkbox:hover::after),
:deep(.ant-checkbox-group .ant-checkbox-wrapper:hover .ant-checkbox::after) {
  visibility: visible;
}

:deep(.ant-checkbox-group .ant-checkbox-inner) {
  display: block;
  position: relative;
  top: 0;
  left: 0;
  width: 16px;
  height: 16px;
  transition: all 0.3s;
  border-width: 1px;
  border-style: solid;
  border-radius: 50%;
  border-color: #d9d9d9;
  background-color: #fff;
}

:deep(.ant-checkbox-group .ant-checkbox-inner::after) {
  content: ' ';
  display: block;
  position: absolute;
  top: 50%;
  left: 50%;
  width: 16px;
  height: 16px;
  margin-top: -8px;
  margin-left: -8px;
  transform: scale(0);
  transition: all 0.3s cubic-bezier(0.78, 0.14, 0.15, 0.86);
  border-top: 0;
  border-left: 0;
  border-radius: 16px;
  opacity: 0;
  background-color: #1890ff;
}

:deep(.ant-checkbox-group .ant-checkbox-input) {
  position: absolute;
  z-index: 1;
  inset: 0;
  opacity: 0;
  cursor: pointer;
}

:deep(.ant-checkbox-group .ant-checkbox-checked .ant-checkbox-inner) {
  border-color: #1890ff;
}

:deep(.ant-checkbox-group .ant-checkbox-checked .ant-checkbox-inner::after) {
  transform: scale(0.5);
  transition: all 0.3s cubic-bezier(0.78, 0.14, 0.15, 0.86);
  opacity: 1;
}

:deep(.ant-badge-count) {
  right: -5px;
}
</style>
