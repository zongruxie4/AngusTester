<script setup lang="ts">
import { ref } from 'vue';
import { Toggle, Icon, NoData } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import { testCase } from '@/api/tester';
import { CaseDetail } from '@/views/test/types';
import { CaseActionAuth } from '@/views/test/case/types';

import RichEditor from '@/components/richEditor/index.vue';

interface Props {
  id?: number;
  dataSource?: CaseDetail;
  projectId?: string;
  actionAuth?: CaseActionAuth[];
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined,
  dataSource: undefined,
  projectId: undefined,
  actionAuth: () => ([])
});

const emit = defineEmits<{
  (event: 'change', value: Partial<CaseDetail>): void;
  (event: 'loadingChange', value: boolean): void;
}>();

const { t } = useI18n();

const preconditionExpand = ref(true);
const preconditionRichRef = ref();
const preconditionError = ref();
const isEditPrecondition = ref(false);
const preconditionContent = ref();
const savePreconditionLoading = ref(false);

/**
 * <p>Enter editing mode for precondition.</p>
 * <p>Sets the current precondition content for editing.</p>
 */
const handleEditPrecondition = () => {
  isEditPrecondition.value = true;
  preconditionContent.value = props.dataSource?.precondition || undefined;
};

/**
 * <p>Cancel precondition editing.</p>
 * <p>Exits editing mode without saving changes.</p>
 */
const cancelEditPrecondition = () => {
  isEditPrecondition.value = false;
};

/**
 * <p>Persist precondition with length validation.</p>
 * <p>Saves the precondition if changed and validates character limit.</p>
 */
const savePrecondition = async () => {
  if (!props.dataSource) {
    isEditPrecondition.value = false;
    return;
  }

  if (preconditionContent.value === props.dataSource.precondition) {
    isEditPrecondition.value = false;
    return;
  }
  if (preconditionRichRef.value.getLength() > 6000) {
    preconditionError.value = true;
    return;
  }
  preconditionError.value = false;
  if (savePreconditionLoading.value) {
    return;
  }
  savePreconditionLoading.value = true;
  const [error] = await testCase.updateCase([{
    id: props.dataSource.id,
    precondition: preconditionContent.value
  }]);
  savePreconditionLoading.value = false;
  if (error) {
    return;
  }
  isEditPrecondition.value = false;
  emit('change', { precondition: preconditionContent.value });
};
</script>
<template>
  <Toggle
    v-model:open="preconditionExpand"
    class="mt-3.5">
    <template #title>
      <div class="flex items-center space-x-2">
        <span class="text-3.5">{{ t('common.precondition') }}</span>
        <template v-if="isEditPrecondition">
          <Button
            class="font-normal text-theme-special"
            type="link"
            size="small"
            @click="savePrecondition">
            {{ t('actions.save') }}
          </Button>
          <Button
            class="font-normal text-theme-special"
            type="link"
            size="small"
            @click="cancelEditPrecondition">
            {{ t('actions.cancel') }}
          </Button>
        </template>
        <Icon
          v-else-if="props.actionAuth.includes('edit')"
          icon="icon-xiugai"
          class="text-3.5 text-theme-special text-theme-text-hover cursor-pointer"
          @click="handleEditPrecondition" />
      </div>
    </template>

    <template #default>
      <template v-if="isEditPrecondition">
        <RichEditor
          ref="preconditionRichRef"
          v-model:value="preconditionContent"
          :options="{ placeholder: t('testCase.messages.enterPrecondition')}" />
        <div
          v-show="preconditionError"
          class="text-status-error">
          {{ t('testCase.messages.descCharLimit2000') }}
        </div>
      </template>
      <template v-else>
        <template v-if="dataSource?.precondition">
          <RichEditor :value="dataSource.precondition" mode="view" />
        </template>
        <template v-else>
          <NoData
            :text="t('common.noData')"
            class="my-8"
            size="small" />
        </template>
      </template>
    </template>
  </Toggle>
</template>
