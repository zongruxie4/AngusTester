<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { ActivityInfo, IconRefresh, Input, PureCard, Spin } from '@xcan-angus/vue-ui';
import { Pagination } from 'ant-design-vue';
import { useActivityData } from '@/views/apis/mock/detail/composables/useActivityData';

const { t } = useI18n();

interface Props {
  id:string;
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined
});

// Use composable to encapsulate data and actions
// <p>
// Keeps component lean and improves testability & reusability
const {
  params,
  total,
  activities,
  detailKeyword,
  loading,
  loadActivities,
  onSearchChange,
  onPageChange
} = useActivityData(props.id);

// Render total text for Pagination
const showTotal = (totalNum: number) => {
  const totalPage = Math.ceil(totalNum / params.value.pageSize);
  return t('mock.mockDetail.activity.pageInfo', { total: totalNum, current: params.value.pageNo, totalPage });
};

// Narrow the type for UI component to ensure `avatar` is a required string
// <p>
// This avoids TS mismatch against ActivityInfo's prop typing (expects non-optional avatar)
interface ActiveObjMinimal { avatar: string; [key: string]: any }
const activitiesForUI = computed<ActiveObjMinimal[]>(() => activities as unknown as ActiveObjMinimal[]);
</script>
<template>
  <PureCard class="p-3.5 flex flex-col h-full justify-between space-y-3.5">
    <div class="flex items-center justify-between">
      <Input
        v-model:value="detailKeyword"
        :allowClear="true"
        class="w-100"
        :placeholder="t('mock.mockDetail.activity.searchPlaceholder')"
        size="small"
        @change="onSearchChange($event.target.value)" />
      <IconRefresh
        :loading="loading"
        class="text-4.5"
        @click="loadActivities" />
    </div>
    <Spin :spinning="loading" class="flex-1 overflow-y-auto -mr-3.5 pr-2">
      <ActivityInfo :dataSource="activitiesForUI" />
    </Spin>
    <Pagination
      v-if="total>10"
      v-model:current="params.pageNo"
      v-model:page-size="params.pageSize"
      :total="total"
      showSizeChanger
      :showTotal="showTotal"
      @change="onPageChange" />
  </PureCard>
</template>
