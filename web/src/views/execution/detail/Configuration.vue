<script setup lang="ts">
import { ref } from 'vue';
import { Button } from 'ant-design-vue';
import { Spin } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { useConfigData } from './composables/useConfigData';
import ExecSettingForm from '@/components/ExecSettingForm/index.vue';

/**
 * Props for the Configuration component
 */
interface Props {
  /** Execution name */
  execName: string;
  /** Execution ID */
  execId: string;
  /** Script information */
  scriptInfo: Record<string, any>;
  /** Loading state */
  loading: boolean;
}

// Define component props with defaults
const props = withDefaults(defineProps<Props>(), {
  execName: '',
  execId: '',
  scriptInfo: undefined,
  loading: false
});

// Initialize internationalization
const { t } = useI18n();

// Define component events
const emit = defineEmits<{(e: 'update:loading', value: boolean): void}>();

// Use configuration data composable for logic
const { execSettingFormRef, saveSetting } = useConfigData(
  props.execId,
  props.execName,
  props.scriptInfo,
  () => props.loading,
  emit
);

// Reference to the execution form
const execFormRef = ref();
</script>
<template>
  <Spin
    ref="execFormRef"
    :spinning="loading"
    class="h-full overflow-y-auto py-3.5 px-8 w-full">
    <div style="max-width: 1440px;">
      <ExecSettingForm
        ref="execSettingFormRef"
        :scriptType="props?.scriptInfo?.type"
        :scriptInfo="props?.scriptInfo" />
      <div class="flex pl-3.5 mt-10 pb-8">
        <Button
          size="small"
          type="primary"
          class="mr-5"
          @click="saveSetting">
          {{ t('actions.save') }}
        </Button>
        <RouterLink to="/execution">
          <Button size="small">{{ t('actions.cancel') }}</Button>
        </RouterLink>
      </div>
    </div>
  </Spin>
</template>
