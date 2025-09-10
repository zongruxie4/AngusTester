<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import StatusTag from '@/views/report/preview/execFunction/sampling/http/collapse/statusTag/index.vue';

const { t } = useI18n();

export interface Props {
  value: {
    target: 'THROUGHPUT';
    name: string;
    description: string;
    enabled: boolean;
    beforeName: string;
    transactionName: string;
    permitsPerSecond: string;
    timeoutInMs: string;
  };
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

</script>

<template>
  <div class="dashed-border h-10.5 leading-5 flex items-center px-3 rounded border border-solid border-theme-text-box">
    <span class="flex-shrink-0 mr-3">
      <svg
        t="1725160437620"
        class="icon"
        viewBox="0 0 1024 1024"
        version="1.1"
        xmlns="http://www.w3.org/2000/svg"
        p-id="14425"
        width="16"
        height="16"><path d="M910.57142832 473.81428584a38.57142832 38.57142832 0 0 1 38.50714335 36.30857168l0.06428585 2.27571416V846.28571416a102.85714248 102.85714248 0 0 1-99.64285752 102.80571416L846.28571416 949.14285752H515.49714248a38.57142832 38.57142832 0 0 1-2.26285664-77.07857168l2.26285664-0.06428584H846.28571416a25.71428584 25.71428584 0 0 0 25.65-23.78571416L872 846.28571416V512.38571416a38.57142832 38.57142832 0 0 1 38.57142832-38.57142832zM877.59285752 146.42a38.57142832 38.57142832 0 0 1 1.55571416 52.89428584l-1.55571416 1.65857168-209.95714336 209.95714248A191.99571416 191.99571416 0 0 1 704.85714248 524.85714248c0 106.50857168-86.34857167 192.85714248-192.85714248 192.85714336a191.99571416 191.99571416 0 0 1-113.92714248-37.23428584l-208.41428584 208.41428584a38.57142832 38.57142832 0 0 1-56.10857168-52.90714336l1.55571416-1.64571416L346.14285752 623.31714248A191.95714248 191.95714248 0 0 1 319.14285752 524.85714248c0-106.50857168 86.34857167-192.85714248 192.85714248-192.85714248 35.97428584 0 69.63428584 9.84857168 98.46 27L823.04 146.39428584a38.57142832 38.57142832 0 0 1 54.55285752 0zM512 409.14285752a115.71428584 115.71428584 0 1 0 0 231.4285708 115.71428584 115.71428584 0 0 0 0-231.4285708z m2.57142833-334.28571504a38.57142832 38.57142832 0 0 1 2.27571415 77.07857168L514.57142833 152H177.71428584a25.71428584 25.71428584 0 0 0-25.65 23.78571416L152 177.71428584v333.77142832a38.57142832 38.57142832 0 0 1-77.07857168 2.27571416L74.85714248 511.49857167V177.71428584a102.85714248 102.85714248 0 0 1 99.64285752-102.80571416L177.71428584 74.85714248h336.85714249z" p-id="14426"></path></svg></span>
    <div class="flex-1 flex items-center mr-3">
      <div :title="props.value?.name" class="truncate min-w-55 max-w-100 mr-5 name">{{ props.value?.name }}</div>
      <div class="flex items-center mr-5">
        <span class="mr-0.5">{{ t('reportPreview.execFunction.sampling.collapse.throughput.requestsPerSecond') }}</span>
        <span>{{ props.value?.permitsPerSecond }}</span>
      </div>
      <div class="flex items-center mr-5">
        <span class="mr-0.5">{{ t('reportPreview.execFunction.sampling.collapse.throughput.waitTimeout') }}</span>
        <span>{{ props.value?.timeoutInMs }}</span>
        <span>ms</span>
      </div>
    </div>
    <StatusTag v-if="!props.value?.enabled" class="mr-7" />
  </div>
</template>

<style scoped>
.embed.dashed-border {
  padding: 0 12px;
  border: none;
  border-bottom: 1px dashed  var(--border-text-box);
}

.embed .name {
  min-width: 208px;
}
</style>
