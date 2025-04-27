<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { Modal } from '@xcan-angus/vue-ui';
import { DirectoryTree } from 'ant-design-vue';
import { http, STORAGE } from '@xcan-angus/tools';
import type { TreeProps } from 'ant-design-vue';

interface Props {
  visible: boolean
}

const props = withDefaults(defineProps<Props>(), {
  visible: false
});

const emits = defineEmits<{(e: 'update:visible', value: boolean):void, (e: 'ok', value: string):void}>();

const selectFile = ref();

// 空间数据
const spaceData = ref([]);
const loadSpaceData = async () => {
  const [error, data] = await http.get(`${STORAGE}/space/search?pageSize=1000&pageNo=1&appCode=AngusTester`);
  if (error) {
    return;
  }
  spaceData.value = data.data.list.map(item => ({ key: item.id, title: item.name, isLeaf: false, spaceId: item.id, dirId: '-1' }));
};

const loadFile:TreeProps['loadData'] = (node) => {
  return new Promise((resolve) => {
    if (node.dataRef.children) {
      resolve();
      return;
    }
    const { spaceId, dirId } = node.dataRef;
    http.get(`${STORAGE}/space/object/search?spaceId=${spaceId}&parentDirectoryId=${dirId}&pageSize=1000&pageNo=1`)
      .then(data => {
        const [error, resp] = data;
        if (error) {
          resolve();
          return;
        }
        node.dataRef.children = resp.data.list.map(item => {
          return {
            key: item.id,
            spaceId,
            dirId: item.id,
            title: item.name,
            isLeaf: item.type.value === 'FILE',
            selectable: item.type.value === 'FILE'
          };
        });
        spaceData.value = [...spaceData.value];
        resolve();
      });
  });
};

const cancel = () => {
  emits('update:visible', false);
};

const confirm = () => {
  if (!selectFile.value?.length) {
    cancel();
  }
  emits('ok', selectFile.value[0]);
  cancel();
};

onMounted(() => {
  loadSpaceData();
});
</script>
<template>
  <Modal
    title="选择文件"
    :visible="props.visible"
    @cancel="cancel"
    @ok="confirm">
    {{ selectFile }}
    <DirectoryTree
      v-model:selectedKeys="selectFile"
      :height="500"
      :treeData="spaceData"
      :loadData="loadFile" />
  </Modal>
</template>
