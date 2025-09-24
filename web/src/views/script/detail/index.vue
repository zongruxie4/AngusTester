<script lang="ts" setup>
import { computed, defineAsyncComponent, inject, nextTick, onMounted, ref, Ref } from 'vue';
import { useI18n } from 'vue-i18n';
import {
  ActivityTimeline, AsyncComponent, Drawer, Icon, Input, Modal, modal, Select, Spin, Toolbar
} from '@xcan-angus/vue-ui';
import { useRoute, useRouter } from 'vue-router';
import YAML from 'yaml';
import { Button } from 'ant-design-vue';
import { ScriptPermission } from '@/enums/enums';

import {
  useScriptData,
  useDebug,
  useToolbar,
  useAI,
  useDrawer
} from './composables';
import { appContext } from '@xcan-angus/infra';

const { t } = useI18n();

const ScriptInfo = defineAsyncComponent(() => import('./ScriptInfo.vue'));
const ScriptForm = defineAsyncComponent(() => import('./ScriptForm.vue'));
const MonacoEditor = defineAsyncComponent(() => import('@/components/monacoEditor/index.vue'));
const ExportScriptModal = defineAsyncComponent(() => import('@/components/script/ExportScriptModal.vue'));
const DebugResult = defineAsyncComponent(() => import('./DebugResult.vue'));
const DebugLog = defineAsyncComponent(() => import('./DebugLog.vue'));
const ExecLog = defineAsyncComponent(() => import('@/views/script/detail/ExecLog.vue'));
const ExecRecord = defineAsyncComponent(() => import('@/views/script/detail/ExecRecord.vue'));

const route = useRoute();
const router = useRouter();

const isAdmin = computed(() => appContext.isAdmin());
const projectInfo = inject<Ref<{ id: string; avatar: string; name: string; }>>('projectInfo', ref({ id: '', avatar: '', name: '' }));
const aiEnabled = inject('aiEnabled', ref(false));

const formRef = ref();
const toolbarRef = ref();
const drawerRef = ref();

// Use composables
const {
  FORMAT_OPTIONS,

  // Data refs
  scriptInfo,
  permissionList,
  loading,
  oldScriptValue,
  scriptValue,
  contentType,
  scriptId,
  pageNo,
  pageSize,
  viewMode,
  pageType,
  projectId,

  // Methods
  loadScript,
  addScript,
  updateScript,
  deleteScript,
  cloneScript,
  executeScript
} = useScriptData(projectInfo, isAdmin);

const {
  debugExecInfo,
  pluginType,
  startDebug,
  loadDebugInfo
} = useDebug(scriptId, scriptValue, toolbarRef);

const {
  TOOLBAR_EXTRA_MENU_ITEMS,
  TOOLBAR_MENU_ITEMS,
  toolbarActiveKey,
  height,
  isFull,
  isOpen,
  isMoving,
  closeToolbar,
  toggleFullScreen
} = useToolbar();

const {
  antDrawerVisible,
  aiScriptValue,
  aiKeywords,
  generating,
  openAIDrawer,
  generateScript,
  cancelGenerate,
  confirmGenerate
} = useAI(aiEnabled);

const {
  activeDrawerKey,
  isEditFlag,
  drawerMenuItems,
  handleEdit,
  handleCancel
} = useDrawer(pageType, viewMode);

// Handle save action
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

// Handle delete action
const handleDelete = async () => {
  modal.confirm({
    centered: true,
    content: t('scriptDetail.messages.deleteConfirm'),
    async onOk () {
      await deleteScript();
    }
  });
};

// Handle export action
const exportVisible = ref(false);
const handleExport = () => {
  exportVisible.value = true;
};

// Content type change handler
const contentTypeChange = (value: 'json' | 'yaml') => {
  if (scriptValue.value) {
    scriptValue.value = value === 'json'
      ? JSON.stringify(YAML.parse(scriptValue.value), null, 2)
      : YAML.stringify(YAML.parse(scriptValue.value));
  }
};

onMounted(() => {
  if (scriptId.value) {
    loadScript();
    loadDebugInfo();
  }

  drawerRef.value.open('basicInfo');
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
          :options="FORMAT_OPTIONS"
          @change="contentTypeChange" />

        <template v-if="isEditFlag">
          <div class="flex items-center space-x-2.5">
            <Button size="small" @click="handleCancel(oldScriptValue, scriptValue, viewMode, pageNo, pageSize, router)">
              {{ t('actions.cancel') }}
            </Button>

            <Button
              v-if="!route.params.id&&viewMode==='edit'&&aiEnabled"
              size="small"
              type="primary"
              ghost
              @click="openAIDrawer">
              {{ t('scriptDetail.actions.smartGenerate') }}
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
              :disabled="!permissionList.includes(ScriptPermission.MODIFY)"
              @click="() => handleEdit(drawerRef)">
              <Icon icon="icon-shuxie" class="mr-1" />
              <span>{{ t('actions.edit') }}</span>
            </Button>
            <Button
              size="small"
              :disabled="!permissionList.includes(ScriptPermission.EXPORT)"
              @click="handleExport">
              <Icon icon="icon-daochu" class="mr-1" />
              <span>{{ t('actions.export') }}</span>
            </Button>
            <Button
              size="small"
              :disabled="!permissionList.includes(ScriptPermission.VIEW)"
              @click="cloneScript">
              <Icon icon="icon-fuzhi" class="mr-1" />
              <span>{{ t('actions.clone') }}</span>
            </Button>
            <Button
              size="small"
              :disabled="!permissionList.includes(ScriptPermission.DELETE)"
              @click="handleDelete">
              <Icon icon="icon-qingchu" class="mr-1" />
              <span>{{ t('actions.delete') }}</span>
            </Button>
            <Button
              size="small"
              :disabled="!permissionList.includes(ScriptPermission.TEST)"
              @click="startDebug">
              <Icon icon="icon-tiaoshi" class="mr-1" />
              <span>{{ t('actions.debug') }}</span>
            </Button>
            <Button
              size="small"
              :disabled="!permissionList.includes(ScriptPermission.TEST)"
              @click="executeScript">
              <Icon icon="icon-zhihangjiaoben" class="mr-1" />
              <span>{{ t('actions.addExecution') }}</span>
            </Button>
            <RouterLink :to="`/script?pageNo=${pageNo}&pageSize=${pageSize}`">
              <Button size="small">
                <Icon icon="icon-fanhui" class="mr-1" />
                <span>{{ t('actions.back') }}</span>
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
        :menuItems="TOOLBAR_MENU_ITEMS"
        :extraMenuItems="TOOLBAR_EXTRA_MENU_ITEMS"
        class="relative z-1 bg-white">
        <template #toggle>
          <Icon
            :icon="isOpen ? 'icon-shouqi' : 'icon-spread'"
            style="font-size: 14px;cursor: pointer;"
            @click="() => closeToolbar(toolbarRef)" />
        </template>

        <template #screen>
          <Icon
            :icon="isFull ? 'icon-tuichuzuida' : 'icon-zuidahua'"
            style="font-size: 14px;cursor: pointer;"
            @click="() => toggleFullScreen(toolbarRef)" />
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

        <ScriptInfo
          v-else
          :dataSource="scriptInfo"
          style="height:calc(100% - 30px);"
          class="pr-4 mb-4 mt-5" />
      </template>

      <template #execRecord>
        <ExecRecord
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
      @cancel="cancelGenerate"
      @ok="() => confirmGenerate(scriptValue)">
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
            @click="generateScript">
            {{ t('actions.generate') }}
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
