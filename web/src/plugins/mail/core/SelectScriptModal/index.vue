<script setup lang="ts">
import { computed, ref, onMounted, watch, inject } from 'vue';
import { Checkbox } from 'ant-design-vue';
import { Modal, Scroll, Input, Icon, notification } from '@xcan-angus/vue-ui';
import { debounce } from 'throttle-debounce';
import { duration, TESTER } from '@xcan-angus/tools';
import YAML from 'yaml';
import { script } from '@/api/tester';

type DataItem = {
  id:string;
  name:string;
  plugin:string;
  type:{value:string;message:string;};
}

export interface Props {
  visible:boolean;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e:'update:visible', value:boolean):void;
  (e:'ok', value:any):void;
  (e:'cancel'):void;
}>();

const projectInfo = inject('projectInfo', ref({ id: '' }));

const inputValue = ref<string>();
const checkedId = ref<string>();
const dataList = ref<DataItem[]>([]);
const loading = ref(false);

const inputChange = debounce(duration.search, (event:{target:{value:string}}) => {
  const value = event.target.value;
  inputValue.value = value;
});

const scrollChange = (data:DataItem[]) => {
  dataList.value = data;
};

const checkChange = (event:{target:{checked:boolean}}, id:string) => {
  const checked = event.target.checked;
  if (checked) {
    checkedId.value = id;
    return;
  }

  checkedId.value = undefined;
};

const ok = async () => {
  // 查询
  loading.value = true;
  const [error, { data }] = await script.loadDetail(checkedId.value);
  loading.value = false;
  if (error) {
    return;
  }

  try {
    let fileContent = data.content;
    fileContent = fileContent.replace(/\\\n/g, '');
    fileContent = YAML.parse(fileContent);
    emit('ok', fileContent);
    emit('update:visible', false);
  } catch (error) {
    notification.error(error.message);
  }
};

const cancel = () => {
  emit('cancel');
  emit('update:visible', false);
};

const reset = () => {
  checkedId.value = undefined;
  inputValue.value = undefined;
  dataList.value = [];
  loading.value = false;
};

onMounted(() => {
  watch(() => props.visible, (newValue) => {
    if (!newValue) {
      return;
    }

    reset();
  });
});

const scrollParams = computed(() => {
  const params = { filters: undefined, plugin: 'Mail', projectId: projectInfo.value?.id };
  if (inputValue.value) {
    params.filters = [{
      key: 'name',
      op: 'MATCH_END',
      value: inputValue.value
    }];
  }

  return params;
});

const action = `${TESTER}/script/search`;
</script>
<template>
  <Modal
    title="选择脚本"
    :visible="props.visible"
    :centered="true"
    :width="700"
    :confirmLoading="loading"
    class="select-api-modal-wrap"
    @ok="ok"
    @cancel="cancel">
    <div class="h-full space-y-2.5">
      <Input
        :value="inputValue"
        :allowClear="true"
        placeholder="查询脚本名称"
        trim
        @change="inputChange">
        <template #suffix>
          <Icon icon="icon-sousuo" />
        </template>
      </Input>

      <div style="height: calc(100% - 38px);">
        <div class="bg-table-header flex items-center pl-2 h-8 leading-5 rounded">
          <div class="w-4 h-5 flex items-center justify-center flex-shrink-0"></div>
          <div class="flex-1 px-2">名称</div>
          <div class="flex-shrink-0 px-2 w-25">插件</div>
          <div class="flex-shrink-0 px-2 w-28">类型</div>
        </div>

        <Scroll
          v-if="props.visible"
          style="height: calc(100% - 32px);"
          :action="action"
          :lineHeight="32"
          :params="scrollParams"
          class="py-1"
          @change="scrollChange">
          <div
            v-for="item in dataList"
            :key="item.id"
            class="api-item flex items-center h-7 pl-2 leading-5 mb-1 rounded cursor-pointer">
            <div class="w-4 h-5 flex items-center justify-center flex-shrink-0">
              <Checkbox
                :checked="checkedId===item.id"
                class="checkbox-box-white"
                @change="checkChange($event,item.id)" />
            </div>
            <div :title="item.name" class="flex-1 truncate px-2">
              {{ item.name }}
            </div>
            <div class="flex-shrink-0 px-2 w-25">
              {{ item.plugin }}
            </div>
            <div class="flex-shrink-0 px-2 w-28">
              {{ item.type.message }}
            </div>
          </div>
        </Scroll>
      </div>
    </div>
  </Modal>
</template>

<style>
.select-api-modal-wrap {
  height: calc(60%);
  min-height: 480px;
}

.select-api-modal-wrap .ant-modal-content {
  height: 100%;
}

.select-api-modal-wrap .ant-modal-body {
  height: calc(100% - 92px);
}
</style>

<style scoped>
.bg-table-header {
  background-color: rgba(247, 248, 251, 100%);
}

.checkbox-box-white :deep(.ant-checkbox) {
  background-color: #fff;
}

.api-item:hover {
  background-color: var(--content-tabs-bg-hover);
}

.ant-checkbox-wrapper{
  align-items:center;
  line-height: 20px;
}

:deep(.ant-checkbox) {
  top:0;
}
</style>
