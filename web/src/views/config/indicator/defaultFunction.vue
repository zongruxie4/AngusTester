<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { Icon, IndicatorAssert } from '@xcan-angus/vue-ui';
import ExpandGrid from './expandGrid.vue';
import { Button, RadioGroup, Switch } from 'ant-design-vue';
import { enumUtils } from '@xcan-angus/infra';
import { setting } from '@/api/gm';

// 冒烟测试指标选项
const smokeEnumOpt = ref<{value: string; label: string}[]>([]);
// 安全测试指标选项
const SecurityEnumOpt = ref<{value: string; label: string}[]>([]);

// 断言组件 Ref
const smokeAssetRef = ref();
const securityAssetRef = ref();

const loadIndicatorEnum = () => {
  const data1 = enumUtils.enumToMessages('SmokeCheckSetting');
  smokeEnumOpt.value = (data1 || []).map(i => ({ ...i, label: i.message }));
  const data2 = enumUtils.enumToMessages('SecurityCheckSetting');
  SecurityEnumOpt.value = (data2 || []).map(i => ({ ...i, label: i.message }));
};

// 获取默认指标
const loadIndicatorData = async () => {
  const [error, { data }] = await setting.getFuncIndicator();
  if (error) {
    return;
  }
  const { securityCheckSetting, smokeCheckSetting, ...others } = data;
  settingData.value = {
    ...others,
    securityCheckSetting: securityCheckSetting.value || securityCheckSetting,
    smokeCheckSetting: smokeCheckSetting.value || smokeCheckSetting
  };
};

type FuncIndicator = {
  securityCheckSetting: 'IS_SECURITY_CODE' | 'NOT_SECURITY_CODE' | 'USER_DEFINED_ASSERTION';
  smokeCheckSetting: 'API_AVAILABLE'| 'SERVICE_AVAILABLE'| 'USER_DEFINED_ASSERTION';
  userDefinedSecurityAssertion?: {
    [key: string]: string
  };
  userDefinedSmokeAssertion?: {
    [key: string]: string
  };
  smoke: boolean;
  security: boolean;
}
// 指标数据
const settingData = ref<FuncIndicator>({
  securityCheckSetting: 'NOT_SECURITY_CODE',
  smokeCheckSetting: 'API_AVAILABLE',
  userDefinedSecurityAssertion: undefined,
  userDefinedSmokeAssertion: undefined,
  smoke: true,
  security: true
});

let initSetting = {};

const editable = ref(false);
const handleEditPerform = () => {
  editable.value = !editable.value;
  if (editable.value) {
    initSetting = JSON.parse(JSON.stringify(settingData.value));
  } else {
    settingData.value = JSON.parse(JSON.stringify(initSetting));
  }
};
const saveInfo = async () => {
  if (smokeAssetRef.value) {
    if (smokeAssetRef.value.validate()) {
      settingData.value.userDefinedSmokeAssertion = smokeAssetRef.value.getData();
    } else {
      return;
    }
  } else {
    settingData.value.userDefinedSmokeAssertion = undefined;
  }
  if (securityAssetRef.value) {
    if (securityAssetRef.value.validate()) {
      settingData.value.userDefinedSecurityAssertion = smokeAssetRef.value.getData();
    } else {
      return;
    }
  } else {
    settingData.value.userDefinedSecurityAssertion = undefined;
  }
  const [error] = await setting.saveFuncIndicator(settingData.value);
  if (error) {
    return;
  }
  editable.value = false;
};

onMounted(() => {
  loadIndicatorEnum();
  loadIndicatorData();
});
</script>
<template>
  <ExpandGrid title="平台默认功能指标">
    <template #button>
      <div class="text-3 flex items-center">
        <template v-if="editable">
          <span class="cursor-pointer" @click.stop="handleEditPerform"><Icon icon="icon-zhongzhi2" class="mr-1" />取消</span>
          <Button
            type="text"
            class="ml-2 text-3 py-0 h-5"
            @click.stop="saveInfo">
            <Icon icon="icon-baocun" class="mr-1" />保存
          </Button>
        </template>
        <Button
          v-else
          class="text-3 py-0 h-5"
          type="text"
          @click.stop="handleEditPerform">
          <Icon icon="icon-shuxie" class="mr-1" />编辑
        </Button>
      </div>
    </template>
    <template #default>
      <div class="flex text-3 pt-3 px-3">
        <div class="flex flex-1 pr-8 items-center">
          <Icon icon="icon-maoyanceshi" class="text-4" />
          <div class="px-4">
            <div class="title-normal">冒烟测试用例</div>
          </div>
          <Switch
            v-model:checked="settingData.smoke"
            size="small"
            :disabled="!editable" />
        </div>
        <div class="flex flex-1 pr-8 items-center ">
          <Icon icon="icon-anquanceshi" class="text-4" />
          <div class="px-4">
            <div class="title-normal">安全测试用例</div>
          </div>
          <Switch
            v-model:checked="settingData.security"
            size="small"
            :disabled="!editable" />
        </div>
      </div>
      <div class="flex text-3 px-3 items-center">
        <div class="mt-2 pl-8 flex-1">用于开发人员或者测试人员快速验证接口基本功能是否可用。</div>
        <div class="mt-2 pl-8 flex-1">用于防止未授权或不合法的访问，如使用有效的身份验证令牌发送请求，确保响应返回不是401无效授权或者403权限不足。</div>
      </div>
      <div class="flex text-3 px-3 pb-3">
        <div class="mt-2 pl-8 flex-1">
          <RadioGroup
            v-model:value="settingData.smokeCheckSetting"
            :disabled="!editable"
            :options="smokeEnumOpt"
            class="flex flex-col space-y-1" />
        </div>
        <div class="mt-2 pl-8 flex-1">
          <RadioGroup
            v-model:value="settingData.securityCheckSetting"
            :disabled="!editable"
            :options="SecurityEnumOpt"
            class="flex flex-col space-y-1" />
        </div>
      </div>
      <div class="flex text-3 px-3">
        <div class="pl-8 flex-1">
          <IndicatorAssert
            v-if="settingData.smokeCheckSetting === 'USER_DEFINED_ASSERTION'"
            ref="smokeAssetRef"
            :value="settingData.userDefinedSmokeAssertion"
            class="max-w-100"
            :viewType="!editable" />
        </div>
        <div class="pl-8 flex-1">
          <IndicatorAssert
            v-if="settingData.securityCheckSetting === 'USER_DEFINED_ASSERTION'"
            ref="securityAssetRef"
            :value="settingData.userDefinedSecurityAssertion"
            class="max-w-100"
            :viewType="!editable" />
        </div>
      </div>
    </template>
  </ExpandGrid>
</template>
<style scoped>
:deep(.ant-radio-disabled+span) {
 color: rgb(82, 90, 101)
}
</style>
