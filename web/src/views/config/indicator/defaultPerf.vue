<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue';
import { Icon, Input, notification, Select, ShortDuration, ApiUtils as apiUtils } from '@xcan-angus/vue-ui';
import { enumLoader } from '@xcan-angus/tools';
import { Button } from 'ant-design-vue';

import ExpandGrid from './expandGrid.vue';
import { setting } from '@/api/gm';
import { splitDuration } from '@/utils/utils';

const editable = ref(false);
const editInfo = ref({
  art: '',
  threads: '',
  errorRate: '',
  tps: '',
  percentile: '',
  duration: '',
  rampUpThreads: undefined,
  rampUpInterval: '1min'
}); // 编辑 info
const info = reactive({ // 展示 info
  art: '',
  threads: '',
  errorRate: '',
  tps: '',
  percentile: '',
  duration: '',
  rampUpThreads: undefined,
  rampUpInterval: '1min'
});
const percentileOpt = ref([]);
const loadPercentileOpt = async () => {
  const [error, data] = await enumLoader.load('Percentile');
  if (error) {
    return;
  }
  percentileOpt.value = (data || []);
};

const handleEditPerform = () => {
  editable.value = !editable.value;
  editInfo.value = JSON.parse(JSON.stringify(info));
};

const loadPerInfo = async () => {
  const [error, res] = await setting.getPerfIndicator();
  if (error) {
    return;
  }
  if (res.data) {
    info.art = res.data.art;
    info.threads = res.data.threads;
    info.errorRate = res.data.errorRate;
    info.tps = res.data.tps;
    info.percentile = res.data.percentile?.value;
    info.percentileName = res.data.percentile?.message;
    info.duration = res.data.duration;
    info.rampUpThreads = res.data.rampUpThreads;
    info.rampUpInterval = res.data.rampUpInterval;
  }
};

const blurDuration = () => {
  const [value, unit] = splitDuration(editInfo.value.duration);
  if (!value || value === '0') {
    editInfo.value.duration = 1 + unit;
  }
  const max = apiUtils.maxDuration(editInfo.value.duration, editInfo.value.rampUpInterval);
  if (max === editInfo.value.rampUpInterval) {
    editInfo.value.rampUpInterval = editInfo.value.duration;
  }
};

const blurRampUpInterval = () => {
  const [value, unit] = splitDuration(editInfo.value.rampUpInterval);
  if (!value) {
    editInfo.value.duration = '0' + unit;
  }
  const max = apiUtils.maxDuration(editInfo.value.duration, editInfo.value.rampUpInterval);
  if (max === editInfo.value.rampUpInterval) {
    editInfo.value.rampUpInterval = editInfo.value.duration;
  }
};

const saveInfo = async () => {
  const [durationValue] = splitDuration(editInfo.value.duration);
  if (!durationValue || !editInfo.value.art || !editInfo.value.threads || !editInfo.value.errorRate || !editInfo.value.tps) {
    notification.error('各项均为必填项');
    return;
  }
  if (editInfo.value.threads === '0') {
    notification.error('并发数须大于零');
    return;
  }
  if (durationValue === '0') {
    notification.error('测试时长须大于零');
    return;
  }
  if (editInfo.value.art === '0') {
    notification.error('响应时间须大于零');
    return;
  }
  if (editInfo.value.tps === '0') {
    notification.error('吞吐量须大于零');
    return;
  }
  if (+editInfo.value.errorRate < 0 || +editInfo.value.errorRate > 100) {
    notification.error('错误率须在0% 到 100% 之间');
    return;
  }
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const { percentileName, ...otherInfo } = editInfo.value;
  const [error] = await setting.savePerIndicator({ ...otherInfo });
  if (error) {
    return;
  }
  editable.value = false;
  loadPerInfo();
};

onMounted(() => {
  loadPerInfo();
  loadPercentileOpt();
});
</script>

<template>
  <ExpandGrid title="平台默认性能指标">
    <template #button>
      <div class="text-3 flex items-center">
        <template v-if="editable">
          <span class="cursor-pointer" @click.stop="handleEditPerform"><Icon icon="icon-zhongzhi2" class="mr-1" />取消</span>
          <Button
            type="text"
            class="ml-2 text-3 py-0 h-5"
            @click.stop="saveInfo">
            <Icon icon="icon-baocun" class="mr-1" />保存
          </Button>
        </template>
        <Button
          v-else
          class="text-3 py-0 h-5"
          type="text"
          @click.stop="handleEditPerform">
          <Icon icon="icon-shuxie" class="mr-1" />编辑
        </Button>
      </div>
    </template>
    <template #default>
      <ul class="flex text-3 pt-3 quota-wrapper pl-3 space-x-8">
        <li>
          <div class="quota-label">并发数 (用户)</div>
          <div v-if="editable">
            <Input
              v-model:value="editInfo.threads"
              dataType="number"
              class="w-20"
              size="small" />
          </div>
          <div v-else class="w-25 bg-gray-light rounded h-7 leading-7 px-2">{{ info.threads }}</div>
        </li>
        <li>
          <div class="quota-label">测试时长</div>
          <div v-if="editable" class="flex items-center">
            <!-- <span class="pr-3">>=</span> -->
            <ShortDuration
              v-model:value="editInfo.duration"
              :inputProps="{
                class: 'w-20',
                onblur: blurDuration
              }"
              :selectProps="{ class: '!w-15' }" />
          </div>
          <div v-else class="w-25 bg-gray-light rounded h-7 leading-7 px-2">{{ info.duration }}</div>
        </li>
        <li>
          <div class="quota-label">响应时间 (RT)</div>
          <div v-if="editable" class="flex items-center">
            <Select
              v-model:value="editInfo.percentile"
              class="w-25 mr-2 quota-rt"
              :fieldNames="{label: 'message', value: 'value'}"
              :allowClear="false"
              size="small"
              :options="percentileOpt" />
            <span class="pr-2">&lt;=</span>
            <Input
              v-model:value="editInfo.art"
              dataType="number"
              class="w-20"
              size="small" />
            <span class="pl-2">ms</span>
          </div>
          <div v-else class="min-w-25 bg-gray-light rounded h-7 leading-7 px-2">{{ info.percentileName }}&lt;={{ info.art }}ms</div>
        </li>
        <li>
          <div class="quota-label">每秒事务数 (TPS)</div>
          <div v-if="editable" class="flex items-center">
            <span class="pr-2">>=</span>
            <Input
              v-model:value="editInfo.tps"
              dataType="number"
              class="w-20"
              size="small" />
          </div>
          <div v-else class="w-25 bg-gray-light rounded h-7 leading-7 px-2"> >= {{ info.tps }}</div>
        </li>
        <li>
          <div class="quota-label">错误率 (ErrorRate)</div>
          <div v-if="editable">
            <span class="pr-2">&lt;=</span>
            <Input
              v-model:value="editInfo.errorRate"
              dataType="float"
              :decimalPoint="10"
              class="w-15"
              size="small" />
            <span class="pl-2">%</span>
          </div>
          <div v-else class="w-25 bg-gray-light rounded h-7 leading-7 px-2"> &lt;= {{ info.errorRate }}% </div>
        </li>
        <li>
          <div class="quota-label">增压并发数</div>
          <div v-if="editable">
            <Input
              v-model:value="editInfo.rampUpThreads"
              dataType="number"
              class="w-20"
              size="small" />
          </div>
          <div v-else class="w-25 bg-gray-light rounded h-7 leading-7 px-2"> {{ info.rampUpThreads }} </div>
        </li>
        <li>
          <div class="quota-label">增压测试时长</div>
          <div v-if="editable">
            <ShortDuration
              v-model:value="editInfo.rampUpInterval"
              :inputProps="{
                class: 'w-20',
                onblur: blurRampUpInterval
              }"
              :selectProps="{ class: '!w-15' }" />
          </div>
          <div v-else class="w-25 bg-gray-light rounded h-7 leading-7 px-2"> {{ info.rampUpInterval }} </div>
        </li>
      </ul>
    </template>
  </ExpandGrid>
</template>
<style scoped>
:deep(.ant-input-group-wrapper .ant-input-group-addon) {
  @apply bg-transparent border-none leading-7;
}

:deep(.ant-input-group-wrapper .ant-input-affix-wrapper) {
  @apply rounded w-20;
}

.quota-rt :deep(.ant-select-selector) {
  @apply h-7;
}

.quota-label {
  @apply mb-4 whitespace-nowrap;
}
</style>
