<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { Grid, Icon } from '@xcan-angus/vue-ui';
import { appContext } from '@xcan-angus/infra';
import dayjs from 'dayjs';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

import { edition } from '@/api/store';

const editionType = ref<string>();

const columnsPriv = [[
  {
    label: t('appConfig.permitInfo.columns.editionType'),
    dataIndex: 'editionType'
  },
  {
    label: t('appConfig.permitInfo.columns.goodsCode'),
    dataIndex: 'goodsCode'
  },
  {
    label: t('appConfig.permitInfo.columns.provider'),
    dataIndex: 'provider'
  },
  {
    label: t('appConfig.permitInfo.columns.issuer'),
    dataIndex: 'issuer'
  },
  {
    label: t('appConfig.permitInfo.columns.holder'),
    dataIndex: 'holder'
  },
  {
    label: t('appConfig.permitInfo.columns.licenseNo'),
    dataIndex: 'licenseNo'
  },
  {
    label: t('appConfig.permitInfo.columns.beginDate'),
    dataIndex: 'beginDate'
  },
  {
    label: t('appConfig.permitInfo.columns.endDate'),
    dataIndex: 'endDate'
  },
  {
    label: t('appConfig.permitInfo.columns.signature'),
    dataIndex: 'signature'
  }
]];

const columnsCloud = [[
  {
    label: t('appConfig.permitInfo.columns.editionType'),
    dataIndex: 'editionType'
  },
  {
    label: t('appConfig.permitInfo.columns.goodsCode'),
    dataIndex: 'goodsCode'
  },
  {
    label: t('appConfig.permitInfo.columns.provider'),
    dataIndex: 'provider'
  },
  {
    label: t('appConfig.permitInfo.columns.issuer'),
    dataIndex: 'issuer'
  },
  {
    label: t('appConfig.permitInfo.columns.holder'),
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
      {{ text }} ( {{ t('appConfig.permitInfo.remainingDays', { days: dayjs(text).diff(dayjs().format(),'day') > 0 ? dayjs(text).diff(dayjs().format(),'day') : 0 }) }} )
    </template>
  </Grid>
</template>
