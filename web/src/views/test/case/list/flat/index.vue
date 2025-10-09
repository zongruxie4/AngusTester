<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { Checkbox, Pagination } from 'ant-design-vue';
import { Icon, ReviewStatus } from '@xcan-angus/vue-ui';
import { ReviewStatus as ReviewStatusEnum, PageQuery } from '@xcan-angus/infra';
import { CaseDetail } from '@/views/test/types';

import TestResult from '@/components/TestResult/index.vue';

const { t } = useI18n();

interface Props {
  params: PageQuery;
  total: number;
  loading: boolean;
  checkedCase: CaseDetail;
  caseList: CaseDetail[];
}

const props = withDefaults(defineProps<Props>(), {
  params: undefined,
  total: 0,
  loading: false,
  checkedCase: undefined,
  caseList: () => []
});

// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
  (e: 'update:selectedRowKeys', value: number[]): void;
  (e: 'select', value: CaseDetail): void;
  (e: 'change', value: { current: number; pageSize: number; }): void;
  (e: 'update:params', params): void;
  (e: 'getData'): void;
}>();

const showTotal = (_total: number) => {
  return t('testCase.messages.totalItems', { total: _total });
};

/**
 * Clear all selections across groups or flat list
 */
const cancelCheckAll = (_selectedRowKeys) => {
  emits('update:selectedRowKeys', _selectedRowKeys);
  props.caseList.forEach(item => {
      item.checked = false;
    });
};

/**
 * Select or deselect a single item; updates group state accordingly
 */
const handleCheckOne = (e, item) => {
  item.checked = e.target.checked;
    const _selectedRowKeys = props.caseList.filter(item => item.checked).map(item => item.id);
    if (_selectedRowKeys.length) {
      emits('update:selectedRowKeys', _selectedRowKeys);
    } else {
      emits('update:selectedRowKeys', []);
    }
};

/**
 * Flat view: emit selected case for detail panel
 */
const handleSelectCase = (_case: CaseDetail) => {
  emits('select', _case);
};

/**
 * Emit pagination change
 */
const tableChange = ({ current, pageSize }) => {
  emits('change', { current, pageSize });
};

defineExpose({
  cancelCheckAll
});
</script>
<template>
  <div class="flex border-t border-theme-text-box overflow-auto">
    <div class="w-65 border-r border-theme-text-box justify-between h-full flex flex-col">
      <div class="text-3 leading-3 text-theme-sub-content overflow-y-auto flex-1">
        <div
            v-for="item in props.caseList"
            :key="item.id"
            :class="{ 'bg-theme-tabs-selected': props.checkedCase?.id === item.id }"
            class="item p-2 border-b cursor-pointer border-theme-text-box bg-theme-menu-hover flex"
            @click="handleSelectCase(item)">
            <Checkbox
              class="-mt-0.75 mr-2"
              :checked="item.checked"
              :value="item.id"
              @click.stop
              @change="(e) => handleCheckOne(e, item)" />

            <div class="flex-1 min-w-0">
              <div class="flex items-center text-theme-title">
                <Icon
                  icon="icon-gongnengyongli"
                  class="mr-1.5 flex-none text-4" />
                <div
                  class="truncate flex-1 min-w-0"
                  :title="item.name">
                  {{ item.name }}
                </div>
              </div>

              <div class="flex mt-2">
                <div class="pl-5">{{ item.code }}</div>
                <template v-if="item.reviewStatus?.value && item.reviewStatus?.value !== ReviewStatusEnum.PASSED">
                  <ReviewStatus :value="item.reviewStatus" class="ml-5" />
                </template>
                <template v-else>
                  <TestResult :value="item.testResult" class="ml-5" />
                </template>
              </div>
            </div>
          </div>
      </div>

      <template v-if="props.caseList.length">
        <Pagination
          :current="props.params.pageNo"
          :pageSize="props.params.pageSize"
          :total="total"
          :showSizeChanger="false"
          :showTotal="showTotal"
          showLessItems
          size="small"
          class="mr-2 mt-2"
          @change="(current, pageSize) => tableChange({ current, pageSize })" />
      </template>
    </div>

    <slot name="info"></slot>
  </div>
</template>
<style scoped>
.top-count-element {
  height: 0;
  transition: all 0.5s ease-in-out;
}

.top-count-element-expanded {
  height: auto;
  transition: height 0.5s ease-in-out;
}

.fade-enter-from {
  height: 0;
  opacity: 0;
}

.fade-enter-to {
  height: 28px;
  opacity: 1;
}

.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s ease-in-out;
}

.fade-leave-from {
  height: 28px;
  opacity: 1;
}

.fade-leave-to {
  height: 0;
  opacity: 0;
}

.group-case-table :deep(.ant-table-thead) {
  display: none !important;
}

.group-case-table :deep(.ant-table) {
  margin: -2px -4px -2px -8px !important;
  padding-left: 52px;
}
</style>
