<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { Grid, Icon } from '@xcan-angus/vue-ui';
import { appContext } from '@xcan-angus/infra';
import dayjs from 'dayjs';

import { edition } from '@/api/store';

const editionType = ref<string>();

const columnsPriv = [[
  {
    label: '版本类型',
    dataIndex: 'editionType'
  },
  {
    label: '商品编码',
    dataIndex: 'goodsCode'
  },
  {
    label: '提供者',
    dataIndex: 'provider'
  },
  {
    label: '发行者',
    dataIndex: 'issuer'
  },
  {
    label: '持有者',
    dataIndex: 'holder'
  },
  {
    label: '许可编号',
    dataIndex: 'licenseNo'
  },
  {
    label: '许可发行日期',
    dataIndex: 'beginDate'
  },
  {
    label: '许可过期日期',
    dataIndex: 'endDate'
  },
  {
    label: '许可证书MD5签名',
    dataIndex: 'signature'
  }
]];

const columnsCloud = [[
  {
    label: '版本类型',
    dataIndex: 'editionType'
  },
  {
    label: '商品编码',
    dataIndex: 'goodsCode'
  },
  {
    label: '提供者',
    dataIndex: 'provider'
  },
  {
    label: '发行者',
    dataIndex: 'issuer'
  },
  {
    label: '持有者',
    dataIndex: 'holder'
  }
]];

const dataSource = ref({});

const loadInfo = async () => {
  const [error, res] = await edition.getSysEdition();
  if (error) {
    return;
  }
  dataSource.value = res.data;
};

const getVersionTypeIcon = (key: string) => {
  switch (key) {
    case 'DATACENTER': return 'icon-shujuzhongxin';
    case 'CLOUD_SERVICE': return 'icon-yunfuwu';
    case 'ENTERPRISE': return 'icon-qiye';
    case 'COMMUNITY': return 'icon-shequ';
  }
};

onMounted(async () => {
  editionType.value = appContext.getEditionType();
  loadInfo();
});

const columns = computed(() => {
  if (editionType.value === 'CLOUD_SERVICE') {
    return columnsCloud;
  }

  return columnsPriv;
});

</script>
<template>
  <Grid :columns="columns" :dataSource="dataSource">
    <template #editionType="{text}">
      <Icon :icon="getVersionTypeIcon(text?.value)" class="text-4 -mt-0.5" />
      {{ text?.message }}
    </template>
    <template #endDate="{text}">
      {{ text }} ( 剩余{{ dayjs(text).diff(dayjs().format(),'day') > 0 ? dayjs(text).diff(dayjs().format(),'day') : 0  }}天 )
    </template>
  </Grid>
</template>
