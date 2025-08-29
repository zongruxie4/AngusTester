<script lang="ts" setup>
import { ref, watch } from 'vue';
import { Grid, Icon, Tooltip } from '@xcan-angus/vue-ui';
import axios from 'axios';
import dayjs from 'dayjs';
import duration from 'dayjs/plugin/duration';
import { ApiType, routerUtils, ApiUrlBuilder } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';

import { formatBytes } from '@/utils';

const { t } = useI18n();

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
      label: t('node.nodeDetail.agentInfo.columns.version')
    },
    {
      dataIndex: 'home',
      label: t('node.nodeDetail.agentInfo.columns.home')
    }
  ],
  [
    {
      dataIndex: 'uptime',
      label: t('node.nodeDetail.agentInfo.columns.uptime')
    },
    {
      dataIndex: 'diskSpace',
      label: t('node.nodeDetail.agentInfo.columns.diskSpace')
    }
  ],
  [
    {
      dataIndex: 'port',
      label: t('node.nodeDetail.agentInfo.columns.port')
    },
    {
      dataIndex: 'health',
      label: t('node.nodeDetail.agentInfo.columns.health')
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

  // const host = await site.getUrl('apis');
  // const privHost = await site.getUrl('at');
  // const isPrivate = await site.isPrivate();
  const routeConfig = routerUtils.getTesterApiRouteConfig(ApiType.PUB_API);
  const url = ApiUrlBuilder.buildApiUrl(routeConfig, `/proxy?targetAddr=http://${newValue}:${props.port}`);
  axios.get(url, {
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
      dataSource.value.uptime = `${hour ? hour + t('node.nodeDetail.agentInfo.timeUnits.hour') : ''}${minute ? minute + t('node.nodeDetail.agentInfo.timeUnits.minute') : ''}${seconds ? seconds + t('node.nodeDetail.agentInfo.timeUnits.second') : ''}`;
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
    {{ t('node.nodeDetail.agentInfo.title') }}
  </div>
  <Grid
    class="ml-6"
    :columns="columns"
    :dataSource="dataSource">
    <template #health="text">
      <template v-if="text.text && text.text==='UP'">{{ t('node.nodeDetail.agentInfo.health.normal') }}</template>
      <template v-else-if="text.text">
        <Tooltip>
          <template #title>{{ dataSource.errorTip }}</template>
          <span>
            {{ t('node.nodeDetail.agentInfo.health.abnormal') }}
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
