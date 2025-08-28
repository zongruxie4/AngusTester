<script lang="ts" setup>
import { computed, defineAsyncComponent, ref } from 'vue';
import { Button, TabPane, Tabs } from 'ant-design-vue';
import { Icon, Input, Spin } from '@xcan-angus/vue-ui';
import { debounce } from 'throttle-debounce';
import { duration } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { useTrashActions } from './composables/useTrashActions';
import type { TrashComponentProps, TrashTargetType, TrashItem } from './types';

/**
 * Component props with default values
 */
const props = withDefaults(defineProps<TrashComponentProps>(), {
  projectId: '',
  userInfo: () => ({ id: '' })
});

// Internationalization
const { t } = useI18n();

// Async component import
const Table = defineAsyncComponent(() => import('./table.vue'));

// Reactive state
const activeKey = ref<TrashTargetType>('SERVICE');
const inputValue = ref<string>();
const tableDataMap = ref<Record<TrashTargetType, TrashItem[]>>({} as Record<TrashTargetType, TrashItem[]>);

// Use trash actions composable
const {
  loading,
  refreshNotify,
  recoverAll,
  deleteAll,
  refresh
} = useTrashActions(props.projectId);

/**
 * Debounced input change handler
 */
const inputChange = debounce(duration.search, (event: Event) => {
  const target = event.target as HTMLInputElement;
  inputValue.value = target.value;
});

/**
 * Computed parameters for SERVICE tab
 */
const serviceParams = computed(() => {
  const params: {
    targetType: 'SERVICE';
    targetName?: string;
  } = {
    targetType: 'SERVICE'
  };

  if (inputValue.value) {
    params.targetName = inputValue.value;
  }

  return params;
});

/**
 * Computed parameters for API tab
 */
const apiParams = computed(() => {
  const params: {
    targetType: 'API';
    targetName?: string;
  } = {
    targetType: 'API'
  };

  if (inputValue.value) {
    params.targetName = inputValue.value;
  }

  return params;
});

/**
 * Computed property to check if bulk action buttons should be disabled
 */
const buttonDisabled = computed(() => {
  return !tableDataMap.value[activeKey.value]?.length;
});

/**
 * Handle table data change events
 * @param listData - Updated table data
 * @param key - Tab key (SERVICE or API)
 */
const handleTableChange = (listData: TrashItem[], key: TrashTargetType) => {
  tableDataMap.value[key] = listData;
};

/**
 * Handle recover all action
 */
const handleRecoverAll = async () => {
  await recoverAll();
};

/**
 * Handle delete all action
 */
const handleDeleteAll = async () => {
  await deleteAll();
};

/**
 * Handle refresh action
 */
const handleRefresh = () => {
  refresh();
};
</script>
<template>
  <Spin :spinning="loading" class="h-full px-5 py-5 overflow-auto text-3">
    <!-- Header section with search and action buttons -->
    <div class="flex items-center justify-between mb-1">
      <!-- Search section -->
      <div class="flex items-center">
        <Input
          :value="inputValue"
          :allowClear="true"
          :maxlength="200"
          trim
          :placeholder="t('apiTrash.searchPlaceholder')"
          class="w-75"
          @change="inputChange">
          <template #suffix>
            <Icon class="text-3.5 cursor-pointer text-theme-content" icon="icon-sousuo" />
          </template>
        </Input>
        <div class="flex-1 truncate text-theme-sub-content space-x-1 ml-2">
          <Icon icon="icon-tishi1" class="text-3.5 text-tips" />
          <span>{{ t('apiTrash.tips') }}</span>
        </div>
      </div>

      <!-- Action buttons -->
      <div class="space-x-2.5">
        <Button
          :disabled="buttonDisabled"
          size="small"
          type="primary"
          @click="handleRecoverAll">
          <Icon icon="icon-zhongzhi" class="text-3.5 mr-1" />
          <span>{{ t('apiTrash.buttons.recoverAll') }}</span>
        </Button>
        <Button
          :disabled="buttonDisabled"
          size="small"
          type="primary"
          danger
          @click="handleDeleteAll">
          <Icon icon="icon-qingchu" class="text-3.5 mr-1" />
          <span>{{ t('actions.deleteAll') }}</span>
        </Button>
        <Button
          size="small"
          type="default"
          @click="handleRefresh">
          <Icon icon="icon-shuaxin" class="text-3.5 mr-1" />
          <span>{{ t('actions.refresh') }}</span>
        </Button>
      </div>
    </div>

    <!-- Tabs section -->
    <Tabs v-model:activeKey="activeKey">
      <!-- SERVICE tab -->
      <TabPane key="SERVICE" :tab="t('apiTrash.tabs.service')">
        <Table
          v-model:spinning="loading"
          :notify="refreshNotify"
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :params="serviceParams"
          @tableChange="handleTableChange($event, 'SERVICE')" />
      </TabPane>

      <!-- API tab -->
      <TabPane key="API" :tab="t('apiTrash.tabs.api')">
        <Table
          v-model:spinning="loading"
          :notify="refreshNotify"
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :params="apiParams"
          @tableChange="handleTableChange($event, 'API')" />
      </TabPane>
    </Tabs>
  </Spin>
</template>
