<script setup lang="ts">
import { computed } from 'vue';
import { Icon } from '@xcan-angus/vue-ui';

interface Props {
  icon:string
  name:string,
  value: string | number,
  iconColor?:string
}

const props = withDefaults(defineProps<Props>(), {
  icon: undefined,
  name: undefined,
  value: undefined,
  iconColor: undefined
});

const iconStyle = computed(() => {
  return 'font-size: 40px;' + (props.iconColor ? `color:${props.iconColor}` : '');
});

</script>

<template>
  <div class="flex py-3.5 px-5 border border-theme-text-box flex-1 rounded 2xl:px-3.5">
    <div class="mr-3.5"><Icon :icon="icon" :style="iconStyle" /></div>
    <div class="flex-1">
      <div class="text-3 leading-3 text-theme-sub-content font-normal">
        {{ name }}
      </div>
      <div class="text-5 leading-5 text-theme-content font-semibold mt-3">
        <template v-if="$slots.value">
          <slot name="value"></slot>
        </template>
        <template v-else>
          {{ value??'--' }}
        </template>
      </div>
    </div>
  </div>
</template>
