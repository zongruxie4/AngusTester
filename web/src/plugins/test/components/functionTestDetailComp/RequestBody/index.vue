<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { NoData, FormatHighlight } from '@xcan-angus/vue-ui';

import { ExecContent } from '@/plugins/test/types';

const { t } = useI18n();

export interface Props {
  value: ExecContent
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

const forms = computed(() => {
  return props.value?.content?.request0?.forms;
});

const rawContent = computed(() => {
  return props.value?.content?.request0?.body;
});
</script>
<template>
  <template v-if="!forms&&!rawContent">
    <NoData size="small" class="my-10" />
  </template>
  <template v-else-if="forms">
    <div class="pt-4.75 pb-4 pl-0.5">
      <FormatHighlight
        :dataSource="forms"
        dataType="json"
        format="preview" />
    </div>
  </template>
  <template v-else-if="rawContent">
    <div class="pt-4.75 pb-4 pl-0.5">
      <FormatHighlight
        :dataSource="rawContent"
        dataType="plain"
        format="preview" />
    </div>
  </template>
</template>
