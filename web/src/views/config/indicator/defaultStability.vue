<!-- eslint-disable @typescript-eslint/no-unused-vars -->
<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue';
import { Icon, Input, notification, Select, ShortDuration } from '@xcan-angus/vue-ui';
import { enumLoader } from '@xcan-angus/tools';
import { Button } from 'ant-design-vue';
import { splitDuration } from '@/utils/utils';

import ExpandGrid from './expandGrid.vue';
import { setting } from '@/api/comm';

const editable = ref(false);
const visible = ref(true);

const handleEditPerform = () => {
  editable.value = !editable.value;
  visible.value = true;
  editInfo.value = JSON.parse(JSON.stringify({
    ...info
  }));
};

const durationUnitOpt = ref([]);
const loadDurationUnitOpt = async () => {
  const [error, data] = await enumLoader.load('ShortTimeUnit');
  if (error) {
    return;
  }
  durationUnitOpt.value = (data || []);
};

const editInfo = ref({
  cpu: '',
  threads: '',
  disk: '',
  duration: '',
  errorRate: '',
  network: '',
  memory: '',
  percentile: 'ALL',
  art: '',
  tps: ''
});

const info = reactive({ // 展示 info
  cpu: '',
  threads: '',
  disk: '',
  duration: '',
  errorRate: '',
  network: '',
  memory: '',
  percentile: 'ALL',
  art: '',
  tps: ''
});

const percentileOpt = ref([]);
const loadPercentileOpt = async () => {
  const [error, data] = await enumLoader.load('Percentile');
  if (error) {
    return;
  }
  percentileOpt.value = data;
};

const loadInfo = async () => {
  const [error, res] = await setting.loadStabilityIndicator();
  if (error) {
    return;
  }
  if (res.data) {
    const { threads, cpu, disk, duration, errorRate, memory, network } = res.data;
    // const [value, unit] = splitDuration(duration);
    info.threads = threads;
    info.cpu = cpu;
    info.errorRate = errorRate;
    info.disk = disk;
    info.memory = memory;
    info.network = network;
    info.duration = duration;
    info.art = res.data.art;
    info.tps = res.data.tps;
    info.percentile = res.data.percentile?.value;
    info.percentileName = res.data.percentile?.message;
    // info.duration = {
    //   value, unit
    // };
  }
};

const saveInfo = async () => {
  const [durationValue] = splitDuration(editInfo.value.duration);
  if (!durationValue || !editInfo.value.threads || !editInfo.value.errorRate) {
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
  if (+editInfo.value.errorRate < 0 || +editInfo.value.errorRate > 100) {
    notification.error('错误率须在0% 到 100% 之间');
    return;
  }
  if (+editInfo.value.cpu > 100) {
    notification.error('CPU使用率须小于100%之间');
    return;
  }
  if (+editInfo.value.memory > 100) {
    notification.error('内存使用率须小于100%之间');
    return;
  }
  if (+editInfo.value.disk > 100) {
    notification.error('磁盘使用率须小于100%之间');
    return;
  }
  if (+editInfo.value.network > 100) {
    notification.error('网络使用量须小于10000MB');
    return;
  }
  const params = {
    ...editInfo.value
  };
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const { percentileName, ...otherInfo } = params;
  const [error] = await setting.saveStabilityIndicator({ ...otherInfo });
  if (error) {
    return;
  }
  editable.value = false;
  loadInfo();
};

onMounted(() => {
  loadPercentileOpt();
  loadInfo();
  loadDurationUnitOpt();
});
</script>
<template>
  <ExpandGrid v-model:visible="visible" title="平台默认稳定性指标">
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
          <div v-if="editable" class="flex items-center">
            <!-- <span class="pr-2">>=</span> -->
            <Input
              v-model:value="editInfo.threads"
              class="w-20"
              dataType="number"
              size="small" />
          </div>
          <div v-else class="w-25 bg-gray-light rounded h-7 leading-7 px-2">{{ info.threads }}</div>
        </li>
        <li>
          <div class="quota-label">测试时长</div>
          <div v-if="editable" class="flex items-center">
            <!-- <span class="pr-2">>=</span> -->
            <ShortDuration
              v-model:value="editInfo.duration"
              :inputProps="{class: 'w-20'}"
              :selectProps="{class: '!w-15'}" />
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
          <div class="quota-label">错误率 (ErrorRate)</div>
          <div v-if="editable" class="flex items-center">
            <span class="pr-2">&lt;=</span>
            <Input
              v-model:value="editInfo.errorRate"
              class="w-18"
              dataType="float"
              :decimalPoint="10"
              size="small" />
            <span class="pl-2">%</span>
          </div>

          <div v-else class="w-25 bg-gray-light rounded h-7 leading-7 px-2"> &lt;= {{ info.errorRate }}%</div>
        </li>
        <li>
          <div class="quota-label">每秒事务数（TPS）</div>
          <div v-if="editable">
            <Input
              v-model:value="editInfo.tps"
              dataType="number"
              class="w-20"
              size="small" />
          </div>
          <div v-else class="w-25 bg-gray-light rounded h-7 leading-7 px-2"> {{ info.tps }} </div>
        </li>
        <li>
          <div class="quota-label">应用系统平均负载</div>
          <div v-if="editable" class="flex flex-wrap load-wrapper">
            <div class="flex items-center mr-2">
              <span class="pr-2">CPU使用率：&lt;=</span>
              <Input
                v-model:value="editInfo.cpu"
                size="small"
                class="w-20"
                dataType="float" />
              <span class="pl-2">%</span>
            </div>
            <div class="flex items-center mr-2">
              <span class="pr-2">内存使用率：&lt;=</span>
              <Input
                v-model:value="editInfo.memory"
                size="small"
                class="w-20"
                dataType="float" />
              <span class="pl-2">%</span>
            </div>
            <div class="flex items-center mr-2">
              <span class="pr-2">磁盘使用率：&lt;=</span>
              <Input
                v-model:value="editInfo.disk"
                size="small"
                class="w-20"
                dataType="float" />
              <span class="pl-2">%</span>
            </div>
            <div class="flex items-center">
              <span class="pr-2">网络使用量：&lt;=</span>
              <Input
                v-model:value="editInfo.network"
                size="small"
                class="w-20"
                :max="10000"
                :decimalPoint="2"
                dataType="float" />
              <span class="pl-2">MB</span>
            </div>
          </div>
          <div v-else class="min-w-30 bg-gray-light rounded h-7 leading-7 px-2">
            CPU使用率：&lt;={{ info.cpu }}% | 内存使用率：&lt;= {{ info.memory }}% | 磁盘使用率：&lt;= {{ info.disk }}% | 网络使用量：&lt;={{ info.network }}MB
          </div>
        </li>
      </ul>
    </template>
  </ExpandGrid>
</template>
<style scoped>
.quota-label {
  @apply mb-4 whitespace-nowrap;
}

:deep(.ant-input-group-wrapper .ant-input-group-addon) {
  @apply bg-transparent border-none leading-7;
}

:deep(.ant-input-group-wrapper .ant-input-affix-wrapper) {
  @apply rounded w-20;
}

.load-wrapper > div {
  @apply mb-3;
}

.load-wrapper > div :deep(.text-theme-title) {
  @apply text-3 text-text-content;
}
</style>
