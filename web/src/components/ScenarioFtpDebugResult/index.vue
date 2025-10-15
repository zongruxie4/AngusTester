<script setup lang="ts">
import { computed, ref } from 'vue';
import { Alert, Collapse, CollapsePanel, Tabs, TabPane, Tag } from 'ant-design-vue';
import { Arrow, Colon, Icon } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';

import { useI18n } from 'vue-i18n';
import StatusTag from './StatusTag/index.vue';
import { FtpInfo, ExecContent } from './PropsType';
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
  value: FtpInfo;
  content: ExecContent[];
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  content: undefined
});

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

const httpContent = computed(() => {
  if (props.value?.enabled === true) {
    return props.content?.[0];
  }

  return undefined;
});

const requestData = (httpContent) => {
  return httpContent?.content?.request0?.data;
};

const requestDataSize = (httpContent) => {
  const size = httpContent?.content?.request0?.size;
  if (size === undefined || size === null) {
    return '0B';
  }

  return utils.formatBytes(+size);
};

const saveFilePath = (httpContent):string => {
  return httpContent?.content?.response?.saveFilePath;
};

// const fileName = computed(() => {
//   if (!saveFilePath.value) {
//     return '';
//   }

//   const name = saveFilePath.value.replace(/.*\/([^/]+\.[^./\\]+)$/, '$1');
//   return name;
// });

const responseSize = (httpContent) => {
  const size = httpContent?.content?.response?.size;
  if (size === undefined || size === null) {
    return '0B';
  }

  return utils.formatBytes(+size);
};

const runtime = (httpContent) => {
  const timeStamp = httpContent?.content?.response?.timeline?.total;
  if (timeStamp === undefined || timeStamp === null) {
    return '0' + t('xcan_scenarioDebugResult.milliseconds');
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
  return httpContent.value?.content?.failMessage;
};

const showBasicInfo = (httpContent) => {
  return !!httpContent;
};

const isUploadFile = (httpContent) => {
  return httpContent?.uploadFile === true;
};
</script>
<template>
  <Collapse
    v-for="httpContent in props.content"
    :activeKey="collapseActiveKey"
    style="background-color: #fff;font-size: 12px;"
    class="timeline-collapse">
    <CollapsePanel
      :key="UUID"
      :showArrow="false"
      collapsible="disabled">
      <template #header>
        <div class="w-full flex items-center px-3 py-2.5 cursor-pointer" @click="arrowChange(httpContent.key)">
          <Icon class="flex-shrink-0 text-4 mr-3" icon="icon-chajianpeizhi" />
          <div :title="props.value?.name" class="truncate min-w-55 max-w-100 mr-5 name">{{ props.value?.name }}</div>
          <Tag
            v-if="isUploadFile(httpContent)"
            class="mr-3"
            style="line-height: 20px;"
            color="#87d068">
            {{ t('actions.upload') }}
          </Tag>
          <Tag
            v-else
            class="mr-3"
            style="line-height: 20px;"
            color="#2db7f5">
            {{ t('actions.download') }}
          </Tag>
          <div class="flex-1 justify-end flex items-center mr-3">
            <template v-if="showBasicInfo(httpContent)">
              <div class="mr-5 truncate">
                <span class="mr-0.5">{{ t('common.duration') }}<Colon /></span>
                <span class="text-theme-sub-content"> {{ runtime(httpContent) }}</span>
              </div>
            </template>
            <template v-if="!props.value?.enabled">
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
        style="margin-bottom: 12px;" />
      <Tabs
        type="card"
        size="small"
        class="card-tabs">
        <TabPane key="request">
          <template #tab>
            <div>
              <span>{{ t('xcan_scenarioDebugResult.request') }}</span>
              <span class="ml-0.75">({{ requestDataSize(httpContent) }})</span>
            </div>
          </template>
          <div class="p-3 max-h-40 overflow-auto break-all whitespace-pre-wrap">{{ requestData(httpContent) }}</div>
        </TabPane>
        <TabPane key="response">
          <template #tab>
            <div>
              <span>{{ t('xcan_scenarioDebugResult.response') }}</span>
              <span class="ml-0.75">({{ responseSize(httpContent) }})</span>
            </div>
          </template>
          <div class="p-3 max-h-40 overflow-auto break-all whitespace-pre-wrap">
            {{ saveFilePath(httpContent) }}
            <!-- <a
              :href="saveFilePath"
              :download="fileName"
              style="color:#1890ff;">{{ fileName }}</a> -->
          </div>
        </TabPane>
      </Tabs>
    </CollapsePanel>
  </Collapse>
</template>

<style scoped>
.ant-collapse> :deep(.ant-collapse-item)>.ant-collapse-header {
  padding: 0;
  border-color: var(--border-divider);
  line-height: 20px;
}

.ant-collapse> :deep(.ant-collapse-item) .ant-collapse-content-box {
  padding: 12px 12px 0;
  line-height: 20px;
}

.card-tabs.ant-tabs-top> :deep(.ant-tabs-nav) {
  margin: 0;
}

.card-tabs.ant-tabs-card.ant-tabs-small> :deep(.ant-tabs-nav) .ant-tabs-tab {
  padding: 3px 10px;
  line-height: 20px;
}
</style>
