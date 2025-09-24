<script setup lang='ts'>
import { defineProps, defineEmits } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Icon, SearchPanel } from '@xcan-angus/vue-ui';

// Define props
interface Props {
  options: any[];
  width?: number;
  loading?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  width: 284,
  loading: false
});

// Define emits
const emit = defineEmits<{
  change: [data: any[]];
  batchStart: [];
  batchDelete: [];
  export: [item?: any];
  refresh: [];
}>();

// Use i18n
const { t } = useI18n();

// Event handlers
const handleSearchChange = (data: any[]) => {
  emit('change', data);
};

const handleBatchStart = () => {
  emit('batchStart');
};

const handleBatchDelete = () => {
  emit('batchDelete');
};

const handleRefresh = () => {
  emit('refresh');
};
</script>

<template>
  <div class="flex items-start justify-between mb-3.5">
    <SearchPanel
      :options="props.options"
      :width="props.width"
      class="flex-1 mr-2"
      @change="handleSearchChange" />
    <div class="space-x-2.5">
      <Button
        type="primary"
        size="small"
        href="/mockservice/add"
        class="inline-flex space-x-1">
        <Icon icon="icon-jia" class="text-3.5" />
        {{ t('mock.actions.addMock') }}
      </Button>
      <Button
        size="small"
        @click="handleBatchStart">
        <Icon icon="icon-qidong" class="mr-1 text-3.5" />
        <span>{{ t('actions.start') }}</span>
      </Button>
      <Button
        size="small"
        @click="handleBatchDelete">
        <Icon icon="icon-qingchu" class="mr-1 text-3.5" />
        <span>{{ t('actions.delete') }}</span>
      </Button>
      <Button
        size="small"
        :disabled="props.loading"
        @click="handleRefresh">
        <Icon icon="icon-shuaxin" class="text-3.5 mr-1" />
        <span>{{ t('actions.refresh') }}</span>
      </Button>
    </div>
  </div>
</template>
