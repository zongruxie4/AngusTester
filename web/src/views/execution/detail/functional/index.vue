<script setup lang="ts">
import { defineAsyncComponent, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { ExecStatus } from '@/enums/enums';
import { exec } from 'src/api/ctrl';

import { FunctionalTestProps } from '@/views/execution/detail/types';

// Define input props for functional test detail view
// eslint-disable-next-line import/no-absolute-path
const props = withDefaults(defineProps<FunctionalTestProps>(), {
  loading: false,
  execInfo: undefined,
  exception: undefined
});

// Define emitted events

const emit = defineEmits<{
  // Emitted after execution metadata is loaded
  (e: 'loaded', data: { [key: string]: any }): void;
  // Two-way binding for loading indicator
  (e: 'update:loading', value: boolean): void;
}>();

// Async Child Components (by plugin type)
const Http = defineAsyncComponent(() => import('./Http.vue'));
const Jdbc = defineAsyncComponent(() => import('./Jdbc.vue'));
const Mail = defineAsyncComponent(() => import('./Mail.vue'));
const Smtp = defineAsyncComponent(() => import('./Smtp.vue'));
const Tcp = defineAsyncComponent(() => import('./Tcp.vue'));
const Ftp = defineAsyncComponent(() => import('./Ftp.vue'));
const Ldap = defineAsyncComponent(() => import('./Ldap.vue'));
const WebSocket = defineAsyncComponent(() => import('./WebSocket.vue'));
const Mobile = defineAsyncComponent(() => import('./Mobile.vue'));

// State & Timers
let refreshTimer: NodeJS.Timeout | null = null;
let refreshDelayMs = 0;

// Collected execution content (aggregated paginated results)
const executionContent = ref<{ [key: string]: any }[]>([]);

/**
 * Load execution metadata and content once.
 * <p>
 * If execution is still pending or running, schedule the next refresh.
 */
const loadExecutionOverview = async () => {
  const [error, { data }] = await exec.getInfo(props.execInfo.id);
  if (error) {
    emit('update:loading', false);
    return;
  }

  emit('loaded', data);
  await loadExecutionContent();
  if ([ExecStatus.PENDING, ExecStatus.RUNNING].includes(data.status?.value)) {
    await scheduleNextRefresh();
  }
};

/**
 * Load paginated execution content and merge into a single list.
 */
const loadExecutionContent = async () => {
  let total = 0;
  const pageSize = 50;
  const aggregatedList: any[] = [];

  // Load a single page
  const loadPage = async (pageNo = 1) => {
    const [error, res] = await exec.getSampleExtensionContent(
      props.execInfo.id,
      { pageNo, pageSize, extField: 'SampleResultContent' }
    );
    if (error) {
      emit('update:loading', false);
      return;
    }

    total = +res.data?.total;
    if (res.data?.list?.length) {
      aggregatedList.push(...res.data.list);
    }
  };

  await loadPage();
  if (total <= pageSize) {
    executionContent.value = aggregatedList;
    return;
  }

  const totalPages = Math.ceil(total / pageSize);
  for (let pageNo = 2; pageNo <= totalPages; pageNo++) {
    await loadPage(pageNo);
  }

  executionContent.value = aggregatedList;
};

/**
 * Schedule next refresh according to `refreshDelayMs`.
 */
const scheduleNextRefresh = async () => {
  if (refreshTimer) {
    clearTimeout(refreshTimer);
  }
  refreshTimer = setTimeout(async () => {
    await loadExecutionOverview();
  }, refreshDelayMs);
};

// Lifecycle
onMounted(() => {
  // React to execution info changes and kick off initial load
  watch(() => props.execInfo, async (newValue) => {
    const status = newValue?.status?.value;
    if (!status) {
      return;
    }

    // Compute refresh delay (min 3s)
    const reportInterval = +props.execInfo?.reportInterval?.replace(/[^\d]/g, '');
    refreshDelayMs = (reportInterval < 3 ? 3 : reportInterval) * 1000;

    if ([ExecStatus.PENDING, ExecStatus.RUNNING].includes(status)) {
      await loadExecutionOverview();
    } else {
      if (refreshTimer) {
        clearTimeout(refreshTimer);
      }
      await loadExecutionContent();
    }
    emit('update:loading', false);
  }, { immediate: true });
});

onBeforeUnmount(() => {
  if (refreshTimer) {
    clearTimeout(refreshTimer);
  }
});
</script>
<template>
  <template v-if="props.execInfo?.plugin==='Http'">
    <Http
      :execInfo="props.execInfo"
      :execContent="executionContent"
      :exception="props.exception" />
  </template>
  <template v-else-if="props.execInfo?.plugin==='Jdbc'">
    <Jdbc
      :execInfo="props.execInfo"
      :execContent="executionContent"
      :exception="props.exception" />
  </template>
  <template v-else-if="props.execInfo?.plugin === 'Mail'">
    <Mail
      :execInfo="props.execInfo"
      :execContent="executionContent"
      :exception="props.exception" />
  </template>
  <template v-else-if="props.execInfo?.plugin === 'Smtp'">
    <Smtp
      :execInfo="props.execInfo"
      :execContent="executionContent"
      :exception="props.exception" />
  </template>
  <template v-else-if="props.execInfo?.plugin === 'Tcp'">
    <Tcp
      :execInfo="props.execInfo"
      :execContent="executionContent"
      :exception="props.exception" />
  </template>
  <template v-else-if="props.execInfo?.plugin === 'Ftp'">
    <Ftp
      :execInfo="props.execInfo"
      :execContent="executionContent"
      :exception="props.exception" />
  </template>
  <template v-else-if="props.execInfo?.plugin === 'Ldap'">
    <Ldap
      :execInfo="props.execInfo"
      :execContent="executionContent"
      :exception="props.exception" />
  </template>
  <template v-else-if="props.execInfo?.plugin === 'WebSocket'">
    <WebSocket
      :execInfo="props.execInfo"
      :execContent="executionContent"
      :exception="props.exception" />
  </template>
  <template v-else-if="props.execInfo?.plugin === 'Mobile'">
    <Mobile
      :execInfo="props.execInfo"
      :execContent="executionContent"
      :exception="props.exception" />
    
  </template>

  
</template>
