<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { Button, Checkbox, Pagination } from 'ant-design-vue';
import { Colon, Icon, ReviewStatus } from '@xcan-angus/vue-ui';
import { ReviewStatus as ReviewStatusEnum, PageQuery } from '@xcan-angus/infra';
import { CaseDetailChecked, EnabledGroup, GroupCaseList } from '../types';

import TestResult from '@/components/TestResult/index.vue';

const { t } = useI18n();

interface Props {
  params: PageQuery;
  total: number;
  loading: boolean;
  checkedCase: CaseDetailChecked;
  enabledGroup: EnabledGroup;
  caseList: CaseDetailChecked[];
  groupCaseList: GroupCaseList[];
}

const props = withDefaults(defineProps<Props>(), {
  params: undefined,
  total: 0,
  loading: false,
  checkedCase: undefined,
  enabledGroup: false,
  caseList: () => [],
  groupCaseList: () => []
});

// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
  (e: 'update:selectedRowKeys', value: string[]): void;
  (e: 'select', value: CaseDetailChecked): void;
  (e: 'change', value: { current: number; pageSize: number; }): void;
  (e: 'update:params', params): void;
  (e: 'getData'): void;
}>();

const showTotal = (_total: number) => {
  return props.enabledGroup
    ? t('functionCase.infoView.totalGroups', { total: _total })
    : t('functionCase.infoView.totalItems', { total: _total });
};

const hanldeListExpand = (item) => {
  item.isOpen = !item.isOpen;
};

const cancelCheckAll = (_selectedRowKeys) => {
  emits('update:selectedRowKeys', _selectedRowKeys);
  if (props.enabledGroup) {
    props.groupCaseList.forEach(childrenRecord => {
      childrenRecord.selectedRowKeys = _selectedRowKeys.includes(childrenRecord.id)
        ? childrenRecord.children.map(item => item.id)
        : [];
    });
  } else {
    props.caseList.forEach(item => {
      item.checked = false;
    });
  }
};

const handleCheckAll = (e, item) => {
  item.checkAll = e.target.checked;
  item.indeterminate = false;
  if (e.target.checked) {
    item.selectedRowKeys = item.children.map(item => item.id);
  } else {
    item.selectedRowKeys = [];
  }

  const _selectedRowKeys = props.groupCaseList.flatMap(record => record.selectedRowKeys);
  if (_selectedRowKeys.length) {
    emits('update:selectedRowKeys', _selectedRowKeys);
    return;
  }

  emits('update:selectedRowKeys', []);
};

const handleCheckOne = (e, groupItem, item) => {
  if (props.enabledGroup) {
    if (e.target.checked) {
      if (!groupItem.selectedRowKeys.includes(item.id)) {
        groupItem.selectedRowKeys.push(item.id);
      }

      if (groupItem.selectedRowKeys.length === groupItem.children.length) {
        groupItem.checkAll = true;
      } else {
        groupItem.indeterminate = true;
      }
    } else {
      groupItem.selectedRowKeys = groupItem.selectedRowKeys.filter(f => f !== item.id);
      groupItem.checkAll = false;
      if (groupItem.selectedRowKeys.length === 0) {
        groupItem.indeterminate = false;
        emits('update:selectedRowKeys', []);
      } else {
        groupItem.indeterminate = true;
      }
    }
    const _selectedRowKeys = props.groupCaseList.flatMap(record => record.selectedRowKeys);
    if (_selectedRowKeys.length) {
      emits('update:selectedRowKeys', _selectedRowKeys);
      return;
    }

    emits('update:selectedRowKeys', []);
  } else {
    item.checked = e.target.checked;
    const _selectedRowKeys = props.caseList.filter(item => item.checked).map(item => item.id);
    if (_selectedRowKeys.length) {
      emits('update:selectedRowKeys', _selectedRowKeys);
    } else {
      emits('update:selectedRowKeys', []);
    }
  }
};

// 平铺视图点击数据获取详情
const hanldeSelectCase = (_case: CaseDetailChecked) => {
  emits('select', _case);
};

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
        <template v-if="props.enabledGroup">
          <div v-for="groupCase in props.groupCaseList" :key="groupCase.id">
            <div
              class="item p-2 border-b cursor-pointer border-theme-text-box bg-theme-menu-hover flex justify-between items-center">
              <Button class="h-4 w-4 p-0 leading-4 flex-none -ml-1">
                <Icon
                  class="text-3"
                  :class="groupCase.isOpen ? 'expand-icon-open' : 'expand-icon'"
                  :icon="groupCase.isOpen ? 'icon-jian' : 'icon-jia'"
                  @click="hanldeListExpand(groupCase)" />
              </Button>

              <Checkbox
                class="mx-2"
                :checked="groupCase.checkAll"
                :indeterminate="groupCase.indeterminate"
                @change="(e) => handleCheckAll(e, groupCase)" />

              <div class="flex items-center text-theme-title flex-1">
                <Icon icon="icon-mokuai" class="mr-1 flex-none text-4" />
                <div
                  class="truncate"
                  style="width: 200px;"
                  :title="groupCase.groupName">
                  {{ groupCase.groupName }}
                </div>
              </div>
            </div>

            <template v-if="groupCase.isOpen">
              <div
                v-for="item in groupCase.children"
                :key="item.id"
                :class="{ 'bg-theme-tabs-selected': props.checkedCase?.id === item.id }"
                class="item p-2 border-b cursor-pointer border-theme-text-box bg-theme-menu-hover flex"
                @click="hanldeSelectCase(item)">
                <Checkbox
                  class="-mt-0.75 ml-5 mr-2"
                  :checked="groupCase.selectedRowKeys?.includes(item.id)"
                  :value="item.id"
                  @click.stop
                  @change="(e) => handleCheckOne(e, groupCase, item)" />

                <div class="flex-1">
                  <div class="flex justify-between">
                    <div class="flex items-center text-theme-title">
                      <Icon
                        icon="icon-gongnengyongli"
                        class="mr-1.5 flex-none text-4" />
                      <div
                        class="truncate"
                        style="width: 140px;"
                        :title="item.name">
                        {{ item.name }}
                      </div>
                    </div>

                    <template v-if="item.reviewStatus?.value && item.reviewStatus?.value !== ReviewStatusEnum.PASSED">
                      <ReviewStatus :value="item.reviewStatus" class="ml-5" />
                    </template>

                    <template v-else>
                      <TestResult :value="item.testResult" class="ml-5" />
                    </template>
                  </div>

                  <div class="flex justify-between mt-2 text-3">
                    <div>{{ item.code }}</div>
                    <div>
                      {{ t('common.priority') }}
                      <Colon class="mr-1" />{{ item.priority.message }}
                    </div>
                  </div>
                </div>
              </div>
            </template>
          </div>
        </template>

        <template v-else>
          <div
            v-for="item in props.caseList"
            :key="item.id"
            :class="{ 'bg-theme-tabs-selected': props.checkedCase?.id === item.id }"
            class="item p-2 border-b cursor-pointer border-theme-text-box bg-theme-menu-hover flex"
            @click="hanldeSelectCase(item)">
            <Checkbox
              class="-mt-0.75 mr-2"
              :checked="item.checked"
              :value="item.id"
              @click.stop
              @change="(e) => handleCheckOne(e, undefined, item)" />

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
        </template>
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
