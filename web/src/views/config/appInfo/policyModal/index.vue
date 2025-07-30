<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { debounce } from 'throttle-debounce';
import { Icon, Input, Modal, Scroll } from '@xcan-angus/vue-ui';
import { Checkbox, CheckboxGroup, Divider } from 'ant-design-vue';
import { duration, GM } from '@xcan-angus/infra';

interface Props {
  visible: boolean;
  appId: string;
  type?: 'Group' | 'Dept' | 'User'
  userId?:string;
  deptId?: string;
  groupId?:string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  appId: undefined,
  userId: undefined,
  deptId: undefined,
  type: undefined,
  groupId: undefined
});

const emit = defineEmits<{(e: 'update:visible', value:boolean): void,
(e: 'change', addIds:string[], addPolicys:{id:string, name:string}[]): void }>();

const { t } = useI18n();

const params = ref<{ filters:{key:'name', op: 'MATCH_END', value: string | undefined}[], enabled:boolean, appId:string}>({ filters: [], enabled: true, appId: props.appId });
const notify = ref(0);
const dataList = ref<{id:string, name:string, code:string, description:string, appName:string, enabled:boolean}[]>([]); // 已加载的策略
const checkedList = ref<string[]>([]); // 当前已选择的策略的Ids
const indeterminate = ref(false);
const loading = ref(false);

const onCheckAllChange = e => {
  if (e.target.checked) {
    checkedList.value = dataList.value.map(m => m.id);
    indeterminate.value = true;
  } else {
    indeterminate.value = false;
    checkedList.value = [];
  }
};

const handleInputChange = debounce(duration.search, (event:ChangeEvent) => {
  const value = event.target.value;
  if (value) {
    params.value.filters[0] = { key: 'name', op: 'MATCH_END', value: value };
    return;
  }
  params.value.filters = [];
});

const handleOk = () => {
  // 所有选择的策略 checkedList:ids, checkedPolicys:policys
  const checkedPolicys = dataList.value.filter(item => checkedList.value.includes(item.id));
  emit('change', checkedList.value, checkedPolicys);
  emit('update:visible', false);
};

const handleCancel = () => {
  emit('update:visible', false);
};

const handleChange = (value) => {
  dataList.value = value;
};

watch(() => props.visible, newValue => {
  if (newValue) {
    notify.value++;
  }
});

const action = computed(() => {
  switch (props.type) {
    case 'User':
      return `${GM}/auth/user/${props.userId}/unauth/policy`;
    case 'Group':
      return `${GM}/auth/group/${props.groupId}/unauth/policy`;
    case 'Dept':
      return `${GM}/auth/dept/${props.deptId}/unauth/policy`;
    default: return '';
  }
});

</script>
<template>
  <Modal
    :title="t('授权策略')"
    :visible="props.visible"
    :centered="true"
    :keyboard="true"
    :width="1160"
    class="my-modal"
    @cancel="handleCancel"
    @ok="handleOk">
    <div class="-mt-3">
      <div class="mb-2 flex space-x-2">
        <Input
          placeholder="查询策略名称"
          size="small"
          class="w-1/2"
          allowClear
          @change="handleInputChange">
          <template #suffix>
            <Icon icon="icon-sousuo" class="text-4 text-theme-sub-content" />
          </template>
        </Input>
      </div>
      <div class="flex py-0.5 bg-theme-form-head text-theme-title text-3 font-normal mb-1">
        <div class="pl-6 w-46">
          ID
        </div>
        <div class="w-40 mr-2">
          {{ t('名称') }}
        </div>
        <div class="w-60 mr-2">
          {{ t('编码') }}
        </div>
        <div class="w-100 mr-2">
          {{ t('描述') }}
        </div>
        <div class="w-20">
          {{ t('状态') }}
        </div>
      </div>
      <Scroll
        v-model="loading"
        :action="action"
        class="pr-2 h-65"
        :params="params"
        :notify="notify"
        @change="handleChange">
        <CheckboxGroup
          v-model:value="checkedList"
          style="width: 100%;"
          class="space-y-2">
          <div
            v-for="item,index in dataList"
            :key="item.id"
            class="flex-1 items-center flex text-3 text-theme-content"
            :calss="{'mt-2':index>0}">
            <Checkbox
              :value="item.id">
            </Checkbox>
            <div class="truncate w-40 ml-2 mt-0.5">{{ item.id }}</div>
            <div class="truncate w-40 mr-2 mt-0.5" :title="item.name">{{ item.name }}</div>
            <div class="truncate w-60 mr-2 mt-0.5" :title="item.code">{{ item.code }}</div>
            <div class="truncate w-100 mr-2 mt-0.5" :title="item.description">{{ item.description }}</div>
            <div class="truncate w-20 mt-0.5">{{ item.enabled?'启用':'禁用' }}</div>
          </div>
        </CheckboxGroup>
      </Scroll>
      <Divider class="my-2" />
      <Checkbox :indeterminate="indeterminate" @change="onCheckAllChange">
        全选
      </Checkbox>
    </div>
  </Modal>
</template>
