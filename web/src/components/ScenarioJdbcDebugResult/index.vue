<script setup lang="ts">
import { inject, computed, defineAsyncComponent, ref } from 'vue';
import { Alert, Collapse, CollapsePanel, Tabs, TabPane } from 'ant-design-vue';
import { Arrow, Colon, Icon } from '@xcan-angus/vue-ui';

import { useI18n } from 'vue-i18n';

import StatusTag from './StatusTag/index.vue';
import { ExecContent, ExecInfo } from './PropsType';
const { t } = useI18n();

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

export interface Props {
  value: ExecInfo['task']['pipelines'][number];
  content: ExecContent[];
  ignoreAssertions: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  content: undefined,
  ignoreAssertions: false
});

const RequestParameter = defineAsyncComponent(() => import('./RequestParameter/index.vue'));
const AssertionsResult = defineAsyncComponent(() => import('./AssertionsResult/index.vue'));

const setGlobalTabActiveKey = inject<(key:'executeConfig')=>void>('setGlobalTabActiveKey');

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

const status = (httpContent) => {
  if (!httpContent) {
    return 'block';
  }

  return httpContent.content?.success ? 'success' : 'fail';
};

const queryType = (httpContent) => {
  return httpContent.type?.value;
};

const runtime = (httpContent) => {
  const timeStamp = httpContent?.content?.response?.timeline?.total;
  if (timeStamp === undefined || timeStamp === null) {
    return '--';
  }

  return formatTime(+timeStamp);
};

const failMessage = (httpContent) => {
  return httpContent?.content?.failMessage;
};

const showBasicInfo = (httpContent) => {
  return !!httpContent;
};
</script>
<template>
  <Collapse
    v-for="httpContent in props.content"
    :key="httpContent.key"
    :activeKey="collapseActiveKey"
    style="background-color: #fff;font-size: 12px; "
    class="timeline-collapse mb-2">
    <CollapsePanel
      :key="httpContent.key"
      :showArrow="false"
      collapsible="disabled">
      <template #header>
        <div class="w-full flex items-center px-3 py-2.5 cursor-pointer" @click="arrowChange(httpContent.key)">
          <Icon class="flex-shrink-0 text-4 mr-3" icon="icon-jdbc" />
          <div :title="httpContent?.name" class="truncate min-w-55 max-w-100 mr-5 name">{{ httpContent?.name }}</div>
          <div class="flex-1 justify-end flex items-center mr-3">
            <template v-if="showBasicInfo(httpContent)">
              <div class="mr-5">
                <span class="mr-0.5">{{ t('common.duration') }}<Colon /></span>
                <span class="text-theme-sub-content"> {{ runtime(httpContent) }}</span>
              </div>
            </template>
            <template v-if="!httpContent?.finish">
              <StatusTag />
            </template>
            <template v-else>
              <StatusTag :value="status(httpContent)" />
            </template>
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
        <TabPane key="general" :tab="t('xcan_scenarioDebugResult.requestParameter')">
          <RequestParameter
            :type="queryType(httpContent)"
            :value="httpContent"
            class="py-3" />
        </TabPane>
        <TabPane key="assertions" :tab="t('xcan_scenarioDebugResult.assertionsResult')">
          <Alert
            v-if="props.ignoreAssertions === true"
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
                  {{ t('xcan_scenarioDebugResult.executeConfig') }} > {{ t('xcan_scenarioDebugResult.pluginConfig') }}
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
