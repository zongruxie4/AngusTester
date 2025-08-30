<script setup lang="ts">
import { Icon } from '@xcan-angus/vue-ui';

interface Props {
  icon:string,
  title:string,
  visible:boolean,
}

const props = withDefaults(defineProps<Props>(), {
  icon: '',
  title: '',
  visible: false
});

const emit = defineEmits<{(e: 'update:visible', visible: boolean): void, }>();

const expand = () => {
  emit('update:visible', !props.visible);
};

</script>

<template>
  <div class="flex justify-between items-center py-1.5 bg-theme-menu-hover cursor-pointer min-w-full border-b border-theme-text-box" @click.self="expand">
    <template v-if="!$slots.title">
      <div class="text-3 leading-3 font-medium text-theme-title"><Icon :icon="icon" class="text-3 leading-3 mr-1 -mt-0.5" />{{ title }}</div>
    </template>
    <slot name="title"></slot>
    <Icon
      icon="icon-xiangxia"
      class="text-3 leading-4 transition transform text-theme-sub-content"
      :class="visible?'':'-rotate-90'"
      @click.stop="expand" />
  </div>
</template>
