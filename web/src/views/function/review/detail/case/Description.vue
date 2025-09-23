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
  <div class="bg-white rounded-lg">
    <div v-if="props.caseInfo?.description" class="bg-gray-50 rounded-lg p-4">
      <RichEditor
        :value="props.caseInfo?.description"
        mode="view" />
    </div>

    <div v-else class="text-center py-8 text-gray-400">
      <Icon icon="icon-kong" class="text-4xl mb-2" />
      <div>{{ t('caseReview.comp.description.noDescription') }}</div>
    </div>
  </div>
</template>
