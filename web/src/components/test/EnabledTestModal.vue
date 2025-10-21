<script lang="ts" setup>
import { ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Hints, notification } from '@xcan-angus/vue-ui';
import { Checkbox, Modal, Switch } from 'ant-design-vue';
import { services } from '@/api/tester';

const { t } = useI18n();

interface Props {
  visible: boolean;
  id?: string; // serviceId
}

const props = withDefaults(defineProps<Props>(), {
  id: ''
});

// Component events
const emits = defineEmits<{(e: 'update:visible', value: boolean): void}>();

// Component state
const enabledTestTypes = ref(['PERFORMANCE', 'STABILITY', 'FUNCTIONAL']);
const visibleTestTypes = ref(['PERFORMANCE', 'STABILITY', 'FUNCTIONAL']);
const isValidationEnabled = ref(false);

// Test type configuration
const testTypeOptions = [
  {
    label: t('commonComp.apis.enabledTestModal.functionalTest'),
    value: 'FUNCTIONAL'
  },
  {
    label: t('commonComp.apis.enabledTestModal.performanceTest'),
    value: 'PERFORMANCE'
  },
  {
    label: t('commonComp.apis.enabledTestModal.stabilityTest'),
    value: 'STABILITY'
  }
];

/**
 * Handle test type enable/disable toggle
 * @param isEnabled - Whether the test type is enabled
 * @param testType - Test type to toggle
 */
const handleTestTypeToggle = (isEnabled: boolean, testType: string) => {
  if (isEnabled) {
    enabledTestTypes.value.push(testType);
  } else {
    enabledTestTypes.value = enabledTestTypes.value.filter(type => type !== testType);
  }
};

/**
 * Handle test type visibility toggle
 * @param event - Checkbox change event
 * @param testType - Test type to toggle visibility
 */
const handleTestTypeVisibilityToggle = (event: any, testType: string) => {
  if (event.target.checked) {
    visibleTestTypes.value.push(testType);
  } else {
    visibleTestTypes.value = visibleTestTypes.value.filter(type => type !== testType);
  }
};

// Loading state
const isLoading = ref(false);

/**
 * Handle modal confirmation and save test type settings
 */
const handleModalConfirmation = async () => {
  isLoading.value = true;
  if (!visibleTestTypes.value.length) {
    handleModalClose();
    isLoading.value = false;
    return;
  }

  const enabledTypes = visibleTestTypes.value.filter(type => enabledTestTypes.value.includes(type));
  if (enabledTypes.length) {
    const [error] = await services.toggleTestEnabled(props.id, true, { testTypes: enabledTypes }, {
      paramsType: true
    });
    if (error) {
      return;
    }
  }

  const disabledTypes = visibleTestTypes.value.filter(type => !enabledTypes.includes(type));
  if (disabledTypes.length) {
    const [error] = await services.toggleTestEnabled(props.id, false, { testTypes: disabledTypes }, {
      paramsType: true
    });
    if (error) {
      return;
    }
  }

  isLoading.value = false;
  notification.success(t('commonComp.apis.enabledTestModal.successMessage'));
  emits('update:visible', false);
};

/**
 * Handle modal close
 */
const handleModalClose = () => {
  emits('update:visible', false);
};

// Watch for modal visibility changes
watch(() => props.visible, () => {
  enabledTestTypes.value = ['PERFORMANCE', 'STABILITY', 'FUNCTIONAL'];
  visibleTestTypes.value = ['PERFORMANCE', 'STABILITY', 'FUNCTIONAL'];
  isValidationEnabled.value = false;
}, {
  immediate: true
});

</script>
<template>
  <Modal
    :title="t('commonComp.apis.enabledTestModal.title')"
    :confirmLoading="isLoading"
    :visible="props.visible"
    :width="350"
    @cancel="handleModalClose"
    @ok="handleModalConfirmation">
    <Hints :text="t('commonComp.apis.enabledTestModal.hint')" />
    <div class="mt-2">
      <div
        v-for="testType in testTypeOptions"
        :key="testType.value"
        class="flex items-center mb-2 flex-1">
        <Checkbox :checked="visibleTestTypes.includes(testType.value)" @change="handleTestTypeVisibilityToggle($event, testType.value)" />
        <span class="w-20 ml-2">{{ testType.label }}</span>
        <div>
          <Switch
            v-show="visibleTestTypes.includes(testType.value)"
            :checked="enabledTestTypes.includes(testType.value)"
            :checkedChildren="t('status.enabled')"
            :unCheckedChildren="t('status.disabled')"
            size="small"
            @click="handleTestTypeToggle($event, testType.value)" />
        </div>
      </div>
    </div>
  </Modal>
</template>
