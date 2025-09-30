<script lang="ts" setup>
// Vue core imports
import { onMounted, reactive, watch } from 'vue';

// UI component imports
import { Input, SelectEnum } from '@xcan-angus/vue-ui';

// Infrastructure imports
import { Percentile } from '@xcan-angus/infra';

/**
 * Component props interface for response time configuration
 */
export interface Props {
  percentile: string,
  art: string,
  disabled: boolean
}

// Component props with default values
const props = withDefaults(defineProps<Props>(), {
  percentile: 'ALL',
  art: '',
  disabled: true
});

// Component events
const emit = defineEmits<{(e:'update:percentile', value: string):void, (e:'update:art', value: string):void}>();

// Response time configuration state
const responseTimeConfig = reactive({
  percentile: '',
  art: ''
});

/**
 * Watch for changes in response time configuration and emit updates
 */
watch(() => responseTimeConfig, () => {
  emit('update:percentile', responseTimeConfig.percentile);
  emit('update:art', responseTimeConfig.art);
}, {
  deep: true
});

/**
 * Watch for changes in props and update local state
 */
watch([() => props.art, () => props.percentile], () => {
  responseTimeConfig.percentile = props.percentile;
  responseTimeConfig.art = props.art;
});

/**
 * Initialize component state on mount
 */
onMounted(() => {
  responseTimeConfig.percentile = props.percentile;
  responseTimeConfig.art = props.art;
});

</script>
<template>
  <div class="flex items-center">
    <SelectEnum
      v-model:value="responseTimeConfig.percentile"
      :disabled="disabled"
      :allowClear="false"
      class="flex-1"
      :enumKey="Percentile" />
    <span class="px-3">&lt;=</span>
    <Input
      v-model:value="responseTimeConfig.art"
      :disabled="disabled"
      dataType="number"
      size="small"
      class="flex-1"
      :allowClear="false" />
    <span class="w-8 text-center">ms</span>
  </div>
</template>
