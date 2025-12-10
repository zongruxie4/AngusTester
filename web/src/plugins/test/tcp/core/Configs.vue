<script setup lang="ts">
import { defineAsyncComponent, ref, provide, watch, onMounted, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { Badge, Tabs, TabPane } from 'ant-design-vue';
import { Spin, notification, AsyncComponent, Drawer, Toolbar, Icon } from '@xcan-angus/vue-ui';
import { utils, TESTER } from '@xcan-angus/infra';
import { AxiosRequestConfig } from 'axios';
import { isEqual, cloneDeep } from 'lodash-es';
import { exec, scenario } from '@/api/tester';

import { ScenarioPermission } from '@/enums/enums';
import ButtonGroup from '@/plugins/test/components/ButtonGroup/index.vue';
import { ButtonGroupMenuItem, ButtonGroupMenuKey } from '@/plugins/test/components/ButtonGroup/PropsType';
import { TOOLBAR_MENUITEMS, TOOLBAR_EXTRA_MENUITEMS, DRAWER_MENUITEMS } from './data';
import { ScenarioInfo, ScenarioConfig, SaveFormData, ScriptType, PipelineConfig } from '@/plugins/test/types/index';
import { ExecContent } from '@/plugins/test/types';

const { t } = useI18n();

export interface Props {
  tabKey: string;
  scenarioInfo: {
    name: string;
    id?: string;
  };
  userInfo: { [key: string]: any };
  appInfo: { [key: string]: any };
  projectId: string;
  updateRefreshNotify: (value: string) => void;
  updateTabPane: (data: any) => void;
  getTabPane: (data: any) => any;
  replaceTabPane: (key: string, data: { _id: string; name: string; value: 'Tcp', scenarioInfo: { id: string; } }) => any;
}

const props = withDefaults(defineProps<Props>(), {
  tabKey: undefined,
  scenarioInfo: undefined,
  userInfo: undefined,
  appInfo: undefined,
  updateRefreshNotify: undefined,
  updateTabPane: undefined,
  getTabPane: undefined,
  replaceTabPane: undefined
});

const HttpTestInfo = defineAsyncComponent(() => import('@/components/test/httpTestInfo/index.vue'));
const Indicator = defineAsyncComponent(() => import('@/components/indicator/index.vue'));
const AuthorizeModal = defineAsyncComponent(() => import('@/components/auth/AuthorizeModal.vue'));
const SaveForm = defineAsyncComponent(() => import('@/plugins/test/components/Drawer/SaveForm/index.vue'));
const ActivityTimeline = defineAsyncComponent(() => import('@/plugins/test/components/Drawer/ActivityTimeline/index.vue'));
const SmartComment = defineAsyncComponent(() => import('@/plugins/test/components/Drawer/SmartComment/index.vue'));

const ScriptConfig = defineAsyncComponent(() => import('@/plugins/test/components/ScriptConfig/index.vue'));
const UIConfig = defineAsyncComponent(() => import('./UIConfig/index.vue'));
const ExportScriptModal = defineAsyncComponent(() => import('@/plugins/test/components/ExportScriptModal/index.vue'));
const SelectScriptModal = defineAsyncComponent(() => import('@/plugins/test/components/SelectScriptModal/index.vue'));
const ImportScript = defineAsyncComponent(() => import('@/plugins/test/components/ImportScript/index.vue'));
const ExecuteConfig = defineAsyncComponent(() => import('@/plugins/test/components/ExecuteConfig/index.vue'));
const DebugResult = defineAsyncComponent(() => import('./DebugResult/index.vue'));
const DebugLog = defineAsyncComponent(() => import('@/plugins/test/components/DebugLog/index.vue'));
const ExecLog = defineAsyncComponent(() => import('@/plugins/test/components/ExecLog/index.vue'));

const codeConfigRef = ref();
const uiConfigRef = ref();
const executeConfigRef = ref();
const toolbarRef = ref();
const drawerRef = ref();
const saveFormRef = ref();

const taskErrorNum = ref(0);
const executeErrorNum = ref(0);

const activeKey = ref<'taskConfig' | 'executeConfig'>('taskConfig');
const toolbarActiveKey = ref<'debugResult' | 'logs' | 'null'>('null');
const height = ref(34);
const isFull = ref(false);
const isOpen = ref(false);
const isMoving = ref(false);
const isPageViewMode = ref(true);
const selectModalVisible = ref(false);// 选择脚本弹窗
const uploadVisible = ref(false);// 导入脚本弹窗
const exportModalVisible = ref(false);// 导出脚本弹窗
const authVisible = ref(false);// 显示权限弹窗
let controller: AbortController;// 用于终止请求的实例
const loading = ref(false);
const debugLoading = ref(false);
const loaded = ref(false);
const rendered = ref(false);
// 默认显示页面视图，新建场景没有保存之前不显示【执行测试】【导出脚本】、【关注】、【取消关注】、【收藏】、【取消收藏】、【权限】、【刷新】按钮
const hideButtonSet = ref<Set<ButtonGroupMenuKey>>(new Set<ButtonGroupMenuKey>(['test', 'pageView', 'export', 'follow', 'cancelFollow', 'favourite', 'cancelFavourite', 'permission', 'refresh']));
const scenarioConfigData = ref<ScenarioConfig>();// 场景详情信息
const saveFormConfigData = ref<{
  id: string;
  name: string;
  description: string;
  scriptId?: string;
  scriptName?: string;
  moduleId?: string;
}>();

const scriptConfig = ref<{
  type: ScriptType;
  configuration: ScenarioConfig['script']['configuration'];
  task: {
    arguments: ScenarioConfig['script']['task']['arguments'];
  };
  plugin: 'Tcp';
}>({
  type: undefined,
  configuration: undefined,
  task: {
    arguments: undefined
  },
  plugin: 'Tcp'
});

const debugExecInfo = ref<{
  id: string;
  execNode: { id: string, name: string, ip: string, agentPort: string, publicIp: string };
  sampleContents: ExecContent[];
  task: {
    arguments: {
      ignoreAssertions: boolean;
    };
    pipelines: {
      name: string;
    }[];
  };
  schedulingResult: {
    console: string[];
    success: boolean;
    exitCode: string;
  }
}>();

const debugHttpError = ref<{
  exitCode: string;
  message: string;
}>();

const buttonGroupClick = async (data: ButtonGroupMenuItem) => {
  const key = data.key;
  if (key === 'save') {
    if (!scriptId.value) {
      drawerRef.value.open(key);
    } else {
      save();
    }
    return;
  }

  if (key === 'refresh') {
    const data = await loadScenarioInfo(scenarioConfigData.value?.id);
    setScenarioConfigData(data);
    setSaveFormData(data);
    return;
  }

  if (key === 'follow') {
    followHandler(true);
    return;
  }

  if (key === 'cancelFollow') {
    followHandler(false);
    return;
  }

  if (key === 'favourite') {
    favouriteHandler(true);
    return;
  }

  if (key === 'cancelFavourite') {
    favouriteHandler(false);
    return;
  }

  if (key === 'pageView') {
    if (typeof codeConfigRef.value?.isValid === 'function') {
      if (!codeConfigRef.value.isValid()) {
        return;
      }

      if (typeof codeConfigRef.value.getData === 'function') {
        const _scriptData = codeConfigRef.value.getData();
        if (_scriptData) {
          let _configuration = _scriptData.configuration;
          let _arguments = _scriptData.task?.arguments;
          const defaultConfig = generateDefaultConfig(_scriptData.type);
          if (!_configuration) {
            _configuration = defaultConfig.configuration;
          }

          if (!_arguments) {
            _arguments = defaultConfig.arguments;
          }

          setScriptConfig(_configuration, _arguments, _scriptData.type);

          scenarioConfigData.value.script = _scriptData;
          if (_scriptData.task?.pipelines) {
            scenarioConfigData.value.script.task.pipelines = _scriptData.task.pipelines.map(item => {
              return {
                ...item,
                id: utils.uuid()
              };
            });
          }
        }
      }
    }

    hideButtonSet.value.delete('codeView');
    hideButtonSet.value.add('pageView');
    isPageViewMode.value = true;
    return;
  }

  if (key === 'codeView') {
    if (typeof uiConfigRef.value?.getData === 'function') {
      const pipelines: PipelineConfig[] = uiConfigRef.value.getData();
      scenarioConfigData.value.script.task.pipelines = pipelines;
      if (typeof executeConfigRef.value?.getData === 'function') {
        const configData = executeConfigRef.value?.getData();
        let _configuration = cloneDeep(configData.configuration);
        let _arguments = cloneDeep(configData.arguments);
        const defaultConfig = generateDefaultConfig(configData.type);
        if (!_configuration) {
          _configuration = defaultConfig.configuration;
        }

        if (!_arguments) {
          _arguments = defaultConfig.arguments;
        }

        scenarioConfigData.value.script.configuration = _configuration;
        scenarioConfigData.value.script.task.arguments = _arguments;
      }
    }

    toOpenapiObject(scenarioConfigData.value.script);
    hideButtonSet.value.delete('pageView');
    hideButtonSet.value.add('codeView');
    isPageViewMode.value = false;
    return;
  }

  if (key === 'select') {
    selectModalVisible.value = true;
    return;
  }

  if (key === 'import') {
    uploadVisible.value = true;
    return;
  }

  if (key === 'export') {
    exportModalVisible.value = true;
    return;
  }

  if (key === 'debug') {
    if (typeof saveFormRef.value?.isValid === 'function') {
      if (!saveFormRef.value.isValid()) {
        return;
      }
    }

    if (!scenarioConfigData.value?.id) {
      notification.info(t('tcpPlugin.notifications.debugSaveRequired'));
      drawerRef.value.open('save');
      return;
    }

    let saveFormData = null;
    if (typeof saveFormRef.value?.getData === 'function') {
      saveFormData = saveFormRef.value.getData();
    }
    const error = await save(saveFormData, false);
    if (error instanceof Error) {
      return;
    }

    notification.success({
      duration: 1.3,
      description: t('tcpPlugin.notifications.autoSaveSuccess')
    });
    toDebug();
    return;
  }

  if (key === 'permission') {
    authVisible.value = true;
    return;
  }

  if (key === 'test') {
    createTest();
  }
};

const toDebug = async () => {
  loading.value = true;
  const { id, script: _scriptInfo, scriptId } = scenarioConfigData.value;
  const { type } = _scriptInfo;
  const params: {
    broadcast: true;
    scenarioId: string;
    scriptId: string;
    scriptType: string;
  } = {
    broadcast: true,
    scenarioId: id,
    scriptId,
    scriptType: type
  };

  debugLoading.value = true;
  const [error, { data }] = await exec.startScenarioDebug(params);
  loading.value = false;
  debugLoading.value = false;
  if (error) {
    debugHttpError.value = {
      exitCode: error.code,
      message: error.message
    };
    debugExecInfo.value = undefined;
  } else {
    debugExecInfo.value = data;
    debugHttpError.value = undefined;
  }

  if (typeof toolbarRef.value?.open === 'function') {
    toolbarRef.value.open('debugResult');
  }
};

const createTest = async () => {
  loading.value = true;
  const [error] = await exec.addExecByScript({
    scriptId: scriptId.value
  });
  loading.value = false;
  if (error) {
    return;
  }
  notification.success(t('tcpPlugin.notifications.createTestSuccess'));
};

const selectScriptOk = (data: ScenarioConfig['script']) => {
  const scriptType = data.type;
  const newPipelines: PipelineConfig[] = data?.task?.pipelines || [];
  if (hideButtonSet.value.has('pageView')) {
    // UI视图
    let oldPipelines: PipelineConfig[] = [];
    oldPipelines = uiConfigRef.value?.getData() || [];
    let hasEnabled = false;
    const pipelines = [...oldPipelines, ...newPipelines].map(item => {
      // 只允许启用一个
      let enabled = item.enabled;
      if (hasEnabled) {
        enabled = false;
      } else if (enabled) {
        hasEnabled = true;
      }

      return {
        ...item,
        enabled,
        id: utils.uuid()
      };
    });

    scenarioConfigData.value.script.task.pipelines = cloneDeep(pipelines);

    // 只有新建场景且初始配置没有修改才替换配置
    if (!scriptId.value) {
      let _configuration = data.configuration;
      let _arguments = data.task.arguments;
      const defaultConfig = generateDefaultConfig(scriptConfig.value.type);
      if (!_configuration) {
        _configuration = defaultConfig.configuration;
      }

      if (!_arguments) {
        _arguments = defaultConfig.arguments;
      }

      if (typeof executeConfigRef.value?.getData === 'function') {
        const configData = executeConfigRef.value?.getData();
        if (!isEqual(defaultConfig, { configuration: configData.configuration, arguments: configData.arguments })) {
          scenarioConfigData.value.script.configuration = _configuration;
          scenarioConfigData.value.script.task.arguments = _arguments;
          scenarioConfigData.value.script.type = scriptType;
          setScriptConfig(_configuration, _arguments, scriptType);
        }
      } else {
        scenarioConfigData.value.script.configuration = _configuration;
        scenarioConfigData.value.script.task.arguments = _arguments;
        scenarioConfigData.value.script.type = scriptType;
        setScriptConfig(_configuration, _arguments, scriptType);
      }
    } else {
      // 替换变量
      setConfigVariables(data.configuration?.variables);
    }
  } else {
    // 代码视图
    if (codeConfigRef.value) {
      if (!codeConfigRef.value?.isValid()) {
        // 替换为新脚本
        scenarioConfigData.value.script = cloneDeep(data);
        return;
      }

      const scriptInfo: ScenarioConfig['script'] = codeConfigRef.value.getData();
      if (!scriptInfo) {
        scenarioConfigData.value.script = cloneDeep(data);
      } else {
        scenarioConfigData.value.script = cloneDeep(scriptInfo);
      }

      const oldPipelines = scriptInfo.task?.pipelines || [];
      const pipelines = cloneDeep([...oldPipelines, ...newPipelines]);

      if (!Object.prototype.hasOwnProperty.call(scenarioConfigData.value.script, 'configuration')) {
        scenarioConfigData.value.script.configuration = cloneDeep(data.configuration);
      }

      if (!Object.prototype.hasOwnProperty.call(scenarioConfigData.value.script, 'plugin')) {
        scenarioConfigData.value.script.plugin = data.plugin;
      }

      if (!Object.prototype.hasOwnProperty.call(scenarioConfigData.value.script, 'type')) {
        scenarioConfigData.value.script.type = data.type;
      }

      if (!Object.prototype.hasOwnProperty.call(scenarioConfigData.value.script, 'task')) {
        scenarioConfigData.value.script.issue = {
          pipelines
        };
      } else {
        if (!Object.prototype.hasOwnProperty.call(scenarioConfigData.value.script.task, 'arguments') && data.task?.arguments) {
          scenarioConfigData.value.script.task.arguments = data.task.arguments;
        }

        scenarioConfigData.value.script.task.pipelines = pipelines;
      }

      // 只有新建场景且初始配置没有修改才替换配置
      if (!scriptId.value) {
        const configData = {
          configuration: undefined,
          arguments: undefined
        };

        if (scenarioConfigData.value.script?.configuration) {
          configData.configuration = scenarioConfigData.value.script.configuration;
        }

        if (scenarioConfigData.value.script?.task?.arguments) {
          configData.arguments = scenarioConfigData.value.script.task.arguments;
        }

        const defaultConfig = generateDefaultConfig(scenarioConfigData.value.script?.type);
        if (!isEqual(defaultConfig, configData)) {
          let _configuration = data.configuration;
          let _arguments = data.task.arguments;
          if (!_configuration) {
            _configuration = defaultConfig.configuration;
          }

          if (!_arguments) {
            _arguments = defaultConfig.arguments;
          }

          scenarioConfigData.value.script.configuration = _configuration;
          scenarioConfigData.value.script.task.arguments = _arguments;
          scenarioConfigData.value.script.type = scriptType;
          setScriptConfig(_configuration, _arguments, scriptType);
        }
      } else {
        // 替换变量
        setConfigVariables(data.configuration?.variables);
      }
    }
  }
};

const setConfigVariables = (data?: { [key: string]: any }[]) => {
  if (!data?.length) {
    return;
  }

  if (!scriptConfig.value?.configuration?.variables) {
    scriptConfig.value.configuration.variables = [];
  }

  scriptConfig.value = { ...scriptConfig.value };
  const names = scriptConfig.value?.configuration?.variables.map(item => item.name) || [];
  for (let i = 0, len = data.length; i < len; i++) {
    const name = data[i].name;
    if (!names.includes(name)) {
      scriptConfig.value?.configuration?.variables.push(data[i]);
    }
  }
};

const setScriptConfig = (_configuration, _arguments, type: ScriptType) => {
  scriptConfig.value = {
    type,
    plugin: 'Tcp',
    configuration: _configuration,
    task: {
      ...scriptConfig.value.task,
      arguments: _arguments
    }
  };
};

const cancel = () => {
  if (typeof drawerRef.value?.open === 'function') {
    drawerRef.value.close();
  }
};

const scriptTypeChange = (value: ScriptType) => {
  scenarioConfigData.value.script.type = value;
  if (typeof executeConfigRef.value?.getData === 'function') {
    const configData = executeConfigRef.value.getData();
    scenarioConfigData.value.script.configuration = cloneDeep(configData.configuration);
    scenarioConfigData.value.script.task.arguments = cloneDeep(configData.arguments);
  }

  if (value === 'TEST_FUNCTIONALITY') {
    scenarioConfigData.value.script.configuration.thread.threads = '1';
    scenarioConfigData.value.script.configuration.iterations = '1';
    scenarioConfigData.value.script.task.arguments.ignoreAssertions = false;
  } else if (value === 'TEST_PERFORMANCE') {
    scenarioConfigData.value.script.configuration.duration = '50min';
    if (scenarioConfigData.value.script.configuration.thread) {
      scenarioConfigData.value.script.configuration.thread.threads = '5000';
      scenarioConfigData.value.script.configuration.thread.rampUpInterval = '1min';
      scenarioConfigData.value.script.configuration.thread.rampUpThreads = '100';
    } else {
      scenarioConfigData.value.script.configuration.thread = {
        threads: '5000',
        rampUpInterval: '1min', // {{ t('tcpPlugin.comments.rampUpInterval') }}
        rampUpThreads: '100' // {{ t('tcpPlugin.comments.rampUpThreads') }}
      };
    }
  } else if (value === 'TEST_STABILITY') {
    scenarioConfigData.value.script.configuration.duration = '30min';
    scenarioConfigData.value.script.configuration.thread.threads = '200';
  }

  scriptConfig.value.type = value;
  scriptConfig.value.configuration = cloneDeep(scenarioConfigData.value.script.configuration);
  scriptConfig.value.task.arguments = cloneDeep(scenarioConfigData.value.script.task.arguments);
};

const scriptTypeExcludes = ({ value }): boolean => {
  return !['TEST_PERFORMANCE', 'TEST_FUNCTIONALITY', 'TEST_STABILITY', 'TEST_CUSTOMIZATION'].includes(value);
};

const close = () => {
  if (typeof toolbarRef.value?.open === 'function') {
    toolbarRef.value.open();
  }
};

const fullScreen = () => {
  if (typeof toolbarRef.value?.fullScreen === 'function') {
    toolbarRef.value.fullScreen();
  }
};

const isValid = async (): Promise<boolean> => {
  if (typeof uiConfigRef.value?.isValid === 'function') {
    if (!uiConfigRef.value.isValid()) {
      notification.error(t('tcpPlugin.notifications.taskConfigError'));
      return false;
    }
  }

  if (typeof executeConfigRef.value?.isValid === 'function') {
    const validFlag = await executeConfigRef.value.isValid();
    if (!validFlag) {
      notification.error(t('tcpPlugin.notifications.executeConfigError'));
      return false;
    }
  }

  return true;
};

const getData = (): {
  configuration: ScenarioConfig['script']['configuration'];
  arguments: ScenarioConfig['script']['task']['arguments'];
  pipelines: PipelineConfig[];
} => {
  const data: {
    configuration: ScenarioConfig['script']['configuration'];
    arguments: ScenarioConfig['script']['task']['arguments'];
    pipelines: PipelineConfig[];
  } = {
    configuration: undefined,
    arguments: undefined,
    pipelines: []
  };

  if (hideButtonSet.value.has('pageView')) {
    if (typeof uiConfigRef.value?.getData === 'function') {
      data.pipelines = uiConfigRef.value.getData();
    }

    if (typeof executeConfigRef.value?.getData === 'function') {
      const tempData = executeConfigRef.value.getData();
      data.arguments = tempData.arguments;
      data.configuration = tempData.configuration;
    }
  } else {
    const _scriptData = codeConfigRef.value.getData();
    if (_scriptData) {
      data.configuration = _scriptData.configuration;
      data.arguments = _scriptData.task?.arguments;
      data.pipelines = _scriptData.task?.pipelines;
    }
  }

  return cloneDeep(data);
};

const followHandler = (value: boolean) => {
  const id = scenarioConfigData.value?.id;
  if (value) {
    toFollow(id);
    return;
  }

  cancelFollow(id);
};

const toFollow = async (id: string) => {
  if (loading.value) {
    return;
  }

  loading.value = true;
  const [error] = await scenario.addScenarioFollow(id);
  loading.value = false;
  if (error) {
    return;
  }

  hideButtonSet.value.delete('cancelFollow');
  hideButtonSet.value.add('follow');
  scenarioConfigData.value.follow = true;
  notification.success(t('tcpPlugin.notifications.followSuccess'));
};

const cancelFollow = async (id: string) => {
  if (loading.value) {
    return;
  }

  loading.value = true;
  const [error] = await scenario.deleteScenarioFollow(id);
  loading.value = false;
  if (error) {
    return;
  }

  hideButtonSet.value.delete('follow');
  hideButtonSet.value.add('cancelFollow');
  scenarioConfigData.value.follow = false;
  notification.success(t('tcpPlugin.notifications.cancelFollowSuccess'));
};

const favouriteHandler = (value: boolean) => {
  const id = scenarioConfigData.value?.id;
  if (value) {
    toFavourite(id);
    return;
  }

  cancelFavourite(id);
};

const toFavourite = async (id: string) => {
  if (loading.value) {
    return;
  }

  loading.value = true;
  const [error] = await scenario.addScenarioFavorite(id);
  loading.value = false;
  if (error) {
    return;
  }

  hideButtonSet.value.delete('cancelFavourite');
  hideButtonSet.value.add('favourite');
  scenarioConfigData.value.favourite = true;
  notification.success(t('tcpPlugin.notifications.favouriteSuccess'));
};

const cancelFavourite = async (id: string) => {
  if (loading.value) {
    return;
  }

  loading.value = true;
  const [error] = await scenario.deleteScenarioFavorite(id);
  loading.value = false;
  if (error) {
    return;
  }

  hideButtonSet.value.delete('favourite');
  hideButtonSet.value.add('cancelFavourite');
  scenarioConfigData.value.favourite = false;
  notification.success(t('tcpPlugin.notifications.cancelFavouriteSuccess'));
};

const save = async (data?: {
  description: string;
  projectId: string;
  name: string;
  moduleId: string;
}, notificationFlag = true) => {
  const validFlag = await isValid();
  if (!validFlag) {
    return new Error(t('tcpPlugin.notifications.parameterError'));
  }

  if (controller) {
    controller.abort();
  }

  const {
    id,
    script,
    scriptId,
    plugin,
    description,
    name
  } = cloneDeep(scenarioConfigData.value);

  const params: SaveFormData = {
    description,
    id,
    name,
    plugin,
    scriptId,
    script,
    projectId: props.projectId
  };

  if (data) {
    params.description = data.description;
    params.name = data.name;
    params.moduleId = data.moduleId;
  }

  const formData = getData();
  params.script.task.pipelines = cloneDeep(formData.pipelines);

  if (formData.arguments) {
    params.script.task.arguments = formData.arguments;
  }

  if (formData.configuration) {
    params.script.configuration = formData.configuration;
  }

  if (params.script.configuration.startMode?.value) {
    params.script.configuration.startMode = params.script.configuration.startMode.value;
  }

  if (params.script.configuration.onError?.action?.value) {
    params.script.configuration.onError.action = params.script.configuration.onError.action.value;
  }

  toOpenapiObject(params.script);

  controller = new AbortController();
  const axiosConfig: AxiosRequestConfig = {
    signal: controller.signal
  };
  loading.value = true;
  if (!params.id) {
    const [error, res] = await scenario.addScenario(params, axiosConfig);
    loading.value = false;
    if (error) {
      return error;
    }

    const scenarioId = res.data.id;
    scenarioConfigData.value.id = scenarioId;
    initBtn();

    if (typeof props.replaceTabPane === 'function') {
      props.replaceTabPane(props.tabKey, { _id: scenarioId, name: params.name, value: 'Tcp', scenarioInfo: { id: scenarioId } });
    }

    const scenarioInfoData = await loadScenarioInfo(scenarioId);
    setScenarioConfigData(scenarioInfoData);
    setSaveFormData(scenarioInfoData);

    // 通知主页、场景列表刷新
    if (typeof props.updateRefreshNotify === 'function') {
      props.updateRefreshNotify(utils.uuid());
    }
  } else {
    const [error] = await scenario.putScenario(params, axiosConfig);
    loading.value = false;
    if (error) {
      return error;
    }

    saveFormConfigData.value = {
      ...saveFormConfigData.value,
      name: params.name,
      description: params.description,
      moduleId: params.moduleId
    };

    if (typeof props.updateTabPane === 'function') {
      props.updateTabPane({ _id: params.id, name: params.name });
    }
  }

  if (notificationFlag) {
    notification.success(t('actions.tips.saveSuccess'));
  }

  if (typeof drawerRef.value?.open === 'function') {
    drawerRef.value.close();
  }

  // 新建、编辑名称、目录、描述刷新场景列表
  let refreshFlag = !params.id;
  if (params.name !== scenarioConfigData.value.name) {
    refreshFlag = true;
  } else if (params.description !== scenarioConfigData.value.description) {
    refreshFlag = true;
  }

  if (refreshFlag) {
    // 刷新已经打开的场景列表
    if (typeof props.updateTabPane === 'function') {
      if (typeof props.getTabPane === 'function') {
        const tabData = props.getTabPane('scenarioList');
        if (tabData?.length) {
          props.updateTabPane({ _id: 'scenarioList', notify: utils.uuid() });
        }
      }
    }
  }
};

const setSaveFormData = (data: ScenarioInfo) => {
  if (!data) {
    return;
  }

  const { name, description, scriptId, scriptName, id, moduleId } = data;
  saveFormConfigData.value = {
    id,
    name,
    description,
    scriptId,
    scriptName,
    moduleId
  };
};

const setScenarioConfigData = (data: ScenarioInfo) => {
  if (!data) {
    return;
  }

  const {
    script,
    scriptId,
    scriptName
  } = data;
  scenarioConfigData.value = {
    ...data,
    scriptId,
    scriptName,
    script: undefined
  };

  if (script) {
    const scriptType = script.type?.value || script.type;
    const defaultConfig = generateDefaultConfig(scriptType);
    let _arguments = script.task?.arguments;
    if (!_arguments) {
      _arguments = defaultConfig.arguments;
    }
    const _configuration = script.configuration || defaultConfig.configuration;
    scenarioConfigData.value.script = {
      ...script,
      configuration: _configuration,
      type: scriptType,
      task: {
        arguments: _arguments,
        pipelines: []
      }
    };

    if (script.task) {
      scenarioConfigData.value.script.issue = {
        ...script.task,
        arguments: _arguments,
        pipelines: []
      };

      if (script.task.pipelines?.length) {
        scenarioConfigData.value.script.task.pipelines = script.task.pipelines.map(item => {
          return {
            id: utils.uuid(),
            ...item,
            type: item.type?.value || item.type
          };
        });
      }
    }

    setScriptConfig(_configuration, _arguments, scriptType);
  }

  initBtn();
};

const loadScenarioInfo = async (id: string) => {
  if (!id) {
    return;
  }

  loading.value = true;
  const [error, { data }]: [Error, { data: ScenarioInfo }] = await scenario.getScenarioDetail(id, { silence: false });
  loading.value = false;
  loaded.value = true;
  if (error || !data) {
    rendered.value = true;
    return undefined;
  }

  if (!data?.script?.task?.pipelines?.length) {
    rendered.value = true;
  }

  toOpenapiObject(data.script);
  let hasEnabled = false;
  data.script?.task?.pipelines?.forEach(item => {
    // 只允许启用一个
    if (hasEnabled) {
      item.enabled = false;
    } else if (item.enabled) {
      hasEnabled = true;
    }
  });

  return data;
};

const loadDebugInfo = async () => {
  const id = props.scenarioInfo?.id;
  if (!id) {
    return;
  }

  const [error, { data }] = await exec.getScenarioDebugDetail(id);
  if (error) {
    return;
  }

  debugExecInfo.value = data;
};

const initBtn = () => {
  // 已经保存的场景有【执行测试】、【导出脚本】、【关注】、【取消关注】、【收藏】、【取消收藏】、【权限】、【刷新】按钮
  hideButtonSet.value.delete('export');
  hideButtonSet.value.delete('permission');
  hideButtonSet.value.delete('refresh');
  hideButtonSet.value.delete('test');

  // 已经关注的场景显示【取消关注】按钮
  if (scenarioConfigData.value.follow) {
    hideButtonSet.value.delete('cancelFollow');
    hideButtonSet.value.add('follow');
  } else {
    hideButtonSet.value.delete('follow');
    hideButtonSet.value.add('cancelFollow');
  }

  // 已经收藏的场景显示【取消收藏】按钮
  if (scenarioConfigData.value.favourite) {
    hideButtonSet.value.delete('cancelFavourite');
    hideButtonSet.value.add('favourite');
  } else {
    hideButtonSet.value.delete('favourite');
    hideButtonSet.value.add('cancelFavourite');
  }
};

const renderChange = (value: boolean) => {
  rendered.value = value;
};

const taskErrorNumChange = (value: number) => {
  taskErrorNum.value = value;
};

const executeErrorNumChange = (value: number) => {
  executeErrorNum.value = value;
};

const toOpenapiObject = (data: { [key: string]: any }): { [key: string]: any } => {
  if (!data || typeof data !== 'object') {
    return;
  }

  if (Array.isArray(data)) {
    for (let i = 0, len = data.length; i < len; i++) {
      const item = data[i];
      if (item !== null && typeof item === 'object') {
        toOpenapiObject(item);
      }
    }

    return;
  }

  for (const key in data) {
    if (key === 'id') {
      delete data[key];
    } else {
      const item = data[key];
      if (item === null || item === undefined || item === '') {
        delete data[key];
      } else {
        if (typeof item === 'object') {
          if (Array.isArray(item)) {
            if (item.length) {
              toOpenapiObject(item);
            } else {
              delete data[key];
            }
          } else {
            const keys = Object.keys(item);
            if (keys.length) {
              if (keys.length === 2 && keys.includes('message') && keys.includes('value')) {
                data[key] = data[key].value;
              } else {
                toOpenapiObject(item);
              }
            } else {
              delete data[key];
            }
          }
        }
      }
    }
  }
};

onMounted(() => {
  loadDebugInfo();

  watch(() => props.scenarioInfo, async (newValue) => {
    if (!newValue) {
      return;
    }

    const id = newValue.id;
    const { name } = newValue;
    const type = 'TEST_PERFORMANCE';
    const { arguments: _arguments, configuration } = generateDefaultConfig(type);
    scenarioConfigData.value = {
      name,
      description: '',
      favourite: false,
      follow: false,
      id: undefined,
      plugin: 'Tcp',
      scriptId: undefined,
      scriptName: undefined,
      script: {
        type,
        plugin: 'Tcp',
        configuration,
        task: {
          arguments: _arguments,
          pipelines: [{
            id: utils.uuid(),
            target: 'TCP',
            name: '',
            description: '',
            enabled: true,
            beforeName: '',
            data: '',
            dataEncoding: 'none',
            setting: {
              tcpClientImplClass: '',
              reUseConnection: true,
              closeConnection: false,
              soLinger: null,
              tcpNoDelay: false,
              tcpCharset: 'UTF-8',
              eolByte: '',
              eomByte: '',
              binaryPrefixLength: ''
            },
            server: {
              server: '',
              port: '',
              connectTimeout: '6s', // 1s ～ 86400s
              responseTimeout: '60s'// 1s ～ 86400s
            },
            variables: null
          }]
        }
      }
    };

    setScriptConfig(configuration, _arguments, scenarioConfigData.value.script.type);

    saveFormConfigData.value = {
      id,
      name,
      description: '',
      scriptId: undefined,
      scriptName: undefined,
      moduleId: undefined
    };

    if (!id) {
      loaded.value = true;
      return;
    }

    const data = await loadScenarioInfo(id);
    setScenarioConfigData(data);
    setSaveFormData(data);
  }, { immediate: true });
});

const generateDefaultConfig = (type: ScriptType) => {
  const data = {
    arguments: {
      ignoreAssertions: true,
      updateTestResult: false
    },
    configuration: {
      thread: {
        threads: '1'
      },
      onError: {
        sampleError: true,
        sampleErrorNum: '20'
      }
    },
    type
  };

  if (type === 'TEST_FUNCTIONALITY') {
    data.configuration.thread.threads = '1';
    data.configuration.iterations = '1';
    data.arguments.ignoreAssertions = false;
  } else if (type === 'TEST_PERFORMANCE') {
    data.configuration.duration = '50min';
    data.configuration.thread = {
      threads: '5000',
      rampUpInterval: '1min', // {{ t('tcpPlugin.comments.rampUpInterval') }}
      rampUpThreads: '100' // {{ t('tcpPlugin.comments.rampUpThreads') }}
    };
  } else if (type === 'TEST_STABILITY') {
    data.configuration.duration = '30min';
    data.configuration.thread.threads = '200';
  }

  return data;
};

const scriptId = computed((): string => {
  return saveFormConfigData.value?.scriptId;
});

const tabText = computed(() => {
  return isPageViewMode.value ? { task: t('common.taskConfig'), execute: t('common.execConfig') } : { task: '', execute: '' };
});

const drawerMenuItems = computed(() => {
  if (scenarioConfigData.value?.id) {
    if (scenarioConfigData.value.plugin === 'Tcp') {
      return DRAWER_MENUITEMS;
    }
    return DRAWER_MENUITEMS.filter(item => !['indicator', 'testInfo'].includes(item.key));
  }

  return DRAWER_MENUITEMS.filter(item => ['save'].includes(item.key));
});

const setGlobalTabActiveKey = (key: 'taskConfig' | 'executeConfig') => {
  activeKey.value = key;
};

provide('userInfo', props.userInfo);
provide('setGlobalTabActiveKey', setGlobalTabActiveKey);
</script>
<template>
  <Spin :spinning="loading || !rendered" class="h-full overflow-hidden text-3 pt-1.5">
    <Tabs
      v-model:activeKey="activeKey"
      class="pure-tab-wrap h-full leading-5"
      size="small">
      <template #rightExtra>
        <ButtonGroup :hideKeys="hideButtonSet" :debugLoading="debugLoading" @click="buttonGroupClick" />
      </template>
      <TabPane key="taskConfig">
        <template #tab>
          <Badge
            v-if="tabText.task"
            class="count-Badge-container"
            size="small"
            :count="taskErrorNum">
            <div>{{ tabText.task }}</div>
          </Badge>
        </template>
      </TabPane>
      <TabPane key="executeConfig">
        <template #tab>
          <Badge
            v-if="tabText.execute"
            class="count-Badge-container"
            size="small"
            :count="executeErrorNum">
            <div>{{ tabText.execute }}</div>
          </Badge>
        </template>
      </TabPane>
    </Tabs>
    <div
      style="height: calc(100% - 38px);"
      class="w-full flex flex-nowrap items-center overflow-x-auto overflow-y-hidden">
      <div class="flex-1 h-full min-w-0">
        <div
          class="bg-white overflow-auto"
          :class="{ 'transition-150': !isMoving }"
          :style="'height:calc(100% - ' + height + 'px);'">
          <AsyncComponent :visible="!isPageViewMode">
            <ScriptConfig
              v-show="!isPageViewMode"
              ref="codeConfigRef"
              :value="scenarioConfigData?.script" />
          </AsyncComponent>
          <AsyncComponent :visible="activeKey === 'taskConfig'">
            <UIConfig
              v-show="isPageViewMode && activeKey === 'taskConfig'"
              ref="uiConfigRef"
              :loaded="loaded"
              :value="scenarioConfigData?.script?.task?.pipelines"
              @errorNumChange="taskErrorNumChange"
              @renderChange="renderChange" />
          </AsyncComponent>
          <AsyncComponent :visible="activeKey === 'executeConfig'">
            <ExecuteConfig
              v-show="isPageViewMode && activeKey === 'executeConfig'"
              ref="executeConfigRef"
              :value="scriptConfig"
              :excludes="scriptTypeExcludes"
              @scriptTypeChange="scriptTypeChange"
              @errorNumChange="executeErrorNumChange" />
          </AsyncComponent>
        </div>

        <Toolbar
          ref="toolbarRef"
          v-model:clientHeight="height"
          v-model:isFull="isFull"
          v-model:isOpen="isOpen"
          v-model:isMoving="isMoving"
          v-model:activeKey="toolbarActiveKey"
          :menuItems="TOOLBAR_MENUITEMS"
          :extraMenuItems="TOOLBAR_EXTRA_MENUITEMS"
          :destroyInactiveTabPane="false"
          class="relative z-1 bg-white">
          <template #toggle>
            <Icon
              :icon="isOpen ? 'icon-shouqi' : 'icon-spread'"
              style="font-size: 14px;cursor: pointer;"
              @click="close" />
          </template>
          <template #screen>
            <Icon
              :icon="isFull ? 'icon-tuichuzuida' : 'icon-zuidahua'"
              style="font-size: 14px;cursor: pointer;"
              @click="fullScreen" />
          </template>
          <template #debugResult>
            <DebugResult :value="debugExecInfo" :httpError="debugHttpError" />
          </template>
          <template #logs>
            <DebugLog :value="debugExecInfo?.schedulingResult" />
          </template>
          <template #execLog>
            <ExecLog
              :execId="debugExecInfo?.id"
              :execNode="debugExecInfo?.execNode"
              :schedulingResult="debugExecInfo?.schedulingResult" />
          </template>
        </Toolbar>
      </div>
      <Drawer
        ref="drawerRef"
        :menuItems="drawerMenuItems"
        class="flex-shrink-0">
        <template #save>
          <SaveForm
            ref="saveFormRef"
            class="pr-3.5 py-3"
            :value="saveFormConfigData"
            :loading="loading"
            :projectId="props.projectId"
            @save="save"
            @canecel="cancel" />
        </template>
        <template #indicator>
          <Indicator :id="scenarioConfigData?.id" type="SCENARIO" />
        </template>

        <template #testInfo>
          <HttpTestInfo
            :id="scenarioConfigData?.id"
            class="mt-2"
            type="SCENARIO" />
        </template>

        <template #activity>
          <ActivityTimeline
            :id="scenarioConfigData?.id"
            infoKey="description"
            :projectId="props.projectId"
            class="pt-4.75 pr-3.5" />
        </template>

        <template #comment>
          <SmartComment
            :id="scenarioConfigData?.id"
            :userId="props.userInfo?.id"
            class="h-full pt-4.75 pr-3.5" />
        </template>
      </Drawer>
    </div>

    <AsyncComponent :visible="authVisible">
      <AuthorizeModal
        v-model:visible="authVisible"
        :enumKey="ScenarioPermission"
        :appId="props.appInfo?.id"
        :listUrl="`${TESTER}/scenario/auth?scenarioId=${scenarioConfigData?.id}`"
        :delUrl="`${TESTER}/scenario/auth`"
        :addUrl="`${TESTER}/scenario/${scenarioConfigData?.id}/auth`"
        :updateUrl="`${TESTER}/scenario/auth`"
        :enabledUrl="`${TESTER}/scenario/${scenarioConfigData?.id}/auth/enabled`"
        :initStatusUrl="`${TESTER}/scenario/${scenarioConfigData?.id}/auth/status`"
        :onTips="t('tcpPlugin.permissions.onTips')"
        :offTips="t('tcpPlugin.permissions.offTips')"
        :title="t('tcpPlugin.permissions.title')" />
    </AsyncComponent>

    <AsyncComponent :visible="exportModalVisible">
      <ExportScriptModal :id="scriptId" v-model:visible="exportModalVisible" />
    </AsyncComponent>

    <AsyncComponent :visible="selectModalVisible">
      <SelectScriptModal v-model:visible="selectModalVisible" plugin="Tcp" @ok="selectScriptOk" />
    </AsyncComponent>

    <AsyncComponent :visible="uploadVisible">
      <ImportScript v-model:visible="uploadVisible" @ok="selectScriptOk" />
    </AsyncComponent>
  </Spin>
</template>

<style scoped>
.pure-tab-wrap.ant-tabs.ant-tabs {
  height: 38px;
}

.pure-tab-wrap.ant-tabs> :deep(.ant-tabs-nav) {
  height: 38px;
  margin: 0;
  padding: 0 20px;
  user-select: none;
}

.pure-tab-wrap.ant-tabs> :deep(.ant-tabs-nav)>.ant-tabs-nav-wrap>.ant-tabs-nav-list>.ant-tabs-tab {
  padding: 7px 0;
}

.transition-150 {
  transition: all 150ms linear 0ms;
}

.log-item {
  line-height: 18px;
  word-wrap: break-word;
  word-break: break-all;
  white-space: pre-line;
}

.count-Badge-container :deep(.ant-badge-count) {
  top: -2px;
  right: -5px;
}
</style>
