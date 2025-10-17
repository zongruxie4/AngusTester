<script setup lang="ts">
import { ref, watch } from 'vue';
import { Table } from '@xcan-angus/vue-ui';
import { XCanDexie } from '@xcan-angus/infra';
import dayjs from 'dayjs';

interface Props {
  dataSource:any,
  host: string
}

const props = withDefaults(defineProps<Props>(), {});

// Initialize Dexie database for cookie storage
const dexie = new XCanDexie<{id:string;data: any}>('cookies');

// Define known cookie attribute keys for parsing
const definedCookieKeys = ['Domain', 'domain', 'Path', 'path', 'Expires', 'expires', 'HttpOnly', 'httpOnly', 'Secure', 'secure', 'Max-Age', 'max-age'];

// Ref for raw cookie data before processing
const rawCookieData = ref<Array<Record<string, string>>>([]);

// Ref for processed cookies to display in the table
const processedCookies = ref<Array<Record<string, string>>>([]);

/**
 * Process and store cookies in the database
 * Calculates expiration time and stores cookies with proper keys
 */
const processAndStoreCookies = async () => {
  // Extract host from the provided URL or use a default
  const host = props.host ? new URL(props.host).host : '_';

  // Process each cookie item
  for (const item of rawCookieData.value) {
    const expires = item.expires;
    let expiresIn = 0;

    // Calculate expiration time in seconds
    if (expires) {
      const endTime = dayjs(expires).unix();
      const startTime = dayjs().unix();
      expiresIn = endTime - startTime;
    } else if (item['Max-Age'] || item['max-age']) {
      expiresIn = Number(item['Max-Age'] || item['max-age']);
    }

    // Store cookie in the database with calculated expiration
    await dexie.add({ id: `${host}-${item.name}`, data: { ...item } }, expiresIn);
  }

  // Retrieve cookies from database for the current host
  const dbInstance = dexie.instance();
  const result = await dbInstance.table('cookies').where('id').startsWithIgnoreCase(host);

  // Populate processed cookies array
  processedCookies.value = [];
  result.each(item => {
    processedCookies.value.push(item.data);
  });
};

/**
 * Watch for changes in dataSource and process cookies
 */
watch(() => props.dataSource, () => {
  // Array to hold parsed cookie objects
  const parsedCookies:Array<Record<string, string>> = [];

  // Process each cookie string in the dataSource
  (props.dataSource || []).forEach(cookie => {
    // Decode and split cookie string into individual attributes
    const cookieAttributes = decodeURIComponent(cookie).split(';');

    // Initialize cookie object with default values
    const cookieObject:Record<string, string> = {
      httpOnly: 'false',
      secure: 'false',
      path: '/',
      domain: props.host || '',
      expires: ''
    } as Record<string, string>;

    // Parse each attribute of the cookie
    cookieAttributes.forEach(attribute => {
      const keyValue = attribute.trim().split('=');

      // Handle boolean attributes (e.g., HttpOnly, Secure)
      if (keyValue.length === 1) {
        cookieObject[keyValue[0]] = keyValue[0];
      }

      // Handle known cookie attributes
      if (keyValue.length > 1 && definedCookieKeys.includes(keyValue[0])) {
        cookieObject[keyValue[0]] = keyValue[1];
      } else if (keyValue.length > 1) {
        // Handle name-value pair
        cookieObject.name = keyValue[0];
        cookieObject.value = keyValue[1];
      }
    });

    // Add parsed cookie to the result array
    parsedCookies.push(cookieObject);
  });

  // Update raw cookie data and process cookies
  rawCookieData.value = parsedCookies;
  processAndStoreCookies();
}, {
  immediate: true
});

/**
 * Column definitions for the cookie table
 */
export const CookieColumns = [
  {
    title: 'Name',
    dataIndex: 'name',
    key: 'name'
  },
  {
    title: 'Value',
    dataIndex: 'value',
    key: 'value'
  },
  {
    title: 'Domain',
    dataIndex: 'domain',
    key: 'domain'
  },
  {
    title: 'Path',
    dataIndex: 'path',
    key: 'path'
  },
  {
    title: 'Expires / Max-Age',
    dataIndex: 'expires',
    key: 'expires'
  },
  {
    title: 'HttpOnly',
    dataIndex: 'httpOnly',
    key: 'httpOnly'
  },
  {
    title: 'Secure',
    dataIndex: 'secure',
    key: 'secure'
  }
];

// Style configuration for empty table state
const emptyTextStyle = {
  margin: '0 auto',
  height: '186px',
  width: '85px'
};
</script>
<template>
  <Table
    rowKey="id"
    :dataSource="processedCookies"
    :columns="CookieColumns"
    :emptyTextStyle="emptyTextStyle"
    size="small"
    :noDataSize="'small'"
    noDataText="No cookies found">
    <template #bodyCell="{column, record}">
      <template v-if="column.key === 'expires'">
        {{ record.expires || record['Max-Age'] || record['max-age'] || '-' }}
      </template>
      <template v-if="column.key === 'domain'">
        {{ record.domain || record['domain'] }}
      </template>
      <template v-if="column.key === 'path'">
        {{ record.path || record['Path'] }}
      </template>
      <template v-if="column.key === 'httpOnly'">
        {{ record.httpOnly || record['HttpOnly'] }}
      </template>
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
