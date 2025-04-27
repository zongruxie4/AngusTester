<script lang="ts" setup>
// import RichBrowser from '@xcan/browser';
import RichEditor from '@/components/richEditor/index.vue';

interface Props {
  caseInfo?: {[key: string]: any};
  contentBg: string;
}
const props = withDefaults(defineProps<Props>(), {
  caseInfo: undefined,
  contentBg: ''
});

</script>
<template>
  <div class="space-y-3">
    <div class="font-semibold text-3.5">
      描述 {{ props.contentBg }}
    </div>
    <div v-if="props.caseInfo?.description">
      <RichEditor
        :value="props.caseInfo?.description"
        mode="view" />
    </div>

    <div v-else class="text-text-sub-content">
      无描述。
    </div>
  </div>
</template>
