<script lang="ts" setup>
import { defineAsyncComponent } from 'vue';

interface Props {
  caseInfo?: { precondition: string};
  contentClass: string
}
const props = withDefaults(defineProps<Props>(), {
  caseInfo: undefined,
  contentClass: ''
});

const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));

</script>
<template>
  <div class="space-y-3">
    <div class="font-semibold text-3.5">
      前置条件
    </div>

    <div v-if="props.caseInfo?.precondition" :class="props.contentClass">
      <!-- {{ props.caseInfo?.precondition }} -->
      <RichEditor :value="props.caseInfo?.precondition" mode="view" />
    </div>
    <div
      v-else
      class="text-sub-content"
      :class="props.contentClass">
      无数据~
    </div>
  </div>
</template>
