<template>
  <div>
    <li
      v-for="(attr, idx) in dataSource"
      :key="idx"
      class="pt-4 ml-3 pl-3"
      :class="{'attr-item-wrap': idx + 1 < dataSource.length && !$props.withoutBorder}">
      <div
        class="relative leading-7 flex items-center justify-between "
        :class="{'attr-item-last': idx + 1 === dataSource.length && !$props.withoutBorder, 'attr-item': !$props.withoutBorder}">
        <div class="pl-1">
          <Arrow
            v-if="attr.children?.length"
            v-model:open="attr.open"
            @change="toogle" />
          <Popover trigger="hover">
            <span>{{ attr.name }}<IconRequired v-if="attr.required" /></span>
            <template #content>
              <Grid
                :dataSource="attr"
                :columns="columns" />
            </template>
          </Popover>
          <span class="ml-2" :class="typeColor[attr.type]">{{ attr.type }}</span>
          <span v-if="attr.type === 'object'"> {{ ` {${attr.children?.length || 0}\}` }} </span>
          <span v-if="attr.format">{{ `<${attr.format}>` }}</span>
          <span v-if="attr.$ref" class="text-status-warn">$ref</span>
        </div>
        <div v-show="!$props.viewType" class="space-x-1">
          <Button
            type="link"
            size="small"
            class="py-0"
            @click="editSelf(attr, )">
            <Icon
              icon="icon-bianji"
              class="text-3.5" />
          </Button>
          <Button
            type="link"
            size="small"
            class="py-0"
            :disabled="!['object', 'array'].includes(attr.type) || attr.type === 'array' && attr.children?.length > 0"
            @click="addChild(attr)">
            <Icon
              icon="icon-jia"
              class="text-3.5" />
          </Button>
          <Button
            type="link"
            size="small"
            class="py-0"
            @click="delSelf(dataSource, idx)">
            <Icon
              icon="icon-qingchu"
              class="text-3.5" />
          </Button>
        </div>
      </div>
      <AttrItemList
        v-if="attr.children"
        v-show="attr.open"
        :dataSource="attr.children"
        :parentType="attr.type"
        @add="addChild"
        @del="delSelf"
        @edit="editSelf" />
    </li>
  </div>
</template>
<script>
import { defineComponent } from 'vue';
import { Arrow, Grid, Icon, IconRequired } from '@xcan-angus/vue-ui';
import { Button, Popover } from 'ant-design-vue';
// import { useI18n } from 'vue-i18n';

export default defineComponent({
  name: 'AttrItemList',
  components: { Arrow, Icon, Popover, Grid, Button, IconRequired },
  props: {
    dataSource: {
      type: Array,
      default: () => ([])
    },
    parentType: {
      type: String,
      default: 'object'
    },
    withoutBorder: {
      type: Boolean,
      default: false
    },
    viewType: {
      type: Boolean,
      default: false
    }
  },
  emits: ['add', 'del', 'edit'],
  data () {
    return {
      columns: [[
        {
          dataIndex: 'name',
          label: this.$t('service.dataModel.form.name')
        },
        {
          dataIndex: 'required',
          label: this.$t('service.dataModel.form.required')
        },
        {
          dataIndex: 'nullabled',
          label: 'nullabled'
        },
        {
          dataIndex: 'deprecated',
          label: this.$t('service.dataModel.form.deprecated')
        },
        {
          dataIndex: 'type',
          label: this.$t('service.dataModel.form.type')
        },
        {
          dataIndex: 'format',
          label: this.$t('service.dataModel.form.format')
        },
        {
          dataIndex: 'example',
          label: this.$t('service.dataModel.form.example')
        },
        {
          dataIndex: 'description',
          label: this.$t('service.dataModel.form.description')
        }
      ]],
      typeColor: {
        string: 'text-status-success',
        number: 'text-status-pink',
        integer: 'text-status-blue-light',
        object: 'text-status-orange',
        array: 'text-status-purple',
        boolean: 'text-status-error1'
      }
    };
  },
  methods: {
    addChild (attr) {
      this.$emit('add', attr);
    },
    delSelf (parent, idx) {
      this.$emit('del', parent, idx);
    },
    editSelf (attr, type = this.$props.parentType, excludesAttr = (this.$props.parentType === 'object' ? this.$props.dataSource.map(i => i.name).filter(name => name !== attr.name) : [])) {
      this.$emit('edit', attr, type, excludesAttr);
    }
  }
});
</script>
<style scoped>

.attr-item-wrap {
  border-left: 2px solid #07F
}

.attr-item-last::before {
  content: '';
  display: inline-block;
  position: absolute;
  top: -16px;
  left: -12px;
  height: 32px;
  border-left: 2px solid #07F;
}

.attr-item::after {
  content: '';
  display: inline-block;
  position: absolute;
  top: 50%;
  left: -12px;
  width: 12px;
  border-top: 2px solid #07F;
}
</style>
