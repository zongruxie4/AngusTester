
<script setup lang="ts">
import { computed, defineAsyncComponent, inject, ref } from 'vue';
import { AsyncComponent, Icon, Tooltip, VuexHelper } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { utils } from '@xcan-angus/infra';
import { Button } from 'ant-design-vue';

const LocalImport = defineAsyncComponent(() => import('../../sidebar/components/localImport/index.vue'));

interface Props {
  info:any;
  projectAuths: string[];
}

const props = withDefaults(defineProps<Props>(), {
  info: () => ({}),
  projectAuths: () => []
});

const { t } = useI18n();
// eslint-disable-next-line @typescript-eslint/no-empty-function
const addTabPane = inject<(data: any) => void>('addTabPane', () => { });

const visible = ref(false);

const debugging = () => {
  addTabPane({
    value: 'API',
    unarchived: true,
    name: '添加API',
    _id: utils.uuid('api') + 'API',
    serviceId: props.info.id,
    serviceName: props.info.name
  });
};

const openImport = () => {
  visible.value = true;
};

const entries:{titleIcon: string; title: string; content: string; icon: string; action: string; click:() => void, disabled: boolean }[] = computed(() => {
  return [
    {
      titleIcon: 'icon-jiekoutiaoshi',
      title: t('apis.text.t7'),
      content: t('apis.text.t8'),
      icon: 'icon-tiaoshi',
      action: t('apis.text.t9'),
      click: debugging
    },
    // props.info.type === 'P' && {
    //   titleIcon: 'icon-chuangjianxiangfuwu',
    //   title: t('apis.home.h17'),
    //   content: t('apis.text.t11'),
    //   icon: 'icon-chuangjianfuwu',
    //   action: t('apis.text.t13'),
    //   click: createService,
    //   disabled: !props.projectAuths.includes('ADD')
    // },
    {
      titleIcon: 'icon-daoruxiangmufuwu',
      title: t('apis.text.t31') + t('apis.text.t21'),
      content: t('apis.text.t15'),
      icon: 'icon-daoru',
      action: t('apis.text.t16'),
      click: openImport,
      disabled: !props.projectAuths.includes('ADD')
    }
  ].filter(Boolean);
});

const { useMutations, useState } = VuexHelper;
const { stepVisible, stepKey, stepContent } = useState(['stepVisible', 'stepKey', 'stepContent'], 'guideStore');
const { updateGuideStep } = useMutations(['updateGuideStep'], 'guideStore');

const guideStep = () => {
  updateGuideStep({ visible: true, key: 'debugApiOne' });
  entries.value[0].click();
};
</script>
<template>
  <div class="space-y-2">
    <div class="title-normal">{{ t('apis.text.t6') }}</div>
    <div class="flex flex-nowrap space-x-5 px-4">
      <div
        v-for="(entery,index) in entries"
        :key="entery.titleIcon"
        style="width:calc((100% - 40px)/3);"
        class="p-5 flex flex-col justify-between min-h-37.5 min-w-50 border border-theme-text-box rounded">
        <div>
          <div class="flex items-center space-x-2">
            <Icon :icon="entery.titleIcon" class="text-white -mt-0.5 text-5" />
            <span class="title-normal">{{ entery.title }}</span>
          </div>
          <div class="mt-3 text-content">{{ entery.content }}</div>
        </div>
        <Tooltip
          :visible="stepVisible && stepKey === 'selectDebugApi' && index === 0"
          placement="rightTop"
          destroyTooltipOnHide>
          <template #title>
            <div class="p-2 text-3">
              <div class="text-4 text-text-title">{{ stepContent.title }}</div>
              <div class="mt-2">{{ stepContent.content }}</div>
              <div class="flex justify-end mt-5">
                <Button
                  size="small"
                  type="primary"
                  @click="guideStep">
                  下一步
                </Button>
              </div>
            </div>
          </template>
          <Button
            type="link"
            size="small"
            class="w-fit h-5 space-x-1 mt-2"
            :class="{'text-theme-special text-theme-text-hover': !entery.disabled}"
            :disabled="entery.disabled"
            @click="entery.click">
            <Icon :icon="entery.icon" />
            <span> {{ entery.action }}</span>
          </Button>
        </Tooltip>
      </div>
    </div>
  </div>
  <AsyncComponent :visible="visible">
    <LocalImport
      v-model:visible="visible"
      :serviceId="info.id"
      source="projectOrService" />
  </AsyncComponent>
</template>

<style scoped>
.ant-btn-link{
  padding: 0;
}
</style>
