
<script setup lang="ts">
import { computed } from '@vue/reactivity';
import { Icon } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';

interface Props {
  info: any;
  serviceAuths: string[];
}

const props = withDefaults(defineProps<Props>(), {
  info: {},
  serviceAuths: () => []
});

const emits = defineEmits<{(e: 'openDrawer', key: string): void }>();

const projectType = computed(() => {
  return t('apis.configuration.type_service');
});

const { t } = useI18n();
const dataSource = computed(() => [[{
  id: 'syncConfig',
  key: 'syncConfig',
  icon: 'icon-peizhifuwutongbu',
  title: t('actions.sync'),
  content: t('apis.configuration.syncDescription', { type: projectType.value }),
  linkText: t('common.actions'),
  disabled: !props.serviceAuths.includes('MODIFY')
},
{
  id: 'security',
  key: 'security',
  icon: 'icon-renzhengtou',
  title: t('apis.configuration.security'),
  content: t('apis.configuration.securityDescription', { type: projectType.value }),
  linkText: t('common.actions'),
  disabled: !props.serviceAuths.includes('MODIFY')
},
{
  id: 'serverConfig',
  key: 'serverConfig',
  icon: 'icon-host',
  title: t('apis.configuration.server'),
  content: t('apis.configuration.serverDescription'),
  linkText: t('common.actions'),
  disabled: !props.serviceAuths.includes('MODIFY')
}
]
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
