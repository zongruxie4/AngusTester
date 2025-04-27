<script lang="ts" setup>
import { ref, watch } from 'vue';
import { Grid, Icon, Tooltip } from '@xcan-angus/vue-ui';
import axios from 'axios';
import dayjs from 'dayjs';
import duration from 'dayjs/plugin/duration';
import { site, PUB_TESTER } from '@xcan-angus/tools';

import { formatBytes } from '@/utils';

interface Props {
  ip: string;
  port?: string;
}
dayjs.extend(duration);
const props = withDefaults(defineProps<Props>(), {
  // dataSource: () => ({})
  ip: '',
  port: '6807'
});

const columns = [
  [
    {
      dataIndex: 'version',
      label: '版本'
    },
    {
      dataIndex: 'home',
      label: '安装路径'
    }
  ],
  [
    {
      dataIndex: 'uptime',
      label: '运行时长'
    },
    {
      dataIndex: 'diskSpace',
      label: '磁盘空间'
    }
  ],
  [
    {
      dataIndex: 'port',
      label: '占用端口'
    },
    {
      dataIndex: 'health',
      label: '健康状态'
    }
  ]
];

const showErr = ref(false);
const errorInfo = ref('');
const closeErrInfo = () => {
  showErr.value = false;
};

const dataSource = ref<{[key: string]: string}>({});

watch(() => props.ip, async newValue => {
  if (!newValue) {
    return;
  }

  const host = await site.getUrl('apis');
  const privHost = await site.getUrl('at');
  const isPrivate = await site.isPrivate();
  axios.get(`${isPrivate ? privHost : host}${isPrivate ? '/pubapi/v1' : PUB_TESTER}/proxy?targetAddr=http://${newValue}:${props.port}`, {
    timeout: 2000
  })
    .then(resp => {
      const data = resp.data;
      showErr.value = false;
      dataSource.value.version = data.version;
      dataSource.value.home = data.home;
      const hour = dayjs.duration(+data.uptime).hours();
      const minute = dayjs.duration(+data.uptime).minutes();
      const seconds = dayjs.duration(+data.uptime).seconds();
      dataSource.value.uptime = `${hour ? hour + '小时' : ''}${minute ? minute + '分钟' : ''}${seconds ? seconds + '秒' : ''}`;
      dataSource.value.diskSpace = data.diskSpace;
      dataSource.value.ip = data.server?.ip;
      dataSource.value.port = data.server?.port;
      dataSource.value.health = data.health?.status?.status; // 'UP' | ''
      if (dataSource.value.health !== 'UP') {
        const errorObj = data.health?.detail || {};
        dataSource.value.errorTip = Object.values(errorObj).join(',');
      }
    })
    .catch(err => {
      showErr.value = true;
      if (err.response.data) {
        errorInfo.value = err.response.data;
      } else {
        errorInfo.value = '';
      }
    });
}, {
  immediate: true
});

</script>
<template>
  <div v-if="showErr" class="border border-border-error rounded-xl px-2 py-1 flex items-center text-3 space-x-1 my-4 bg-bg-red">
    <Icon icon="icon-tishi1" class="text-blue-icon text-3.5" />
    <span class="text-rule flex-1">{{ errorInfo || `无访问代理信息，连接失败地址：http://${props.ip}:${props.port}` }}</span>
    <Icon
      icon="icon-cuowu"
      class="text-3.5 cursor-pointer"
      @click="closeErrInfo" />
  </div>
  <div class="font-medium text-3 my-5">
    代理信息
  </div>
  <Grid
    class="ml-6"
    :columns="columns"
    :dataSource="dataSource">
    <template #health="text">
      <template v-if="text.text && text.text==='UP'">正常</template>
      <template v-else-if="text.text">
        <Tooltip>
          <template #title>{{ dataSource.errorTip }}</template>
          <span>
            异常
            <Icon class="text-rule" icon="icon-jinggao" />
          </span>
        </Tooltip>
      </template>
    </template>
    <template #diskSpace="record">
      <template v-if="record.text">
        <span :class="{'text-rule': record.text.used / record.text.total > 0.8}">
          {{ formatBytes(record.text.used) }} / {{ formatBytes(record.text.total) }}
        </span>
      </template>
    </template>
  </Grid>
</template>
