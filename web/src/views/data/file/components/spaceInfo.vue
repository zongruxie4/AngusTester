<script setup lang="ts">
import { ref, watch } from 'vue';
import { Grid, Spin } from '@xcan-angus/vue-ui';
import { pubSpace, space } from '@/api/storage';

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
    { dataIndex: 'name', label: '名称' },
    { dataIndex: 'id', label: 'ID' },
    props.type !== 'space' && { dataIndex: 'summary', label: '实际大小', customRender: ({ text }) => text?.usedSize },
    props.type === 'space' && { dataIndex: 'quotaSize', label: '配额', customRender: ({ text }) => text ? text?.value + text?.unit?.message : '--' },
    { dataIndex: 'type', label: '格式', customRender: ({ text }) => text?.message || '空间' },
    { dataIndex: 'createdByName', label: '添加人' },
    { dataIndex: 'createdDate', label: '添加时间' },
    { dataIndex: 'lastModifiedDate', label: '更新时间' },
    props.type === 'space' && { dataIndex: 'remark', label: '备注' },
    props.type === 'file' && { dataIndex: 'mockFunc', label: '调用表达式' }
  ].filter(Boolean)
];

const dataSource = ref({});
const loading = ref(false);
const loadInfo = async () => {
  loading.value = true;
  const [error, res] = await (props.type === 'space' ? space.loadDetail(props.id) : props.pubapi ? pubSpace.getFileInfo(props.id) : space.getFileDetail(props.id));
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
