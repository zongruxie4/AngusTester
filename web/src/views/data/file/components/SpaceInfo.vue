<script setup lang="ts">
import { ref, watch } from 'vue';
import { Grid, Spin } from '@xcan-angus/vue-ui';
import { pubSpace, space } from '@/api/storage';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

interface Props {
  id: string,
  type?: 'space' | 'file' | 'directory',
  pubapi?: boolean
}
const props = withDefaults(defineProps<Props>(), {
  type: 'space',
  pubapi: false
});

const infoColumns = [
  [
    { dataIndex: 'name', label: t('fileSpace.spaceDetail.columns.name') },
    { dataIndex: 'id', label: t('fileSpace.spaceDetail.columns.id') },
    props.type !== 'space' && { dataIndex: 'summary', label: t('fileSpace.spaceDetail.columns.actualSize'), customRender: ({ text }) => text?.usedSize },
    props.type === 'space' && { dataIndex: 'quotaSize', label: t('fileSpace.spaceDetail.columns.quota'), customRender: ({ text }) => text ? text?.value + text?.unit?.message : '--' },
    { dataIndex: 'type', label: t('fileSpace.spaceDetail.columns.format'), customRender: ({ text }) => text?.message || '空间' },
    { dataIndex: 'createdByName', label: t('fileSpace.spaceDetail.columns.createdBy') },
    { dataIndex: 'createdDate', label: t('fileSpace.spaceDetail.columns.createdDate') },
    { dataIndex: 'lastModifiedDate', label: t('fileSpace.spaceDetail.columns.lastModifiedDate') },
    props.type === 'space' && { dataIndex: 'remark', label: t('fileSpace.spaceDetail.columns.remark') },
    props.type === 'file' && { dataIndex: 'mockFunc', label: t('fileSpace.spaceDetail.columns.mockFunc') }
  ].filter(Boolean)
];

const dataSource = ref({});
const loading = ref(false);
const loadInfo = async () => {
  loading.value = true;
  const [error, res] = await (props.type === 'space' ? space.getSpaceDetail(props.id) : props.pubapi ? pubSpace.getFileInfo(props.id) : space.getFileDetail(props.id));
  loading.value = false;
  if (error) {
    return;
  }
  dataSource.value = res.data;
};

watch(() => props.id, newValue => {
  if (!newValue) {
    return;
  }
  loadInfo();
}, {
  immediate: true
});
</script>
<template>
  <Spin :spinning="loading">
    <Grid
      class="mt-3"
      :columns="infoColumns"
      :dataSource="dataSource"
      font-size="12px">
    </Grid>
  </Spin>
</template>
