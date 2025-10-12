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
 * <p>Initiates description editing mode by opening the toggle and enabling edit flag.</p>
 * <p>Sets the content value to the current case description.</p>
 */
const handleEditDescription = () => {
  remarkExpand.value = true;
  isEditDescription.value = true;
  descriptionContent.value = props.dataSource?.description || '';
};

/**
 * <p>Cancels description editing and resets content to original value.</p>
 */
const cancelEditDescription = () => {
  isEditDescription.value = false;
  descriptionContent.value = props.dataSource?.description || '';
};

/**
 * <p>Saves the description changes by calling the API to update case description.</p>
 * <p>Validates content length before saving and handles errors appropriately.</p>
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
      <div class="flex items-center text-3.5">
        <span>{{ t('common.description') }}</span>
        <template v-if="isEditDescription">
          <Button
            size="small"
            type="link"
            @click="cancelEditDescription">
            {{ t('actions.cancel') }}
          </Button>
          <Button
            size="small"
            type="link"
            @click="saveDescription">
            {{ t('actions.confirm') }}
          </Button>
        </template>
        <Button
          v-else-if="props.actionAuth.includes('edit')"
          type="link"
          class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none"
          @click="handleEditDescription">
          <Icon icon="icon-shuxie" class="text-3.5" />
        </Button>
      </div>
    </template>

    <template #default>
      <template v-if="isEditDescription">
        <div class="mb-2.5 mt-2.5 ml-5.5">
          <RichEditor
            ref="descRichRef"
            v-model:value="descriptionContent"
            :options="{placeholder: t('common.placeholders.inputDescription30')}"
            :placeholder="t('common.placeholders.inputDescription30')" />
          <div v-show="descError" class="text-status-error text-3">
            {{ t('testPlan.messages.charLimit2000') }}
          </div>
        </div>
      </template>

      <div v-if="!isEditDescription" class="browser-container">
        <RichEditor
          v-if="dataSource?.description"
          :value="dataSource.description"
          mode="view" />
      </div>

      <NoData
        v-if="!isEditDescription && !dataSource?.description"
        size="small"
        style="min-height: 160px;" />
    </template>
  </Toggle>
</template>

<style scoped>
.border-none {
  border: none;
}

.browser-container  {
  padding-left: 21px;
  transform: translateY(1px);
}
</style>
