<script setup lang="ts">
import { inject, defineAsyncComponent, ref, computed } from 'vue';
import { Alert, Button, Collapse, CollapsePanel, Tabs, TabPane } from 'ant-design-vue';
import { Arrow, Colon, Icon, HttpMethodText } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';

import { useI18n } from 'vue-i18n';

import StatusTag from './StatusTag.vue';
// import { HTTPInfo } from './PropsType';
import { ExecContent } from './PropsType';
const { t } = useI18n();
// import { formatTime } from 'lib/core/utils';

export interface Props {
  content: ExecContent[];
  ignoreAssertions: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  content: undefined,
  ignoreAssertions: undefined
});

const RequestHeaders = defineAsyncComponent(() => import('./RequestHeaders.vue'));
const RequestBody = defineAsyncComponent(() => import('./RequestBody.vue'));
const ResponseBody = defineAsyncComponent(() => import('./ResponseBody.vue'));
const TimeLine = defineAsyncComponent(() => import('./TimeLine.vue'));
const AssertionsResult = defineAsyncComponent(() => import('./Assertion.vue'));

const setGlobalTabActiveKey = inject<(key:'executeConfig')=>void>('setGlobalTabActiveKey');

const formatTime = (timestamp:number):string => {
  const second = 1000;
  const minute = 60 * second;
  const hour = 60 * minute;
  if (timestamp < second) {
    return timestamp + t('xcan_scenarioDebugResult.milliseconds');
  }

  if (timestamp < minute) {
    return timestamp / second + t('xcan_scenarioDebugResult.seconds');
  }

  if (timestamp < hour) {
    const remainder = timestamp % minute;
    if (remainder === 0) {
      return timestamp / minute + t('xcan_scenarioDebugResult.minutes');
    }

    return Math.floor(timestamp / minute) + t('xcan_scenarioDebugResult.minutes') + remainder / second + t('xcan_scenarioDebugResult.seconds');
  }

  const remainder = timestamp % hour;
  if (remainder === 0) {
    return timestamp / hour + t('xcan_scenarioDebugResult.hours');
  }

  let suffix = '';
  if (remainder < hour) {
    const _remainder = remainder % minute;
    if (_remainder === 0) {
      suffix += remainder / minute + t('xcan_scenarioDebugResult.minutes');
    } else {
      suffix += Math.floor(remainder / minute) + t('xcan_scenarioDebugResult.minutes') + _remainder / second + t('xcan_scenarioDebugResult.seconds');
    }
  }

  return Math.floor(timestamp / hour) + t('xcan_scenarioDebugResult.hours') + suffix;
};

// const UUID = utils.uuid();
// const collapseActiveKey = ref<string>();
// const arrowOpen = ref(collapseActiveKey.value === UUID);

const arrowOpenConfig = ref({});

const arrowChange = (key: string) => {
  // arrowOpen.value = open;
  arrowOpenConfig.value[key] = !arrowOpenConfig.value[key];
  if (!arrowOpenConfig.value[key]) {
    delete arrowOpenConfig.value[key];
  }
  // if (open) {
  //   collapseActiveKey.value = UUID;
  //   return;
  // }

  // collapseActiveKey.value = undefined;
};

const collapseActiveKey = computed(() => {
  return Object.keys(arrowOpenConfig.value);
});

// const httpContent = computed(() => {
//   const linkName = props.value?.linkName;
//   if (!linkName) {
//     return undefined;
//   }

//   return props.content?.find(item => linkName === item.name);
// });

const method = (httpContent) => {
  return httpContent?.content?.request0?.method;
};

const url = (httpContent) => {
  return httpContent.value?.content?.request0?.url;
};

const bodySize = (httpContent) => {
  const size = httpContent?.content?.response?.bodySize;
  if (size === undefined || size === null) {
    return '--';
  }

  return utils.formatBytes(+size);
};

const getRuntime = (httpContent) => {
  const timeStamp = httpContent?.content?.response?.timeline?.total;
  if (timeStamp === undefined || timeStamp === null) {
    return '--';
  }

  return formatTime(+timeStamp);
};

const status = (httpContent) => {
  if (!httpContent) {
    return 'block';
  }

  return httpContent.content?.success ? 'success' : 'fail';
};

const failMessage = (httpContent) => {
  return httpContent?.content?.failMessage;
};

// const showBasicInfo = computed(() => {
//   return !!httpContent.value;
// });
</script>
<template>
  <div class="space-y-3">
    <Collapse
      v-for="httpContent in props.content"
      :key="httpContent.key"
      :activeKey="collapseActiveKey"
      style="background-color: #fff;font-size: 12px;"
      class="timeline-collapse">
      <CollapsePanel
        :key="httpContent.key"
        :showArrow="false"
        collapsible="disabled">
        <template #header>
          <div class="w-full flex items-center px-3 py-2.5 cursor-pointer" @click="arrowChange(httpContent.key)">
            <Icon
              class="flex-shrink-0 text-4 mr-3"
              icon="icon-httpcanshu" />
            <div :title="httpContent?.name" class="truncate min-w-55 max-w-100 mr-5 name">{{ httpContent?.name }}</div>
            <HttpMethodText
              class="mr-5"
              :value="method(httpContent)"
              style="min-width: 0;" />
            <div :title="url(httpContent)" class="flex-1 truncate mr-5">{{ url(httpContent) }}</div>
            <div class="flex-1 justify-end flex items-center mr-3">
              <template>
                <div class="mr-5">
                  <span class="mr-0.5">{{ t('protocol.statusCode') }}<Colon /></span>
                  <span class="text-theme-sub-content">{{ httpContent?.content?.response?.status || '--' }}</span>
                </div>
                <div class="mr-5">
                  <span class="mr-0.5">{{ t('common.duration') }}<Colon /></span>
                  <span class="text-theme-sub-content"> {{ getRuntime(httpContent) }}</span>
                </div>
                <div class="mr-5">
                  <span class="mr-0.5">{{ t('common.size') }}<Colon /></span>
                  <span class="text-theme-sub-content"> {{ bodySize(httpContent) }}</span>
                </div>
              </template>
              <StatusTag :value="status(httpContent)" />
            </div>
            <Arrow :open="!!arrowOpenConfig[httpContent.key]" @change="arrowChange(httpContent.key)" />
          </div>
        </template>
        <Alert
          v-if="!!failMessage(httpContent)"
          :message="failMessage(httpContent)"
          type="error"
          class="mt-3" />
        <Tabs
          type="card"
          size="small"
          class="mt-3 card-tabs">
          <TabPane key="general" :tab="t('common.basicInfo')">
            <RequestHeaders :value="httpContent" class="py-3" />
          </TabPane>
          <TabPane key="requestBody" :tab="t('protocol.requestBody')">
            <RequestBody :value="httpContent" class="py-3" />
          </TabPane>
          <TabPane key="response" :tab="t('protocol.response')">
            <ResponseBody
              :url="httpContent?.content?.request0?.url"
              :value="httpContent?.content?.response"
              class="py-3" />
          </TabPane>
          <TabPane key="timeline" :tab="t('common.timeline')">
            <TimeLine :value="httpContent?.content?.response?.timeline" />
          </TabPane>
          <TabPane key="assertions" :tab="t('common.assertionsResult')">
            <Alert
              v-if="props.ignoreAssertions===true"
              closable
              type="info"
              style="border-radius: 4px;"
              class="mt-3">
              <template #message>
                <div class="leading-5 text-3 flex items-center whitespace-pre-line flex-wrap">
                  {{ t('xcan_scenarioDebugResult.ignoreAssertionsMessage') }}<Button
                    size="small"
                    class="mx-1 px-0 h-5 leading-5"
                    type="link"
                    @click="setGlobalTabActiveKey('executeConfig')">
                    {{ t('common.execConfig') }} > {{ t('common.pluginConfig') }}
                  </Button>{{ t('xcan_scenarioDebugResult.closeIgnoreAssertions') }}
                </div>
              </template>
            </Alert>
            <AssertionsResult
              :value="httpContent"
              :ignoreAssertions="props.ignoreAssertions"
              class="py-3" />
          </TabPane>
        </Tabs>
      </CollapsePanel>
    </Collapse>
  </div>
</template>

<style scoped>
.ant-collapse > :deep(.ant-collapse-item) > .ant-collapse-header {
  padding: 0;
  border-color: var(--border-divider);
  line-height: 20px;
}

.ant-collapse > :deep(.ant-collapse-item) .ant-collapse-content-box {
  padding: 0 12px;
  line-height: 20px;
}

.embed.ant-collapse > :deep(.ant-collapse-item) > .ant-collapse-header {
  border: none;
  border-bottom: 1px dashed  var(--border-text-box);
}

.embed.ant-collapse,
.embed :deep(.ant-collapse-content),
.embed.ant-collapse > .ant-collapse-item {
  border: none;
}

.embed.ant-collapse > .ant-collapse-item.ant-collapse-item-active {
  border-bottom: 1px dashed var(--border-text-box);
}

.embed .name {
  min-width: 208px;
}

.card-tabs.ant-tabs-top > :deep(.ant-tabs-nav) {
  margin: 0;
}

.card-tabs.ant-tabs-card.ant-tabs-small > :deep(.ant-tabs-nav) .ant-tabs-tab {
  padding: 3px 10px;
  line-height: 20px;
}
</style>
