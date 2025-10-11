<script lang="ts" setup>
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';

import { testCase } from '@/api/tester';
import RichEditor from '@/components/richEditor/index.vue';

const { t } = useI18n();

interface Props {
  caseInfo?: {[key: string]: any};
  contentBg: string;
  readonly: boolean;
}
const props = withDefaults(defineProps<Props>(), {
  caseInfo: undefined,
  contentBg: '',
  readonly: true
});

const emits = defineEmits<{(e: 'change')}>();

const descRichRef = ref();
const descriptionContent = ref();
const descError = ref(false);
const isEditDescription = ref(false);
const saveLoading = ref(false);
const toEdit = () => {
  isEditDescription.value = true;
  descriptionContent.value = props?.caseInfo?.description;
};

const cancel = () => {
  isEditDescription.value = false;
};

const confirm = async () => {
  if (descRichRef.value.getLength() > 6000) {
    descError.value = true;
    return;
  }
  descError.value = false;
  if (descriptionContent.value === props.caseInfo?.description) {
    isEditDescription.value = false;
    return;
  }
  if (saveLoading.value) {
    return;
  }
  saveLoading.value = true;
  const [error] = await testCase.updateCase([{
    id: props?.caseInfo?.id,
    description: descriptionContent.value
  }]);
  saveLoading.value = false;
  if (error) {
    return;
  }
  isEditDescription.value = false;
  emits('change');
};
</script>
<template>
  <div class="bg-white rounded-lg">
    <template v-if="!props.readonly">
      <div v-show="!isEditDescription" class="flex justify-end">
        <Button
          size="small"
          type="link"
          @click="toEdit">
          <Icon icon="icon-xiugai" />
        </Button>
      </div>
      <div v-show="isEditDescription" class="flex justify-end">
        <Button
          size="small"
          type="link"
          @click="cancel">
          {{ t('actions.cancel') }}
        </Button>
        <Button
          :loading="saveLoading"
          size="small"
          type="link"
          @click="confirm">
          {{ t('actions.confirm') }}
        </Button>
      </div>
    </template>

    <template v-if="isEditDescription">
      <div class="mt-3 mx-2">
        <RichEditor
          ref="descRichRef"
          v-model:value="descriptionContent"
          class="add-case" />
        <div v-show="descError" class="text-status-error">
          {{ t('testPlan.messages.charLimit2000') }}
        </div>
      </div>
    </template>

    <div v-else-if="props.caseInfo?.description" class="bg-gray-50 rounded-lg p-4">
      <RichEditor
        :value="props.caseInfo?.description"
        mode="view" />
    </div>

    <div v-else class="text-center py-8 text-gray-400">
      <Icon icon="icon-kong" class="text-4xl mb-2" />
      <div>{{ t('common.noDescription') }}</div>
    </div>
  </div>
</template>
