<script setup lang="ts">
import { ref } from 'vue';
import { Toggle, Icon, NoData } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import { testCase } from '@/api/tester';
import { CaseDetail } from '@/views/test/types';
import RichEditor from '@/components/richEditor/index.vue';

interface Props {
  id?: number;
  dataSource?: CaseDetail;
  projectId?: string;
  taskId?: number;
  actionAuth?: {[key: string]: any};
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined,
  dataSource: undefined,
  projectId: undefined,
  taskId: undefined,
  actionAuth: () => ({})
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'change', value: Partial<CaseDetail>): void;
  (event: 'loadingChange', value: boolean): void;
}>();

const { t } = useI18n();

const remarkExpand = ref(true);
const descRichRef = ref();
const descError = ref(false);
const isEditDescription = ref(false);
const descriptionContent = ref();
const saveDescriptionLoading = ref(false);

/**
 * <p>Enter editing mode for description.</p>
 * <p>Sets the current description content for editing.</p>
 */
const handleEditDescription = () => {
  isEditDescription.value = true;
  descriptionContent.value = props.dataSource?.description || undefined;
};

/**
 * <p>Cancel description editing.</p>
 * <p>Exits editing mode without saving changes.</p>
 */
const cancelEditDescription = () => {
  isEditDescription.value = false;
};

/**
 * <p>Persist description with length validation.</p>
 * <p>Saves the description if changed and validates character limit.</p>
 */
const saveDescription = async () => {
  if (!props.dataSource) {
    isEditDescription.value = false;
    return;
  }

  if (descRichRef.value.getLength() > 6000) {
    descError.value = true;
    return;
  }
  descError.value = false;
  if (descriptionContent.value === props.dataSource?.description) {
    isEditDescription.value = false;
    return;
  }
  if (saveDescriptionLoading.value) {
    return;
  }
  saveDescriptionLoading.value = true;
  const [error] = await testCase.updateCase([{
    id: props.dataSource.id,
    description: descriptionContent.value
  }]);
  saveDescriptionLoading.value = false;
  if (error) {
    return;
  }
  isEditDescription.value = false;
  emit('change', { description: descriptionContent.value });
};
</script>

<template>
  <Toggle
    v-model:open="remarkExpand"
    class="mt-3.5">
    <template #title>
      <div class="flex items-center space-x-2">
        <span class="text-3.5">{{ t('common.description') }}</span>
        <template v-if="isEditDescription">
          <Button
            class="font-normal text-theme-special"
            type="link"
            size="small"
            @click="saveDescription">
            {{ t('actions.save') }}
          </Button>
          <Button
            class="font-normal text-theme-special"
            type="link"
            size="small"
            @click="cancelEditDescription">
            {{ t('actions.cancel') }}
          </Button>
        </template>
        <Icon
          v-else-if="props.actionAuth['edit']"
          icon="icon-xiugai"
          class="text-3.5 cursor-pointer text-theme-special text-theme-text-hover "
          @click="handleEditDescription" />
      </div>
    </template>

    <template #default>
      <template v-if="isEditDescription">
        <div class="mt-3 mx-2">
          <RichEditor
            ref="descRichRef"
            v-model:value="descriptionContent"
            class="add-case" />
          <div v-show="descError" class="text-status-error">{{ t('testPlan.messages.charLimit2000') }}</div>
        </div>
      </template>

      <template v-else-if="dataSource?.description">
        <RichEditor
          :value="dataSource.description"
          mode="view" />
      </template>

      <template v-else>
        <NoData
          :text="t('common.noData')"
          size="small"
          class="mt-20" />
      </template>
    </template>
  </Toggle>
</template>
