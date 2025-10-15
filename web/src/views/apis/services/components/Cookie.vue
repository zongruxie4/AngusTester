<script setup lang="ts">
import { ref, watch } from 'vue';
import { Table } from '@xcan-angus/vue-ui';
import { XCanDexie } from '@xcan-angus/infra';
import dayjs from 'dayjs';


import {CookieColumns} from "@/views/apis/services/components/PropsTypes";

interface Props {
  dataSource:any,
  host: string
}

const props = withDefaults(defineProps<Props>(), {});

const dexie = new XCanDexie<{id:string;data: any}>('cookies');
const definedKey = ['Domain', 'domain', 'Path', 'path', 'Expires', 'expires', 'HttpOnly', 'httpOnly', 'Secure', 'secure', 'Max-Age', 'max-age'];
const data = ref<Array<Record<string, string>>>([]);
const cookies = ref<Array<Record<string, string>>>([]);

const getCookies = async () => {
  const host = props.host ? new URL(props.host).host : '_';
  for (const item of data.value) {
    const expires = item.expires;
    let expiresIn = 0;
    if (expires) {
      const endTime = dayjs(expires).unix();
      const startTime = dayjs().unix();
      expiresIn = endTime - startTime;
    } else if (item['Max-Age'] || item['max-age']) {
      expiresIn = Number(item['Max-Age'] || item['max-age']);
    }
    await dexie.add({ id: `${host}-${item.name}`, data: { ...item } }, expiresIn);
  }
  const instanced = dexie.instance();
  const result = await instanced.table('cookies').where('id').startsWithIgnoreCase(host);
  cookies.value = [];
  result.each(item => {
    cookies.value.push(item.data);
  });
};

watch(() => props.dataSource, () => {
  const result:Array<Record<string, string>> = [];
  (props.dataSource || []).forEach(cookie => {
    const cookies = decodeURIComponent(cookie).split(';');
    const row:Record<string, string | boolean> = {
      httpOnly: 'false',
      secure: 'false',
      path: '/',
      domain: props.host || '',
      expires: ''
    };
    cookies.forEach(str => {
      const keyValue = str.trim().split('=');
      if (keyValue.length === 1) {
        row[keyValue[0]] = keyValue[0];
      }
      if (keyValue.length > 1 && definedKey.includes(keyValue[0])) {
        row[keyValue[0]] = keyValue[1];
      } else if (keyValue.length > 1) {
        row.name = keyValue[0];
        row.value = keyValue[1];
      }
    });
    result.push(row);
  });
  data.value = result;
  getCookies();
}, {
  immediate: true
});

const emptyTextStyle = {
  margin: '0 auto',
  height: '186px',
  width: '85px'
};
</script>
<template>
  <Table
    rowKey="id"
    :dataSource="cookies"
    :columns="CookieColumns"
    :emptyTextStyle="emptyTextStyle"
    size="small">
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
