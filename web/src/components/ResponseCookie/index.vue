<script setup lang="ts">
import { watch, ref, onMounted, nextTick } from 'vue';
import { Table } from '@xcan-angus/vue-ui';
import { columns } from './interface';
import { XCanDexie } from '@xcan-angus/infra';
import dayjs from 'dayjs';

interface Props {
  dataSource: any; // Raw Set-Cookie array (e.g., ["name=value; Path=/; HttpOnly", ...])
  host: string;    // Current request URL used to scope cookies in local storage
}

// Define props (no defaults required; upstream should supply both fields)
const props = withDefaults(defineProps<Props>(), {});

/**
 * Emitted events
 */
const emit = defineEmits<{
  (e: 'rendered', value: true): void; // Emitted after initial render completes
}>();

/**
 * Local Dexie instance for cookie persistence
 * Each cookie is stored under key `${host}-${cookieName}` with TTL derived from Expires/Max-Age
 */
const dexie = new XCanDexie<{ id: string; data: Record<string, any> }>('cookies');

/**
 * Well-known cookie attribute keys (case-insensitive variants)
 * Used to distinguish between attributes and the actual cookie name/value
 */
const definedKey = [
  'Domain', 'domain',
  'Path', 'path',
  'Expires', 'expires',
  'HttpOnly', 'httpOnly',
  'Secure', 'secure',
  'Max-Age', 'max-age'
];

/**
 * Parsed cookies from current response (normalized rows)
 */
const data = ref<Array<Record<string, string>>>([]);

/**
 * Cookies retrieved from Dexie store filtered by host
 */
const cookies = ref<Array<Record<string, string>>>([]);

/**
 * Persist cookies to Dexie and refresh visible list
 * - Uses host from props to namespace cookie keys
 * - Computes TTL from Expires or Max-Age when available
 */
const getCookies = async (): Promise<void> => {
  const host = props.host ? new URL(props.host).host : '_';

  // Persist/refresh TTL for each parsed cookie row
  for (const item of data.value) {
    const expires = item.expires;
    let expiresIn = 0;

    // Compute TTL from Expires (absolute) or Max-Age (relative)
    if (expires) {
      const endTime = dayjs(expires).unix();
      const startTime = dayjs().unix();
      expiresIn = endTime - startTime;
    } else if (item['Max-Age'] || item['max-age']) {
      expiresIn = Number(item['Max-Age'] || item['max-age']);
    }

    await dexie.add({ id: `${host}-${item.name}`, data: { ...item } }, expiresIn);
  }

  // Load cookies for the current host
  const instanced = dexie.instance();
  const result = await instanced.table('cookies').where('id').startsWithIgnoreCase(host);
  cookies.value = [];
  result.each((item: { data: Record<string, string> }) => {
    cookies.value.push(item.data);
  });
};

/**
 * Watch raw Set-Cookie array, parse into normalized row objects, then persist
 */
watch(() => props.dataSource, () => {
  const result: Array<Record<string, string>> = [];

  (props.dataSource || []).forEach((cookie: string) => {
    // Example: "name=value; Path=/; HttpOnly; Secure"
    const parts = decodeURIComponent(cookie).split(';');
    const row: Record<string, string | boolean> = {
      httpOnly: 'false',
      secure: 'false',
      path: '/',
      domain: props.host || '',
      expires: ''
    };

    parts.forEach(str => {
      const keyValue = str.trim().split('=');

      // Single token like "HttpOnly" or "Secure"
      if (keyValue.length === 1) {
        row[keyValue[0]] = keyValue[0];
      }

      // Attribute or name/value pair
      if (keyValue.length > 1 && definedKey.includes(keyValue[0])) {
        row[keyValue[0]] = keyValue[1];
      } else if (keyValue.length > 1) {
        row.name = keyValue[0];
        row.value = keyValue[1];
      }
    });

    result.push(row as Record<string, string>);
  });

  data.value = result;
  getCookies();
}, {
  immediate: true
});

onMounted(() => {
  nextTick(() => {
    emit('rendered', true);
  });
});
</script>
<template>
  <!-- Cookies table: shows normalized cookie rows with key attributes -->
  <Table
    :dataSource="cookies"
    :columns="columns"
    size="small">
    <!-- Custom cell renderers for common attributes and variants -->
    <template #bodyCell="{column, record}">
      <!-- Expires/Max-Age display with fallback to dash -->
      <template v-if="column.key === 'expires'">
        {{ record.expires || record['Max-Age'] || record['max-age'] || '-' }}
      </template>

      <!-- Domain (case-insensitive variants) -->
      <template v-if="column.key === 'domain'">
        {{ record.domain || record['domain'] }}
      </template>

      <!-- Path (case-insensitive variants) -->
      <template v-if="column.key === 'path'">
        {{ record.path || record['Path'] }}
      </template>

      <!-- HttpOnly (case-insensitive variants) -->
      <template v-if="column.key === 'httpOnly'">
        {{ record.httpOnly || record['HttpOnly'] }}
      </template>

      <!-- Secure (case-insensitive variants) -->
      <template v-if="column.key === 'secure'">
        {{ record.secure || record['Secure'] }}
      </template>
    </template>
  </Table>
</template>
<style scoped>
.ant-table-wrapper {
  @apply py-3 overflow-auto;
}

.ant-table-wrapper :deep(.ant-table-content) .ant-table-thead > tr > th {
  @apply bg-gray-light;
}

:deep(.ant-table.ant-table-small) {
  @apply text-3 leading-3;
}

.ant-table-wrapper :deep(.ant-table.ant-table-small) .ant-table-tbody > tr > td,
.ant-table-wrapper :deep(.ant-table.ant-table-small) .ant-table-thead > tr > th {
  @apply py-3 bg-white border-t-0;
}

.ant-table-wrapper :deep(.ant-table.ant-table-small) .ant-table-tbody > tr > td {
  @apply border-b-0;
}

.ant-table-wrapper :deep(.ant-table.ant-table-small) .ant-table-tbody .ant-empty.ant-empty-normal {
  @apply hidden;
}

</style>
