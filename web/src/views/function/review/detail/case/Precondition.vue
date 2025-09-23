<script lang="ts" setup>
import { defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

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
  <div class="bg-white rounded-lg">
    <div v-if="props.caseInfo?.precondition" :class="props.contentClass">
      <div class="bg-gray-50 rounded-lg p-4">
        <RichEditor :value="props.caseInfo?.precondition" mode="view" />
      </div>
    </div>
    <div
      v-else
      class="text-center py-8 text-gray-400"
      :class="props.contentClass">
      <Icon icon="icon-kong" class="text-4xl mb-2" />
      <div>{{ t('common.noData') }}</div>
    </div>
  </div>
</template>
