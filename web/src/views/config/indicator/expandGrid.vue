<script lang="ts" setup>
import { isBoolean } from '@intlify/shared';
import { ref, watch } from 'vue';
import { Arrow } from '@xcan-angus/vue-ui';

interface Props {
  title: string,
  visible?: boolean,
  arrowClass?: string
}

const props = withDefaults(defineProps<Props>(), {
  title: '',
  arrowClass: ''
});

const expand = ref(true);

const emits = defineEmits<{(e: 'update:visible', value: boolean): void}>();
const toggleExpand = () => {
  expand.value = !expand.value;
};
watch(() => expand.value, () => {
  emits('update:visible', expand.value);
});

watch(() => props.visible, newValue => {
  if (isBoolean(newValue)) {
    expand.value = newValue;
  }
});

</script>
<template>
  <div>
    <div class="py-2 flex items-center border-b" @click.stop="toggleExpand">
      <slot name="title">
        <span class="font-medium flex-1">{{ title }}</span>
      </slot>
      <slot name="button"></slot>
      <Arrow v-model:open="expand" :class="arrowClass" />
    </div>
    <div class="content-wapper" :class="{'max-h-0 opacity-0': !expand, 'max-h-fit': expand }">
      <slot></slot>
    </div>
  </div>
</template>
<style scoped>
.content-wapper {
  @apply overflow-hidden transition-all duration-500;
}
</style>
