<script setup lang="ts">
import { computed, defineAsyncComponent, ref } from 'vue';
import { Alert, Collapse, CollapsePanel, Tabs, TabPane, Tag } from 'ant-design-vue';
import { Arrow, Colon, Icon } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';

import { useI18n } from 'vue-i18n';

import StatusTag from '../StatusTag/index.vue';
import { HTTPInfo } from './PropsType';
import { ExecContent } from '../PropsType';
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
  value: HTTPInfo;
  content: ExecContent;
  ignoreAssertions: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  content: undefined,
  ignoreAssertions: undefined
});

const RequestHeaders = defineAsyncComponent(() => import('./RequestHeaders/index.vue'));
const Response = defineAsyncComponent(() => import('./Response/index.vue'));

const UUID = utils.uuid();
const collapseActiveKey = ref<string>();
const arrowOpen = ref(collapseActiveKey.value === UUID);

const arrowChange = (open: boolean) => {
  arrowOpen.value = open;
  if (open) {
    collapseActiveKey.value = UUID;
    return;
  }

  collapseActiveKey.value = undefined;
};

const httpContent = computed(() => {
  return props.content;
  // const linkName = props.value?.name;
  // if (!linkName) {
  //   return undefined;
  // }

  // return props.content?.find(item => linkName === item.name);
});

const caseId = computed(() => {
  return httpContent.value?.caseId;
});

const apisId = computed(() => {
  return httpContent.value?.apisId;
});

const bodySize = computed(() => {
  const size = httpContent.value?.content?.response?.size;
  if (size === undefined || size === null) {
    return '0B';
  }

  return utils.formatBytes(+size);
});

const runtime = computed(() => {
  const timeStamp = httpContent.value?.content?.response?.timeline?.total;
  if (timeStamp === undefined || timeStamp === null) {
    return '';
  }

  return formatTime(+timeStamp);
});

const status = computed(() => {
  if (!httpContent.value) {
    return 'block';
  }

  return httpContent.value.content?.success ? 'success' : 'fail';
});

const failMessage = computed(() => {
  return httpContent.value?.content?.failMessage;
});

</script>
<template>
  <Collapse
    :activeKey="collapseActiveKey"
    style="background-color: #fff;font-size: 12px;"
    class="timeline-collapse">
    <CollapsePanel
      :key="UUID"
      :showArrow="false"
      collapsible="disabled">
      <template #header>
        <div class="w-full flex items-center px-3 py-2.5 cursor-pointer" @click="arrowChange(!arrowOpen)">
          <template v-if="caseId">
            <Icon class="flex-shrink-0 text-4 mr-3" icon="icon-cese" />
          </template>
          <template v-else>
            <Icon
              v-if="apisId"
              class="flex-shrink-0 text-4 mr-3"
              icon="icon-yinyonghttp" />
            <Icon
              v-else
              class="flex-shrink-0 text-4 mr-3"
              icon="icon-chajianpeizhi" />
          </template>
          <div :title="httpContent?.name" class="truncate min-w-55 max-w-100 mr-5 name">{{ httpContent?.name }}</div>
          <Tag v-if="httpContent?.content?.protocol?.value" class="!leading-5">{{ t('xcan_scenarioDebugResult.protocol') }}：{{ httpContent?.content?.protocol?.value }}</Tag>
          <Tag v-if="httpContent?.content?.response?.messageTotal" class="!leading-5">{{ t('xcan_scenarioDebugResult.totalEmails') }}{{ httpContent?.content?.response?.messageTotal }}{{ t('xcan_scenarioDebugResult.emails') }}</Tag>
          <Tag v-if="httpContent?.content?.response?.messageRead" class="!leading-5">{{ t('xcan_scenarioDebugResult.readEmails') }}{{ httpContent?.content?.response?.messageRead }}{{ t('xcan_scenarioDebugResult.emails') }}</Tag>
          <div class="flex-1 justify-end flex items-center mr-3">
            <template v-if="!httpContent?.enabled">
              <StatusTag />
            </template>
            <template v-else>
              <div class="mr-5">
                <span class="mr-0.5">{{ t('xcan_scenarioDebugResult.duration') }}<Colon /></span>
                <span class="text-theme-sub-content"> {{ runtime }}</span>
              </div>
              <div class="mr-5">
                <span class="mr-0.5">{{ t('common.size') }}<Colon /></span>
                <span class="text-theme-sub-content"> {{ bodySize }}</span>
              </div>
              <StatusTag :value="status" />
            </template>
          </div>
          <Arrow :open="arrowOpen" @change="arrowChange" />
        </div>
      </template>
      <Alert
        v-if="!!failMessage"
        :message="failMessage"
        type="error"
        class="mt-3" />
      <Tabs
        type="card"
        size="small"
        class="mt-3 card-tabs">
        <TabPane key="general" :tab="`${t('xcan_scenarioDebugResult.request')} (${utils.formatBytes(httpContent?.content?.request0?.size)})`">
          <RequestHeaders :value="httpContent" class="py-3" />
        </TabPane>
        <TabPane key="responses" :tab="`${t('xcan_scenarioDebugResult.response')} (${bodySize})`">
          <Response :value="httpContent" class="py-3" />
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
