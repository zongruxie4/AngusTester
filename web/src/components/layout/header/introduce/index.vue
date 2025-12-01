<script lang="ts" setup>
import { Icon } from '@xcan-angus/vue-ui';

interface Props {
  title: string,
  steps: {icon: string, title: string, content: string}[]
}

withDefaults(defineProps<Props>(), {
  title: '',
  steps: () => ([])
});

</script>
<template>
  <div class="border border-blue-border rounded p-4 bg-blue-bg-light text-3.5 font-serif">
    <div v-if="title" class="flex mb-5">
      <Icon icon="icon-tishi1" class="text-blue-border text-3.5 mr-1" />
      <p class="font-semibold flex-1">{{ title }}</p>
    </div>
    <div v-show="steps.length" class="flex justify-between px-5">
      <div
        v-for="step in steps"
        :key="step.title"
        class="flex-1">
        <div class="text-theme-title font-semibold mb-2 flex items-center">
          <Icon :icon="step.icon" v-if="step.icon" class="text-3.75 mr-1.5" />
          <span>{{ step.title }}</span>
        </div>
        <div class="text-theme-sub-content min-w-63 max-w-4/5">{{ step.content }}</div>
      </div>
    </div>
  </div>
</template>
