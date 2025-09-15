<script setup lang="ts">
import { computed, defineAsyncComponent, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon, NoData, Scroll } from '@xcan-angus/vue-ui';
import { TESTER, PageQuery } from '@xcan-angus/infra';
import { task } from '@/api/tester';
import { TaskInfoProps } from '@/views/task/task/list/task/types';
import { Remark } from '@/views/task/task/types';

// Component Props
const props = withDefaults(defineProps<TaskInfoProps>(), {
  id: '-1',
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

/**
 * <p>Query parameters for remark fetching</p>
 * <p>Constructs parameters for fetching remarks ordered by creation date</p>
 */
const queryParams = computed(() => {
  // Only fetch remarks if we have a valid task ID
  if (!props.id || props.id === '' || props.id === '-1') {
    return null; // Return null to prevent API call
  }
  return { orderBy: 'createdDate', orderSort: PageQuery.OrderSort.Asc, taskId: props.id };
});
</script>
<template>
  <div class="basic-info-drawer">
    <div class="basic-info-header">
      <h3 class="basic-info-title">{{ t('backlog.remark') }}</h3>
    </div>

    <!-- Scrollable Content Area -->
    <div class="scrollable-content">
      <div class="basic-info-content">
        <!-- Only render Scroll component when we have valid taskId -->
        <Scroll
          v-if="queryParams"
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
        
        <!-- Show message when no valid taskId -->
        <div v-else class="flex items-center justify-center h-full">
          <NoData size="small" />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Main container styles */
.basic-info-drawer {
  width: 370px;
  height: 100%;
  background: #ffffff;
  font-size: 12px;
  line-height: 1.4;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* Header styles */
.basic-info-header {
  padding: 12px 20px 8px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.basic-info-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin: 0;
  line-height: 1.2;
}

/* Scrollable content area */
.scrollable-content {
  flex: 1;
  overflow-y: auto;
  padding: 0;
}

/* Content area styles */
.basic-info-content {
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.browser-container  {
  padding-left: 22px;
}
</style>
