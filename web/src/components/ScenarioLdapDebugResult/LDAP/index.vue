<script setup lang="ts">
import { computed, ref } from 'vue';
import { Alert, Collapse, CollapsePanel, Tag } from 'ant-design-vue';
import { Arrow, Colon, Icon } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';

import { useI18n } from 'vue-i18n';

import StatusTag from '../StatusTag/index.vue';
import { LdapInfo } from './PropsType';
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
  value: LdapInfo;
  content: ExecContent;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  content: undefined
});

const UUID = utils.uuid();
const collapseActiveKey = ref<string>();

const INNER_UUID = utils.uuid();
const innerActiveKey = ref<string>(INNER_UUID);

const arrowChange = (open: boolean) => {
  if (open) {
    collapseActiveKey.value = UUID;
    return;
  }

  collapseActiveKey.value = undefined;
};

const innerArrowChange = (open: boolean) => {
  if (open) {
    innerActiveKey.value = INNER_UUID;
    return;
  }

  innerActiveKey.value = undefined;
};

const arrowOpen = computed(() => {
  return collapseActiveKey.value === UUID;
});

const innerArrowOpen = computed(() => {
  return innerActiveKey.value === INNER_UUID;
});

const httpContent = computed(() => {
  return props.content;
  // if (props.value?.enabled === true) {
  //   return props.content?.[0];
  // }

  // return undefined;
});

const requestData = computed(() => {
  return httpContent.value?.content?.request0?.data;
});

const requestDataSize = computed(() => {
  const size = httpContent.value?.content?.request0?.size;
  if (size === undefined || size === null) {
    return '0B';
  }

  return utils.formatBytes(+size);
});

const runtime = computed(() => {
  const timeStamp = httpContent.value?.content?.response?.timeline?.total;
  if (timeStamp === undefined || timeStamp === null) {
    return '0';
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

const showBasicInfo = computed(() => {
  return !!httpContent.value;
});

const tagStyle = computed(() => {
  const testType = httpContent.value?.testType?.value;
  if (testType === 'ADD') {
    return 'processing';
  }

  if (testType === 'DELETE') {
    return 'error';
  }

  if (testType === 'MODIFY') {
    return 'warning';
  }

  if (testType === 'SEARCH') {
    return 'success';
  }

  return 'default';
});
</script>
<template>
  <Collapse
    v-model:activeKey="collapseActiveKey"
    style="background-color: #fff;font-size: 12px;"
    class="timeline-collapse"
    @change="(keys:string[])=>arrowChange(keys.includes(UUID))">
    <CollapsePanel :key="UUID" :showArrow="false">
      <template #header>
        <div class="w-full flex items-center px-3 py-2.5 cursor-pointer">
          <Icon
            class="flex-shrink-0 text-4 mr-3"
            icon="icon-chajianpeizhi" />
          <div :title="httpContent?.name" class="truncate min-w-55 max-w-100 mr-5 name">{{ httpContent?.name }}</div>
          <Tag
            class="flex-shrink-0 mr-3"
            :color="tagStyle"
            style="line-height: 20px;"
            size="small">
            {{ httpContent?.testType?.message }}
          </Tag>
          <div class="flex-1 justify-end flex items-center mr-3">
            <template v-if="showBasicInfo">
              <div class="mr-5 truncate">
                <span class="mr-0.5">{{ t('xcan_scenarioDebugResult.duration') }}<Colon /></span>
                <span class="text-theme-sub-content"> {{ runtime }}</span>
              </div>
            </template>
            <template v-if="!httpContent?.enabled">
              <StatusTag />
            </template>
            <template v-else>
              <StatusTag :value="status" />
            </template>
          </div>
          <Arrow :open="arrowOpen" @change="arrowChange" />
        </div>
      </template>
      <Alert
        v-if="!!failMessage"
        :message="failMessage"
        type="error" />
      <Collapse
        v-model:activeKey="innerActiveKey"
        :bordered="false"
        style="background-color: #fff;font-size: 12px;"
        class="border-none-collapse"
        @change="(keys:string[])=>innerArrowChange(keys.includes(INNER_UUID))">
        <CollapsePanel :key="INNER_UUID" :showArrow="false">
          <template #header>
            <div class="w-full flex items-center">
              <Arrow :open="innerArrowOpen" @change="innerArrowChange" />
              <div class="ml-1 font-bold flex items-center">
                <span>{{ t('xcan_scenarioDebugResult.requestContent') }}</span>
                <span class="ml-0.75">({{ requestDataSize }})</span>
              </div>
            </div>
          </template>
          <div>{{ requestData }}</div>
        </CollapsePanel>
      </Collapse>
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
  padding: 14px;
  line-height: 20px;
}

.border-none-collapse.ant-collapse > :deep(.ant-collapse-item) .ant-collapse-content-box {
  padding: 6px 20px;
  line-height: 20px;
}

.border-none-collapse.ant-collapse-borderless > :deep(.ant-collapse-item) {
  border: none;
}
</style>
