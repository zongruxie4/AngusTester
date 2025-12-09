<script setup lang="ts">
import { defineAsyncComponent, ref, provide, watch, onMounted, computed } from 'vue';
import { Badge, Tabs, TabPane } from 'ant-design-vue';
import { Spin, notification, AsyncComponent, Drawer, Toolbar, Icon, SelectEnum } from '@xcan-angus/vue-ui';
import { utils, TESTER, ScriptType as ScriptTypeInfra } from '@xcan-angus/infra';
import { AxiosRequestConfig } from 'axios';
import { isEqual, cloneDeep } from 'lodash-es';
import { exec, scenario } from '@/api/tester';
import { useI18n } from 'vue-i18n';

import { ScenarioPermission, ScriptLanguage } from '@/enums/enums';
import ButtonGroup from '@/plugins/test/components/ButtonGroup/index.vue';
import { ButtonGroupMenuItem, ButtonGroupMenuKey } from '@/plugins/test/components/ButtonGroup/PropsType';
import { ScenarioInfo, ScenarioConfig, SaveFormData, ScriptType, PipelineConfig } from '@/plugins/test/types/index';
import { ExecContent } from '@/plugins/test/types';

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
  replaceTabPane: (key: string, data: { _id: string; name: string; value: 'Web', scenarioInfo: { id: string; } }) => any;
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

const { t } = useI18n();

const HttpTestInfo = defineAsyncComponent(() => import('@/components/test/httpTestInfo/index.vue'));
const Indicator = defineAsyncComponent(() => import('@/components/indicator/index.vue'));
const AuthorizeModal = defineAsyncComponent(() => import('@/components/auth/AuthorizeModal.vue'));
const SaveForm = defineAsyncComponent(() => import('@/plugins/test/components/Drawer/SaveForm/index.vue'));
const ActivityTimeline = defineAsyncComponent(() => import('@/plugins/test/components/Drawer/ActivityTimeline/index.vue'));
const SmartComment = defineAsyncComponent(() => import('@/plugins/test/components/Drawer/SmartComment/index.vue'));

const ScriptConfig = defineAsyncComponent(() => import('@/plugins/test/components/ScriptConfig/index.vue'));
// const UIConfig = defineAsyncComponent(() => import('./UIConfig/index.vue'));
const ExportScriptModal = defineAsyncComponent(() => import('@/plugins/test/components/ExportScriptModal/index.vue'));
const SelectScriptModal = defineAsyncComponent(() => import('@/plugins/test/components/SelectScriptModal/index.vue'));
const ImportScript = defineAsyncComponent(() => import('@/plugins/test/components/ImportScript/index.vue'));
// const ExecuteConfig = defineAsyncComponent(() => import('@/plugins/test/components/ExecuteConfig/index.vue'));
// const DebugResult = defineAsyncComponent(() => import('./DebugResult/index.vue'));
const DebugLog = defineAsyncComponent(() => import('@/plugins/test/components/DebugLog/index.vue'));
const ExecLog = defineAsyncComponent(() => import('@/plugins/test/components/ExecLog/index.vue'));

const codeConfigRef = ref();
// const uiConfigRef = ref();
// const executeConfigRef = ref();
const toolbarRef = ref();
const drawerRef = ref();
const saveFormRef = ref();

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
const hideButtonSet = ref<Set<ButtonGroupMenuKey>>(new Set<ButtonGroupMenuKey>(['test', 'pageView', 'export', 'follow', 'cancelFollow', 'favourite', 'cancelFavourite', 'permission', 'refresh', 'codeView']));
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
  task: {
    scriptContent: '',
    scriptLanguage: ''
  };
  plugin: 'Web';
}>({
  type: 'TEST_PERFORMANCE',
  task: {
    scriptContent: '',
    scriptLanguage: ''
  },
  plugin: 'Web'
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
      notification.info(t('ftpPlugin.messages.debugSaveRequired'));
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
      description: t('httpPlugin.messages.autoSaveAndDebug')
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
  debugLoading.value = false;
  loading.value = false;
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
  notification.success(t('httpPlugin.messages.createExecutionSuccess'));
};

const selectScriptOk = (data: ScenarioConfig['script']) => {
  const scriptType = data.type;
  const newPipelines: PipelineConfig[] = data?.task?.pipelines || [];
  if (codeConfigRef.value) {
        const scriptInfo: string = codeConfigRef.value.getData();
        if (!scriptInfo) {
        scenarioConfigData.value.script.task.scriptContent = data?.task?.scriptContent;
        } else {
        scenarioConfigData.value.script.task.scriptContent = scriptInfo;
        }

        const oldPipelines = scenarioConfigData.value?.script?.task?.pipelines || [];
        const pipelines = cloneDeep([...oldPipelines, ...newPipelines]);


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
        };

        // 只有新建场景且初始配置没有修改才替换配置
        if (!scriptId.value) {
        const configData = {
            scriptContent: undefined,
            scriptLanguage: undefined
        };

        if (scenarioConfigData.value?.script?.task?.scriptContent) {
            configData.scriptContent = scenarioConfigData.value.script.task.scriptContent;
        }

        if (scenarioConfigData.value.script?.task?.scriptLanguage) {
            configData.scriptLanguage = scenarioConfigData.value.script.task.scriptLanguage;
        }

        const defaultConfig = generateDefaultConfig(scenarioConfigData.value?.script?.type);
        if (!isEqual(defaultConfig, configData)) {
            scenarioConfigData.value.script.type = scriptType;
            setScriptConfig(scriptType);
        }
    }
  }
};


const setScriptConfig = (type: ScriptType) => {
  scriptConfig.value = {
    type,
    plugin: 'Web',
    task: {
      ...scriptConfig.value.task
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
  scriptConfig.value.type = value;
};

const scriptTypeExcludes = ({ value }): boolean => {
  return !['TEST_PERFORMANCE', 'TEST_FUNCTIONALITY', 'TEST_STABILITY',  'TEST_COMPATIBILITY', 'TEST_COMPLIANCE'].includes(value);
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

  return true;
};

const getData = (): {
    scriptContent: string;
    scriptLanguage: string;
} => {
  const data: {
    scriptContent: string;
    scriptLanguage: string;
  } = {
    scriptContent: '',
    scriptLanguage: scenarioConfigData.value?.script?.task?.scriptLanguage
  };

  const _scriptData = codeConfigRef.value.getData();
    if (_scriptData) {
        data.scriptContent = _scriptData;
    }

  return data;
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
  notification.success(t('httpPlugin.messages.followSuccess'));
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
  notification.success(t('httpPlugin.messages.cancelFollowSuccess'));
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
  notification.success(t('httpPlugin.messages.favouriteSuccess'));
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
  notification.success(t('httpPlugin.messages.cancelFavouriteSuccess'));
};

const save = async (data?: {
  description: string;
  projectId: string;
  name: string;
}, notificationFlag = true) => {
  const validFlag = await isValid();
  if (!validFlag) {
    return new Error(t('httpPlugin.messages.paramError'));
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
  }

  const formData = getData();
  params.script.task.pipelines = cloneDeep(formData.pipelines);

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
      props.replaceTabPane(props.tabKey, { _id: scenarioId, name: params.name, value: 'Web', scenarioInfo: { id: scenarioId } });
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
      description: params.description
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
   
    scenarioConfigData.value.script = {
      ...script,
      type: scriptType,
      task: {
        scriptContent: script.task?.scriptContent,
        scriptLanguage: script.task?.scriptLanguage
      }
    };

    if (script.task) {
      scenarioConfigData.value.script.issue = {
        ...script.task,
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

    setScriptConfig(scriptType);
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
    scenarioConfigData.value = {
      name,
      description: '',
      favourite: false,
      follow: false,
      id: undefined,
      plugin: 'Web',
      scriptId: undefined,
      scriptName: undefined,
      script: {
        type,
        plugin: 'Web',
        task: {
          scriptLanguage: 'TypeScript',
          scriptContent: '',
        }
      }
    };

    setScriptConfig(scenarioConfigData.value.script.type);

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
    type
  };
  return data;
};

const scriptId = computed((): string => {
  return saveFormConfigData.value?.scriptId;
});

const toolbarMenuItems = computed(() => [
  {
    key: 'debugResult',
    name: t('common.debugResult')
  },
  {
    key: 'logs',
    name: t('common.scheduleLog')
  },
  {
    key: 'execLog',
    name: t('common.execLog')
  }
]);

const toolbarExtraMenuItems = computed(() => [
  {
    key: 'toggle',
    name: t('actions.toggle')
  },
  {
    key: 'screen',
    name: t('actions.fullScreen')
  }
]);

const drawerMenuItems = computed(() => {
  const baseItems = [
    {
      icon: 'icon-fuwuxinxi',
      name: t('actions.save'),
      key: 'save'
    },
    {
      icon: 'icon-zhibiao',
      name: t('ftpPlugin.drawer.indicator'),
      key: 'indicator'
    },
    {
      icon: 'icon-zhihangceshi',
      key: 'testInfo',
      name: t('ftpPlugin.drawer.testInfo')
    },
    {
      icon: 'icon-lishijilu',
      name: t('ftpPlugin.drawer.activity'),
      key: 'activity'
    },
    {
      icon: 'icon-pinglun',
      name: t('common.comment'),
      key: 'comment'
    }
  ];

  if (scenarioConfigData.value?.id) {
    if (scenarioConfigData.value.plugin === 'Web') {
      return baseItems;
    }
    return baseItems.filter(item => !['indicator', 'testInfo'].includes(item.key));
  }

  return baseItems.filter(item => ['save'].includes(item.key));
});

const setGlobalTabActiveKey = (key: 'taskConfig' | 'executeConfig') => {
  activeKey.value = key;
};

provide('userInfo', props.userInfo);
provide('setGlobalTabActiveKey', setGlobalTabActiveKey);
</script>
<template>
  <Spin  class="h-full overflow-hidden text-3 pt-1.5">
    <Tabs
      v-model:activeKey="activeKey"
      class="pure-tab-wrap h-full leading-5"
      size="small">
      <template #rightExtra>
        <ButtonGroup :hideKeys="hideButtonSet" :debugLoading="debugLoading" @click="buttonGroupClick" />
      </template>
      <template #leftExtra>
        <SelectEnum
            v-if="scenarioConfigData?.script?.task"
            v-model:value="scenarioConfigData.script.task.scriptLanguage"
            class="w-40 mr-2"
            :enumKey="ScriptLanguage" />
        <SelectEnum
            :value="scenarioConfigData?.script?.type"
            class="w-40"
            :excludes="scriptTypeExcludes"
            :enumKey="ScriptTypeInfra"
            @change="scriptTypeChange" />
      </template>
    </Tabs>
    <div
      style="height: calc(100% - 38px);"
      class="w-full flex flex-nowrap items-center overflow-x-auto overflow-y-hidden">
      <div class="flex-1 h-full min-w-0">
        <div
          class="bg-white overflow-auto"
          :class="{ 'transition-150': !isMoving }"
          :style="'height:calc(100% - ' + height + 'px);'">
          <ScriptConfig
            ref="codeConfigRef"
            :readonly="false"
            :language="(scenarioConfigData?.script?.task?.scriptLanguage|| '').toLowerCase()"
            :value="scenarioConfigData?.script?.task?.scriptContent || ''" />
        </div>
        <Toolbar
          ref="toolbarRef"
          v-model:clientHeight="height"
          v-model:isFull="isFull"
          v-model:isOpen="isOpen"
          v-model:isMoving="isMoving"
          v-model:activeKey="toolbarActiveKey"
          :menuItems="toolbarMenuItems"
          :extraMenuItems="toolbarExtraMenuItems"
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
            <!-- <DebugResult :value="debugExecInfo" :httpError="debugHttpError" /> -->
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
            :projectId="props.projectId"
            :value="saveFormConfigData"
            :loading="loading"
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
        :onTips="t('ftpPlugin.auth.onTips')"
        :offTips="t('ftpPlugin.auth.offTips')"
        :title="t('ftpPlugin.auth.title')"
        @change="authFlagChange" />
    </AsyncComponent>

    <AsyncComponent :visible="exportModalVisible">
      <ExportScriptModal :id="scriptId" v-model:visible="exportModalVisible" />
    </AsyncComponent>

    <AsyncComponent :visible="selectModalVisible">
      <SelectScriptModal v-model:visible="selectModalVisible" plugin="Mobile" @ok="selectScriptOk" />
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
