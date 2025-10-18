<script lang="ts" setup>
import { ref, watch } from 'vue';
import { Hints, Icon, Input } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import { apis } from '@/api/tester';

interface Props {
  data?: {
    url: string;
    description?: string
  };
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  data: () => ({ url: '' }),
  id: ''
});

const { t } = useI18n();
const emits = defineEmits<{(e: 'update:data', value: {url: string;description?: string}): void}>();

// Reactive references for external documentation URL and description
const url = ref<string>('');
const description = ref<string | undefined>('');
// Flag to control edit mode
const isEditable = ref<boolean>(false);

/**
 * Enable edit mode for external documentation
 */
const enableEditMode = () => {
  isEditable.value = true;
};

/**
 * Cancel editing and revert to original values
 */
const cancelEditing = () => {
  isEditable.value = false;
  url.value = props.data?.url || '';
  description.value = props.data?.description || '';
};

/**
 * Save external documentation data
 * @returns Promise that resolves when save operation completes
 */
const saveExternalDoc = async () => {
  // Create updated external documentation object
  const externalDocs = {
    ...props.data,
    url: url.value,
    description: description.value
  };

  // Update API with new external documentation data
  const [error] = await apis.updateApi([{ id: props.id, externalDocs }]);
  if (error) {
    return;
  }

  // Emit updated data to parent component
  emits('update:data', externalDocs);
  isEditable.value = false;
};

// Watch for changes in props.data and update reactive references
watch(() => props.data, (newValue) => {
  url.value = newValue?.url || '';
  description.value = newValue?.description || '';
}, { immediate: true });
</script>
<template>
  <div class="pl-2">
    <Hints :text="t('service.externalDoc.hints')" />
    <div class="flex">
      <div>
        <Input
          v-model:value="url"
          :maxLength="100"
          :placeholder="t('service.externalDoc.urlPlaceholder')"
          :disabled="!isEditable" />
        <Input
          v-model:value="description"
          :disabled="!isEditable"
          :maxLength="100"
          type="textarea"
          :placeholder="t('service.externalDoc.descriptionPlaceholder')"
          class="mt-1" />
      </div>
      <Icon
        icon="icon-shuxie"
        class="text-4 ml-1 cursor-pointer text-text-link"
        @click="enableEditMode" />
    </div>
    <template v-if="isEditable">
      <Button
        type="link"
        size="small"
        @click="saveExternalDoc">
        {{ t('actions.save') }}
      </Button>
      <Button
        type="link"
        size="small"
        @click="cancelEditing">
        {{ t('actions.cancel') }}
      </Button>
    </template>
  </div>
</template>
