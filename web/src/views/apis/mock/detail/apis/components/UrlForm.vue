<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Composite, HttpMethodText, IconCopy, IconRequired, Input, Select } from '@xcan-angus/vue-ui';

import SelectEnum from '@/components/enum/SelectEnum.vue';

// Props & Emits
interface Props {
  method:string;
  options: { label: string; value: string; }[]
  endpoint:string|undefined;
  server?:string;
  readonly?:boolean;
}

const props = withDefaults(defineProps<Props>(), {
  method: undefined,
  endpoint: undefined,
  server: undefined,
  options: () => [],
  readonly: false
});

const { t } = useI18n();

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:method', value: string): void;
  (e: 'update:endpoint', value: string): void;
  (e: 'update:server', value: string): void;
}>();

// ==================== Reactive State ====================
const domain = ref();
const pathname = ref();
const error = ref(false);

// ==================== Computed Properties ====================
/**
 * Copy text for URL construction
 */
const copyText = computed(() => {
  let url = '';
  if (domain.value) {
    url += domain.value;
  }

  if (pathname.value) {
    url += pathname.value;
  }

  return url;
});

// ==================== Methods ====================
/**
 * Handle HTTP method selection change
 * @param value - Selected method value
 */
const handleMethodChange = (value: string) => {
  emit('update:method', value);
};

/**
 * Handle server/domain selection change
 * @param value - Selected server value
 */
const handleServerChange = (value: any) => {
  domain.value = value;
  emit('update:server', value);
};

/**
 * Handle endpoint input change
 * @param event - Input change event
 */
const handleEndpointChange = (event: any) => {
  const value = event.target.value;
  pathname.value = value;
  emit('update:endpoint', value);
  error.value = false;
};

/**
 * Handle endpoint input blur validation
 * @param event - Input blur event
 */
const handleEndpointBlur = (event: any) => {
  const value = event.target.value;
  pathname.value = value;
  emit('update:endpoint', value);
  error.value = !isValidPath(value);
};

/**
 * Validate pathname format
 * @param _pathname - Pathname to validate
 * @returns Whether pathname is valid
 */
const isValidPath = (_pathname: string): boolean => {
  if (!_pathname) {
    return false;
  }

  return true;
};

/**
 * Validate the entire form
 * @returns Whether form is valid
 */
const isValid = ():boolean => {
  let errorNum = 0;
  if (!isValidPath(pathname.value)) {
    errorNum++;
  }

  error.value = !!errorNum;
  return !errorNum;
};

// ==================== Watchers ====================
onMounted(() => {
  watch(() => props.endpoint, (newValue) => {
    error.value = false;
    pathname.value = newValue;
  }, { immediate: true });
});

// ==================== Expose Methods ====================
defineExpose({
  isValid
});
</script>
<template>
  <div class="leading-5">
    <div class="mb-0.5">
      <IconRequired />
      <span>{{ t('common.methodPath') }}</span>
    </div>
    <div class="flex items-center">
      <Composite>
        <SelectEnum
          :value="props.method"
          :disabled="props.readonly"
          class="w-25 flex-shrink-0"
          enumKey="HttpMethod"
          :placeholder="t('protocol.requestMethod')"
          @change="handleMethodChange">
          <template #option="record">
            <HttpMethodText :value="record.value" />
          </template>
        </SelectEnum>
        <Select
          v-model:value="domain"
          :options="props.options"
          defaultActiveFirstOption
          class="w-70 flex-shrink-0"
          :placeholder="t('protocol.domain')"
          @change="handleServerChange" />
        <Input
          :value="pathname"
          :error="error"
          :maxlength="800"
          :disabled="props.readonly"
          trim
          class="flex-1"
          :placeholder="t('actions.tips.maxLengthSupported', {max: '800'})"
          @blur="handleEndpointBlur"
          @change="handleEndpointChange" />
      </Composite>
      <span class="text-theme-sub-content">
        <IconCopy class="flex-shrink-0 ml-1.5" :copyText="copyText" />
      </span>
    </div>
  </div>
</template>
<style scoped>
.error.ant-input-affix-wrapper {
  border-width: 1px;
  border-style: solid;
}

.error-border {
  border-color: rgba(245, 34, 45, 100%);
}

.select-input-container {
  transition: all 200ms linear 0ms;
}

.select-input-container:hover,
.select-input-container:focus,
.select-input-container-focused {
  border-color: #40a9ff;
}
</style>
