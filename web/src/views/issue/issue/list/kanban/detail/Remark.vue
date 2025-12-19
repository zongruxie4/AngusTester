<script setup lang="ts">
import { computed, defineAsyncComponent, ref } from 'vue';
import { Button } from 'ant-design-vue';
import { Icon, NoData, Scroll } from '@xcan-angus/vue-ui';
import { TESTER, utils } from '@xcan-angus/infra';
import { issue } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { Remark } from '@/views/issue/issue/types';
import { BaseProps } from '@/types/types';

/**
 * Props for remark list and submission
 */
const props = withDefaults(defineProps<BaseProps>(), {
  id: undefined
});

const { t } = useI18n();

const notify = ref();

// Async Components
const RichEditor = defineAsyncComponent(() => import('@/components/editor/richEditor/index.vue'));

/**
 * Remark items currently loaded in the scroll list
 */
const remarkList = ref<Remark[]>([]);

/**
 * Content to be submitted via rich editor
 */
const currentContent = ref<string>('');

/**
 * Validation flag for content length or emptiness
 */
const hasValidationError = ref(false);

/**
 * Build query params for Scroll component
 */
const queryParams = computed(() => {
  if (!props.id) {
    return null;
  }
  return { orderBy: 'createdDate', orderSort: 'DESC', taskId: props.id };
});

/**
 * Update local list when Scroll emits data
 */
const handleScrollChange = (data: Remark[]) => {
  remarkList.value = data;
};

/**
 * Delete a remark by id then update local list
 */
const handleDeleteRemark = async (id: number) => {
  const [error] = await issue.deleteTaskRemark(id);
  if (error) {
    return;
  }
  remarkList.value = remarkList.value.filter(item => item.id !== id);
};

/**
 * Submit remark content
 * Validate content and call API to add remark
 */
const handleSubmitRemark = async () => {
  if (!currentContent.value) {
    return;
  }
  if (isContentEmpty(currentContent.value)) {
    return;
  }
  if (isContentTooLong()) {
    hasValidationError.value = true;
    return;
  }
  hasValidationError.value = false;

  const req = { taskId: props.id, content: currentContent.value } as const;
  const [error] = await issue.addTaskRemark(req);
  if (error) {
    return;
  }
  notify.value = utils.uuid();
  currentContent.value = '';
};

/**
 * Detect empty rich text (only newlines)
 */
const isContentEmpty = (content: string) => {
  try {
    const parsed = JSON.parse(content);
    if (!parsed?.length) return true;
    if (parsed.length > 1) return false;
    return !parsed[0].insert.replaceAll('\n', '');
  } catch {
    return false;
  }
};

/**
 * Guard maximum length
 */
const isContentTooLong = () => {
  // RichEditor exposes getLength via ref in backlog version; here we conservatively check raw length
  // Server also validates length; keep client-side quick guard ~6000 chars
  return currentContent.value && currentContent.value.length > 6000;
};
</script>
<template>
  <div class="basic-info-drawer">
    <div class="basic-info-header">
      <h3 class="basic-info-title">{{ t('common.remark') }}</h3>
    </div>

    <div class="pr-2">
      <div class="mb-2.5 border border-gray-200">
        <RichEditor
          :value="currentContent"
          :height="150"
          :options="{theme: 'bubble', placeholder: t('common.placeholders.inputDescription30')}"
          @change="(v:string)=> currentContent = v" />
        <div v-show="hasValidationError" class="text-status-error">
          {{ t('common.placeholders.inputDescription30') }}
        </div>
      </div>
      <div class="space-x-2.5 w-full flex items-center justify-end">
        <Button
          size="small"
          type="primary"
          @click="handleSubmitRemark">
          {{ t('actions.confirm') }}
        </Button>
      </div>
    </div>

    <!-- Scrollable Content Area -->
    <div class="scrollable-content">
      <div class="basic-info-content">
        <Scroll
          v-if="queryParams"
          :action="`${TESTER}/task/remark`"
          :hideNoData="true"
          :params="queryParams"
          :lineHeight="56"
          :notify="notify"
          style="height: calc(100% - 30px);"
          transition
          @change="handleScrollChange">
          <template v-if="remarkList?.length">
            <div
              v-for="remark in remarkList"
              :key="remark.id"
              class="mb-1.5 last:mb-0">
              <div class="flex items-center mb-0.5">
                <div class="border-2 border-theme-text-box w-2.5 h-2.5 rounded-full"></div>
                <div class="ml-3 font-normal text-3 flex items-center space-x-2 leading-4">
                  <div class="text-theme-content font-medium">{{ remark.creator }}</div>
                  <div class="text-theme-content font-medium">{{ t('issue.messages.addedRemark') }}</div>
                  <div class="text-theme-sub-content">{{ remark.createdDate }}</div>
                  <Icon
                    icon="icon-qingchu"
                    class="cursor-pointer text-theme-text-hover text-3.5"
                    @click="handleDeleteRemark(remark.id)" />
                </div>
              </div>
              <div class="browser-container">
                <RichEditor :value="remark.content" mode="view" />
              </div>
            </div>
          </template>
          <NoData v-else size="small" />
        </Scroll>
        <div v-else class="flex items-center justify-center h-full">
          <NoData size="small" />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Main container styles */
.basic-info-drawer { width: 370px; height: 100%; background: #ffffff; font-size: 12px; line-height: 1.4; overflow: hidden; display: flex; flex-direction: column; }
.basic-info-header { padding: 12px 20px 8px; border-bottom: 1px solid #f0f0f0; background: #fafafa; }
.basic-info-title { font-size: 14px; font-weight: 600; color: #262626; margin: 0; line-height: 1.2; }
.scrollable-content { flex: 1; overflow-y: auto; padding: 0; }
.basic-info-content { padding: 16px 20px; display: flex; flex-direction: column; gap: 12px; }
.browser-container { padding-left: 22px; }
</style>
