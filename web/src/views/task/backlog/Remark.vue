<script setup lang="ts">
import { computed, defineAsyncComponent, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon, NoData, Scroll } from '@xcan-angus/vue-ui';
import { TESTER, PageQuery } from '@xcan-angus/infra';
import { task } from '@/api/tester';
import { TaskInfoProps } from '@/views/task/task/list/task/types';
import { Remark } from '@/views/task/backlog/types';

// Component Props
const props = withDefaults(defineProps<TaskInfoProps>(), {
  id: '',
  notify: undefined
});

const { t } = useI18n();

// Async Components
const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));

// Reactive State Variables
const remarkList = ref<Remark[]>([]);

/**
 * <p>Handle scroll data change</p>
 * <p>Updates remark list when new data is received from the Scroll component</p>
 */
const handleScrollDataChange = (data: Remark[]) => {
  remarkList.value = data;
};

/**
 * <p>Delete remark</p>
 * <p>Removes a remark from the server and updates the local list</p>
 */
const deleteRemark = async (remarkId: string) => {
  const [error] = await task.deleteTaskRemark(remarkId);
  if (error) {
    return;
  }

  remarkList.value = remarkList.value.filter(item => item.id !== remarkId);
};

// Computed Properties
/**
 * <p>Query parameters for remark fetching</p>
 * <p>Constructs parameters for fetching remarks ordered by creation date</p>
 */
const queryParams = computed(() => {
  return { orderBy: 'createdDate', orderSort: PageQuery.OrderSort.Asc, taskId: props.id };
});
</script>
<template>
  <div class="h-full text-3 leading-5 pl-5">
    <div class="text-theme-title mb-2.5 font-semibold">{{ t('backlog.remark') }}</div>

    <Scroll
      :action="`${TESTER}/task/remark`"
      :hideNoData="true"
      :params="queryParams"
      :lineHeight="56"
      :notify="props.notify"
      style="height: calc(100% - 30px);"
      transition
      @change="handleScrollDataChange">
      <template v-if="remarkList?.length">
        <div
          v-for="(remark) in remarkList"
          :key="remark.id"
          class="mb-1.5 last:mb-0">
          <div class="flex items-center mb-0.5">
            <div class="border-2 border-theme-text-box w-2.5 h-2.5 rounded-full"></div>

            <div class="ml-3 font-normal text-3 flex items-center space-x-2 leading-4">
              <div class="text-theme-content font-medium">{{ remark.createdByName }}</div>
              <div class="text-theme-content font-medium">{{ t('backlog.addedRemark') }}</div>
              <div class="text-theme-sub-content">{{ remark.createdDate }}</div>
              <Icon
                icon="icon-qingchu"
                class="cursor-pointer text-theme-text-hover text-3.5"
                @click="deleteRemark(remark.id)" />
            </div>
          </div>

          <div class="browser-container">
            <RichEditor :value="remark.content" mode="view" />
          </div>
        </div>
      </template>

      <NoData v-else size="small" />
    </Scroll>
  </div>
</template>

<style scoped>
.browser-container  {
  padding-left: 22px;
}
</style>
