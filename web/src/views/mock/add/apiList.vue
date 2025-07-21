<script setup lang="ts">
import { computed, inject, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { HttpMethodText, Icon, Input } from '@xcan-angus/vue-ui';
import { Checkbox, CheckboxGroup, Divider, FormItemRest } from 'ant-design-vue';
import { apis } from '@/api/tester';

interface Props {
 serviceId:string;
 checkedIds: string[];
}

const projectInfo = inject('projectInfo', ref({ id: '' }));
const projectId = computed(() => {
  return projectInfo?.value?.id;
});

const props = withDefaults(defineProps<Props>(), {
  serviceId: '',
  checkedIds: () => []
});

const emits = defineEmits<{(e: 'update:apiIds', value:string[]): void}>();

const { t } = useI18n();
const name = ref('');
const params = computed(() => {
  if (name.value) {
    return { filters: [{ key: 'summary', op: 'MATCH_END', value: name.value }] };
  }
  return { filters: [] };
});

const dataList = ref<{id:string, summary:string}[]>([]); // 所有接口
const checkedList = ref<string[]>(props.checkedIds); // 当前已选择的用户
// const oldCheckedList = ref<string[]>([]); // 后台已关联的用户

const onCheckAllChange = e => {
  if (e.target.checked) {
    checkedList.value = dataList.value.map(m => m.id);
  } else {
    checkedList.value = [];
  }
};

// const getList = (apiList) => {
//   dataList.value = apiList;
// };

const loadList = async () => {
  const [error, { data }] = await apis.getApiList({
    ...params.value,
    pageSize: 2000,
    pageNo: 1,
    serviceId: props.serviceId,
    projectId: projectId.value
  });
  if (error) {
    return;
  }
  checkedList.value = [];
  dataList.value = data?.list || [];
};

watch(() => checkedList.value, (newValue) => {
  emits('update:apiIds', [...new Set([...newValue, ...props.checkedIds])]);
});

watch(() => props.checkedIds, (newValue) => {
  checkedList.value = newValue;
}, {
  deep: true
});

watch([() => projectId.value, () => props.serviceId, () => params.value], () => {
  if (projectId.value) {
    loadList();
  }
}, {
  immediate: true
});
</script>
<template>
  <div class="border p-3.5 rounded">
    <FormItemRest>
      <Input
        v-model:value="name"
        :placeholder="t('查询接口名称')"
        :disabled="!props.serviceId"
        class="mb-2"
        allowClear>
        <template #suffix>
          <Icon icon="icon-sousuo" class="text-4 text-theme-sub-content" />
        </template>
      </Input>
      <div class="flex py-0.5 bg-bg-table-head text-text-title text-3 font-normal mb-1 pl-7.5">
        <div class="w-20 mr-2">
          请求方法
        </div>
        <div class="w-40 mr-2">
          URL
        </div>
        <div class="flex-1">
          接口名称
        </div>
      </div>
      <!-- <div class="h-70 overflow-y-auto space-y-2">

      </div> -->
      <CheckboxGroup
        v-model:value="checkedList"
        style="width: 100%;"
        class="space-y-2 h-70 overflow-y-auto">
        <div
          v-for="item,index in dataList"
          :key="item.id"
          class="flex flex-1 items-center text-3 text-text-content"
          :calss="{'mt-1':index>0}">
          <Checkbox
            :value="item.id"
            class="mr-3.5 -mb-0.5">
          </Checkbox>
          <HttpMethodText class="w-20 mr-2 h-5 pt-0.75" :value="item?.method" />

          <div
            class="truncate mr-2 pt-1 w-40"
            :title="item.endpoint">
            {{ item.endpoint }}
          </div>

          <div
            class="truncate pt-1 flex-1"
            :title="item.summary">
            {{ item.summary }}
          </div>
        </div>
      </CheckboxGroup>
      <Divider class="my-2" />
      <Checkbox
        :checked="(checkedList.length > 0 && checkedList.length === dataList.length)"
        :indeterminate="(checkedList.length > 0 && checkedList.length !== dataList.length)"
        @change="onCheckAllChange">
        {{ t('全选') }}
      </Checkbox>
    </FormItemRest>
  </div>
</template>
