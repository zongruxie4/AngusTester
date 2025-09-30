<script setup lang="ts">
// Vue core imports
import { ref, defineAsyncComponent, reactive, onMounted, nextTick, onBeforeUnmount, computed, watch } from 'vue';
import { useI18n } from 'vue-i18n';

// UI component imports
import { Form, FormItem, Switch, Collapse, CollapsePanel, Checkbox, CheckboxGroup, Badge } from 'ant-design-vue';
import { Input, Select, SelectEnum, DatePicker, Icon, IconRequired, Arrow, RadioGroup, Tooltip, ShortDuration, Colon } from '@xcan-angus/vue-ui';

// Infrastructure imports
import { TESTER, SupportedLanguage, DatabaseType, TransactionIsolation, PoolType, StartMode, ActionWhenError } from '@xcan-angus/infra';
import type { Rule } from 'ant-design-vue/es/form';
import elementResizeDetector from 'element-resize-detector';

const { t } = useI18n();

/**
 * Component props interface for execution setting form
 */
export interface Props {
  scriptType: string;
  scriptInfo?: Record<string, any>;
  addType?: 'expr'; // Experience execution type
}

// Component props with defaults
const props = withDefaults(defineProps<Props>(), {
  scriptInfo: undefined,
  addType: undefined
});

// Async component imports
const LineChart = defineAsyncComponent(() => import('./LineChart.vue'));
const Variables = defineAsyncComponent(() => import('./Variables.vue'));

/**
 * Split time string into number and unit parts
 */
const splitTime = (str: string): [string, string] => {
  const number = str.replace(/\D/g, '');
  const unit = str.replace(/\d/g, '');
  return [number, unit];
};

// Chart data state
const chartData = reactive({
  xData: [],
  yData: []
});

// Configuration state
const configurationTends = ref();
const pluginType = ref();
const pluginSettingExtends = ref();
const configVariables = ref([]);

/**
 * Update chart data based on execution parameters
 */
const updateChartData = () => {
  const { duration, thread } = executionParams.value; // durationUnit
  // const durationOfSec = durationUnit.value === 's'
  //   ? +duration
  //   : durationUnit.value === 'min'
  //     ? +duration * 60
  //     : +duration * 3600;
  const durationOfSec = +duration;
  const xData = [];
  const yData = [];
  if (!duration || !thread) {
    return;
  }
  if (isRampUpEnabled.value && isRampDownEnabled.value) {
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

  if (isRampUpEnabled.value && !isRampDownEnabled.value) {
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

  if (!isRampUpEnabled.value && isRampDownEnabled.value) {
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
  if (!isRampUpEnabled.value && !isRampDownEnabled.value) {
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

/**
 * Initialize execution parameters from script info
 */
const initializeExecutionParameters = () => {
  if (!props.scriptInfo) {
    return;
  }

  configurationTends.value = {};
  pluginSettingExtends.value = undefined;
  const { configuration, plugin, task, type } = props.scriptInfo;
  pluginType.value = plugin;
  if (configuration) {
    const { duration, thread, iterations, startMode, startAtDate, priority, reportInterval, lang, startupTimeout, runnerSetupTimeout, shutdownTimeout, onError, nodeSelectors, variables, ...configOhters } = configuration;
    if (configOhters) {
      configurationTends.value = configOhters;
    }

    if (props.scriptType === type) {
      if (duration) {
        const strs = splitTime(duration);
        executionParams.value.duration = strs[0];
        durationUnit.value = strs[1];
      }

      if (iterations) {
        executionParams.value.iterations = iterations || '';
      }

      if (thread) {
        const { threads, rampUpInterval, rampUpThreads, rampDownInterval, rampDownThreads, resetAfterRamp } = thread;
        if (threads) {
          executionParams.value.thread.threads = threads;
        }

        if (rampUpInterval && rampUpThreads) {
          isRampUpEnabled.value = true;
          executionParams.value.thread.rampUpInterval = rampUpInterval.match(/\d+/)[0];
          executionParams.value.thread.rampUpThreads = rampUpThreads;
        } else {
          isRampUpEnabled.value = false;
        }

        if (rampDownInterval && rampDownThreads) {
          isRampDownEnabled.value = true;
          executionParams.value.thread.rampDownInterval = rampDownInterval.match(/\d+/)[0];
          executionParams.value.thread.rampDownThreads = rampDownThreads;
        } else {
          isRampDownEnabled.value = false;
        }

        executionParams.value.thread.resetAfterRamp = typeof resetAfterRamp === 'boolean' ? [resetAfterRamp] : [];
      }
    } else {
      if (props.scriptType !== type && props.scriptType === 'TEST_FUNCTIONALITY') {
        executionParams.value.thread.threads = '1';
        executionParams.value.iterations = '1';
      }

      if (props.scriptType !== type && props.scriptType === 'TEST_PERFORMANCE') {
        executionParams.value.thread.threads = '5000';
        executionParams.value.thread.rampUpInterval = '1';
        executionParams.value.thread.rampUpThreads = '100';
        executionParams.value.thread.resetAfterRamp = [];
        executionParams.value.duration = '50';
        durationUnit.value = 'min';
        isRampUpEnabled.value = true;
      }

      if (props.scriptType !== type && props.scriptType === 'TEST_STABILITY') {
        executionParams.value.thread.threads = '200';
        executionParams.value.duration = '30';
        durationUnit.value = 'min';
      }
    }

    globalSettingsParams.value.startMode = startMode?.value || undefined;
    globalSettingsParams.value.startAtDate = startAtDate || undefined;
    globalSettingsParams.value.priority = priority || '';
    if (reportInterval) {
      const strs = splitTime(reportInterval);
      globalSettingsParams.value.reportInterval = strs[0];
      reportIntervalUnit.value = strs[1];
    }
    globalSettingsParams.value.lang = lang || undefined;
    if (shutdownTimeout) {
      const strs = splitTime(shutdownTimeout);
      globalSettingsParams.value.shutdownTimeout = strs[0];
      shutdownTimeoutUnit.value = strs[1];
    }

    if (startupTimeout) {
      const strs = splitTime(startupTimeout);
      globalSettingsParams.value.startupTimeout = strs[0];
      startupTimeoutUnit.value = strs[1];
    }

    if (runnerSetupTimeout) {
      const strs = splitTime(runnerSetupTimeout);
      globalSettingsParams.value.runnerSetupTimeout = strs[0];
      runnerSetupTimeoutUnit.value = strs[1];
    }

    if (onError) {
      const { action, sampleError, sampleErrorNum } = onError;
      globalSettingsParams.value.onError.action = action?.value || undefined;
      globalSettingsParams.value.onError.sampleError = typeof sampleError === 'boolean' ? sampleError : true;
      globalSettingsParams.value.onError.sampleErrorNum = globalSettingsParams.value.onError.sampleError ? sampleErrorNum || '20' : '';
    }

    if (nodeSelectors) {
      const { num, strategy, availableNodeIds, appNodeIds } = nodeSelectors;
      globalSettingsParams.value.nodeSelectors.num = num || '';
      globalSettingsParams.value.nodeSelectors.availableNodeIds = availableNodeIds?.length ? availableNodeIds : [];
      globalSettingsParams.value.nodeSelectors.appNodeIds = appNodeIds?.length ? appNodeIds : [];
      if (strategy) {
        const { enabled, maxTaskNum, lastExecuted, specEnabled, cpuSpec, memorySpec, diskSpec, idleRateEnabled, cpuIdleRate, memoryIdleRate, diskIdleRate } = strategy;
        globalSettingsParams.value.nodeSelectors.enabled = !!enabled;
        if (enabled) {
          globalSettingsParams.value.nodeSelectors.maxTaskNum = maxTaskNum || '';
          globalSettingsParams.value.nodeSelectors.lastExecuted = !!lastExecuted;
          globalSettingsParams.value.nodeSelectors.idleRateEnabled = !!idleRateEnabled;
          globalSettingsParams.value.nodeSelectors.specEnabled = !!specEnabled;
          if (specEnabled) {
            globalSettingsParams.value.nodeSelectors.cpuSpec = cpuSpec || '';
            globalSettingsParams.value.nodeSelectors.memorySpec = memorySpec ? memorySpec.replace(/GB/g, '') : '';
            globalSettingsParams.value.nodeSelectors.diskSpec = diskSpec ? diskSpec.replace(/GB/g, '') : '';
          }
          if (idleRateEnabled) {
            globalSettingsParams.value.nodeSelectors.cpuIdleRate = cpuIdleRate ? cpuIdleRate.replace(/%/g, '') : '';
            globalSettingsParams.value.nodeSelectors.memoryIdleRate = memoryIdleRate ? memoryIdleRate.replace(/%/g, '') : '';
            globalSettingsParams.value.nodeSelectors.diskIdleRate = diskIdleRate ? diskIdleRate.replace(/%/g, '') : '';
          }
        }
      }
    }

    if (variables?.length) {
      configVariables.value = variables;
    }
  } else {
    if (props.scriptType !== type && props.scriptType === 'TEST_FUNCTIONALITY') {
      executionParams.value.thread.threads = '1';
      executionParams.value.iterations = '1';
    } else {
      executionParams.value.iterations = '';
    }

    if (props.scriptType !== type && props.scriptType === 'TEST_PERFORMANCE') {
      executionParams.value.thread.threads = '5000';
      executionParams.value.thread.rampUpInterval = '1';
      executionParams.value.thread.rampUpThreads = '100';
      executionParams.value.thread.resetAfterRamp = [];
      executionParams.value.duration = '50';
      durationUnit.value = 'min';
      isRampUpEnabled.value = true;
    }

    if (props.scriptType !== type && props.scriptType === 'TEST_STABILITY') {
      executionParams.value.thread.threads = '200';
      executionParams.value.duration = '30';
      durationUnit.value = 'min';
    }
  }

  if (plugin === 'Http') {
    if (task) {
      const { arguments: ar } = task;
      if (ar) {
        const { httpSetting, ignoreAssertions, updateTestResult, ...others } = ar;
        httpSettingsParams.value.ignoreAssertions = typeof ignoreAssertions === 'boolean' ? ignoreAssertions : props.scriptType !== 'TEST_FUNCTIONALITY';
        httpSettingsParams.value.updateTestResult = !!updateTestResult;

        if (httpSetting) {
          const { connectTimeout, readTimeout, retryNum, maxRedirects, ...settingOthers } = httpSetting;
          if (connectTimeout) {
            const strs = splitTime(connectTimeout);
            httpSettingsParams.value.httpSetting.connectTimeout = strs[0];
            httpSettingReadTimeoutUnit.value = strs[1];
          }

          if (readTimeout) {
            const strs = splitTime(readTimeout);
            httpSettingsParams.value.httpSetting.readTimeout = strs[0];
            httpSettingReadTimeoutUnit.value = strs[1];
          }

          httpSettingsParams.value.httpSetting.retryNum = retryNum || '';
          httpSettingsParams.value.httpSetting.maxRedirects = maxRedirects || '';
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
        webSocketSettingsParams.value.ignoreAssertions = typeof ignoreAssertions === 'boolean' ? ignoreAssertions : props.scriptType !== 'TEST_FUNCTIONALITY';
        webSocketSettingsParams.value.updateTestResult = !!updateTestResult;

        if (webSocketSetting) {
          const { connectTimeout, responseTimeout, reconnectionInterval, maxReconnections, ...settingOthers } = webSocketSetting;
          if (connectTimeout) {
            const strs = splitTime(connectTimeout);
            webSocketSettingsParams.value.webSocketSetting.connectTimeout = strs[0];
            webSocketSettingConnectTimeoutUnit.value = strs[1];
          }

          if (responseTimeout) {
            const strs = splitTime(responseTimeout);
            webSocketSettingsParams.value.webSocketSetting.responseTimeout = strs[0];
            webSocketSettingResponseTimeoutUnit.value = strs[1];
          }

          if (reconnectionInterval) {
            const strs = splitTime(reconnectionInterval);
            webSocketSettingsParams.value.webSocketSetting.reconnectionInterval = strs[0];
            reconnectionIntervalUnit.value = strs[1];
          }

          webSocketSettingsParams.value.webSocketSetting.maxReconnections = maxReconnections || '';

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
        jdbcSettingsParams.value.ignoreAssertions = typeof ignoreAssertions === 'boolean' ? ignoreAssertions : props.scriptType !== 'TEST_FUNCTIONALITY';
        jdbcSettingsParams.value.updateTestResult = !!updateTestResult;

        if (jdbcSetting) {
          const { type, driverClassName, isolation, jdbcUrl, username, password, pool, ...settingOthers } = jdbcSetting;

          jdbcSettingsParams.value.jdbcSetting.type = type;
          jdbcSettingsParams.value.jdbcSetting.driverClassName = driverClassName || '';
          jdbcSettingsParams.value.jdbcSetting.jdbcUrl = jdbcUrl || '';
          jdbcSettingsParams.value.jdbcSetting.isolation = isolation;
          jdbcSettingsParams.value.jdbcSetting.username = username || '';
          jdbcSettingsParams.value.jdbcSetting.password = password || '';
          if (pool) {
            isOpenPool.value = true;
            const { name, maximumPoolSize, minimumIdle, maxWaitTimeoutMillis } = pool;
            jdbcSettingsParams.value.pool.name = name;
            jdbcSettingsParams.value.pool.maximumPoolSize = maximumPoolSize || '';
            jdbcSettingsParams.value.pool.maxWaitTimeoutMillis = maxWaitTimeoutMillis || '';
            jdbcSettingsParams.value.pool.minimumIdle = minimumIdle || '';
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

// Component lifecycle and watchers
watch(() => props.scriptType, () => {
  resetParam();
  initializeExecutionParameters();
});

// Execution parameters state
const executionFormRef = ref();
const executionParams = ref({
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

/**
 * Handle threads input blur event
 */
const handleThreadsInputBlur = (e) => {
  executionParams.value.thread.threads = e.target.value;
  if (+executionParams.value.thread.rampUpThreads > e.target.value) {
    executionParams.value.thread.rampUpThreads = '';
  }
  if (+executionParams.value.thread.rampUpThreads > e.target.value) {
    executionParams.value.thread.rampUpThreads = '';
  }
  setExecParamErrNum();
};

/**
 * Handle iterations input blur event
 */
const handleIterationsInputBlur = (e) => {
  executionParams.value.iterations = e.target.value;
  setExecParamErrNum();
};

/**
 * Handle ramp up interval input blur event
 */
const handleRampUpIntervalInputBlur = (e) => {
  executionParams.value.thread.rampUpInterval = e.target.value;
  setExecParamErrNum();
};
/**
 * Handle ramp up threads input blur event
 */
const handleRampUpThreadsInputBlur = (e) => {
  executionParams.value.thread.rampUpThreads = e.target.value;
  setExecParamErrNum();
};
/**
 * Handle ramp down interval input blur event
 */
const handleRampDownIntervalInputBlur = (e) => {
  executionParams.value.thread.rampDownInterval = e.target.value;
  setExecParamErrNum();
};
/**
 * Handle ramp down threads input blur event
 */
const handleRampDownThreadsInputBlur = (e) => {
  executionParams.value.thread.rampDownThreads = e.target.value;
  setExecParamErrNum();
};

const durationUnit = ref('s');
/**
 * Handle duration input blur event
 */
const handleDurationInputBlur = (value) => {
  const strs = splitTime(value);
  executionParams.value.duration = strs[0];
  if (+executionParams.value.thread.rampUpInterval > +strs[0]) {
    executionParams.value.thread.rampUpInterval = '';
  }
  if (+executionParams.value.thread.rampDownInterval > +strs[0]) {
    executionParams.value.thread.rampDownInterval = '';
  }
  setExecParamErrNum();
};

/**
 * Handle duration change event
 */
const handleDurationChange = (value) => {
  const strs = splitTime(value);
  durationUnit.value = strs[1];
};

// Computed properties
const timeUnitMessage = computed(() => {
  switch (durationUnit.value) {
    case 'ms':
      return t('xcan_execSettingForm.timeUnit.milliseconds');
    case 's':
      return t('xcan_execSettingForm.timeUnit.seconds');
    case 'min':
      return t('xcan_execSettingForm.timeUnit.minutes');
    case 'h':
      return t('xcan_execSettingForm.timeUnit.hours');
    case 'd':
      return t('xcan_execSettingForm.timeUnit.days');
    default:
      return t('xcan_execSettingForm.timeUnit.seconds');
  }
});

// Ramp up and ramp down state
const isRampUpEnabled = ref(false); // Start with direct pressure by default
const isRampDownEnabled = ref(false); // End with direct pressure by default

/**
 * Handle ramp up enabled change
 */
const handleRampUpEnabledChange = (value) => {
  isRampUpEnabled.value = value;
  if (!isRampUpEnabled.value) {
    executionParams.value.thread.rampUpInterval = '';
    executionParams.value.thread.rampUpThreads = '';
  }
};

/**
 * Handle ramp down enabled change
 */
const handleRampDownEnabledChange = (value) => {
  isRampDownEnabled.value = value;
  if (!isRampDownEnabled.value) {
    executionParams.value.thread.rampDownInterval = '';
    executionParams.value.thread.rampDownThreads = '';
  }
};

// Global settings state
const globalSettingsFormRef = ref();
const globalSettingsParams = ref({
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

const shutdownTimeoutUnit = ref('s');
const shutdownTimeoutChange = (value) => {
  const strs = splitTime(value);
  globalSettingsParams.value.shutdownTimeout = strs[0];
  shutdownTimeoutUnit.value = strs[1];
};

const reportIntervalUnit = ref('s');
const reportIntervalChange = (value) => {
  const strs = splitTime(value);
  globalSettingsParams.value.reportInterval = strs[0];
  reportIntervalUnit.value = strs[1];
};

const startupTimeoutUnit = ref('s');
const startupTimeoutChange = (value) => {
  const strs = splitTime(value);
  globalSettingsParams.value.startupTimeout = strs[0];
  startupTimeoutUnit.value = strs[1];
};

const runnerSetupTimeoutUnit = ref('s');
const runnerSetupTimeoutChange = (value) => {
  const strs = splitTime(value);
  globalSettingsParams.value.runnerSetupTimeout = strs[0];
  runnerSetupTimeoutUnit.value = strs[1];
};

// HTTP settings state
const httpSettingsFormRef = ref();
const httpSettingsParams = ref({
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
  httpSettingsParams.value.httpSetting.connectTimeout = strs[0];
  httpSettingConnectTimeoutUnit.value = strs[1];
};
const httpReadTimeoutChange = (value) => {
  const strs = splitTime(value);
  httpSettingsParams.value.httpSetting.readTimeout = strs[0];
  httpSettingReadTimeoutUnit.value = strs[1];
};

const extendParams = ref({
  extend: [{ key: '', value: '' }]
});

// WebSocket settings state
const webSocketSettingsFormRef = ref();
const webSocketSettingsParams = ref({
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
  webSocketSettingsParams.value.webSocketSetting.connectTimeout = strs[0];
  webSocketSettingConnectTimeoutUnit.value = strs[1];
};
const webSocletResponseTimeoutChange = (value) => {
  const strs = splitTime(value);
  webSocketSettingsParams.value.webSocketSetting.responseTimeout = strs[0];
  webSocketSettingResponseTimeoutUnit.value = strs[1];
};
const webSocletReconnectionIntervalChange = (value) => {
  const strs = splitTime(value);
  webSocketSettingsParams.value.webSocketSetting.reconnectionInterval = strs[0];
  reconnectionIntervalUnit.value = strs[1];
};

// JDBC settings state
const jdbcSettingsFormRef = ref();
const jdbcSettingsParams = ref({
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
  jdbcSettingsParams.value.pool.maxWaitTimeoutMillis = strs[0];
  maxWaitTimeoutMillisUnit.value = strs[1];
};

const keyValidator = async (_rule: Rule, value: string) => {
  if (!value) {
    return Promise.reject(new Error(t('xcan_execSettingForm.enterParameterName')));
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
      return Promise.reject(new Error(t('xcan_execSettingForm.variableNameExists')));
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

    if (!executionParams.value.duration) {
      return Promise.reject(new Error(t('xcan_execSettingForm.enterIterationsOrDuration')));
    } else {
      return Promise.resolve();
    }
  } else {
    executionFormRef.value.clearValidate('duration');
    if (['TEST_FUNCTIONALITY'].includes(props.scriptType)) {
      if (+value < 1 || +value > 10) {
        return Promise.reject(new Error(t('xcan_execSettingForm.functionalTestIterationsRange')));
      }
      return Promise.resolve();
    }

    if (props.addType === 'expr') {
      if (+value < 1 || +value > 100000) {
        return Promise.reject(new Error(t('xcan_execSettingForm.experienceIterationsRange')));
      }
    } else {
      if (+value < 1 || +value > 100000000000) {
        return Promise.reject(new Error(t('xcan_execSettingForm.iterationsRange')));
      }
    }

    return Promise.resolve();
  }
};

const threadsValidator = async (_rule: Rule, value: string) => {
  if (!value) {
    return Promise.reject(new Error(t('xcan_execSettingForm.enterThreadCount')));
  } else {
    if (props.scriptType === 'TEST_FUNCTIONALITY') {
      return Promise.resolve();
    }

    if (['MOCK_DATA', 'MOCK_APIS'].includes(props.scriptType)) {
      if (+value > 1000 || +value < 1) {
        return Promise.reject(new Error(t('xcan_execSettingForm.mockDataTaskThreadRange')));
      }

      return Promise.resolve();
    }

    if (+value > 10000 || +value < 1) {
      return Promise.reject(new Error(t('xcan_execSettingForm.taskThreadRange')));
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
    if (!executionParams.value.iterations) {
      return Promise.reject(new Error(t('xcan_execSettingForm.enterDurationOrIterations')));
    } else {
      return Promise.resolve();
    }
  } else {
    if (props.addType === 'expr') {
      if (durationUnit.value === 'min' && +value > 30) {
        return Promise.reject(new Error(t('xcan_execSettingForm.experienceExecutionMaxDuration')));
      }
      if (durationUnit.value === 's' && +value > 1800) {
        return Promise.reject(new Error(t('xcan_execSettingForm.experienceExecutionMaxDuration')));
      }
    }
    executionFormRef.value.clearValidate('iterations');
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
  await executionFormRef.value.validate().then(
    () => {
      execParamErrNum.value = 0;
    }
  ).catch(
    (error) => {
      execParamErrNum.value = error.errorFields?.length || 0;
      errors['execut-form'] = error;
      valid = false;
    });
  await globalSettingsFormRef.value.validate().then(
    () => {
      globalParamErrNum.value = 0;
    }
  ).catch(
    (error) => {
      globalParamErrNum.value = error.errorFields?.length || 0;
      errors['global-form'] = error;
      valid = false;
    });
  if (pluginType.value === 'Http') {
    await httpSettingsFormRef.value.validate().then().catch(
      (error) => {
        errors['http-form'] = error;
        valid = false;
      });
  }
  if (pluginType.value === 'WebSocket') {
    await webSocketSettingsFormRef.value.validate().then().catch(
      (error) => {
        errors['websocket-form'] = error;
        valid = false;
      });
  }
  if (pluginType.value === 'Jdbc') {
    await jdbcSettingsFormRef.value.validate(
      () => {
        jdbcParamErrNum.value = 0;
      }
    ).then().catch(
      (error) => {
        jdbcParamErrNum.value = error.errorFields?.length || 0;
        errors['jdbc-form'] = error;
        valid = false;
      });
  }
  return { valid, errors };
};

const variablesRef = ref();
/**
 * Get form data for submission
 */
const getData = () => {
  const _variables = variablesRef.value
    ? variablesRef.value.tableData.map(item => {
      let obj:any = {
        enabled: item.enabled,
        name: item.name,
        value: item.value
      };

      if (item.description) {
        obj = {
          ...obj,
          description: item.description
        };
      }
      return obj;
    })
    : [];

  let _thread:any = {
    threads: executionParams.value.thread.threads
  };

  if (props.scriptType !== 'TEST_FUNCTIONALITY') {
    if (+executionParams.value.thread.rampUpInterval > 0 && executionParams.value.thread.rampUpThreads) {
      _thread = {
        ..._thread,
        rampUpInterval: executionParams.value.thread.rampUpInterval + durationUnit.value,
        rampUpThreads: executionParams.value.thread.rampUpThreads
      };
    }
    if (+executionParams.value.thread.rampDownInterval > 0 && executionParams.value.thread.rampDownThreads) {
      _thread = {
        ..._thread,
        rampDownInterval: executionParams.value.thread.rampDownInterval + durationUnit.value,
        rampDownThreads: executionParams.value.thread.rampDownThreads
      };
    }

    if ((_thread?.rampUpThreads || _thread?.rampDownThreads) && executionParams.value.thread.resetAfterRamp?.length) {
      _thread = {
        ..._thread,
        resetAfterRamp: executionParams.value.thread.resetAfterRamp[0]
      };
    }
  }

  let params:any = {
    configuration: {
      ...configurationtends.value,
      thread: _thread
    }
  };

  if (+executionParams.value.duration > 0 && props.scriptType !== 'TEST_FUNCTIONALITY' && props.scriptType !== 'MOCK_DATA') {
    params = {
      ...params,
      configuration: {
        ...params.configuration,
        duration: executionParams.value.duration + durationUnit.value
      }
    };
  }

  if (executionParams.value.iterations) {
    params = {
      ...params,
      configuration: {
        ...params.configuration,
        iterations: executionParams.value.iterations
      }
    };
  }

  if (globalSettingsParams.value.nodeSelectors.num) {
    params = {
      ...params,
      configuration: {
        ...params.configuration,
        lang: globalSettingsParams.value.lang
      }
    };
  }

  if (globalSettingsParams.value.startMode) {
    params = {
      ...params,
      configuration: {
        ...params.configuration,
        startMode: globalSettingsParams.value.startMode
      }
    };

    if (globalSettingsParams.value.startMode === 'TIMING') {
      params = {
        ...params,
        configuration: {
          ...params.configuration,
          startAtDate: globalSettingsParams.value.startAtDate
        }
      };
    }
  }

  if (globalSettingsParams.value.lang) {
    params = {
      ...params,
      configuration: {
        ...params.configuration,
        lang: globalSettingsParams.value.lang
      }
    };
  }

  if (globalSettingsParams.value.priority) {
    params = {
      ...params,
      configuration: {
        ...params.configuration,
        priority: globalSettingsParams.value.priority
      }
    };
  }

  if (+globalSettingsParams.value.reportInterval > 0) {
    params = {
      ...params,
      configuration: {
        ...params.configuration,
        reportInterval: globalSettingsParams.value.reportInterval + reportIntervalUnit.value
      }
    };
  }
  if (+globalSettingsParams.value.shutdownTimeout > 0) {
    params = {
      ...params,
      configuration: {
        ...params.configuration,
        shutdownTimeout: globalSettingsParams.value.shutdownTimeout + shutdownTimeoutUnit.value
      }
    };
  }
  if (+globalSettingsParams.value.startupTimeout > 0) {
    params = {
      ...params,
      configuration: {
        ...params.configuration,
        startupTimeout: globalSettingsParams.value.startupTimeout + startupTimeoutUnit.value
      }
    };
  }
  if (+globalSettingsParams.value.runnerSetupTimeout > 0) {
    params = {
      ...params,
      configuration: {
        ...params.configuration,
        runnerSetupTimeout: globalSettingsParams.value.runnerSetupTimeout + runnerSetupTimeoutUnit.value
      }
    };
  }

  if (globalSettingsParams.value.nodeSelectors.num) {
    params = {
      ...params,
      configuration: {
        ...params.configuration,
        nodeSelectors: {
          ...params.configuration.nodeSelectors,
          num: globalSettingsParams.value.nodeSelectors.num
        }
      }
    };
  }

  if (globalSettingsParams.value.nodeSelectors.availableNodeIds?.length) {
    params = {
      ...params,
      configuration: {
        ...params.configuration,
        nodeSelectors: {
          ...params.configuration.nodeSelectors,
          availableNodeIds: globalSettingsParams.value.nodeSelectors.availableNodeIds
        }
      }
    };
  }

  if (globalSettingsParams.value.nodeSelectors.appNodeIds?.length) {
    params = {
      ...params,
      configuration: {
        ...params.configuration,
        nodeSelectors: {
          ...params.configuration.nodeSelectors,
          appNodeIds: globalSettingsParams.value.nodeSelectors.appNodeIds
        }
      }
    };
  }

  const _enabled = globalSettingsParams.value.nodeSelectors.enabled;
  let _strategy:any = null;

  if (_enabled) {
    _strategy = {
      ..._strategy,
      idleRateEnabled: globalSettingsParams.value.nodeSelectors.idleRateEnabled,
      lastExecuted: globalSettingsParams.value.nodeSelectors.lastExecuted,
      specEnabled: globalSettingsParams.value.nodeSelectors.specEnabled
    };

    if (globalSettingsParams.value.nodeSelectors.maxTaskNum) {
      _strategy = {
        ..._strategy,
        maxTaskNum: globalSettingsParams.value.nodeSelectors.maxTaskNum
      };
    }

    if (_strategy.idleRateEnabled) {
      if (globalSettingsParams.value.nodeSelectors.cpuIdleRate) {
        _strategy = {
          ..._strategy,
          cpuIdleRate: globalSettingsParams.value.nodeSelectors.cpuIdleRate
        };
      }
      if (globalSettingsParams.value.nodeSelectors.diskIdleRate) {
        _strategy = {
          ..._strategy,
          diskIdleRate: globalSettingsParams.value.nodeSelectors.diskIdleRate
        };
      }
      if (globalSettingsParams.value.nodeSelectors.memoryIdleRate) {
        _strategy = {
          ..._strategy,
          memoryIdleRate: globalSettingsParams.value.nodeSelectors.memoryIdleRate
        };
      }
    }

    if (_strategy.specEnabled) {
      if (globalSettingsParams.value.nodeSelectors.cpuSpec) {
        _strategy = {
          ..._strategy,
          cpuSpec: globalSettingsParams.value.nodeSelectors.cpuSpec
        };
      }
      if (globalSettingsParams.value.nodeSelectors.diskSpec) {
        _strategy = {
          ..._strategy,
          diskSpec: globalSettingsParams.value.nodeSelectors.diskSpec
        };
      }
      if (globalSettingsParams.value.nodeSelectors.memorySpec) {
        _strategy = {
          ..._strategy,
          memorySpec: globalSettingsParams.value.nodeSelectors.memorySpec
        };
      }
    }
  }

  if (_strategy) {
    params = {
      ...params,
      configuration: {
        ...params.configuration,
        nodeSelectors: {
          ...params.configuration.nodeSelectors,
          strategy: _strategy
        }
      }
    };
  }

  let _onError:any = {};
  if (globalSettingsParams.value.onError.action) {
    _onError = {
      action: globalSettingsParams.value.onError.action
    };
  }

  if (globalSettingsParams.value.onError.sampleError) {
    _onError = {
      ..._onError,
      sampleError: globalSettingsParams.value.onError.sampleError,
      sampleErrorNum: globalSettingsParams.value.onError.sampleErrorNum
    };
  } else {
    _onError = {
      ..._onError,
      sampleError: globalSettingsParams.value.onError.sampleError
    };
  }

  if (Object.keys(_onError).length) {
    params = {
      ...params,
      configuration: {
        ...params.configuration,
        onError: _onError
      }
    };
  }

  if (_variables?.length) {
    params = {
      ...params,
      configuration: {
        ...params.configuration,
        variables: _variables
      }
    };
  }

  let _arguments:any = {};
  if (pluginType.value === 'Http') {
    let _setting:any = { ...pluginSettingExtends.value };
    if (extendParams.value.extend?.length > 0) {
      extendParams.value.extend.forEach(item => {
        if (item.key && item.value) {
          _arguments[item.key] = item.value;
        }
      });
    }
    _arguments = {
      ..._arguments,
      ignoreAssertions: httpSettingsParams.value.ignoreAssertions,
      updateTestResult: httpSettingsParams.value.updateTestResult
    };

    if (+httpSettingsParams.value.httpSetting.connectTimeout > 0) {
      _setting = {
        ..._setting,
        connectTimeout: httpSettingsParams.value.httpSetting.connectTimeout + httpSettingConnectTimeoutUnit.value
      };
    }
    if (+httpSettingsParams.value.httpSetting.readTimeout > 0) {
      _setting = {
        ..._setting,
        readTimeout: httpSettingsParams.value.httpSetting.readTimeout + httpSettingReadTimeoutUnit.value
      };
    }
    if (httpSettingsParams.value.httpSetting.retryNum) {
      _setting = {
        ..._setting,
        retryNum: httpSettingsParams.value.httpSetting.retryNum
      };
    }
    if (httpSettingsParams.value.httpSetting.maxRedirects) {
      _setting = {
        ..._setting,
        maxRedirects: httpSettingsParams.value.httpSetting.maxRedirects
      };
    }

    if (Object.keys(_setting)?.length) {
      _arguments = {
        ..._arguments,
        httpSetting: _setting
      };
    }
  }

  if (pluginType.value === 'WebSocket') {
    let _setting:any = { ...pluginSettingExtends.value };
    if (extendParams.value.extend?.length > 0) {
      extendParams.value.extend.forEach(item => {
        if (item.key && item.value) {
          _arguments[item.key] = item.value;
        }
      });
    }

    _arguments = {
      ..._arguments,
      ignoreAssertions: webSocketSettingsParams.value.ignoreAssertions,
      updateTestResult: webSocketSettingsParams.value.updateTestResult
    };

    if (+webSocketSettingsParams.value.webSocketSetting.maxReconnections) {
      _setting = {
        ..._setting,
        maxReconnections: webSocketSettingsParams.value.webSocketSetting.maxReconnections
      };
    }

    if (+webSocketSettingsParams.value.webSocketSetting.connectTimeout > 0) {
      _setting = {
        ..._setting,
        connectTimeout: webSocketSettingsParams.value.webSocketSetting.connectTimeout + webSocketSettingConnectTimeoutUnit.value
      };
    }
    if (+webSocketSettingsParams.value.webSocketSetting.responseTimeout > 0) {
      _setting = {
        ..._setting,
        responseTimeout: webSocketSettingsParams.value.webSocketSetting.responseTimeout + webSocketSettingResponseTimeoutUnit.value
      };
    }
    if (+webSocketSettingsParams.value.webSocketSetting.reconnectionInterval > 0) {
      _setting = {
        ..._setting,
        reconnectionInterval: webSocketSettingsParams.value.webSocketSetting.reconnectionInterval + reconnectionIntervalUnit.value
      };
    }

    if (Object.keys(_setting)?.length) {
      _arguments = {
        ..._arguments,
        webSocketSetting: _setting
      };
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

    const { ignoreAssertions, updateTestResult, jdbcSetting, pool } = jdbcSettingsParams.value;
    const { type, driverClassName, jdbcUrl, username, password, isolation } = jdbcSetting;
    const { name, maximumPoolSize, minimumIdle, maxWaitTimeoutMillis } = pool;
    _arguments = {
      ..._arguments,
      ignoreAssertions: ignoreAssertions,
      updateTestResult: updateTestResult,
      jdbcSetting: {
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
      }
    };

    if (password) {
      _arguments = {
        ..._arguments,
        jdbcSetting: {
          ..._arguments.jdbcSetting,
          password: password
        }
      };
    }

    if (isolation) {
      _arguments = {
        ..._arguments,
        jdbcSetting: {
          ..._arguments.jdbcSetting,
          isolation: isolation
        }
      };
    }

    if (maxWaitTimeoutMillis) {
      _arguments = {
        ..._arguments,
        jdbcSetting: {
          ..._arguments.jdbcSetting,
          pool: {
            ..._arguments.jdbcSetting.pool,
            maxWaitTimeoutMillis: maxWaitTimeoutMillis
          }
        }
      };
    }
  }

  if (props.scriptType !== 'MOCK_DATA' && Object.keys(_arguments)?.length) {
    params = {
      ...params,
      arguments: _arguments
    };
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

const durationMax = computed(() => props.addType === 'expr' ? 1800 : 86400);

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

  initializeExecutionParameters();
}, {
  immediate: true
});

/**
 * Reset all form parameters to default values
 */
const resetParam = () => {
  executionParams.value = {
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
  globalSettingsParams.value = {
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
  httpSettingsParams.value = {
    ignoreAssertions: undefined,
    updateTestResult: false,
    httpSetting: {
      connectTimeout: '',
      readTimeout: '',
      retryNum: '',
      maxRedirects: ''
    }
  };
  webSocketSettingsParams.value = {
    ignoreAssertions: undefined,
    updateTestResult: false,
    webSocketSetting: {
      connectTimeout: '',
      responseTimeout: '',
      maxReconnections: '',
      reconnectionInterval: ''
    }
  };
  jdbcSettingsParams.value = {
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
        isRampUpEnabled.value = false;
  isRampDownEnabled.value = false;
};

watch(() => executionParams.value, () => {
  updateChartData();
}, {
  deep: true
});

const activeKey = ref<string[]>(['exec']);
const setActiveKey = (key:string) => {
  if (activeKey.value.includes(key)) {
    activeKey.value = activeKey.value.filter(item => item !== key);
  } else {
    activeKey.value.push(key);
  }
};

/**
 * Open execution parameters section
 */
const openExecutParames = () => {
  if (!activeKey.value.includes('exec')) {
    setActiveKey('exec');
  }
};

/**
 * Open global parameters section
 */
const openGlobalParames = () => {
  if (!activeKey.value.includes('advanced')) {
    setActiveKey('advanced');
    nodeParamsOpen.value = true;
    errorParamsOpen.value = true;
    reporteParamsOpen.value = true;
    stopParamsOpen.value = true;
  }
};

/**
 * Open plugin parameters section
 */
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

watch(() => isRampUpEnabled.value, (newValue) => {
  updateChartData();
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

watch(() => isRampDownEnabled.value, (newValue) => {
  updateChartData();
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

  if (superchargedRef.value) {
    erd.removeListener(superchargedRef.value, null);
  }

  erd = null;
});

onMounted(() => {
  updateChartData();
});

const updateRsetAfterRamp = (_value:boolean[]) => {
  const value = _value.length === 1 ? _value[0] : _value[1];
  const _resetAfterRamp = executionParams.value.thread.resetAfterRamp;
  if (_resetAfterRamp?.length) {
    if (_resetAfterRamp[0] === value) {
      executionParams.value.thread.resetAfterRamp = [];
    } else {
      executionParams.value.thread.resetAfterRamp = [value];
    }
  } else {
    executionParams.value.thread.resetAfterRamp = [value];
  }
};

// Form validation state
const execParamErrNum = ref(0);

/**
 * Set execution parameter error numbers for validation
 */
const setExecParamErrNum = () => {
  executionFormRef.value.validate().then(() => {
    execParamErrNum.value = 0;
  }).catch(
    (error) => {
      execParamErrNum.value = error.errorFields?.length || 0;
    });
};

const globalParamErrNum = ref(0);
/**
 * Set global parameter error numbers for validation
 */
const setGlobalParamErrNum = () => {
  globalSettingsFormRef.value.validate().then(
    () => {
      globalParamErrNum.value = 0;
    }).catch(
    (error) => {
      globalParamErrNum.value = error.errorFields?.length || 0;
    });
};

const startAtDateChange = (value) => {
  globalSettingsParams.value.startAtDate = value;
  setGlobalParamErrNum();
};

const jdbcParamErrNum = ref(0);
/**
 * Set plugin parameter error numbers for validation
 */
const setPluginParamErrNum = () => {
  jdbcSettingsFormRef.value.validate().then(
    () => {
      jdbcParamErrNum.value = 0;
    }).catch(
    (error) => {
      jdbcParamErrNum.value = error.errorFields?.length || 0;
    });
};

const errTotal = computed(() => {
  return execParamErrNum.value + globalParamErrNum.value + jdbcParamErrNum.value;
});

// Component exposure
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
          <div class="param-parent-title">
            <Icon icon="icon-zhihangcanshu" class="text-3.5 mr-2" />
            <span>{{ t('xcan_execSettingForm.executionParameters') }}</span>
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
              <IconRequired />{{ t('xcan_execSettingForm.concurrencyThreads') }}
            </div>
            <template v-if="!['MOCK_APIS'].includes(props.scriptType)">
              <div class="h-7 whitespace-nowrap mb-5 pl-1.75 iterations">{{ t('xcan_execSettingForm.iterations') }}</div>
            </template>
            <template v-if="!['MOCK_DATA', 'TEST_FUNCTIONALITY'].includes(props.scriptType)">
              <div class="h-7 whitespace-nowrap mb-5 pl-1.75 duration">{{ t('xcan_execSettingForm.executionDuration') }}</div>
              <div class="h-7 whitespace-nowrap thread-rampUpInterval thread-rampUpThreads pl-1.75" :style="{ 'margin-bottom': superchargedRefHeight + 'px' }">
                {{ t('xcan_execSettingForm.initialRampUp') }}
              </div>
              <div class="h-7 whitespace-nowrap thread-rampDownInterval thread-rampDownThreads pl-1.75" :style="{ 'margin-bottom': stressRelieverRefHeight + 'px' }">
                {{ t('xcan_execSettingForm.finalRampDown') }}
              </div>
              <template v-if="isRampUpEnabled || isRampDownEnabled">
                <div class="h-7 whitespace-nowrap thread-rampDownInterval pl-1.75">
                  {{ t('xcan_execSettingForm.resetSamplingResults') }}
                </div>
              </template>
            </template>
          </div>
          <Form
            ref="executionFormRef"
            class="flex-1"
            :model="executParams"
            :colon="false"
            layout="horizontal">
            <FormItem
              class="pr-5 relative"
              :name="['thread', 'threads']"
              :rules="{ required: true, validator: threadsValidator, trigger: ['change', 'blur'] }">
              <Input
                :value="executionParams.thread.threads"
                dataType="number"
                :min="1"
                :max="threadsMax"
                :disabled="props.scriptType ==='TEST_FUNCTIONALITY'"
                :placeholder="t('xcan_execSettingForm.maxThreads')"
                @blur="handleThreadsInputBlur" />
              <Tooltip
                :title="t('xcan_execSettingForm.maxThreadsTooltip')"
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
                  :value="executionParams.iterations"
                  dataType="number"
                  :min="1"
                  :max="props.addType === 'expr' ? 100000 : 100000000000"
                  size="small"
                  :placeholder="t('xcan_execSettingForm.iterations')"
                  @blur="handleIterationsInputBlur" />
                <Tooltip
                  :title="t('xcan_execSettingForm.iterationsTooltip')"
                  placement="topLeft"
                  arrowPointAtCenter
                  :overlayStyle="{ 'max-width': '600px' }">
                  <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
                </Tooltip>
              </FormItem>
            </template>
            <template v-if="!['MOCK_DATA','TEST_FUNCTIONALITY'].includes(props.scriptType)">
              <FormItem
                class="relative pr-5"
                name="duration"
                :rules="{ required: false, validator: durationValidator, trigger: ['change', 'blur'] }">
                <ShortDuration
                  :value="executionParams.duration + durationUnit"
                  size="small"
                  class="w-full"
                  :max="durationMax"
                  :inputProps="{placeholder: t('xcan_execSettingForm.executionDuration')}"
                  :selectProps="durationSelectProps"
                  @blur="handleDurationInputBlur"
                  @change="handleDurationChange" />
                <Tooltip
                  :title="t('xcan_execSettingForm.executionDurationTooltip')"
                  placement="topLeft"
                  arrowPointAtCenter
                  :overlayStyle="{ 'max-width': '600px' }">
                  <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
                </Tooltip>
              </FormItem>
              <FormItem class="h-7 leading-7">
                <RadioGroup
                  :value="isRampUpEnabled"
                  class="space-x-6 -ml-5 mt-0.5"
                  :disabled="executionParams.duration === '0'"
                  :options="[{ value: false, message: t('xcan_execSettingForm.directPressure') }, { value: true, message: t('xcan_execSettingForm.gradualPressure') }]"
                  @change="handleRampUpEnabledChange">
                </RadioGroup>
              </FormItem>
              <template v-if="isRampUpEnabled">
                <div ref="superchargedRef" class="flex  flex-wrap -mt-3 supercharged">
                  <div class="flex text-3 space-x-1 mr-2">
                    <div class="h-7 leading-7">{{ t('xcan_execSettingForm.every') }}</div>
                    <FormItem
                      class="flex-1"
                      :name="['thread', 'rampUpInterval']"
                      :rules="{ required: isRampUpEnabled, message: t('xcan_execSettingForm.pleaseEnterRampUpInterval'), trigger: ['change', 'blur'] }">
                      <Input
                        v-model:value="executionParams.thread.rampUpInterval"
                        class="w-24"
                        :min="0"
                        :max="+executionParams.duration"
                        dataType="number"
                        :placeholder="t('xcan_execSettingForm.rampUpInterval')"
                        @blur="handleRampUpIntervalInputBlur" />
                    </FormItem>
                    <div class="h-7 leading-7">{{ timeUnitMessage }}</div>
                  </div>
                  <div class="flex text-3 space-x-1">
                    <div class="h-7 leading-7">{{ t('xcan_execSettingForm.increase') }}</div>
                    <FormItem
                      class="flex-1"
                      :name="['thread', 'rampUpThreads']"
                      :rules="{ required: isRampUpEnabled, message: t('xcan_execSettingForm.pleaseEnterRampUpThreads'), trigger: ['change', 'blur'] }">
                      <Input
                        :value="executionParams.thread.rampUpThreads"
                        dataType="number"
                        class="w-24"
                        :min="0"
                        :max="+executionParams.thread.threads"
                        size="small"
                        :placeholder="t('xcan_execSettingForm.rampUpThreads')"
                        @blur="handleRampUpThreadsInputBlur" />
                    </FormItem>
                    <div class="h-7 leading-7 flex items-center">
                      {{ t('xcan_execSettingForm.concurrency') }}
                      <Tooltip
                        placement="topLeft"
                        arrowPointAtCenter
                        :overlayStyle="{ 'max-width': '600px' }">
                        <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
                        <template #title>
                          <div class="flex">
                            <div class="mr-2">
                              <div>{{ t('xcan_execSettingForm.rampUpInterval') }}<Colon /></div>
                              <div>{{ t('xcan_execSettingForm.rampUpThreads') }}<Colon /></div>
                            </div>
                            <div>
                              <div>{{ t('xcan_execSettingForm.rampUpTooltip1') }}</div>
                              <div>{{ t('xcan_execSettingForm.rampUpTooltip2') }}</div>
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
                  :value="isRampDownEnabled"
                  class="space-x-6 -ml-5 mt-0.5"
                  :disabled="executionParams.duration === '0'"
                  :options="[{ value: false, message: t('xcan_execSettingForm.directReduction') }, { value: true, message: t('xcan_execSettingForm.gradualReduction') }]"
                  @change="handleRampDownEnabledChange">
                </RadioGroup>
              </FormItem>
              <template v-if="isRampDownEnabled">
                <div ref="stressRelieverRef" class="flex  flex-wrap -mt-3 stress-reliever">
                  <div class="flex text-3 space-x-1 mr-2">
                    <div class="h-7 leading-7">{{ t('xcan_execSettingForm.every') }}</div>
                    <FormItem
                      class="flex-1"
                      :name="['thread', 'rampDownInterval']"
                      :rules="{ required: stressReliever, message: t('xcan_execSettingForm.pleaseEnterRampDownInterval'), trigger: ['change', 'blur'] }">
                      <Input
                        :value="executionParams.thread.rampDownInterval"
                        class="w-24"
                        :min="0"
                        :max="+executionParams.duration"
                        dataType="number"
                        :placeholder="t('xcan_execSettingForm.rampDownInterval')"
                        @blur="handleRampDownIntervalInputBlur" />
                    </FormItem>
                    <div class="h-7 leading-7">{{ timeUnitMessage }}</div>
                  </div>
                  <div class="flex text-3 space-x-1">
                    <div class="h-7 leading-7">{{ t('xcan_execSettingForm.decrease') }}</div>
                    <FormItem
                      class="flex-1"
                      :name="['thread', 'rampDownThreads']"
                      :rules="{ required: stressReliever, message: t('xcan_execSettingForm.pleaseEnterRampDownThreads'), trigger: ['change', 'blur'] }">
                      <Input
                        :value="executionParams.thread.rampDownThreads"
                        dataType="number"
                        class="w-24"
                        :min="0"
                        :max="+executionParams.thread.threads"
                        size="small"
                        :placeholder="t('xcan_execSettingForm.rampDownThreads')"
                        @blur="handleRampDownThreadsInputBlur" />
                    </FormItem>
                    <div class="h-7 leading-7 flex items-center">
                      {{ t('xcan_execSettingForm.concurrency') }}
                      <Tooltip
                        placement="topLeft"
                        arrowPointAtCenter
                        :overlayStyle="{ 'max-width': '600px' }">
                        <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
                        <template #title>
                          <div class="flex">
                            <div class="mr-2">
                              <div>{{ t('xcan_execSettingForm.rampDownInterval') }}<Colon /></div>
                              <div>{{ t('xcan_execSettingForm.rampDownThreads') }}<Colon /></div>
                            </div>
                            <div>
                              <div>{{ t('xcan_execSettingForm.rampDownTooltip1') }}</div>
                              <div>{{ t('xcan_execSettingForm.rampDownTooltip2') }}</div>
                            </div>
                          </div>
                        </template>
                      </Tooltip>
                    </div>
                  </div>
                </div>
              </template>
              <template v-if="isRampUpEnabled || isRampDownEnabled">
                <FormItem class="h-7 leading-7 pr-5 relative" :name="['thread', 'resetAfterRamp']">
                  <CheckboxGroup :value="executionParams.thread.resetAfterRamp" @change="updateRsetAfterRamp($event)">
                    <Checkbox :value="true">{{ t('status.yes') }}</Checkbox>
                    <Checkbox :value="false">{{ t('status.no') }}</Checkbox>
                  </CheckboxGroup>
                  <Tooltip
                    :title="t('xcan_execSettingForm.resetSamplingResultsTooltip')"
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
        <template v-if="!['MOCK_DATA', 'TEST_FUNCTIONALITY'].includes(props.scriptType) && (isRampUpEnabled || isRampDownEnabled) ">
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
          <div class="param-parent-title">
            <Icon icon="icon-quanjupeizhi" class="text-3.5 mr-2" />
            <span>{{ t('xcan_execSettingForm.advancedParameters') }}</span>
          </div>
        </Badge>
      </template>
      <template #extra>
        <Arrow :open="activeKey.includes('advanced')" @click="setActiveKey('advanced')" />
      </template>
      <div class="flex max-w-150 pl-3.5 text-3">
        <div class="mr-2.5 leading-7 w-35 flex-none global-form">
          <div class="h-7 whitespace-nowrap mb-5 startMode pl-1.75">
            {{ t('xcan_execSettingForm.startupMode') }}
          </div>
          <div v-if="globalSettingsParams.startMode === 'TIMING'" class="h-7 whitespace-nowrap mb-5 -mt-3 startAtDate"></div>
          <div class="h-7 whitespace-nowrap mb-5 priority pl-1.75">
            {{ t('common.priority') }}
          </div>
          <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-1.75 font-medium ">
            {{ t('xcan_execSettingForm.executionNodes') }}
            <Arrow
              :open="nodeParamsOpen"
              class="ml-2"
              @click="openNodeParams" />
          </div>
          <div
            :class="nodeParamsOpen ? 'open-params' : 'stop-params'"
            class="overflow-hidden transition-all"
            style="transition-duration: 400ms;">
            <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 nodeSelectors-num">{{ t('xcan_execSettingForm.executionNodeCount') }}</div>
            <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 nodeSelectors-availableNodeIds">{{ t('xcan_execSettingForm.availableExecutionNodes') }}</div>
            <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 nodeSelectors-appNodeIds">{{ t('xcan_execSettingForm.applicationNodes') }}</div>
            <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 nodeSelectors-enabled">{{ t('xcan_execSettingForm.nodeSelectionStrategy') }}</div>
            <div
              :class="globalSettingsParams.nodeSelectors.enabled ? 'open-params' : 'stop-params'"
              class="overflow-hidden transition-all"
              style="transition-duration: 400ms;">
              <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 nodeSelectors-maxTaskNum">{{ t('xcan_execSettingForm.maxTaskCount') }}</div>
              <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 nodeSelectors-lastExecuted">{{ t('xcan_execSettingForm.lastExecutedNode') }}</div>
              <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25">{{ t('xcan_execSettingForm.minimumSpecificationNodes') }}</div>
              <div
                v-if="globalSettingsParams.nodeSelectors.specEnabled"
                class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 nodeSelectors-specEnabled">
                {{ t('xcan_execSettingForm.minimumSpecification') }}
              </div>
              <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25">{{ t('xcan_execSettingForm.lowestIdleRateNodes') }}</div>
              <div
                v-if="globalSettingsParams.nodeSelectors.idleRateEnabled"
                class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 nodeSelectors-idleRateEnabled">
                {{ t('xcan_execSettingForm.lowestIdleRate') }}
              </div>
            </div>
          </div>
          <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-1.75  font-medium">
            {{ t('xcan_execSettingForm.errorHandling') }}
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
              {{ t('xcan_execSettingForm.errorHandlingMethod') }}
            </div>
            <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 onError-sampleError">{{ t('xcan_execSettingForm.sampleErrorContent') }}</div>
            <div
              :class="globalSettingsParams.onError.sampleError ? 'open-params' : 'stop-params'"
              class="overflow-hidden transition-all"
              style="transition-duration: 400ms;">
              <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 onError-sampleErrorNum">{{ t('xcan_execSettingForm.sampleErrorContentCount') }}</div>
            </div>
          </div>
          <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-1.75  font-medium">
            {{ t('xcan_execSettingForm.reportConfiguration') }}
            <Arrow
              :open="reporteParamsOpen"
              class="ml-2"
              @click="openReportParams" />
          </div>
          <div
            :class="reporteParamsOpen ? 'open-params' : 'stop-params'"
            class="overflow-hidden transition-all"
            style="transition-duration: 400ms;">
            <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 reportInterval">{{ t('xcan_execSettingForm.samplingInterval') }}</div>
            <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 lang">{{ t('xcan_execSettingForm.language') }}</div>
          </div>
          <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-1.75  font-medium">
            {{ t('xcan_execSettingForm.startupConfiguration') }}
            <Arrow
              :open="runnerParamsOpen"
              class="ml-2"
              @click="openRunnerParams" />
          </div>
          <div
            :class="runnerParamsOpen ? 'open-params' : 'stop-params'"
            class="overflow-hidden transition-all"
            style="transition-duration: 400ms;">
            <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 reportInterval">{{ t('xcan_execSettingForm.startupRunnerTimeout') }}</div>
            <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 lang">{{ t('xcan_execSettingForm.samplingInitializationTimeout') }}</div>
          </div>
          <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-1.75  font-medium">
            {{ t('xcan_execSettingForm.stopConfiguration') }}
            <Arrow
              :open="stopParamsOpen"
              class="ml-2"
              @click="openStopParams" />
          </div>
          <div
            :class="stopParamsOpen ? 'open-params' : 'stop-params'"
            class="overflow-hidden transition-all"
            style="transition-duration: 400ms;">
            <div class="h-7 whitespace-nowrap flex items-center pl-5.25 shutdownTimeout">{{ t('xcan_execSettingForm.stopExecutionTimeout') }}</div>
          </div>
        </div>
        <Form
          ref="globalSettingsFormRef"
          class="flex-1"
          :model="globalSettingsParams"
          :colon="false"
          layout="horizontal">
          <FormItem
            class="pr-5 relative"
            name="startMode">
            <RadioGroup
              v-model:value="globalSettingsParams.startMode"
              class="space-x-6 -ml-5 h-7 pt-1"
              :enumKey="StartMode">
            </RadioGroup>
            <Tooltip
              :title="t('xcan_execSettingForm.startupModeTooltip')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{ 'max-width': '600px' }">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2.5" />
            </Tooltip>
          </FormItem>
          <FormItem
            v-if="globalSettingsParams.startMode === 'TIMING'"
            name="startAtDate"
            class="-mt-3 pr-5 relative"
            :rules="{ required: globalSettingsParams.startMode === 'TIMING', message: t('xcan_execSettingForm.startAtDateRule'), trigger: ['change', 'blur'] }">
            <DatePicker
              :value="globalSettingsParams.startAtDate"
              showTime
              class="w-full"
              :placeholder="t('xcan_execSettingForm.scheduledExecutionTime')"
              @change="startAtDateChange" />
            <Tooltip
              :title="t('xcan_execSettingForm.scheduledExecutionTimeTooltip')"
              placement="topLeft"
              arrowPointAtCenter
              :overlayStyle="{ 'max-width': '600px' }">
              <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
            </Tooltip>
          </FormItem>
          <FormItem
            name="priority"
            class="pr-5 relative">
            <Input
              v-model:value="globalSettingsParams.priority"
              dataType="number"
              :min="1"
              :max="2147483647"
              :placeholder="t('common.priority')" />
            <Tooltip
              :title="t('xcan_execSettingForm.priorityTooltip')"
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
            <FormItem
              class="pr-5 relative"
              :name="['nodeSelectors', 'num']">
              <Input
                v-model:value="globalSettingsParams.nodeSelectors.num"
                :min="1"
                :max="props.addType === 'expr' ? 1 : 200"
                :placeholder="t('xcan_execSettingForm.executionNodeCount')"
                dataType="number" />
              <Tooltip
                :title="t('xcan_execSettingForm.executionNodeCountTooltip')"
                placement="topLeft"
                arrowPointAtCenter
                :overlayStyle="{ 'max-width': '600px' }">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
              </Tooltip>
            </FormItem>
            <FormItem class="pr-5 relative" :name="['nodeSelectors', 'availableNodeIds']">
              <Select
                v-model:value="globalSettingsParams.nodeSelectors.availableNodeIds"
                :action="`${TESTER}/node?role=EXECUTION&fullTextSearch=true`"
                :fieldNames="{ label: 'name', value: 'id' }"
                mode="multiple"
                :placeholder="t('xcan_execSettingForm.availableExecutionNodes')" />
              <Tooltip
                :title="t('xcan_execSettingForm.availableExecutionNodesTooltip')"
                placement="topLeft"
                arrowPointAtCenter
                :overlayStyle="{ 'max-width': '600px' }">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
              </Tooltip>
            </FormItem>
            <FormItem class="pr-5 relative" :name="['nodeSelectors', 'appNodeIds']">
              <Select
                v-model:value="globalSettingsParams.nodeSelectors.appNodeIds"
                :action="`${TESTER}/node?role=APPLICATION&fullTextSearch=true`"
                :fieldNames="{ label: 'name', value: 'id' }"
                mode="multiple"
                :placeholder="t('xcan_execSettingForm.applicationNodes')" />
              <Tooltip
                :title="t('xcan_execSettingForm.applicationNodesTooltip')"
                placement="topLeft"
                arrowPointAtCenter
                :overlayStyle="{ 'max-width': '600px' }">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
              </Tooltip>
            </FormItem>
            <FormItem class="h-7 leading-7 pr-5" :name="['nodeSelectors', 'enabled']">
              <Switch
                v-model:checked="globalSettingsParams.nodeSelectors.enabled"
                size="small"
                class="w-8" />
              <Tooltip
                :title="t('xcan_execSettingForm.nodeSelectionStrategyTooltip')"
                placement="topLeft"
                arrowPointAtCenter
                :overlayStyle="{ 'max-width': '600px' }">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute top-2 left-10" />
              </Tooltip>
            </FormItem>
            <div
              :class="globalSettingsParams.nodeSelectors.enabled ? 'open-params' : 'stop-params'"
              class="overflow-hidden transition-all"
              style="transition-duration: 400ms;">
              <FormItem class="pr-5 relative" :name="['nodeSelectors', 'maxTaskNum']">
                <Input
                  v-model:value="globalSettingsParams.nodeSelectors.maxTaskNum"
                  :min="0"
                  :max="1000"
                  :placeholder="t('xcan_execSettingForm.maxTaskCount')" />
                <Tooltip
                  :title="t('xcan_execSettingForm.maxTaskCountTooltip')"
                  placement="topLeft"
                  arrowPointAtCenter
                  :overlayStyle="{ 'max-width': '600px' }">
                  <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
                </Tooltip>
              </FormItem>
              <FormItem class="h-7 leading-7 pr-5 relative" :name="['nodeSelectors', 'lastExecuted']">
                <Switch
                  v-model:checked="globalSettingsParams.nodeSelectors.lastExecuted"
                  size="small"
                  class="w-8" />
                <Tooltip
                  :title="t('xcan_execSettingForm.lastExecutedNodeTooltip')"
                  placement="topLeft"
                  arrowPointAtCenter
                  :overlayStyle="{ 'max-width': '600px' }">
                  <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute top-2 left-10" />
                </Tooltip>
              </FormItem>
              <FormItem class="h-7 leading-7 relative pr-5" :name="['nodeSelectors', 'specEnabled']">
                <Switch
                  v-model:checked="globalSettingsParams.nodeSelectors.specEnabled"
                  size="small"
                  class="w-8" />
                <Tooltip
                  :title="t('xcan_execSettingForm.specificationSelectionTooltip')"
                  placement="topLeft"
                  arrowPointAtCenter
                  :overlayStyle="{ 'max-width': '600px' }">
                  <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute left-10 top-2" />
                </Tooltip>
              </FormItem>
              <div v-if="globalSettingsParams.nodeSelectors.specEnabled" class="flex pr-1">
                <FormItem label="CPU" class="h-7 leading-7" />
                <FormItem class="h-7" :name="['nodeSelectors', 'cpuSpec']">
                  <Input
                    v-model:value="globalSettingsParams.nodeSelectors.cpuSpec"
                    :min="1"
                    :max="16" />
                </FormItem>
                <FormItem label="GB" class="h-7 ml-2 mr-3 leading-7" />
                <FormItem :label="t('xcan_execSettingForm.memory')" class="h-7 leading-7" />
                <FormItem class="h-7" :name="['nodeSelectors', 'memorySpec']">
                  <Input
                    v-model:value="globalSettingsParams.nodeSelectors.memorySpec"
                    :min="1"
                    :max="512" />
                </FormItem>
                <FormItem label="GB" class="h-7  ml-2 mr-3 leading-7" />
                <FormItem :label="t('xcan_execSettingForm.disk')" class="h-7 leading-7" />
                <FormItem class="h-7" :name="['nodeSelectors', 'diskSpec']">
                  <Input
                    v-model:value="globalSettingsParams.nodeSelectors.diskSpec"
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
                      <div>{{ t('xcan_execSettingForm.minCpuSpec') }}</div>
                      <div>{{ t('xcan_execSettingForm.minMemorySpec') }}</div>
                      <div>{{ t('xcan_execSettingForm.minDiskSpec') }}</div>
                    </div>
                  </template>
                </Tooltip>
              </div>
              <FormItem
                class="leading-7 relative pr-5"
                :name="['nodeSelectors', 'idleRateEnabled']"
                :class="globalSettingsParams.nodeSelectors.idleRateEnabled?'h-7':'h-12'">
                <Switch
                  v-model:checked="globalSettingsParams.nodeSelectors.idleRateEnabled"
                  size="small"
                  class="w-8" />
                <Tooltip
                  :title="t('xcan_execSettingForm.idleRateSelectionTooltip')"
                  placement="topLeft"
                  arrowPointAtCenter
                  :overlayStyle="{ 'max-width': '600px' }">
                  <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute left-10 top-2" />
                </Tooltip>
              </FormItem>
              <div v-if="globalSettingsParams.nodeSelectors.idleRateEnabled" class="flex pr-1">
                <FormItem label="CPU" class="h-7 leading-7" />
                <FormItem class="h-7 leading-7" :name="['nodeSelectors', 'cpuIdleRate']">
                  <Input
                    v-model:value="globalSettingsParams.nodeSelectors.cpuIdleRate"
                    :min="1"
                    :max="100" />
                </FormItem>
                <FormItem label="%" class="h-7 ml-2 mr-5 leading-7" />
                <FormItem :label="t('xcan_execSettingForm.memory')" class="h-7 leading-7" />
                <FormItem class="h-7 leading-7" :name="['nodeSelectors', 'memoryIdleRate']">
                  <Input
                    v-model:value="globalSettingsParams.nodeSelectors.memoryIdleRate"
                    :min="1"
                    :max="100" />
                </FormItem>
                <FormItem label="%" class="h-7  ml-2 mr-5 leading-7" />
                <FormItem :label="t('xcan_execSettingForm.disk')" class="h-7 leading-7" />
                <FormItem class="h-7 leading-7" :name="['nodeSelectors', 'diskIdleRate']">
                  <Input
                    v-model:value="globalSettingsParams.nodeSelectors.diskIdleRate"
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
                      <div>{{ t('xcan_execSettingForm.cpuIdleRate') }}</div>
                      <div>{{ t('xcan_execSettingForm.memoryIdleRate') }}</div>
                      <div>{{ t('xcan_execSettingForm.diskIdleRate') }}</div>
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
            <FormItem
              class="pr-5 relative"
              :name="['onError', 'action']">
              <RadioGroup
                v-model:value="globalSettingsParams.onError.action"
                class="space-x-6 -ml-5  h-7 pt-1"
                :enumKey="ActionWhenError">
              </RadioGroup>
              <Tooltip
                :title="t('xcan_execSettingForm.errorHandlingMethodTooltip')"
                placement="topLeft"
                arrowPointAtCenter
                :overlayStyle="{ 'max-width': '600px' }">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2.5" />
              </Tooltip>
            </FormItem>
            <FormItem class="h-7 leading-7 pr-5 relative" :name="['onError', 'sampleError']">
              <Switch
                v-model:checked="globalSettingsParams.onError.sampleError"
                size="small"
                class="w-8" />
              <Tooltip
                :title="t('xcan_execSettingForm.sampleErrorContentTooltip')"
                placement="topLeft"
                arrowPointAtCenter
                :overlayStyle="{ 'max-width': '600px' }">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute left-10 top-2" />
              </Tooltip>
            </FormItem>
            <div
              :class="globalSettingsParams.onError.sampleError ? 'open-params' : 'stop-params'"
              class="overflow-hidden transition-all"
              style="transition-duration: 400ms;">
              <FormItem
                class="pr-5 h-12 relative"
                :name="['onError', 'sampleErrorNum']">
                <Input
                  v-model:value="globalSettingsParams.onError.sampleErrorNum"
                  dataType="number"
                  :min="0"
                  :max="200"
                  size="small"
                  :placeholder="t('xcan_execSettingForm.sampleErrorContentCount')" />
                <Tooltip
                  :title="t('xcan_execSettingForm.sampleErrorContentCountTooltip')"
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
            <FormItem
              class="relative pr-5"
              name="reportInterval">
              <ShortDuration
                :value="globalSettingsParams.reportInterval + reportIntervalUnit"
                size="small"
                :inputProps="{placeholder:t('xcan_execSettingForm.samplingInterval')}"
                :selectProps="reportIntervalSelectProps"
                @change="reportIntervalChange" />
              <Tooltip
                :title="t('xcan_execSettingForm.samplingIntervalTooltip')"
                placement="topLeft"
                arrowPointAtCenter
                :overlayStyle="{ 'max-width': '600px' }">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
              </Tooltip>
            </FormItem>
            <FormItem name="lang" class="pr-5 relative h-12">
              <SelectEnum
                v-model:value="globalSettingsParams.lang"
                :enumKey="SupportedLanguage"
                placeholder="t('xcan_execSettingForm.langPlaceholder')" />
              <Tooltip
                :title="t('xcan_execSettingForm.languageTooltip')"
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
            <FormItem
              class="relative pr-5"
              name="startupTimeout">
              <ShortDuration
                :value="globalSettingsParams.startupTimeout + startupTimeoutUnit"
                size="small"
                :max="2 * 60 * 60"
                :inputProps="{placeholder:t('xcan_execSettingForm.startupRunnerTimeout')}"
                :selectProps="startupTimeoutSelectProps"
                @change="startupTimeoutChange" />
              <Tooltip
                :title="t('xcan_execSettingForm.startupRunnerTimeoutTooltip')"
                placement="topLeft"
                arrowPointAtCenter
                :overlayStyle="{ 'max-width': '600px' }">
                <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
              </Tooltip>
            </FormItem>
            <FormItem name="lang" class="pr-5 relative h-12">
              <ShortDuration
                :value="globalSettingsParams.runnerSetupTimeout+runnerSetupTimeoutUnit"
                size="small"
                :max="2 * 60 * 60"
                :inputProps="{placeholder:t('xcan_execSettingForm.samplingInitializationTimeout')}"
                :selectProps="runnerSetupTimeoutSelectProps"
                @change="runnerSetupTimeoutChange" />
              <Tooltip
                :title="t('xcan_execSettingForm.samplingInitializationTimeoutTooltip')"
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
            <FormItem
              class="relative pr-5"
              name="shutdownTimeout">
              <ShortDuration
                :value="globalSettingsParams.shutdownTimeout + shutdownTimeoutUnit"
                size="small"
                class="w-full"
                :max="2 * 60 * 60"
                :inputProps="{placeholder:t('xcan_execSettingForm.stopExecutionTimeout')}"
                :selectProps="shutdownTimeoutSelectProps"
                @change="shutdownTimeoutChange" />
              <Tooltip
                :title="t('xcan_execSettingForm.stopExecutionTimeoutTooltip')"
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
              <div class="param-parent-title">
                <Icon icon="icon-chajianpeizhi" class="text-3.5 mr-2" />
                <span>{{ t('xcan_execSettingForm.pluginConfiguration') }}</span>
              </div>
            </Badge>
          </template>
          <template #extra>
            <Arrow :open="activeKey.includes('plugin')" @click="setActiveKey('plugin')" />
          </template>
          <template v-if="pluginType === 'Http'">
            <div class="flex pl-3.5 max-w-150 text-3">
              <div class="mr-2.5 leading-7 w-35 flex-none http-form">
                <div class="h-7 whitespace-nowrap mb-5 pl-1.75 ignoreAssertions">{{ t('xcan_execSettingForm.ignoreAssertions') }}</div>
                <div class="h-7 whitespace-nowrap mb-5 updateTestResult pl-1.75">
                  {{ t('xcan_execSettingForm.updateTestResults') }}
                </div>
                <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-1.75  font-medium">
                  {{ t('xcan_execSettingForm.httpParameters') }}
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
                    {{ t('xcan_execSettingForm.connectionTimeout') }}
                  </div>
                  <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 httpSetting-readTimeout">
                    {{ t('xcan_execSettingForm.readTimeout') }}
                  </div>
                  <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 httpSetting-retryNum">
                    {{ t('xcan_execSettingForm.requestFailureRetryCount') }}
                  </div>
                  <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 httpSetting-maxRedirects">
                    {{ t('xcan_execSettingForm.maxRedirectCount') }}
                  </div>
                </div>
                <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-1.75  font-medium">
                  {{ t('xcan_execSettingForm.extendedParameters') }}
                  <Arrow
                    :open="httpExtendedParamsOpen"
                    class="ml-2"
                    @click="openHttpExtendedParams" />
                </div>
                <div
                  :class="httpExtendedParamsOpen ? 'open-params' : 'stop-params'"
                  class="overflow-hidden transition-all"
                  style="transition-duration: 400ms;">
                  <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25">{{ t('xcan_execSettingForm.extendedParameters') }}</div>
                </div>
              </div>
              <div class="flex-1">
                <Form
                  ref="httpSettingsFormRef"
                  :model="httpSettingsParams"
                  :colon="false"
                  layout="horizontal">
                  <FormItem class="h-7 leading-7 pr-5 relative" name="ignoreAssertions">
                    <Switch
                      v-model:checked="httpSettingsParams.ignoreAssertions"
                      size="small"
                      class="w-8" />
                    <Tooltip
                      title="t('xcan_execSettingForm.ignoreAssertionsTooltip')"
                      placement="topLeft"
                      arrowPointAtCenter
                      :overlayStyle="{ 'max-width': '600px' }">
                      <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute left-10 top-2" />
                    </Tooltip>
                  </FormItem>
                  <FormItem class="h-7 leading-7 pr-5 relative" name="updateTestResult">
                    <Switch
                      v-model:checked="httpSettingsParams.updateTestResult"
                      size="small"
                      class="w-8" />
                    <Tooltip
                      title="t('xcan_execSettingForm.updateTestResultsTooltip')"
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
                    <FormItem
                      class="relative pr-5"
                      :name="['httpSetting', 'connectTimeout']">
                      <ShortDuration
                        :value="httpSettingsParams.httpSetting.connectTimeout + httpSettingConnectTimeoutUnit"
                        size="small"
                        class="w-full"
                        placeholder="t('xcan_execSettingForm.connectionTimeoutTime')"
                        :max="100000"
                        :selectProps="shutdownTimeoutSelectProps"
                        @change="httpConnectTimeoutChange" />
                      <Tooltip
                        title="t('xcan_execSettingForm.connectionTimeoutTimeTooltip')"
                        placement="topLeft"
                        arrowPointAtCenter
                        :overlayStyle="{ 'max-width': '600px' }">
                        <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
                      </Tooltip>
                    </FormItem>
                    <FormItem
                      class="relative pr-5"
                      :name="['httpSetting', 'readTimeout']">
                      <ShortDuration
                        :value="httpSettingsParams.httpSetting.readTimeout + httpSettingReadTimeoutUnit"
                        size="small"
                        class="w-full"
                        placeholder="t('xcan_execSettingForm.readTimeoutTime')"
                        :max="100000"
                        :selectProps="shutdownTimeoutSelectProps"
                        @change="httpReadTimeoutChange" />
                      <Tooltip
                        title="t('xcan_execSettingForm.readTimeoutTooltip')"
                        placement="topLeft"
                        arrowPointAtCenter
                        :overlayStyle="{ 'max-width': '600px' }">
                        <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
                      </Tooltip>
                    </FormItem>
                    <FormItem
                      :name="['httpSetting', 'retryNum']"
                      class="pr-5 relative">
                      <Input
                        v-model:value="httpSettingsParams.httpSetting.retryNum"
                        dataType="number"
                        :min="0"
                        :max="6"
                        :placeholder="t('xcan_execSettingForm.requestFailureRetryCount')" />
                      <Tooltip
                        title="t('xcan_execSettingForm.requestFailureRetryCountTooltip')"
                        placement="topLeft"
                        arrowPointAtCenter
                        :overlayStyle="{ 'max-width': '600px' }">
                        <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
                      </Tooltip>
                    </FormItem>
                    <FormItem
                      :name="['httpSetting', 'maxRedirects']"
                      class="pr-5 relative h-12">
                      <Input
                        v-model:value="httpSettingsParams.httpSetting.maxRedirects"
                        dataType="number"
                        :min="1"
                        :max="10"
                        :placeholder="t('xcan_execSettingForm.retryCountRange')" />
                      <Tooltip
                        title="t('xcan_execSettingForm.maxRedirectCountTooltip')"
                        placement="topLeft"
                        arrowPointAtCenter
                        :overlayStyle="{ 'max-width': '600px' }">
                        <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
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
                        class="flex relative space-x-1 flex-1 pr-10">
                        <FormItem
                          class="flex-1"
                          :name="['extend', index, 'key']"
                          :rules="{ required: true, validator: keyValidator }">
                          <Input v-model:value="item.key" :placeholder="t('xcan_execSettingForm.parameterName')" />
                        </FormItem>
                        <FormItem
                          class="flex-1"
                          :name="['extend', index, 'value']"
                          :rules="{ required: true, message: t('xcan_execSettingForm.enterParameterValue') }">
                          <Input v-model:value="item.value" :placeholder="t('xcan_execSettingForm.parameterValue')" />
                        </FormItem>
                        <div
                          class="flex items-center text-4 absolute top-1.5"
                          :class="extendParams.extend.length - 1 === index && extendParams.extend.length !== 19 ? 'right-0' : 'right-4'">
                          <Icon
                            icon="icon-jianshao"
                            class="cursor-pointer mr-1 text-theme-text-hover"
                            @click="delHttpApisSecurityItem(index)" />
                          <template v-if="extendParams.extend.length - 1 === index && extendParams.extend.length !== 19">
                            <Icon
                              icon="icon-tianjia"
                              class="cursor-pointer text-theme-text-hover"
                              @click="addApisSecurityItem" />
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
                <div class="h-7 whitespace-nowrap mb-5 pl-1.75 ignoreAssertions">{{ t('xcan_execSettingForm.ignoreAssertions') }}</div>
                <div class="h-7 whitespace-nowrap mb-5 updateTestResult pl-1.75">
                  {{ t('xcan_execSettingForm.updateTestResults') }}
                </div>
                <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-1.75  font-medium">
                  {{ t('xcan_execSettingForm.webSocketParameters') }}
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
                    {{ t('xcan_execSettingForm.connectionTimeoutTime') }}
                  </div>
                  <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 webSocketSetting-responseTimeout">
                    {{ t('xcan_execSettingForm.responseTimeout') }}
                  </div>
                  <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 webSocketSetting-maxReconnections">
                    {{ t('xcan_execSettingForm.maxReconnections') }}
                  </div>
                  <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 webSocketSetting-reconnectionInterval">
                    {{ t('xcan_execSettingForm.reconnectionInterval') }}
                  </div>
                </div>
                <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-1.75  font-medium">
                  {{ t('xcan_execSettingForm.extendedParameters') }}
                  <Arrow
                    :open="httpExtendedParamsOpen"
                    class="ml-2"
                    @click="openHttpExtendedParams" />
                </div>
                <div
                  :class="httpExtendedParamsOpen ? 'open-params' : 'stop-params'"
                  class="overflow-hidden transition-all"
                  style="transition-duration: 400ms;">
                  <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25">{{ t('xcan_execSettingForm.extendedParameters') }}</div>
                </div>
              </div>
              <div class="flex-1">
                <Form
                  ref="webSocketSettingsFormRef"
                  :model="webSocketSettingsParams"
                  :colon="false"
                  layout="horizontal">
                  <FormItem class="h-7 leading-7 pr-5 relative" name="ignoreAssertions">
                    <Switch
                      v-model:checked="webSocketSettingsParams.ignoreAssertions"
                      size="small"
                      class="w-8" />
                    <Tooltip
                      title="t('xcan_execSettingForm.ignoreAssertionsTooltip')"
                      placement="topLeft"
                      arrowPointAtCenter
                      :overlayStyle="{ 'max-width': '600px' }">
                      <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute left-10 top-2" />
                    </Tooltip>
                  </FormItem>
                  <FormItem class="h-7 leading-7 pr-5 relative" name="updateTestResult">
                    <Switch
                      v-model:checked="webSocketSettingsParams.updateTestResult"
                      size="small"
                      class="w-8" />
                    <Tooltip
                      title="t('xcan_execSettingForm.updateTestResultsTooltip')"
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
                    <FormItem
                      class="relative pr-5"
                      :name="['webSocketSetting', 'connectTimeout']">
                      <ShortDuration
                        :value="webSocketSettingsParams.webSocketSetting.connectTimeout + webSocketSettingConnectTimeoutUnit"
                        size="small"
                        class="w-full"
                        :placeholder="t('xcan_execSettingForm.connectionTimeoutTime')"
                        :max="100000"
                        :selectProps="shutdownTimeoutSelectProps"
                        @change="webSocletConnectTimeoutChange" />
                      <Tooltip
                        title="t('xcan_execSettingForm.connectionTimeoutTimeTooltip')"
                        placement="topLeft"
                        arrowPointAtCenter
                        :overlayStyle="{ 'max-width': '600px' }">
                        <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
                      </Tooltip>
                    </FormItem>
                    <FormItem
                      class="relative pr-5"
                      :name="['webSocketSetting', 'responseTimeout']">
                      <ShortDuration
                        :value="webSocketSettingsParams.webSocketSetting.responseTimeout + webSocketSettingResponseTimeoutUnit"
                        size="small"
                        class="w-full"
                        :placeholder="t('xcan_execSettingForm.readTimeoutTime')"
                        :max="100000"
                        :selectProps="shutdownTimeoutSelectProps"
                        @change="webSocletResponseTimeoutChange" />
                      <Tooltip
                        title="t('xcan_execSettingForm.responseTimeoutTooltip')"
                        placement="topLeft"
                        arrowPointAtCenter
                        :overlayStyle="{ 'max-width': '600px' }">
                        <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
                      </Tooltip>
                    </FormItem>
                    <FormItem
                      :name="['webSocketSetting', 'maxReconnections']"
                      class="pr-5 relative">
                      <Input
                        v-model:value="webSocketSettingsParams.webSocketSetting.maxReconnections"
                        dataType="number"
                        :min="0"
                        :max="100"
                        :placeholder="t('xcan_execSettingForm.maxReconnections')" />
                      <Tooltip
                        title="t('xcan_execSettingForm.maxReconnectionsTooltip')"
                        placement="topLeft"
                        arrowPointAtCenter
                        :overlayStyle="{ 'max-width': '600px' }">
                        <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
                      </Tooltip>
                    </FormItem>
                    <FormItem
                      class="relative pr-5  h-12"
                      :name="['webSocketSetting', 'reconnectionInterval']">
                      <ShortDuration
                        :value="webSocketSettingsParams.webSocketSetting.reconnectionInterval + reconnectionIntervalUnit"
                        size="small"
                        class="w-full"
                        :placeholder="t('xcan_execSettingForm.reconnectionInterval')"
                        :max="0.5 * 60 * 60"
                        :selectProps="reconnectionIntervalSelectProps"
                        @change="webSocletReconnectionIntervalChange" />
                      <Tooltip
                        title="t('xcan_execSettingForm.reconnectionIntervalTooltip')"
                        placement="topLeft"
                        arrowPointAtCenter
                        :overlayStyle="{ 'max-width': '600px' }">
                        <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
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
                        class="flex relative space-x-1 flex-1 pr-10">
                        <FormItem
                          class="flex-1"
                          :name="['extend', index, 'key']"
                          :rules="{ required: true, validator: keyValidator }">
                          <Input v-model:value="item.key" :placeholder="t('xcan_execSettingForm.parameterName')" />
                        </FormItem>
                        <FormItem
                          class="flex-1"
                          :name="['extend', index, 'value']"
                          :rules="{ required: true, message: t('xcan_execSettingForm.enterParameterValue') }">
                          <Input v-model:value="item.value" :placeholder="t('xcan_execSettingForm.parameterValue')" />
                        </FormItem>
                        <div
                          class="flex items-center text-4 absolute top-1.5"
                          :class="extendParams.extend.length - 1 === index && extendParams.extend.length !== 19 ? 'right-0' : 'right-4'">
                          <Icon
                            icon="icon-jianshao"
                            class="cursor-pointer mr-1"
                            @click="delHttpApisSecurityItem(index)" />
                          <template v-if="extendParams.extend.length - 1 === index && extendParams.extend.length !== 19">
                            <Icon
                              icon="icon-tianjia"
                              class="cursor-pointer"
                              @click="addApisSecurityItem" />
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
            <div
              class="flex pl-3.5 max-w-150 text-3">
              <div class="mr-2.5 leading-7 w-35 flex-none jdbc-form">
                <div class="h-7 whitespace-nowrap mb-5 pl-1.75 ignoreAssertions">{{ t('xcan_execSettingForm.ignoreAssertions') }}</div>
                <div class="h-7 whitespace-nowrap mb-5 updateTestResult pl-1.75">
                  {{ t('xcan_execSettingForm.updateTestResults') }}
                </div>
                <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-1.75  font-medium">
                  {{ t('xcan_execSettingForm.jdbcParameters') }}
                  <Arrow
                    :open="jdbcParamsOpen"
                    class="ml-2"
                    @click="openJdbcParams" />
                </div>
                <div
                  :class="jdbcParamsOpen ? 'open-params' : 'stop-params'"
                  class="overflow-hidden transition-all"
                  style="transition-duration: 400ms;">
                  <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-3.5 jdbcSetting-type">
                    <IconRequired />{{ t('xcan_execSettingForm.databaseType') }}
                  </div>
                  <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-3.5 jdbcSetting-driverClassName">
                    <IconRequired />{{ t('xcan_execSettingForm.driverClassName') }}
                  </div>
                  <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-3.5 jdbcSetting-jdbcUrl">
                    <IconRequired />{{ t('xcan_execSettingForm.jdbcUrl') }}
                  </div>
                  <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-3.5 jdbcSetting-username">
                    <IconRequired />{{ t('common.username') }}
                  </div>
                  <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 jdbcSetting-password">
                    {{ t('common.password') }}
                  </div>
                  <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 jdbcSetting-isolation">
                    {{ t('xcan_execSettingForm.transactionIsolation') }}
                  </div>
                </div>
                <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-1.75  font-medium">
                  {{ t('xcan_execSettingForm.connectionPoolConfiguration') }}
                  <Arrow
                    :open="connectionPoolParamsOpen"
                    class="ml-2"
                    @click="openConnectionPoolParams" />
                </div>
                <div
                  :class="connectionPoolParamsOpen ? 'open-params' : 'stop-params'"
                  class="overflow-hidden transition-all"
                  style="transition-duration: 400ms;">
                  <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25">{{ t('xcan_execSettingForm.enableConfiguration') }}</div>
                  <div
                    :class="isOpenPool ? 'open-params' : 'stop-params'"
                    class="overflow-hidden transition-all"
                    style="transition-duration: 400ms;">
                    <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-3.5 pool-name">
                      <IconRequired />{{ t('xcan_execSettingForm.connectionPoolType') }}
                    </div>
                    <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 pool-maximumPoolSize">
                      <IconRequired />{{ t('xcan_execSettingForm.connectionPoolSize') }}
                    </div>
                    <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-3.5 pool-minimumIdle">
                      <IconRequired />{{ t('xcan_execSettingForm.minimumIdleConnections') }}
                    </div>
                    <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25 pool-maxWaitTimeoutMillis">
                      {{ t('xcan_execSettingForm.getConnectTimeoutTime') }}
                    </div>
                  </div>
                </div>
                <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-1.75  font-medium">
                  {{ t('xcan_execSettingForm.extendedParameters') }}
                  <Arrow
                    :open="httpExtendedParamsOpen"
                    class="ml-2"
                    @click="openHttpExtendedParams" />
                </div>
                <div
                  :class="httpExtendedParamsOpen ? 'open-params' : 'stop-params'"
                  class="overflow-hidden transition-all"
                  style="transition-duration: 400ms;">
                  <div class="h-7 whitespace-nowrap mb-5 flex items-center pl-5.25">{{ t('xcan_execSettingForm.extendedParameters') }}</div>
                </div>
              </div>
              <div class="flex-1">
                <Form
                  ref="jdbcSettingsFormRef"
                  :model="jdbcSettingsParams"
                  :colon="false"
                  layout="horizontal">
                  <FormItem class="h-7 leading-7 pr-5 relative" name="ignoreAssertions">
                    <Switch
                      v-model:checked="jdbcSettingsParams.ignoreAssertions"
                      size="small"
                      class="w-8" />
                    <Tooltip
                      :title="t('xcan_execSettingForm.ignoreAssertionsTooltip')"
                      placement="topLeft"
                      arrowPointAtCenter
                      :overlayStyle="{ 'max-width': '600px' }">
                      <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute top-2 left-10" />
                    </Tooltip>
                  </FormItem>
                  <FormItem class="h-7 leading-7 pr-5 relative" name="updateTestResult">
                    <Switch
                      v-model:checked="jdbcSettingsParams.updateTestResult"
                      size="small"
                      class="w-8" />
                    <Tooltip
                      :title="t('xcan_execSettingForm.updateTestResultsTooltip')"
                      placement="topLeft"
                      arrowPointAtCenter
                      :overlayStyle="{ 'max-width': '600px' }">
                      <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute left-10 top-2" />
                    </Tooltip>
                  </FormItem>
                  <div class="h-7 whitespace-nowrap mb-5 leading-7"></div>
                  <div
                    :class="jdbcParamsOpen ? 'open-params' : 'stop-params'"
                    class="overflow-hidden transition-all"
                    style="transition-duration: 400ms;">
                    <FormItem
                      class="pr-5 relative"
                      :name="['jdbcSetting', 'type']"
                      :rules="{ required: true, message: t('xcan_execSettingForm.pleaseSelectDatabaseType'), trigger: ['change', 'blur'] }">
                      <SelectEnum
                        v-model:value="jdbcSettingsParams.jdbcSetting.type"
                        :enumKey="DatabaseType"
                        :placeholder="t('xcan_execSettingForm.databaseType')"
                        @change="setPluginParamErrNum" />
                      <Tooltip
                        :title="t('xcan_execSettingForm.databaseTypeTooltip')"
                        placement="topLeft"
                        arrowPointAtCenter
                        :overlayStyle="{ 'max-width': '600px' }">
                        <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
                      </Tooltip>
                    </FormItem>
                    <FormItem
                      class="pr-5 relative"
                      :name="['jdbcSetting', 'driverClassName']"
                      :rules="{ required: true, message: t('xcan_execSettingForm.databaseDriverClassNamePlaceholder'), trigger: ['change', 'blur'] }">
                      <Input
                        v-model:value="jdbcSettingsParams.jdbcSetting.driverClassName"
                        :maxlength="200"
                        :placeholder="t('xcan_execSettingForm.databaseDriverClassName')"
                        @change="setPluginParamErrNum" />
                      <Tooltip
                        :title="t('xcan_execSettingForm.databaseDriverClassNameTooltip')"
                        placement="topLeft"
                        arrowPointAtCenter
                        :overlayStyle="{ 'max-width': '600px' }">
                        <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
                      </Tooltip>
                    </FormItem>
                    <FormItem
                      class="pr-5 relative"
                      :name="['jdbcSetting', 'jdbcUrl']"
                      :rules="{ required: true, message: t('xcan_execSettingForm.pleaseEnterDatabaseConnectionURL'), trigger: ['change', 'blur'] }">
                      <Input
                        v-model:value="jdbcSettingsParams.jdbcSetting.jdbcUrl"
                        :maxlength="2048"
                        :placeholder="t('xcan_execSettingForm.jdbcUrl')"
                        @change="setPluginParamErrNum" />
                      <Tooltip
                        :title="t('xcan_execSettingForm.jdbcUrlTooltip')"
                        placement="topLeft"
                        arrowPointAtCenter
                        :overlayStyle="{ 'max-width': '600px' }">
                        <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
                      </Tooltip>
                    </FormItem>
                    <FormItem
                      class="pr-5"
                      :name="['jdbcSetting', 'username']"
                      :rules="{ required: true, message: t('xcan_execSettingForm.databaseUsernamePlaceholder'), trigger: ['change', 'blur'] }">
                      <Input
                        v-model:value="jdbcSettingsParams.jdbcSetting.username"
                        :maxlength="200"
                        :placeholder="t('xcan_execSettingForm.databaseUsernameTooltip')"
                        @change="setPluginParamErrNum" />
                    </FormItem>
                    <FormItem
                      class="pr-5"
                      :name="['jdbcSetting', 'password']">
                      <Input
                        v-model:value="jdbcSettingsParams.jdbcSetting.password"
                        type="password"
                        :maxlength="1024"
                        :placeholder="t('xcan_execSettingForm.databasePasswordPlaceholder')" />
                    </FormItem>
                    <FormItem
                      class="pr-5 relative h-12"
                      :name="['jdbcSetting', 'isolation']">
                      <SelectEnum
                        v-model:value="jdbcSettingsParams.jdbcSetting.isolation"
                        :enumKey="TransactionIsolation"
                        :placeholder="t('xcan_execSettingForm.transactionIsolation')" />
                      <Tooltip
                        :title="t('xcan_execSettingForm.transactionIsolationTooltip')"
                        placement="topLeft"
                        arrowPointAtCenter
                        :overlayStyle="{ 'max-width': '600px' }">
                        <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
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
                        v-model:checked="isOpenPool"
                        size="small"
                        class="w-8" />
                      <Tooltip
                        :title="t('xcan_execSettingForm.enableConfigurationTooltip')"
                        placement="topLeft"
                        arrowPointAtCenter
                        :overlayStyle="{ 'max-width': '600px' }">
                        <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute left-10 top-2" />
                      </Tooltip>
                    </FormItem>
                    <div
                      :class="isOpenPool ? 'open-params' : 'stop-params'"
                      class="overflow-hidden transition-all"
                      style="transition-duration: 400ms;">
                      <FormItem
                        class="pr-5 read-only:"
                        :name="['pool', 'name']"
                        :rules="{ required: isOpenPool, message: t('xcan_execSettingForm.poolTypePlaceholder'), trigger: ['change', 'blur'] }">
                        <SelectEnum
                          v-model:value="jdbcSettingsParams.pool.name"
                          :enumKey="PoolType"
                          :placeholder="t('xcan_execSettingForm.connectionPoolType')"
                          @change="setPluginParamErrNum" />
                        <Tooltip
                          :title="t('xcan_execSettingForm.connectionPoolTypeTooltip')"
                          placement="topLeft"
                          arrowPointAtCenter
                          :overlayStyle="{ 'max-width': '600px' }">
                          <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
                        </Tooltip>
                      </FormItem>
                      <FormItem
                        class="relative pr-5"
                        :name="['pool', 'maximumPoolSize']"
                        :rules="{ required: isOpenPool, message: t('xcan_execSettingForm.poolSizeRule'), trigger: ['change', 'blur'] }">
                        <Input
                          v-model:value="jdbcSettingsParams.pool.maximumPoolSize"
                          :min="1"
                          :max="10000"
                          dataType="number"
                          :placeholder="t('xcan_execSettingForm.maximumPoolSizePlaceholder')"
                          @change="setPluginParamErrNum" />
                        <Tooltip
                          :title="t('xcan_execSettingForm.connectionPoolSizeTooltip')"
                          placement="topLeft"
                          arrowPointAtCenter
                          :overlayStyle="{ 'max-width': '600px' }">
                          <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
                        </Tooltip>
                      </FormItem>
                      <FormItem
                        class="relative pr-5"
                        :name="['pool', 'minimumIdle']"
                        :rules="{ required: isOpenPool, message: t('xcan_execSettingForm.minimumIdleRule'), trigger: ['change', 'blur'] }">
                        <Input
                          v-model:value="jdbcSettingsParams.pool.minimumIdle"
                          dataType="number"
                          :placeholder="t('xcan_execSettingForm.minimumIdleConnections')"
                          @change="setPluginParamErrNum" />
                        <Tooltip
                          :title="t('xcan_execSettingForm.minimumIdleConnectionsTooltip')"
                          placement="topLeft"
                          arrowPointAtCenter
                          :overlayStyle="{ 'max-width': '600px' }">
                          <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
                        </Tooltip>
                      </FormItem>
                      <FormItem
                        class="relative pr-5 h-12"
                        :name="['pool', 'maxWaitTimeoutMillis']">
                        <ShortDuration
                          :value="jdbcSettingsParams.pool.maxWaitTimeoutMillis + maxWaitTimeoutMillisUnit"
                          size="small"
                          class="w-full"
                          :inputProps="{placeholder:t('xcan_execSettingForm.connectionTimeoutTime')}"
                          :max="2147483.647"
                          :selectProps="maxWaitTimeoutMillisSelectProps"
                          @change="jdbcMaxWaitTimeoutMillisChange" />
                        <Tooltip
                          :title="t('xcan_execSettingForm.connectionTimeoutTooltip')"
                          placement="topLeft"
                          arrowPointAtCenter
                          :overlayStyle="{ 'max-width': '600px' }">
                          <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1 absolute -right-4.5 top-2" />
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
                        class="flex relative space-x-1 flex-1 pr-10">
                        <FormItem
                          class="flex-1"
                          :name="['extend', index, 'key']"
                          :rules="{ required: true, validator: keyValidator }">
                          <Input v-model:value="item.key" :placeholder="t('xcan_execSettingForm.parameterName')" />
                        </FormItem>
                        <FormItem
                          class="flex-1"
                          :name="['extend', index, 'value']"
                          :rules="{ required: true, message: t('xcan_execSettingForm.enterParameterValue') }">
                          <Input v-model:value="item.value" :placeholder="t('xcan_execSettingForm.parameterValue')" />
                        </FormItem>
                        <div
                          class="flex items-center text-4 absolute top-1.5"
                          :class="extendParams.extend.length - 1 === index && extendParams.extend.length !== 19 ? 'right-0' : 'right-4'">
                          <Icon
                            icon="icon-jianshao"
                            class="cursor-pointer mr-1"
                            @click="delHttpApisSecurityItem(index)" />
                          <template v-if="extendParams.extend.length - 1 === index && extendParams.extend.length !== 19">
                            <Icon
                              icon="icon-tianjia"
                              class="cursor-pointer"
                              @click="addApisSecurityItem" />
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
          <div class="param-parent-title">
            <Icon icon="icon-gonggongbianliang" class="text-3.5 mr-2 -mt-0.5" />{{ t('xcan_execSettingForm.publicVariables') }}
          </div>
        </template>
        <template #extra>
          <Arrow :open="activeKey.includes('variable')" @click="setActiveKey('variable')" />
        </template>
        <Variables ref="variablesRef" :variables="configVariables" />
      </CollapsePanel>
    </template>
  </Collapse>
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

.param-parent-title{
 @apply flex items-center text-3 text-text-title font-medium leading-5;
}

.ant-collapse > :deep(.ant-collapse-item){
  margin-bottom: 20px;
  border: 0 ;
}

.ant-collapse > :deep(.ant-collapse-item > .ant-collapse-header){
 padding: 4px 12px;
 border-bottom: 1px solid var(--border-divider);
}

.ant-collapse > :deep( .ant-collapse-item > .ant-collapse-content > .ant-collapse-content-box){
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
