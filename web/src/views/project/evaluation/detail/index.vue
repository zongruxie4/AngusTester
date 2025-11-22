<script setup lang="ts">
import { computed, inject, onMounted, ref, watch } from 'vue';
import { Button, Descriptions, DescriptionsItem, Tag } from 'ant-design-vue';
import { Icon, notification, Spin } from '@xcan-angus/vue-ui';
import { toClipboard } from '@xcan-angus/infra';
import { evaluation } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { BasicProps } from '@/types/types';
import { ProjectMenuKey } from '@/views/project/menu';
import { EvaluationDetail } from '../types';

const { t } = useI18n();

// Props
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  data: undefined
});

const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));
const addTabPane = inject<(data: any) => void>('addTabPane', () => ({}));

// Reactive data
const loading = ref(false);
const evaluationDetail = ref<EvaluationDetail>();

/**
 * Loads evaluation detail data from API
 */
const loadEvaluationDetail = async (evaluationId: string) => {
  if (loading.value) {
    return;
  }

  loading.value = true;
  const [error, res] = await evaluation.getEvaluationDetail(Number(evaluationId));
  loading.value = false;
  if (error) {
    return;
  }

  const data = res?.data as EvaluationDetail;
  if (!data) {
    return;
  }

  evaluationDetail.value = data;
  updateTabPaneTitle(data.name, evaluationId);
};

/**
 * Updates the tab pane title with the evaluation name
 */
const updateTabPaneTitle = (evaluationName: string, evaluationId: string) => {
  if (evaluationName && typeof updateTabPane === 'function') {
    updateTabPane({ name: evaluationName, _id: evaluationId + '-detail' });
  }
};

/**
 * Navigates to edit page
 */
const handleEdit = () => {
  if (!evaluationDetail.value) {
    return;
  }
  addTabPane({
    _id: String(evaluationDetail.value.id),
    value: 'evaluationEdit',
    noCache: true,
    data: { _id: String(evaluationDetail.value.id), id: String(evaluationDetail.value.id) }
  });
};

/**
 * Generates evaluation result
 */
const handleGenerateResult = async () => {
  if (!evaluationDetail.value) {
    return;
  }

  const id = evaluationDetail.value.id;
  loading.value = true;
  const [error] = await evaluation.generateResult(id);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success(t('evaluation.actions.generateResultSuccess'));
  await loadEvaluationDetail(String(id));
};

/**
 * Copies the evaluation URL to clipboard
 */
const copyEvaluationUrl = () => {
  if (!evaluationDetail.value) {
    return;
  }
  const evaluationUrl = window.location.origin + `/project#${ProjectMenuKey.EVALUATION}?id=${evaluationDetail.value.id}`;
  toClipboard(evaluationUrl).then(() => {
    notification.success(t('actions.tips.copySuccess'));
  }).catch(() => {
    notification.error(t('actions.tips.copyFailed'));
  });
};

/**
 * Refreshes the evaluation detail data
 */
const refreshEvaluationData = async () => {
  const evaluationId = props.data?.id;
  if (!evaluationId) {
    return;
  }
  await loadEvaluationDetail(String(evaluationId));
};

// Lifecycle hooks
onMounted(() => {
  watch(() => props.data, async (newValue, oldValue) => {
    const evaluationId = newValue?.id;
    if (!evaluationId) {
      return;
    }

    const previousEvaluationId = oldValue?.id;
    if (evaluationId === previousEvaluationId) {
      return;
    }

    await loadEvaluationDetail(String(evaluationId));
  }, { immediate: true });
});
</script>

<template>
  <Spin :spinning="loading" class="h-full text-3 leading-5 px-5 py-5 overflow-auto">
    <div class="flex items-start flex-wrap space-y-b-2 space-x-2.5 mb-3.5">
      <Button
        type="default"
        size="small"
        class="p-0">
        <a
          class="flex items-center space-x-1 leading-6.5 px-1.75"
          @click="handleEdit">
          <Icon icon="icon-shuxie" class="text-3.5" />
          <span>{{ t('actions.edit') }}</span>
        </a>
      </Button>

      <Button
        type="default"
        size="small"
        class="flex items-center space-x-1"
        @click="handleGenerateResult">
        <Icon icon="icon-shengcheng" class="text-3.5" />
        <span>{{ t('evaluation.actions.generateResult') }}</span>
      </Button>

      <Button
        size="small"
        class="flex items-center"
        @click="copyEvaluationUrl">
        <Icon class="mr-1 flex-shrink-0" icon="icon-fuzhi" />
        <span>{{ t('actions.copyLink') }}</span>
      </Button>

      <Button
        type="default"
        size="small"
        class="flex items-center"
        @click="refreshEvaluationData">
        <Icon class="mr-1 flex-shrink-0" icon="icon-shuaxin" />
        <span>{{ t('actions.refresh') }}</span>
      </Button>
    </div>

    <Descriptions
      v-if="evaluationDetail"
      :column="2"
      bordered
      size="small">
      <DescriptionsItem :label="t('common.name')">
        {{ evaluationDetail.name }}
      </DescriptionsItem>

      <DescriptionsItem :label="t('evaluation.columns.scope')">
        <Tag v-if="evaluationDetail.scope">
          {{ evaluationDetail.scope?.message || evaluationDetail.scope?.value }}
        </Tag>
      </DescriptionsItem>

      <DescriptionsItem :label="t('evaluation.columns.purposes')">
        <div class="flex flex-wrap gap-1">
          <Tag
            v-for="(purpose, index) in evaluationDetail.purposes"
            :key="index">
            {{ purpose?.message || purpose?.value }}
          </Tag>
        </div>
      </DescriptionsItem>

      <DescriptionsItem
        v-if="evaluationDetail.resourceId"
        :label="t('evaluation.columns.resourceId')">
        {{ evaluationDetail.resourceId }}
      </DescriptionsItem>

      <DescriptionsItem :label="t('common.startDate')">
        {{ evaluationDetail.startDate || '-' }}
      </DescriptionsItem>

      <DescriptionsItem :label="t('common.deadlineDate')">
        {{ evaluationDetail.deadlineDate || '-' }}
      </DescriptionsItem>

      <DescriptionsItem :label="t('common.createdBy')">
        {{ evaluationDetail.createdByName || '-' }}
      </DescriptionsItem>

      <DescriptionsItem :label="t('common.createdDate')">
        {{ evaluationDetail.createdDate || '-' }}
      </DescriptionsItem>

      <DescriptionsItem
        v-if="evaluationDetail.lastModifiedName"
        :label="t('common.lastModifiedBy')">
        {{ evaluationDetail.lastModifiedName }}
      </DescriptionsItem>

      <DescriptionsItem
        v-if="evaluationDetail.lastModifiedDate"
        :label="t('common.lastModifiedDate')">
        {{ evaluationDetail.lastModifiedDate }}
      </DescriptionsItem>

      <DescriptionsItem
        v-if="evaluationDetail.result"
        :label="t('evaluation.columns.result')"
        :span="2">
        <pre class="whitespace-pre-wrap break-words">{{ JSON.stringify(evaluationDetail.result, null, 2) }}</pre>
      </DescriptionsItem>
    </Descriptions>
  </Spin>
</template>

<style scoped>
:deep(.ant-descriptions-item-label) {
  font-weight: 500;
  color: #000;
}
</style>

