<script lang="ts" setup>
import { onMounted, reactive, watch } from 'vue';
import { Input, SelectEnum } from '@xcan-angus/vue-ui';
import { Percentile } from '@xcan-angus/infra';

export interface Props {
  percentile: string,
  art: string,
  disabled: boolean
}

const props = withDefaults(defineProps<Props>(), {
  percentile: 'ALL',
  art: '',
  disabled: true
});

const emit = defineEmits<{(e:'update:percentile', value: string):void, (e:'update:art', value: string):void}>();

const timeValue = reactive({
  percentile: '',
  art: ''
});

watch(() => timeValue, () => {
  emit('update:percentile', timeValue.percentile);
  emit('update:art', timeValue.art);
}, {
  deep: true
});

watch([() => props.art, () => props.percentile], () => {
  timeValue.percentile = props.percentile;
  timeValue.art = props.art;
});

onMounted(() => {
  timeValue.percentile = props.percentile;
  timeValue.art = props.art;
});

</script>
<template>
  <div class="flex items-center">
    <SelectEnum
      v-model:value="timeValue.percentile"
      :disabled="disabled"
      :allowClear="false"
      class="flex-1"
      :enumKey="Percentile" />
    <span class="px-3">&lt;=</span>
    <Input
      v-model:value="timeValue.art"
      :disabled="disabled"
      dataType="number"
      size="small"
      class="flex-1"
      :allowClear="false" />
    <span class="w-8 text-center">ms</span>
  </div>
</template>
