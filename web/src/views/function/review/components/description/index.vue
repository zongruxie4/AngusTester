<script lang="ts" setup>
import { useI18n } from 'vue-i18n';
import RichEditor from '@/components/richEditor/index.vue';

const { t } = useI18n();

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
      {{ t('caseReview.comp.description.title') }} {{ props.contentBg }}
    </div>
    <div v-if="props.caseInfo?.description">
      <RichEditor
        :value="props.caseInfo?.description"
        mode="view" />
    </div>

    <div v-else class="text-text-sub-content">
      {{ t('caseReview.comp.description.noDescription') }}
    </div>
  </div>
</template>
