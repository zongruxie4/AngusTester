<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { Icon, Modal } from '@xcan-angus/vue-ui';
import { travelTreeData } from './utils';
import { Tree } from 'ant-design-vue';
import { modules } from '@/api/tester';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

interface Props {
  visible: boolean;
  projectId: string;
  projectName: string;
  module: {
    id: string;
    childLevels: number;
    pid: string;
  }
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  projectId: '',
  module: {
    id: '',
    childLevels: 0,
    pid: ''
  }
});

const emits = defineEmits<{(e: 'update:visible', value: boolean):void; (e: 'ok'):void}>();

const treeData = ref<any[]>([]);
const loadTreeData = async () => {
  const [error, { data }] = await modules.getModuleTree({
    projectId: props.projectId
  });
  if (error) {
    return;
  }
  treeData.value = [{
    name: props.projectName,
    level: -1,
    id: '-1',
    children: [
      ...travelTreeData(data || [], (item) => {
        if (item.ids.includes(props.module.id)) {
          item.disabled = true;
        }
        if (item.level + props.module.childLevels > 4) {
          item.disabled = true;
        }
      })
    ]
  }];
};

const pid = ref([]);
const loading = ref(false);
const cancel = () => {
  emits('update:visible', false);
};

const ok = async () => {
  if (props.module.pid === pid.value[0]) {
    cancel();
    return;
  }
  loading.value = true;
  const [error] = await modules.updateModule([{
    id: props.module.id,
    pid: pid.value[0]
  }]);
  loading.value = false;
  if (error) {
    return;
  }
  cancel();
  emits('ok');
};

onMounted(() => {
  watch(() => props.visible, newValue => {
    pid.value = [];
    if (newValue) {
      loadTreeData();
    }
  }, {
    immediate: true
  });
});
</script>
<template>
  <Modal
    :title="t('project.projectEdit.module.moveModule')"
    :okButtonProps="{
      disabled: !pid.length,
      loading: loading
    }"
    :visible="props.visible"
    @cancel="cancel"
    @ok="ok">
    <Tree
      v-if="treeData.length"
      v-model:selectedKeys="pid"
      :treeData="treeData"
      blockNode
      class="h-100 overflow-auto"
      defaultExpandAll
      :fieldNames="{
        children: 'children',
        title: 'name',
        key: 'id'
      }">
      <template #title="{name, id}">
        <div class="flex items-center space-x-2">
          <Icon v-if="id !== '-1'" icon="icon-mokuai" />
          <span class="flex-1">{{ name }}</span>
        </div>
      </template>
    </Tree>
  </Modal>
</template>
