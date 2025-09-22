<script setup lang="ts">
import { defineAsyncComponent, inject } from 'vue';
import { Button, Pagination } from 'ant-design-vue';
import { Colon, Icon, modal, NoData, notification } from '@xcan-angus/vue-ui';
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
    <NoData v-if="props.baselineList.length === 0" class="flex-1" />

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

          <div class="flex">
            <div
              class="text-theme-sub-content text-3 leading-4 flex items-center flex-none whitespace-nowrap mr-3.5">
              <Icon
                v-if="item.established"
                icon="icon-duihao-copy"
                class="mr-1" />
              <div>{{ item.established ? t('functionBaseline.list.established') : t('functionBaseline.list.notEstablished') }}</div>
            </div>
          </div>
        </div>

        <div class="px-3.5 flex flex-start justify-between text-3 text-theme-sub-content">
          <div class="flex flex-wrap space-x-8">
            <div class="flex mt-3">
              <div class="mr-2 whitespace-nowrap">
                <span>ID</span>
                <Colon />
              </div>
              <div class="text-theme-content">{{ item.id || "--" }}</div>
            </div>

            <div class="flex mt-3">
              <div class="mr-2 whitespace-nowrap">
                <span>{{ t('functionBaseline.list.testPlan') }}</span>
                <Colon />
              </div>
              <div class="text-theme-content">{{ item.planName || "--" }}</div>
            </div>
          </div>

          <div class="flex ml-8 mt-3">
            <div
              class="truncate text-theme-content"
              style="max-width: 100px;"
              :title="item.lastModifiedByName">
              {{ item.lastModifiedByName }}
            </div>
            <div class="mx-2 whitespace-nowrap">{{ t('functionBaseline.list.modifiedBy') }}</div>
            <div class="whitespace-nowrap text-theme-content">
              {{ item.lastModifiedDate }}
            </div>
          </div>
        </div>

        <div class="px-3.5 flex justify-between items-end text-3 my-2.5 relative">
          <div class="truncate mr-8" :title="item.descriptionText || ''">
            <template v-if="item.description">
              <RichText
                v-model:textValue="item.descriptionText"
                :value="item.description"
                :emptyText="t('functionBaseline.list.noDescription')" />
            </template>
            <span v-else class="text-text-sub-content">
              {{ t('functionBaseline.list.noDescription') }}
            </span>
          </div>
          <div class="flex space-x-3 items-center justify-between h-4 leading-5">
            <RouterLink class="flex items-center space-x-1" :to="`/function#baseline?id=${item.id}&type=edit`">
              <Icon icon="icon-shuxie" class="text-3.5" />
              <span>{{ t('functionBaseline.list.edit') }}</span>
            </RouterLink>

            <Button
              v-if="!item.established"
              size="small"
              type="text"
              class="px-0 flex items-center space-x-1"
              @click="handleBaselineEstablishment(item)">
              <Icon icon="icon-yiwancheng" class="text-3.5" />
              <span>{{ t('functionBaseline.list.establishBaseline') }}</span>
            </Button>

            <Button
              size="small"
              type="text"
              class="px-0 flex items-center space-x-1"
              @click="handleBaselineDeletion(item)">
              <Icon icon="icon-yiwancheng" class="text-3.5" />
              <span>{{ t('functionBaseline.list.delete') }}</span>
            </Button>
          </div>
        </div>
      </div>

      <Pagination
        v-if="props.total > 5"
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
