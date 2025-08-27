<script lang="ts" setup>
// Vue composition API imports
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';

// Ant Design components
import { Tree } from 'ant-design-vue';

// Custom UI components
import { Icon, Modal } from '@xcan-angus/vue-ui';

// Utilities and API
import { travelTreeData } from '@/views/project/project/utils';
import { MoveModuleProps } from '@/views/project/project/types';
import { modules } from '@/api/tester';

// Initialize i18n
const { t } = useI18n();

// Props and emits
const props = withDefaults(defineProps<MoveModuleProps>(), {
  visible: false,
  projectId: '',
  projectName: '',
  module: () => ({
    id: '',
    childLevels: 0,
    pid: ''
  })
});

const emits = defineEmits<{(e: 'update:visible', value: boolean):void; (e: 'ok'):void}>();

// Reactive data
const treeData = ref<any[]>([]);
const pid = ref([]);
const loading = ref(false);

// Data loading function
const getModuleTree = async () => {
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
        if (item.ids?.includes(props.module.id)) {
          item.disabled = true;
        }
        if (item.level + props.module.childLevels > 4) {
          item.disabled = true;
        }
        return item;
      })
    ]
  }];
};

// Event handlers
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

// Lifecycle hooks
onMounted(() => {
  watch(() => props.visible, newValue => {
    pid.value = [];
    if (newValue) {
      getModuleTree();
    }
  }, {
    immediate: true
  });
});
</script>
<template>
  <!-- Modal for moving module to different parent -->
  <Modal
    :title="t('project.projectEdit.module.moveModule')"
    :okButtonProps="{
      disabled: !pid.length,
      loading: loading
    }"
    :visible="props.visible"
    @cancel="cancel"
    @ok="ok">
    <!-- Tree view for selecting target parent module -->
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
      <!-- Custom tree node template -->
      <template #title="{name, id}">
        <div class="flex items-center space-x-2">
          <Icon v-if="id !== '-1'" icon="icon-mokuai" />
          <span class="flex-1">{{ name }}</span>
        </div>
      </template>
    </Tree>
  </Modal>
</template>
