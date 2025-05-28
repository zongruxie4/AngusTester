<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Button, TabPane, Tabs } from 'ant-design-vue';
import { Icon, modal, notification, Select, Spin } from '@xcan-angus/vue-ui';
import YAML from 'yaml';

import { Exception, ExecObj } from './PropsType';
import { exec } from 'src/api/ctrl';


const ExecSetting = defineAsyncComponent(() => import('@/views/execution/info/setting/index.vue'));
const ExecLog = defineAsyncComponent(() => import('@/views/execution/info/log/index.vue'));
const MonacoEditor = defineAsyncComponent(() => import('@/components/monacoEditor/index.vue'));
const Performance = defineAsyncComponent(() => import('@/views/execution/info/perf/index.vue'));
const FuncTest = defineAsyncComponent(() => import('@/views/execution/info/funcTest/index.vue'));
const TestResult = defineAsyncComponent(() => import('@/views/execution/info/testResult/index.vue'));
const ServiceConfig = defineAsyncComponent(() => import('@/views/execution/info/server/index.vue'));

interface Props {
  execId?: string;
  showBackBtn: boolean;
  monicaEditorStyle: {[key: string]:string};
  scriptType?: string;

}
const props = withDefaults(defineProps<Props>(), {
  execId: undefined,
  showBackBtn: true,
  monicaEditorStyle: () => ({}),
  scriptType: undefined
});
const emit = defineEmits<{(e: 'del'):void}>();

const route = useRoute();
const router = useRouter();

const performanceRef = ref();
const funcRef = ref();

const id = props.execId || route.params.id as string;
const pageNo = route.query.pageNo;

const topActiveKey = ref(route.query?.tab === 'log' ? '4' : '1');

const loading = ref(false);
const detail = ref<ExecObj>();
const scriptInfo = ref();
const scriptYamlStr = ref('');

const loadscriptContent = async () => {
  const [error, { data }] = await exec.loadScriptByExecId(id);
  if (error) {
    return;
  }
  scriptYamlStr.value = data;
};

const getDetail = async () => {
  loading.value = true;
  const [error, { data }] = await exec.getDetail(id);

  if (error) {
    loading.value = false;
    return;
  }

  detail.value = data;

  if (detail.value?.scriptType.value === 'MOCK_DATA') {
    detail.value.batchRows = detail.value.task?.mockData?.settings.batchRows || '1';
  }

  const { configuration, plugin, task, scriptId, scriptType } = data;
  scriptInfo.value = {
    id: scriptId,
    type: scriptType.value,
    plugin,
    configuration,
    task
  };
};

const getInfo = (data) => {
  if (!data) {
    return;
  }
  const keys = Object.keys(data);
  if (!keys.length || !detail.value) {
    return;
  }

  for (const key in data) {
    detail.value[key] = data[key];
  }

  setException();
};



onMounted(async () => {
  if (!id) {
    const scriptTypeMsgConfig = {
      TEST_PERFORMANCE: '性能测试',
      TEST_STABILITY: '稳定性测试',
      TEST_FUNCTIONALITY: '功能能测试',
      TEST_CUSTOMIZATION: '自定义测试',
    }
    if (props.scriptType) {
      detail.value = {
        scriptType: {
          value: props.scriptType,
          message: scriptTypeMsgConfig[props.scriptType] || ''
        }
      }
    }
    return;
  }
  await getDetail();
  setException();
});

const handleRestart = async (item:ExecObj) => {
  if (['TEST_PERFORMANCE', 'TEST_STABILITY'].includes(detail.value?.scriptType.value) && performanceRef.value) {
    performanceRef.value.resetData();
  }

  if (detail.value?.scriptType.value === 'MOCK_DATA') {
    performanceRef.value.restartMock();
  }

  exception.value = undefined;
  const _params = {
    broadcast: true,
    id: item.id
  };
  loading.value = true;
  const [error, { data }] = await exec.restart(_params);

  if (error) {
    loading.value = false;
    if (error?.message) {
      exception.value = { code: error?.code, message: error.message, codeName: '退出码', messageName: '失败原因' };
    }
    return;
  }

  const currItemDataList = data.filter(f => f.execId === item.id);
  if (currItemDataList.length) {
    const successFalseItem = currItemDataList.find(f => f.success);
    if (successFalseItem) {
      notification.success('启动成功');
      exception.value = undefined;
    } else {
      notification.error(currItemDataList[0].message);
      exception.value = { code: currItemDataList[0]?.exitCode, message: currItemDataList[0]?.message, codeName: '退出码', messageName: '失败原因' };
    }
  }

  await getDetail();
  setException();
};

const handleStop = async (item:ExecObj) => {
  exception.value = undefined;
  const _params = {
    broadcast: true,
    id: item.id
  };
  loading.value = true;
  const [error, { data }] = await exec.stop(_params);
  loading.value = false;
  if (error) {
    if (error?.message) {
      exception.value = { code: error?.code || '', message: error.message, codeName: '退出码', messageName: '失败原因' };
    }
    return;
  }

  const currItemDataList = data.filter(f => f.execId === item.id);
  if (currItemDataList.length) {
    const successFalseItem = currItemDataList.find(f => f.success);
    if (successFalseItem) {
      notification.success('停止成功');
      exception.value = undefined;
    } else {
      notification.error(currItemDataList[0].message);
      exception.value = { code: currItemDataList[0]?.exitCode, message: currItemDataList[0]?.message, codeName: '退出码', messageName: '失败原因' };
    }
  }

  await getDetail();
  setException();
};

const handleDelete = async (item:ExecObj) => {
  modal.confirm({
    centered: true,
    content: `确定删除【${item.name}】吗？`,
    async onOk () {
      loading.value = true;
      const [error] = await exec.del([item.id]);
      loading.value = false;
      if (error) {
        return;
      }
      notification.success('删除成功');
      if (props.showBackBtn) {
        router.push('/execution');
      } else {
        emit('del');
      }
    }
  });
};

const topTabsChange = (value:string) => {
  if (value === '3' && !scriptYamlStr.value) {
    loadscriptContent();
  }
};

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

const scriptLanguageType = ref('yaml');
const scriptTypeChange = (value:'json' | 'yaml') => {
  scriptLanguageType.value = value;
  if (scriptYamlStr.value) {
    scriptYamlStr.value = value === 'json'
      ? JSON.stringify(YAML.parse(scriptYamlStr.value), null, 4)
      : YAML.stringify(YAML.parse(scriptYamlStr.value));
  }
};

const exception = ref<Exception>();

const setException = () => {
  const lastSchedulingResult = detail.value?.lastSchedulingResult;
  const meterMessage = detail.value?.meterMessage;
  if (lastSchedulingResult?.length) {
    const foundItem = lastSchedulingResult.find(f => !f.success);
    if (foundItem) {
      exception.value = { code: foundItem.exitCode, message: foundItem.message, codeName: '退出码', messageName: '失败原因' };
      return;
    }

    if (meterMessage) {
      exception.value = { code: detail.value?.meterStatus || '', message: meterMessage, codeName: '采样状态', messageName: '失败原因' };
      return;
    }
  }

  if (meterMessage) {
    exception.value = { code: detail.value?.meterStatus || '', message: meterMessage, codeName: '采样状态', messageName: '失败原因' };
    return;
  }

  exception.value = undefined;
};
</script>
<template>
  <Spin class="h-full pb-3.5" :spinning="loading">
    <Tabs
      v-model:activeKey="topActiveKey"
      class="h-full flex flex-col header-tabs"
      size="small"
      @change="topTabsChange">
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
              启动
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
              停止
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
              删除
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
            返回
          </Button>
        </RouterLink>
      </template>
      <TabPane
        key="1"
        tab="执行详情">
        <Performance
          v-if="detail && ['TEST_PERFORMANCE','TEST_STABILITY', 'MOCK_DATA', 'TEST_CUSTOMIZATION'].includes(detail.scriptType.value)"
          v-model:loading="loading"
          ref="performanceRef"
          :detail="detail"
          :exception="exception"
          @loaded="getInfo" />
        <FuncTest
          v-else-if="detail && detail.scriptType.value==='TEST_FUNCTIONALITY'"
          v-model:loading="loading"
          ref="funcRef"
          :execInfo="detail"
          :exception="exception"
          @loaded="getInfo" />
      </TabPane>
      <TabPane key="2" tab="执行配置">
        <ExecSetting
          v-model:loading="loading"
          :execId="id"
          :execName="detail?.name"
          :scriptInfo="scriptInfo" />
      </TabPane>
      <TabPane
        v-if="detail?.plugin === 'Http'"
        key="serviceConfig"
        tab="服务器配置">
        <ServiceConfig :execId="id" />
      </TabPane>
      <TabPane key="3" tab="执行脚本">
        <Select
          :value="scriptLanguageType"
          class="w-40 mb-2"
          :allowClear="false"
          :options="languageOpt"
          @change="scriptTypeChange" />
        <MonacoEditor
          :value="scriptYamlStr"
          class="w-full"
          style="height: calc(100% - 36px);"
          :style="props.monicaEditorStyle"
          :isFormat="true"
          :readOnly="true"
          :language="scriptLanguageType" />
      </TabPane>
      <TabPane key="4" tab="日志">
        <ExecLog
          v-model:loading="loading"
          :execId="detail?.id"
          :execNodes="detail?.execNodes || []"
          :schedulingNum="detail?.schedulingNum"
          :lastSchedulingDate="detail?.lastSchedulingDate"
          :lastSchedulingResult="detail?.lastSchedulingResult || []" />
      </TabPane>
      <TabPane
        v-if="detail?.plugin === 'Http' && ['API', 'SCENARIO'].includes(detail.scriptSource?.value) && detail.status.value === 'COMPLETED'"
        key="5"
        tab="测试结果">
        <TestResult
          :execId="detail?.id"
          :execInfo="detail" />
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
