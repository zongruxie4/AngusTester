<script setup lang="ts">
import { defineAsyncComponent, inject } from 'vue';
import { Button, Pagination } from 'ant-design-vue';
import { Icon, modal, NoData, notification } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { func } from '@/api/tester';
import { BaselineDetail } from '@/views/function/baseline/types';

const RichText = defineAsyncComponent(() => import('@/components/richEditor/textContent/index.vue'));

// Props definition
interface Props {
  baselineList: BaselineDetail[];
  loading: boolean;
  total: number;
  currentPageNo: number;
  currentPageSize: number;
  pageSizeOptions: string[];
  isAdmin: boolean;
}

const props = defineProps<Props>();

// Emits definition
const emit = defineEmits<{
  paginationChange: [pageNo: number, pageSize: number];
  establishBaseline: [baselineData: BaselineDetail];
  deleteBaseline: [baselineData: BaselineDetail];
  refresh: [];
}>();

const { t } = useI18n();

// Dependency injection
const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));

/**
 * Get baseline status style based on established status
 * @param established - Whether baseline is established
 * @returns CSS classes for baseline status container
 */
const getBaselineStatusStyle = (established: boolean) => {
  return established
    ? 'bg-green-50 text-green-700 border-green-200 hover:bg-green-100'
    : 'bg-gray-50 text-gray-700 border-gray-200 hover:bg-gray-100';
};

/**
 * Get baseline status dot style based on established status
 * @param established - Whether baseline is established
 * @returns CSS classes for baseline status dot
 */
const getBaselineStatusDotStyle = (established: boolean) => {
  return established ? 'bg-green-500' : 'bg-gray-500';
};

/**
 * Handles baseline establishment and emits the event to parent
 * @param baselineData - The baseline data to establish
 */
const handleBaselineEstablishment = async (baselineData: BaselineDetail) => {
  emit('establishBaseline', baselineData);
};

/**
 * Handles baseline deletion with confirmation
 * @param baselineData - The baseline data to delete
 */
const handleBaselineDeletion = async (baselineData: BaselineDetail) => {
  modal.confirm({
    content: t('functionBaseline.list.confirmDeleteBaseline', { name: baselineData.name }),
    async onOk () {
      const id = baselineData.id;
      const [error] = await func.deleteBaseline([id]);
      if (error) {
        return;
      }

      notification.success(t('functionBaseline.list.baselineDeletedSuccess'));
      emit('refresh');

      deleteTabPane([id]);
    }
  });
};

/**
 * Handles pagination changes and emits the event to parent
 * @param pageNo - The new page number
 * @param pageSize - The new page size
 */
const handlePaginationChange = (pageNo: number, pageSize: number) => {
  emit('paginationChange', pageNo, pageSize);
};
</script>

<template>
  <div>
    <NoData v-if="props.baselineList.length === 0" class="flex-1 mt-20" />

    <template v-else>
      <div
        v-for="(item, index) in props.baselineList"
        :key="item.id"
        class="mb-3.5 border border-theme-text-box rounded">
        <div class="px-3.5 py-2 flex items-center justify-between bg-theme-form-head w-full relative">
          <div class="truncate" style="width:35%;max-width: 360px;">
            <RouterLink
              class="router-link flex-1 truncate"
              :title="item.name"
              :to="`/function#baseline?id=${item.id}`">
              {{ item.name }}
            </RouterLink>
          </div>

          <div class="flex items-center">
            <div
              class="px-3 py-1.5 rounded-full text-xs font-semibold shadow-sm border transition-all duration-200 hover:shadow-md"
              :class="getBaselineStatusStyle(item.established)">
              <div class="flex items-center space-x-1.5">
                <div
                  class="w-2 h-2 rounded-full"
                  :class="getBaselineStatusDotStyle(item.established)">
                </div>
                <span>{{ item.established ? t('functionBaseline.list.established') : t('functionBaseline.list.notEstablished') }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Baseline information card -->
        <div class="px-4 py-3 bg-theme-bg-subtle/30 border-t border-theme-border-subtle">
          <div class="flex items-center justify-between">
            <!-- Left side: ID + Test Plan + Status -->
            <div class="flex items-center space-x-16">
              <!-- ID -->
              <div class="flex items-center space-x-2">
                <div class="flex flex-col">
                  <span class="text-xs text-theme-sub-content">ID</span>
                  <span class="text-sm text-theme-content">
                    {{ item.id || "--" }}
                  </span>
                </div>
              </div>

              <!-- Test Plan -->
              <div class="flex items-center space-x-2">
                <div class="flex flex-col">
                  <span class="text-xs text-theme-sub-content">{{ t('functionBaseline.list.testPlan') }}</span>
                  <span class="text-sm text-theme-content truncate max-w-82" :title="item.planName">
                    {{ item.planName || "--" }}
                  </span>
                </div>
              </div>
            </div>

            <!-- Right side: Last modified info -->
            <div class="flex items-center">
              <!-- Last modified -->
              <div class="flex items-center space-x-2 text-xs text-theme-sub-content">
                <span class="text-theme-content font-medium truncate max-w-16" :title="item.lastModifiedByName">
                  {{ item.lastModifiedByName }}
                </span>
                <span>{{ t('functionBaseline.list.modifiedBy') }}</span>
                <span class="text-theme-sub-content">{{ item.lastModifiedDate }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Divider line -->
        <div class="border-t border-theme-border-subtle/50"></div>

        <div class="px-3.5 flex justify-between items-start text-3 my-2.5 relative">
          <div
            :title="item.descriptionText || ''"
            class="truncate mr-8"
            style="max-width: 70%;">
            <template v-if="item.description">
              <RichText
                v-model:textValue="item.descriptionText"
                :value="item.description"
                :emptyText="t('functionBaseline.list.noDescription')" />
            </template>
            <span v-else class="text-theme-sub-content">
              {{ t('functionBaseline.list.noDescription') }}
            </span>
          </div>
          <div class="flex items-center justify-between h-4 leading-5">
            <RouterLink class="flex items-center space-x-1" :to="`/function#baseline?id=${item.id}&type=edit`">
              <Icon icon="icon-shuxie" class="text-3.5" />
              <span>{{ t('actions.edit') }}</span>
            </RouterLink>

            <Button
              v-if="!item.established"
              size="small"
              type="text"
              class="px-0 flex items-center ml-3"
              @click="handleBaselineEstablishment(item)">
              <Icon icon="icon-yiwancheng" class="mr-0.5" />
              <span>{{ t('functionBaseline.list.establishBaseline') }}</span>
            </Button>

            <Button
              size="small"
              type="text"
              class="px-0 flex items-center ml-2"
              @click="handleBaselineDeletion(item)">
              <Icon icon="icon-qingchu" class="mr-0.5" />
              <span>{{ t('actions.delete') }}</span>
            </Button>
          </div>
        </div>
      </div>

      <Pagination
        v-if="props.total > 4"
        :current="props.currentPageNo"
        :pageSize="props.currentPageSize"
        :pageSizeOptions="props.pageSizeOptions"
        :total="props.total"
        :hideOnSinglePage="false"
        showSizeChanger
        size="default"
        class="text-right"
        @change="handlePaginationChange" />
    </template>
  </div>
</template>

<style scoped>
.router-link {
  color: #1890ff;
  cursor: pointer;
}
</style>
