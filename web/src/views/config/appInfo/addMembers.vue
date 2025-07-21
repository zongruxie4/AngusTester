<script setup lang="ts">
import { Tooltip } from 'ant-design-vue';
import { computed, inject, Ref, ref } from 'vue';
import { Grid, Modal, Select } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { GM } from '@xcan-angus/tools';

import { app } from 'src/api/gm';

interface Props {
  visible: boolean;
  appId:string;
  type:'USER' | 'DEPT' | 'GROUP';
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  appId: undefined,
  type: 'USER'
});

const emit = defineEmits<{(e: 'update', refresh:boolean): void }>();

const tenantInfo:Ref = inject('tenantInfo', ref());
const { t } = useI18n();

// 选择的用户部门组id
const selectedUserIds = ref();
// 用户部门校验
const userError = ref(false);
const userChange = (value) => {
  userError.value = !value;
};

// 选择的策略ids
const selectedPolicyIds = ref([]);
// 策略校验
const policyError = ref(false);
const policyChange = (value) => {
  policyError.value = !value.length;
};

const handleOk = () => {
  if (!selectedUserIds.value) {
    userError.value = true;
    return;
  }
  if (!selectedPolicyIds.value.length) {
    policyError.value = true;
    return;
  }
  const params = { orgIds: selectedUserIds.value, policyIds: selectedPolicyIds.value };
  if (props.type === 'USER') {
    addUsers(params);
  } else if (props.type === 'DEPT') {
    addDepts(params);
  } else {
    addGroups(params);
  }
};
// 加入部门
const addDepts = async (params:{orgIds:string[], policyIds:string[]}): Promise<void> => {
  const [err] = await app.addAuthDept(props.appId, params);
  if (err) {
    emit('update', false);
    return;
  }

  emit('update', true);
};
// 加入用户
const addUsers = async (params:{orgIds:string[], policyIds:string[]}): Promise<void> => {
  const [err] = await app.addAuthUser(props.appId, params);
  if (err) {
    emit('update', false);
    return;
  }

  emit('update', true);
};
// 加入组
const addGroups = async (params:{orgIds:string[], policyIds:string[]}): Promise<void> => {
  const [err] = await app.addAuthGroup(props.appId, params);
  if (err) {
    emit('update', false);
    return;
  }

  emit('update', true);
};

const handleCancel = () => {
  emit('update', false);
};

// 弹窗title
const title = computed(() => {
  if (props.type === 'USER') {
    return t('加入用户');
  }

  if (props.type === 'DEPT') {
    return t('加入部门');
  }

  return t('加入组');
});

// 选择用户部门组占位提示
const placeholder = computed(() => {
  if (props.type === 'USER') {
    return t('settingApp.placeholder.p1');
  }

  if (props.type === 'DEPT') {
    return t('settingApp.placeholder.p2');
  }

  return t('settingApp.placeholder.p3');
});

// 选择用户部门组请求
const selectAction = computed(() => {
  switch (props.type) {
    case 'USER' :
      return `${GM}/app/${props.appId}/unauth/user`;
    case 'DEPT' :
      return `${GM}/app/${props.appId}/unauth/dept`;
    case 'GROUP':
      return `${GM}/app/${props.appId}/unauth/group`;
    default:
      return `${GM}/app/${props.appId}/unauth/user`;
  }
});

// 策略请求（当前登录用户已有的策略）
const policyAction = computed(() => {
  return `${GM}/auth/user/${tenantInfo.value.id}/policy/associated?appId=${props.appId}`;
});

// 用户的label是fullname
const fieldNames = computed(() => {
  switch (props.type) {
    case 'USER' :
      return { label: 'fullName', value: 'id' };
    case 'DEPT' :
      return { label: 'name', value: 'id' };
    case 'GROUP':
      return { label: 'name', value: 'id' };
    default:
      return { label: 'fullName', value: 'id' };
  }
});

const getPopupContainer = (el: HTMLElement): HTMLElement => {
  if (el.parentElement) {
    return el.parentElement;
  }
  return document.body;
};

// 选择用户部门组 select前的label
const selectLable = computed(() => {
  if (props.type === 'USER') {
    return t('选择用户');
  }

  if (props.type === 'DEPT') {
    return t('选择部门');
  }

  return t('选择组');
});

const gridColumns = [
  [
    {
      label: selectLable.value,
      dataIndex: 'users'
    },
    {
      label: t('选择策略'),
      dataIndex: 'policys'
    }
  ]
];
</script>
<template>
  <Modal
    :title="title"
    :visible="props.visible"
    :centered="true"
    :keyboard="true"
    :width="600"
    @cancel="handleCancel"
    @ok="handleOk">
    <Grid :columns="gridColumns" marginBottom="24px">
      <template #users>
        <Select
          v-model:value="selectedUserIds"
          :placeholder="placeholder"
          :action="selectAction"
          :fieldNames="fieldNames"
          :error="userError"
          allowClear
          showSearch
          mode="multiple"
          class="w-full -mt-1.5"
          optionLabelProp="label"
          @change="userChange">
          <template #option="record">
            <Tooltip
              :title="`ID: ${record[fieldNames.value]}`"
              placement="bottomLeft"
              :getPopupContainer="getPopupContainer">
              {{ record[fieldNames.label] }}
            </Tooltip>
          </template>
        </Select>
      </template>
      <template #policys>
        <Select
          v-model:value="selectedPolicyIds"
          placeholder="选择策略"
          class="w-full -mt-1.5"
          mode="multiple"
          :error="policyError"
          :action="policyAction"
          :fieldNames="{ label: 'name', value: 'id' }"
          allowClear
          showSearch
          @change="policyChange">
        </Select>
      </template>
    </Grid>
  </Modal>
</template>
<style scoped>
.my-tabs-bordr {
  border-color: var(--content-special-text);
}
</style>
