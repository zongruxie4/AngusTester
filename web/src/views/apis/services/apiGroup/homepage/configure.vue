
<script setup lang="ts">
import { computed } from '@vue/reactivity';
import { Icon } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';

interface Props {
  info: any;
  projectAuths: string[];
}

const props = withDefaults(defineProps<Props>(), {
  info: {},
  projectAuths: () => []
});

const emits = defineEmits<{(e: 'openDrawer', key: string): void }>();

const projectType = computed(() => {
  return '服务';
});

const { t } = useI18n();
const dataSource = computed(() => [[{
  id: 'syncConfig',
  key: 'syncConfig',
  icon: 'icon-peizhifuwutongbu',
  title: t('apis.configuration.sync'),
  content: `将外部OpenAPI自动导入到当前${projectType.value}`,
  linkText: t('apis.configuration.action'),
  disabled: !props.projectAuths.includes('MODIFY')
},
{
  id: 'security',
  key: 'security',
  icon: 'icon-renzhengtou',
  title: t('apis.configuration.security'),
  content: `定义当前${projectType.value}下所有接口可以使用的安全方案(Authorization)`,
  linkText: t('apis.configuration.action'),
  disabled: !props.projectAuths.includes('MODIFY')
},
{
  id: 'serverConfig',
  key: 'serverConfig',
  icon: 'icon-host',
  title: t('apis.configuration.server'),
  content: `定义当前${projectType.value}下所有接口可以使用的服务器(Server URL)`,
  linkText: t('apis.configuration.action'),
  disabled: !props.projectAuths.includes('MODIFY')
}
]
// [
//   {
//   id: 'perf',
//   key: 'perf',
//   icon: 'icon-zhibiao',
//   title: t('apis.configuration.performance'),
//   content: `为${projectType.value}下接口设置默认性能指标，性能指标会影响性能测试通过率；如果您想为某个接口单独设置性能指标，您可以在接口性能配置里设置`,
//   linkText: t('apis.configuration.action'),
//   disabled: !props.projectAuths.includes('MODIFY')
// },
// {
//   id: 'shareList',
//   key: 'shareList',
//   icon: 'icon-fenxiang',
//   title:  t('apis.configuration.share'),,
//   content: t('apis.configuration.shareDescription'),
//   linkText: t('apis.configuration.action'),
//   disabled: !props.projectAuths.includes('SHARE')
// },
  // {
  //   id: 'variable',
  //   key: 'variable',
  //   icon: 'icon-bianliang',
  //   title: t('apis.configuration.variables'),
  //   content: t('apis.configuration.variablesDescription'),
  //   linkText: t('apis.configuration.action'),
  //   disabled: !props.projectAuths.includes('MODIFY')
  // }
// ]
]);

const openDrawer = (key: string) => {
  emits('openDrawer', key);
};

</script>
<template>
  <div class="space-y-2">
    <div class="title-normal">{{ t('apis.actions.projectConfig') }}</div>
    <div class="space-y-5 px-4">
      <div
        v-for="(element, index) in dataSource"
        :key="index"
        class="flex flex-nowrap space-x-5">
        <div
          v-for="item in element"
          :key="item.key"
          style="width:calc((100% - 40px)/3);"
          class="flex flex-col p-5 min-w-50 border border-theme-text-box rounded">
          <div class="flex items-center space-x-2">
            <Icon :icon="item.icon" class="text-4 leading-4" />
            <span class="title-normal">{{ item.title }}</span>
          </div>
          <div class="text-content mt-3">
            {{ item.content }}
          </div>
          <Button
            type="link"
            size="small"
            :class="{ 'text-theme-special text-theme-text-hover': !item.disabled }"
            class="w-fit text-3 leading-3 font-normal mt-3.5 px-0"
            :disabled="item.disabled"
            @click="openDrawer(item.key)">
            <span> {{ item.linkText }}</span>
            <Icon icon="icon-xiajiantou" class="text-3 leading-3 mr-1.25 transform -rotate-90" />
          </Button>
        </div>
      </div>
    </div>
  </div>
</template>
