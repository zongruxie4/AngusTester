<script setup lang="ts">
import { defineAsyncComponent, ref, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute } from 'vue-router';
import { Button, TabPane, Tabs } from 'ant-design-vue';
import { Icon, Select, Spin } from '@xcan-angus/vue-ui';
import { ScriptType, ScriptSource } from '@xcan-angus/infra';
import { ExecStatus } from '@/enums/enums';

import { useExecutionDetail } from './composables/useExecutionDetail';

// Async component imports
const MonacoEditor = defineAsyncComponent(() => import('@/components/editor/MonacoEditor/index.vue'));
const ExecSetting = defineAsyncComponent(() => import('./Configuration.vue'));
const ExecLog = defineAsyncComponent(() => import('./ExecLog.vue'));
const PerformanceTest = defineAsyncComponent(() => import('./performance/index.vue'));
const FunctionalTest = defineAsyncComponent(() => import('./functional/index.vue'));
const TestResult = defineAsyncComponent(() => import('./result/index.vue'));
const ServiceConfig = defineAsyncComponent(() => import('./Server.vue'));
// const MobileResult = defineAsyncComponent(() => import('./MobileExecPerfDetail.vue'));

/**
 * Props for the Execution Detail component
 */
interface Props {
  /** Execution ID */
  execId?: string;
  /** Whether to show back button */
  showBackBtn: boolean;
  /** Monaco editor style */
  monicaEditorStyle: {[key: string]:string};
  /** Script type */
  scriptType?: ScriptType;
  /** Plugin type */
  plugin?: string;
}

// Define component props with defaults
const props = withDefaults(defineProps<Props>(), {
  execId: undefined,
  showBackBtn: true,
  monicaEditorStyle: () => ({}),
  scriptType: undefined,
  plugin: undefined
});

// Define component events
const emit = defineEmits<{(e: 'del'):void}>();

// Initialize internationalization and route
const { t } = useI18n();
const route = useRoute();

// Use execution detail composable for logic
const {
  loading,
  detail,
  scriptInfo,
  scriptYamlStr,
  topActiveKey,
  exception,
  performanceRef,
  funcRef,
  getInfo,
  handleRestart,
  handleStop,
  handleDelete,
  topTabsChange,
  scriptTypeChange
} = useExecutionDetail(props, emit);

// Language options for script editor
const languageOpt = [
  {
    value: 'yaml',
    label: 'YAML'
  },
  {
    value: 'json',
    label: 'JSON'
  }
];

const showPerformanceTest = computed(() => {
  return detail.value
    && [ScriptType.TEST_PERFORMANCE,ScriptType.TEST_STABILITY, ScriptType.MOCK_DATA, ScriptType.TEST_CUSTOMIZATION].includes(detail.value.scriptType.value);
});

const showFuncTest = computed(() => {
  return detail.value && [ScriptType.TEST_FUNCTIONALITY, ScriptType.TEST_COMPATIBILITY, ScriptType.TEST_COMPLIANCE].includes(detail.value.scriptType.value);
});

// Reactive state for script language type
const scriptLanguageType = ref('yaml');

// Execution ID from props or route
const id = props.execId || route.params.id as string;

// Page number from route query
const pageNo = route.query.pageNo;
</script>
<template>
  <Spin class="h-full pb-3.5" :spinning="loading">
    <Tabs
      v-model:activeKey="topActiveKey"
      class="h-full flex flex-col header-tabs"
      size="small"
      @change="(value) => topTabsChange(value as string)">
      <template #rightExtra>
        <template v-if="topActiveKey === '1'">
          <template
            v-if="detail
              && [ExecStatus.CREATED,ExecStatus.STOPPED,ExecStatus.FAILED,ExecStatus.COMPLETED,ExecStatus.TIMEOUT].includes(detail?.status?.value)
              && detail?.hasOperationPermission">
            <Button
              size="small"
              type="link"
              class="mr-5 node-action-btn"
              @click="handleRestart(detail)">
              <Icon
                icon="icon-huifu"
                class="mr-1 -mt-0.5 text-3" />
              {{ t('actions.start') }}
            </Button>
          </template>
          <template
            v-if="detail
              && [ExecStatus.PENDING,ExecStatus.RUNNING].includes(detail?.status?.value)
              && detail?.hasOperationPermission">
            <Button
              type="link"
              size="small"
              class="mr-5 node-action-btn"
              @click="handleStop(detail)">
              <Icon
                icon="icon-jinyong"
                class="mr-1 -mt-0.5 text-3" />
              {{ t('actions.stop') }}
            </Button>
          </template>
          <template v-if="detail?.hasOperationPermission">
            <Button
              type="link"
              size="small"
              class="mr-5 node-action-btn"
              @click="handleDelete(detail)">
              <Icon
                icon="icon-qingchu"
                class="mr-1 -mt-0.5 text-3" />
              {{ t('actions.delete') }}
            </Button>
          </template>
        </template>
        <RouterLink :to="pageNo ? `/execution?pageNo=${pageNo}` : `/execution`">
          <Button
            v-show="props.showBackBtn"
            class="node-action-btn"
            size="small"
            type="link">
            <Icon
              icon="icon-fanhui"
              class="mr-1 -mt-0.5 text-3" />
            {{ t('actions.back') }}
          </Button>
        </RouterLink>
      </template>
      <TabPane
        key="1"
        :tab="t('execution.info.executionDetails')">
        <PerformanceTest
          v-if="showPerformanceTest"
          ref="performanceRef"
          v-model:loading="loading"
          :detail="detail"
          :exception="exception"
          @loaded="getInfo" />
        <FunctionalTest
          v-else-if="showFuncTest"
          ref="funcRef"
          v-model:loading="loading"
          :execInfo="detail"
          :exception="exception"
          @loaded="getInfo" />
      </TabPane>
      <TabPane key="2" :tab="t('common.execConfig')">
        <ExecSetting
          v-model:loading="loading"
          :execId="id"
          :execName="detail?.name"
          :scriptInfo="scriptInfo" />
      </TabPane>
      <TabPane
        v-if="detail?.plugin === 'Http'"
        key="serviceConfig"
        :tab="t('execution.info.serverConfig')">
        <ServiceConfig :execId="id" />
      </TabPane>
      <TabPane key="3" :tab="t('execution.info.executionScript')">
        <Select
          :value="scriptLanguageType"
          class="w-40 mb-2"
          :allowClear="false"
          :options="languageOpt"
          @change="(value) => scriptTypeChange(value as 'json' | 'yaml')" />
        <MonacoEditor
          :value="scriptYamlStr"
          class="w-full"
          style="height: calc(100% - 36px);"
          :style="props.monicaEditorStyle"
          :isFormat="true"
          :readOnly="true"
          :language="scriptLanguageType as 'text' | 'json' | 'html' | 'typescript' | 'yaml' | undefined" />
      </TabPane>
      <TabPane key="4" :tab="t('common.execLog')">
        <ExecLog
          v-model:loading="loading"
          :execId="detail?.id"
          :execNodes="detail?.execNodes || []"
          :schedulingNum="detail?.schedulingNum || ''"
          :lastSchedulingDate="detail?.lastSchedulingDate || ''"
          :lastSchedulingResult="detail?.lastSchedulingResult || []" />
      </TabPane>
      <TabPane
        v-if="detail?.plugin === 'Http'
          && detail.scriptSource
          && [ScriptSource.API, ScriptSource.SCENARIO].includes(detail.scriptSource.value)
          && detail.status.value === ExecStatus.COMPLETED"
        key="5"
        :tab="t('common.testResult')">
        <TestResult
          :execId="detail?.id"
          :execInfo="detail as any" />
      </TabPane>
    </Tabs>
  </Spin>
</template>
<style scoped>
.header-tabs > :deep(.ant-tabs-content-holder) {
  @apply overflow-y-auto px-5;
}

.header-tabs > :deep(.ant-tabs-nav ){
  @apply h-10 pr-5 font-medium;

  background-image: linear-gradient(to bottom, rgb(255, 255, 255) 13%, rgb(245, 251, 255) 100%);
}

.header-tabs > :deep(.ant-tabs-nav > .ant-tabs-nav-wrap) {
  @apply pl-5;
}

.node-action-btn {
  @apply rounded mr-2 text-text-content text-3 border-0  px-2 shadow-none inline-flex items-center;
}

.node-action-btn :deep(span) {
  @apply ml-1;
}

.node-action-btn[disabled],
.node-action-btn:not([disabled]),
.node-action-btn:focus {
  @apply bg-transparent !important;
}

.node-action-btn[disabled] {
  @apply opacity-50;
}

.node-action-btn:not([disabled]):hover {
  @apply text-text-link;
}

.node-action-btn::after {
  @apply hidden;
}
</style>
