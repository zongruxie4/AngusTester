<script lang="ts" setup>
import { computed, defineAsyncComponent, inject, nextTick, onMounted, ref, Ref } from 'vue';
import { useI18n } from 'vue-i18n';
import {
  ActivityTimeline, AsyncComponent, Drawer, Icon, Input, Modal, modal, notification, Select, Spin, Toolbar
} from '@xcan-angus/vue-ui';
import { useRoute, useRouter } from 'vue-router';
import YAML from 'yaml';
import { Button } from 'ant-design-vue';

import { script } from '@/api/tester';
import { exec } from 'src/api/ctrl';
import { ai } from 'src/api/gm';
import { LANG_OPTIONS, TOOLBAR_EXTRA_MENUITEMS, TOOLBAR_MENUITEMS } from './data';

const { t } = useI18n();
import { PermissionKey, ScriptInfo } from '../PropsType';

const ScriptDetail = defineAsyncComponent(() => import('./scriptInfo.vue'));
const ScriptForm = defineAsyncComponent(() => import('./scriptForm.vue'));
const ExecutionRecord = defineAsyncComponent(() => import('@/views/script/detail/record/index.vue'));
const MonacoEditor = defineAsyncComponent(() => import('@/components/monacoEditor/index.vue'));
const ExportScriptModal = defineAsyncComponent(() => import('@/components/script/exportModal/index.vue'));
const DebugResult = defineAsyncComponent(() => import('./debugResult.vue'));
const DebugLog = defineAsyncComponent(() => import('./debugLog.vue'));
const ExecLog = defineAsyncComponent(() => import('@/views/script/detail/log/index.vue'));

const route = useRoute();
const router = useRouter();

const isAdmin = inject('isAdmin', ref(false));
const projectInfo = inject<Ref<{ id: string; avatar: string; name: string; }>>('projectInfo', ref({ id: '', avatar: '', name: '' }));
const aiEnabled = inject('aiEnabled', ref(false));

const formRef = ref();
const toolbarRef = ref();
const drawerRef = ref();

const antDrawerVisible = ref(false);
const aiScriptValue = ref('');
const aiKeywords = ref('');
const generating = ref(false);

const scriptInfo = ref<ScriptInfo>();
const permissionList = ref<PermissionKey[]>([]);

const loading = ref(false);
const exportVisible = ref(false);

const oldScriptValue = ref<string>('');
const scriptValue = ref<string>('');
const contentType = ref<'json' | 'yaml'>('yaml');
const pluginType = ref<string>();

const routeQuery = route.query;
const routeParams = route.params;

const scriptId = ref<string>(routeParams.id as string);
const pageNo = routeQuery?.pageNo ? +routeQuery?.pageNo : 1;
const pageSize = routeQuery?.pageSize ? +routeQuery?.pageSize : 10;

const toolbarActiveKey = ref<'debugResult' | 'logs' | undefined>(undefined);
const height = ref(34);
const isFull = ref(false);
const isOpen = ref(false);
const isMoving = ref(false);

const viewMode = ref<'view' | 'edit'>(routeQuery.type as 'view' | 'edit');
const activeDrawerKey = ref('basicInfo');
const isEditFlag = ref<boolean>(viewMode.value === 'edit');

const debugExecInfo = ref<{
  id: string;
  execNode: { id: string, name: string, ip: string, agentPort: string, publicIp: string };
  sampleContents: { [key: string]: any }[];
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
  };
}>();

const toDebug = async () => {
  try {
    const scriptConfig = YAML.parse(scriptValue.value);
    if (!scriptConfig) {
      return;
    }

    const params: {
      broadcast: true;
      scriptId: string;
      scriptType: string;
    } = {
      broadcast: true,
      scriptId: scriptId.value,
      scriptType: scriptConfig.type
    };
    loading.value = true;
    const [error, { data }] = await exec.startDebug(params);
    loading.value = false;
    if (error || !data) {
      return;
    }

    debugExecInfo.value = data;
    pluginType.value = data.plugin;
    if (typeof toolbarRef.value?.open === 'function') {
      toolbarRef.value.open('debugResult');
    }
  } catch (error) {
    notification.error(error.message);
  }
};

const closeToolbar = () => {
  if (typeof toolbarRef.value?.open === 'function') {
    toolbarRef.value.open();
  }
};

const fullScreen = () => {
  if (typeof toolbarRef.value?.fullScreen === 'function') {
    toolbarRef.value.fullScreen();
  }
};

const handleCancel = () => {
  isEditFlag.value = false;
  if (viewMode.value === 'view') {
    scriptValue.value = oldScriptValue.value;
    return;
  }

  router.push(`/script?pageNo=${pageNo}&pageSize=${pageSize}`);
};

const handleEdit = () => {
  if (typeof drawerRef.value?.open === 'function') {
    drawerRef.value.open('basicInfo');
  }
  isEditFlag.value = true;
};

const contentTypeChange = (value: 'json' | 'yaml') => {
  if (scriptValue.value) {
    scriptValue.value = value === 'json'
      ? JSON.stringify(YAML.parse(scriptValue.value), null, 2)
      : YAML.stringify(YAML.parse(scriptValue.value));
  }
};

const handleSave = () => {
  activeDrawerKey.value = 'basicInfo';
  drawerRef.value.open('basicInfo');
  nextTick(() => {
    formRef.value.validate().then(() => {
      const data = formRef.value.getFormData();
      if (!scriptId.value) {
        addScript(data);
      } else {
        updateScript(data);
      }
    });
  });
};

const aiGenerate = () => {
  antDrawerVisible.value = true;
};

const toGenerate = async () => {
  if (!aiEnabled.value) {
    return;
  }

  generating.value = true;
  const [error, res] = await ai.getChartResult({ type: 'WRITE_ANGUS_SCRIPT', question: aiKeywords.value });
  generating.value = false;
  if (error) {
    return;
  }

  const data = (res?.data || { normal: '', content: '' }) as {
    normal:string;
    content:string;
  };
  aiScriptValue.value = data.content;
};

const generateCancel = () => {
  antDrawerVisible.value = false;
};

const generateOk = () => {
  antDrawerVisible.value = false;
  scriptValue.value = aiScriptValue.value;
};

// 添加脚本
const addScript = async (formData) => {
  const scriptYaml = contentType.value === 'json' ? YAML.stringify(YAML.parse(scriptValue.value)) : scriptValue.value;
  loading.value = true;
  const [error, res] = await script.addScript({ ...formData, content: scriptYaml, projectId: projectId.value });
  loading.value = false;
  if (error) {
    return;
  }

  scriptId.value = res?.data?.id;
  notification.success(t('scriptDetail.messages.addScriptSuccess'));
  router.push('/script');
};

// 更新脚本
const updateScript = async (formData) => {
  const scriptYaml = contentType.value === 'json' ? YAML.stringify(YAML.parse(scriptValue.value)) : scriptValue.value;
  loading.value = true;
  const [error] = await script.updateScript({ ...formData, content: scriptYaml, id: scriptId.value, projectId: projectId.value });
  loading.value = false;
  if (error) {
    return;
  }

  notification.success(t('scriptDetail.messages.saveSuccess'));
  if (viewMode.value === 'view') {
    isEditFlag.value = false;

    if (scriptInfo.value) {
      // 更新脚本信息
      scriptInfo.value.name = formData.name;
      scriptInfo.value.description = formData.description;
      scriptInfo.value.type = {
        value: formData.type,
        message: formData.typeName
      };

      scriptInfo.value.content = scriptYaml;
    }

    return;
  }

  router.push(`/script?pageNo=${pageNo}&pageSize=${pageSize}`);
};

// 获取脚本详情
const loadScript = async () => {
  loading.value = true;
  const [error, res] = await script.getScriptDetail(scriptId.value);
  if (error) {
    loading.value = false;
    return;
  }

  const data = res?.data;
  if (data) {
    contentType.value = 'yaml';
    scriptValue.value = data.content;
    oldScriptValue.value = data.content;
    scriptInfo.value = data;

    await loadScriptListAuth(scriptId.value);
  }
};

const loadScriptListAuth = async (id: string) => {
  permissionList.value = [];

  loading.value = true;
  const [error, res] = await script.getScriptCurrentAuth([id]);
  loading.value = false;
  if (error) {
    return;
  }

  const item = res?.data?.[id];
  if (item) {
    const { scriptAuth, permissions } = item;
    let list: PermissionKey[] = [];
    const values = permissions.map(item => item.value);
    if (isAdmin.value || scriptAuth === false) {
      list = ['TEST', 'VIEW', 'MODIFY', 'DELETE', 'EXPORT', 'COLON'];
      if (values.includes('GRANT')) {
        list.push('GRANT');
      }
    } else {
      list = [...values];
      if (values.includes('VIEW') || !values.includes('COLON')) {
        list.push('COLON');
      }
    }

    permissionList.value = list;
  }
};

const handleDelete = async () => {
  modal.confirm({
    centered: true,
    content: t('scriptDetail.messages.deleteConfirm'),
    async onOk () {
      loading.value = true;
      const [error] = await script.deleteScript([scriptId.value]);
      loading.value = false;
      if (error) {
        return;
      }

      notification.success(t('scriptDetail.messages.deleteSuccess'));
      router.push('/script');
    }
  });
};

const handleClone = async () => {
  loading.value = true;
  const [error] = await script.cloneScript(scriptId.value);
  loading.value = false;
  if (error) {
    return;
  }
  notification.success(t('scriptDetail.messages.cloneSuccess'));
};

const handleExport = () => {
  exportVisible.value = true;
};

const handleExec = async () => {
  if (!scriptId.value) {
    return;
  }

  loading.value = true;
  const [error] = await exec.addByScript({ scriptId: scriptId.value });
  loading.value = false;
  if (error) {
    return;
  }

  notification.success(t('scriptDetail.messages.addExecutionSuccess'));
};

const loadDebugInfo = async () => {
  loading.value = true;
  const [error, { data }] = await exec.getDebugScriptInfo(scriptId.value);
  loading.value = false;
  if (error || !data) {
    return;
  }

  debugExecInfo.value = data;
  pluginType.value = data.plugin;
};

onMounted(() => {
  if (scriptId.value) {
    loadScript();
    loadDebugInfo();
  }

  drawerRef.value.open('basicInfo');
});

const projectId = computed(() => {
  return projectInfo.value?.id;
});

const pageType = computed(() => {
  if (viewMode.value === 'view') {
    return 'detail';
  }

  if (viewMode.value === 'edit' && scriptId.value) {
    return 'edit';
  }

  return 'create';
});

const drawerMenuItems = computed(() => {
  if (pageType.value === 'create') {
    return [
      {
        key: 'basicInfo',
        name: t('scriptDetail.tabs.basicInfo'),
        icon: 'icon-fuwuxinxi',
        noAuth: true
      }];
  }

  return [
    {
      key: 'basicInfo',
      name: t('scriptDetail.tabs.basicInfo'),
      icon: 'icon-fuwuxinxi',
      noAuth: true
    },
    {
      key: 'execRecord',
      name: t('scriptDetail.tabs.executionRecord'),
      icon: 'icon-zhihangceshi',
      noAuth: true
    },
    {
      key: 'activity',
      icon: 'icon-lishijilu',
      name: t('scriptDetail.tabs.activity'),
      noAuth: true
    }
  ];
});
</script>
<template>
  <Spin
    class="flex h-full"
    :spinning="loading"
    :delay="0">
    <div class="relative flex-1 h-full min-w-0">
      <div class="px-3 py-2 border-b flex justify-between">
        <Select
          v-model:value="contentType"
          class="w-40"
          :allowClear="false"
          :options="LANG_OPTIONS"
          @change="contentTypeChange" />

        <template v-if="isEditFlag">
          <div class="flex items-center space-x-2.5">
            <Button size="small" @click="handleCancel">
              {{ t('scriptDetail.actions.cancel') }}
            </Button>

            <Button
              v-if="!route.params.id&&viewMode==='edit'&&aiEnabled"
              size="small"
              type="primary"
              ghost
              @click="aiGenerate">
              智能生成
            </Button>

            <Button
              size="small"
              type="primary"
              @click="handleSave">
              {{ t('scriptDetail.actions.saveScript') }}
            </Button>
          </div>
        </template>

        <template v-else>
          <div class="flex-1 flex items-center justify-end space-x-2.5">
            <Button
              size="small"
              :disabled="!permissionList.includes('MODIFY')"
              @click="handleEdit">
              <Icon icon="icon-shuxie" class="mr-1" />
              <span>{{ t('scriptDetail.actions.edit') }}</span>
            </Button>
            <Button
              size="small"
              :disabled="!permissionList.includes('EXPORT')"
              @click="handleExport">
              <Icon icon="icon-daochu" class="mr-1" />
              <span>{{ t('scriptDetail.actions.export') }}</span>
            </Button>
            <Button
              size="small"
              :disabled="!permissionList.includes('VIEW')"
              @click="handleClone">
              <Icon icon="icon-fuzhi" class="mr-1" />
              <span>{{ t('scriptDetail.actions.clone') }}</span>
            </Button>
            <Button
              size="small"
              :disabled="!permissionList.includes('DELETE')"
              @click="handleDelete">
              <Icon icon="icon-qingchu" class="mr-1" />
              <span>{{ t('scriptDetail.actions.delete') }}</span>
            </Button>
            <Button
              size="small"
              :disabled="!permissionList.includes('TEST')"
              @click="toDebug">
              <Icon icon="icon-tiaoshi" class="mr-1" />
              <span>{{ t('scriptDetail.actions.debug') }}</span>
            </Button>
            <Button
              size="small"
              :disabled="!permissionList.includes('TEST')"
              @click="handleExec">
              <Icon icon="icon-zhihangjiaoben" class="mr-1" />
              <span>{{ t('scriptDetail.actions.addExecution') }}</span>
            </Button>
            <RouterLink :to="`/script?pageNo=${pageNo}&pageSize=${pageSize}`">
              <Button size="small">
                <Icon icon="icon-fanhui" class="mr-1" />
                <span>{{ t('scriptDetail.actions.back') }}</span>
              </Button>
            </RouterLink>
          </div>
        </template>
      </div>

      <div
        class="overflow-hidden"
        :class="{ 'transition-150': !isMoving }"
        :style="'height:calc(100% - ' + (height + 45) + 'px);'">
        <MonacoEditor
          v-model:value="scriptValue"
          class="h-full w-full py-3"
          :isFormat="true"
          :readOnly="!isEditFlag"
          :language="contentType" />
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
        class="relative z-1 bg-white">
        <template #toggle>
          <Icon
            :icon="isOpen ? 'icon-shouqi' : 'icon-spread'"
            style="font-size: 14px;cursor: pointer;"
            @click="closeToolbar" />
        </template>

        <template #screen>
          <Icon
            :icon="isFull ? 'icon-tuichuzuida' : 'icon-zuidahua'"
            style="font-size: 14px;cursor: pointer;"
            @click="fullScreen" />
        </template>

        <template #debugResult>
          <DebugResult :value="debugExecInfo" :pluginType="pluginType" />
        </template>

        <template #logs>
          <DebugLog :value="debugExecInfo" :pluginType="pluginType" />
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
      v-model:activeKey="activeDrawerKey"
      :destroyInactiveTabPane="false"
      :menuItems="drawerMenuItems">
      <template #basicInfo>
        <ScriptForm
          v-if="isEditFlag"
          ref="formRef"
          :dataSource="scriptInfo"
          style="height:calc(100% - 30px);"
          class="pr-3.5 mb-3.5 mt-4" />

        <ScriptDetail
          v-else
          :dataSource="scriptInfo"
          style="height:calc(100% - 30px);"
          class="pr-3.5 mb-3.5 mt-4" />
      </template>

      <template #execRecord>
        <ExecutionRecord
          :projectId="projectId"
          :scriptId="scriptId"
          style="height:calc(100% - 30px);"
          class="mb-3.5 mt-4" />
      </template>

      <template #activity>
        <ActivityTimeline
          :id="scriptId"
          style="height:calc(100% - 32px);"
          infoKey="description"
          class="pr-3.5 mb-3.5 mt-4.5"
          type="SCRIPT" />
      </template>
    </Drawer>
  </Spin>

  <AsyncComponent :visible="exportVisible">
    <ExportScriptModal v-model:visible="exportVisible" :ids="[scriptId]" />
  </AsyncComponent>

  <AsyncComponent :visible="antDrawerVisible">
    <Modal
      :visible="antDrawerVisible"
      :okButtonProps="{disabled:generating}"
      :width="1000"
      :title="t('scriptDetail.ai.title')"
      @cancel="generateCancel"
      @ok="generateOk">
      <div style="height: 400px;" class="w-full">
        <div class="flex flex-nowrap justify-between space-x-2.5 mb-3.5">
          <Input
            v-model:value="aiKeywords"
            :readonly="generating"
            :placeholder="t('scriptDetail.ai.placeholder')"
            trim
            allowClear
            class="flex-1"
            :maxlength="2000" />
          <Button
            :loading="generating"
            :disabled="!aiKeywords"
            type="primary"
            size="small"
            @click="toGenerate">
            {{ t('scriptDetail.actions.generate') }}
          </Button>
        </div>
        <MonacoEditor
          v-model:value="aiScriptValue"
          class="w-full"
          style="height:calc(100% - 38px);"
          isFormat
          readOnly
          language="yaml" />
      </div>
    </Modal>
  </AsyncComponent>
</template>

<style scoped>
.transition-150 {
  transition: all 150ms linear 0ms;
}
</style>
