<script setup lang="ts">
import { defineAsyncComponent, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute } from 'vue-router';
import { Button, TabPane, Tabs } from 'ant-design-vue';
import { Icon, Select, Spin } from '@xcan-angus/vue-ui';
import { useExecutionDetail } from './composables/useExecutionDetail';

// Async component imports
const ExecSetting = defineAsyncComponent(() => import('./Configuration.vue'));
const ExecLog = defineAsyncComponent(() => import('./Log.vue'));
const MonacoEditor = defineAsyncComponent(() => import('@/components/monacoEditor/index.vue'));
const Performance = defineAsyncComponent(() => import('./performance/index.vue'));
const FuncTest = defineAsyncComponent(() => import('./functional/index.vue'));
const TestResult = defineAsyncComponent(() => import('./result/index.vue'));
const ServiceConfig = defineAsyncComponent(() => import('./Server.vue'));

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
  scriptType?: string;
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
  loadscriptContent,
  getDetail,
  getInfo,
  handleRestart,
  handleStop,
  handleDelete,
  topTabsChange,
  scriptTypeChange,
  setException
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
          <template v-if="detail && ['CREATED','STOPPED','FAILED','COMPLETED','TIMEOUT'].includes(detail?.status?.value) && detail?.hasOperationPermission">
            <Button
              size="small"
              type="link"
              class="mr-5 node-action-btn"
              @click="handleRestart(detail)">
              <Icon
                icon="icon-huifu"
                class="mr-1 -mt-0.5 text-3" />
              {{ t('execution.info.start') }}
            </Button>
          </template>
          <template v-if="detail && ['PENDING','RUNNING'].includes(detail?.status?.value) && detail?.hasOperationPermission">
            <Button
              type="link"
              size="small"
              class="mr-5 node-action-btn"
              @click="handleStop(detail)">
              <Icon
                icon="icon-jinyong"
                class="mr-1 -mt-0.5 text-3" />
              {{ t('execution.info.stop') }}
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
            {{ t('execution.info.back') }}
          </Button>
        </RouterLink>
      </template>
      <TabPane
        key="1"
        :tab="t('execution.info.executionDetails')">
        <Performance
          v-if="detail && ['TEST_PERFORMANCE','TEST_STABILITY', 'MOCK_DATA', 'TEST_CUSTOMIZATION'].includes(detail.scriptType.value)"
          ref="performanceRef"
          v-model:loading="loading"
          :detail="detail"
          :exception="exception"
          @loaded="getInfo" />
        <FuncTest
          v-else-if="detail && detail.scriptType.value==='TEST_FUNCTIONALITY'"
          ref="funcRef"
          v-model:loading="loading"
          :execInfo="detail"
          :exception="exception"
          @loaded="getInfo" />
      </TabPane>
      <TabPane key="2" :tab="t('execution.info.executionConfig')">
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
      <TabPane key="4" :tab="t('execution.info.log')">
        <ExecLog
          v-model:loading="loading"
          :execId="detail?.id"
          :execNodes="detail?.execNodes || []"
          :schedulingNum="detail?.schedulingNum || ''"
          :lastSchedulingDate="detail?.lastSchedulingDate || ''"
          :lastSchedulingResult="detail?.lastSchedulingResult || []" />
      </TabPane>
      <TabPane
        v-if="detail?.plugin === 'Http' && detail.scriptSource && ['API', 'SCENARIO'].includes(detail.scriptSource.value) && detail.status.value === 'COMPLETED'"
        key="5"
        :tab="t('execution.info.testResult')">
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
