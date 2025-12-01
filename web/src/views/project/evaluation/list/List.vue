<script setup lang="ts">
import { Button, Pagination, Tag } from 'ant-design-vue';
import { Colon, Icon, NoData } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { EvaluationDetail } from '../types';
import { EvaluationPurpose } from '@/enums/enums';

const { t } = useI18n();

/**
 * Get tag color based on purpose value
 */
const getPurposeTagColor = (purpose: any): string => {
  const value = purpose?.value || purpose;

  switch (value) {
    case EvaluationPurpose.FUNCTIONAL_SCORE:
      return 'blue';
    case EvaluationPurpose.PERFORMANCE_SCORE:
      return 'cyan';
    case EvaluationPurpose.STABILITY_SCORE:
      return 'geekblue';
    case EvaluationPurpose.SECURITY_SCORE:
      return 'red';
    case EvaluationPurpose.COMPATIBILITY_SCORE:
      return 'green';
    case EvaluationPurpose.USABILITY_SCORE:
      return 'orange';
    case EvaluationPurpose.MAINTAINABILITY_SCORE:
      return 'purple';
    case EvaluationPurpose.SCALABILITY_SCORE:
      return 'magenta';
    case EvaluationPurpose.COMPLIANCE_SCORE:
      return 'blue';
    case EvaluationPurpose.AVAILABILITY_SCORE:
      return 'cyan';
    default:
      return 'default';
  }
};

interface Props {
  dataList: EvaluationDetail[];
  loading: boolean;
  total: number;
  pageNo: number;
  pageSize: number;
  pageSizeOptions: string[];
}

const props = defineProps<Props>();

const emit = defineEmits<{
  paginationChange: [newPageNo: number, newPageSize: number];
  viewDetail: [evaluationData: EvaluationDetail];
  editEvaluation: [evaluationData: EvaluationDetail];
  deleteEvaluation: [evaluationData: EvaluationDetail];
  generateResult: [evaluationData: EvaluationDetail];
}>();

const handleViewDetail = (evaluationData: EvaluationDetail) => {
  emit('viewDetail', evaluationData);
};

const handleEditEvaluation = (evaluationData: EvaluationDetail) => {
  emit('editEvaluation', evaluationData);
};

const handleDeleteEvaluation = (evaluationData: EvaluationDetail) => {
  emit('deleteEvaluation', evaluationData);
};

const handleGenerateResult = (evaluationData: EvaluationDetail) => {
  emit('generateResult', evaluationData);
};

const handlePaginationChange = (newPageNo: number, newPageSize: number) => {
  emit('paginationChange', newPageNo, newPageSize);
};

// const dropdownMenuItems = [
  // {
  //   key: 'view',
  //   icon: 'icon-chakan',
  //   name: t('actions.view')
  // },
  // {
  //   key: 'edit',
  //   icon: 'icon-shuxie',
  //   name: t('actions.edit')
  // },
  // {
  //   key: 'generateResult',
  //   icon: 'icon-shengcheng',
  //   name: t('evaluation.actions.generateResult')
  // },
  // {
  //   key: 'delete',
  //   icon: 'icon-qingchu',
  //   name: t('actions.delete')
  // }
// ];

// const handleDropdownClick = (
//   evaluationData: EvaluationDetail,
//   actionKey: 'view' | 'edit' | 'delete' | 'generateResult'
// ) => {
//   switch (actionKey) {
//     case 'view':
//       handleViewDetail(evaluationData);
//       break;
//     case 'edit':
//       handleEditEvaluation(evaluationData);
//       break;
//     case 'delete':
//       handleDeleteEvaluation(evaluationData);
//       break;
//     case 'generateResult':
//       handleGenerateResult(evaluationData);
//       break;
//   }
// };
</script>

<template>
  <div>
    <NoData v-if="props.dataList.length === 0" class="flex-1 mt-20" />

    <template v-else>
      <div
        v-for="item in props.dataList"
        :key="item.id"
        class="mb-3.5 border border-theme-text-box rounded">
        <!-- Header section -->
        <div class="px-3.5 py-2 flex items-center justify-between bg-theme-form-head w-full relative">
          <div class="truncate" style="width:35%;max-width: 360px;">
            <a
              class="router-link flex-1 truncate cursor-pointer"
              :title="item.name"
              @click="handleViewDetail(item)">
              {{ item.name }}
            </a>
          </div>

          <div class="text-3 whitespace-nowrap">
            <span class="text-sub-content ml-2">{{ item.startDate || '' }}</span>
            <span class="text-theme-sub-content mx-2">-</span>
            <span class="text-sub-content">{{ item.deadlineDate || '' }}</span>
          </div>

          <div class="flex items-center">
            <!-- <Tag v-if="item.scope" class="mr-3">
              {{ item.scope?.message || item.scope?.value }}
            </Tag> -->
          </div>
        </div>

        <!-- Information section -->
        <div class="px-4 py-3 bg-theme-bg-subtle/30 border-t border-theme-border-subtle">
          <div class="flex items-center justify-between">
            <!-- Left side: Scope + Purposes -->
            <div class="flex items-center space-x-16">
              <!-- Scope -->
              <div class="flex items-center space-x-2">
                <div class="flex flex-col">
                  <span class="text-xs text-theme-sub-content">{{ t('evaluation.columns.scope') }}</span>
                  <span class="text-sm font-medium text-theme-content">
                    {{ (item.scope as any)?.message || (item.scope as any)?.value || item.scope || '-' }}
                  </span>
                </div>
              </div>

              <!-- Purposes -->
              <div class="flex items-center space-x-2 flex-1 min-w-0">
                <div class="flex flex-col">
                  <span class="text-xs text-theme-sub-content">{{ t('evaluation.columns.purposes') }}</span>
                  <div class="flex flex-wrap gap-1 mt-1">
                    <Tag
                      v-for="(purpose, index) in (item.purposes || [])"
                      :key="index"
                      :color="getPurposeTagColor(purpose)"
                      size="small">
                      {{ (purpose as any)?.message || (purpose as any)?.value || purpose }}
                    </Tag>
                  </div>
                </div>
              </div>

            </div>

            <!-- Right side: Created info -->
            <div class="flex items-center space-x-2 text-xs text-theme-sub-content">
              <span class="text-theme-content font-medium truncate max-w-16" :title="item.createdByName">
                {{ item.createdByName || '-' }}
              </span>
              <span class="whitespace-nowrap">{{ t('status.createdAt') }}</span>
              <span class="text-theme-sub-content whitespace-nowrap">{{ item.createdDate || '-' }}</span>
            </div>
          </div>
        </div>

        <!-- Divider line -->
        <div class="border-t flex border-theme-border-subtle/50"></div>


        <div class="flex items-center justify-between py-2">

          <!-- Details section -->
          <div class="px-3.5 flex flex-start justify-between text-3 text-theme-sub-content">
            <div class="flex flex-wrap">

              <!-- Resource Name -->
              <div class="flex">
                <div class="mr-2 whitespace-nowrap">
                  <span>{{  t('evaluation.columns.resourceName') }}</span>
                  <Colon />
                </div>
                <div class="text-theme-content">{{ (item as any)?.resourceName || '-'  }}</div>
              </div>

              <div v-if="item.result" class="flex ml-8">
                <div class="mr-2 whitespace-nowrap">
                  <span>{{ t('evaluation.columns.result') }}</span>
                  <Colon />
                </div>
                <div class="text-theme-content text-green-600">{{ t('evaluation.status.hasResult') }}</div>
              </div>
            </div>
          </div>

          <!-- Actions section -->
          <div class="px-3.5 flex justify-between items-center text-3">
            <div class="flex-1"></div>

            <div class="flex items-center justify-between h-4 leading-5">
              <a
                class="flex items-center space-x-1 cursor-pointer"
                @click="handleViewDetail(item)">
                <Icon icon="icon-chakanhuodong" class="text-3.5" />
                <span>{{ t('actions.view') }}</span>
              </a>

              <a
                class="flex items-center space-x-1 ml-3 cursor-pointer"
                @click="handleEditEvaluation(item)">
                <Icon icon="icon-shuxie" class="text-3.5" />
                <span>{{ t('actions.edit') }}</span>
              </a>

              <Button
                size="small"
                type="text"
                class="px-0 flex items-center ml-2"
                @click="handleGenerateResult(item)">
                <Icon icon="icon-shengchengshuju" class="mr-0.5" />
                <span>{{ t('evaluation.actions.generateResult') }}</span>
              </Button>

              <Button
                size="small"
                type="text"
                class="px-0 flex items-center ml-2"
                @click="handleDeleteEvaluation(item)">
                <Icon icon="icon-qingchu" class="mr-0.5" />
                <span>{{ t('actions.delete') }}</span>
              </Button>

              <!-- <Dropdown
                class="ml-2"
                :admin="false"
                :menuItems="dropdownMenuItems"
                @click="handleDropdownClick(item, $event.key)">
                <Icon icon="icon-gengduo" class="cursor-pointer outline-none items-center" />
              </Dropdown> -->
            </div>
          </div>
        </div>

      </div>

      <Pagination
        v-if="props.total > props.pageSize"
        :current="props.pageNo"
        :pageSize="props.pageSize"
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

.router-link:hover {
  text-decoration: underline;
}
</style>

