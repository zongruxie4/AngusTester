import { defineComponent, ref, reactive, computed, Teleport, watch, onMounted, onBeforeUnmount } from 'vue';
import { Icon } from '@xcan-angus/vue-ui';
import './cascader.css';

const props = {
  visible: {
    type: Boolean,
    required: false,
    default: false
  },
  options: {
    type: Array,
    required: true,
    default: []
  },
  labelKey: {
    type: String,
    required: false,
    default: 'name'
  },
  valueKey: {
    type: String,
    required: false,
    default: 'value'
  },
  childKey: {
    type: String,
    required: false,
    default: 'children'
  },

  // 指定位置
  position: {
    type: Object,
    required: false,
    default: undefined
  }
};

const emits = [
  'update:visible',
  'onSelect'
];

export default defineComponent({
  inheritAttrs: false, //
  props,
  emits,
  setup (props, { slots, emit }) {
    const options = computed(() => {
      return props.options;
    });
    const subOtions = ref([]);
    const position = reactive<{top?: string, left: string, bottom?: string}>({
      left: '0'
    });

    const showMenu = ref(false);

    const maxHeight = ref(500);

    let hoverItem:any = null;

    // 选择
    const onSelect = (item) => {
      if (item.disabled) {
        return;
      }
      const selectValue = item[props.valueKey] === hoverItem[props.valueKey] ? [item] : [hoverItem, item];
      emit('onSelect', selectValue);
      emit('update:visible', false);
    };

    const onHover = (item) => {
      if (item.disabled) {
        return;
      }
      hoverItem = item;
      subOtions.value = (item[props.childKey] || []).filter(i => !!i[props.valueKey]);
    };

    // 自定义渲染 label
    const labelRender = (item) => {
      return slots?.labelRenderer?.(item) || item[props.labelKey];
    };

    const renderOptions = () => {
      return [
        options.value.map((item: any) => {
          return <button class={['block']} disabled={item.disabled}>
            <li class={[item[props.valueKey] === hoverItem?.[props.valueKey] && 'active-item', 'transition-all']} onClick={() => onSelect(item)} onMouseover={() => onHover(item)}>
            {labelRender(item)}

            <span class="ml-2">{!!item[props.childKey]?.filter(i => !!i[props.valueKey]).length && <Icon icon='icon-zhediejiantouyou'></Icon>}</span>
            </li>
            </button>;
        })
      ];
    };

    const renderSubOptions = () => {
      return [
        subOtions.value.map((item: any) => {
          return <button class="block" disabled={item.disabled}><li
          onClick={() => onSelect(item)}
          >{labelRender(item)}</li></button>;
        })
      ];
    };

    const setPosition = () => {
      if (props.position) {
        position.left = props.position.positionX + 2 + 'px';
        position.top = props.position.positionY + 'px';
        let wrapperHeight = Math.min(500, options.value?.length * 26);
        const toTop = props.position.positionY;
        const toBottom = document.documentElement.clientHeight - toTop;
        maxHeight.value = 500;
        if (wrapperHeight > toTop && wrapperHeight > toBottom) {
          wrapperHeight = Math.max(toTop, toBottom);
          maxHeight.value = wrapperHeight;
          if (toTop > toBottom) {
            // position.top = 0 + 'px';
            position.bottom = toBottom + 'px';
            delete position.top;
          } else {
            position.top = toTop + 'px';
          }
        } else if (wrapperHeight < toTop && wrapperHeight < toBottom) {
          position.top = toTop + 'px';
        } else {
          if (toTop > toBottom) {
            // position.top = toTop - wrapperHeight + 'px';
            position.bottom = toBottom + 'px';
            delete position.top;
          } else {
            position.top = toTop + 'px';
          }
        }
      }
    };

    const listenFunc = () => {
      if (!props.visible) {
        return;
      }
      showMenu.value = false;
      setTimeout(() => {
        emit('update:visible', false);
      }, 160);
    };

    watch(() => props.visible, newValue => {
      if (newValue) {
        setTimeout(() => {
          showMenu.value = true;
        }, 160);
        if (props.position) {
          setPosition();
        }
      }
    }, {
      immediate: true
    });

    watch(() => options.value, () => {
      subOtions.value = [];
      hoverItem = null;
    });

    watch(() => props.position, () => {
      subOtions.value = [];
      hoverItem = null;
      if (props.visible) {
        showMenu.value = false;
        setTimeout(() => {
          setTimeout(() => {
            showMenu.value = true;
          }, 160);
          setPosition();
        }, 160);
      } else {
        setPosition();
      }
    }, {
      deep: true
    });

    onMounted(() => {
      document.addEventListener('click', listenFunc, true);
      // document.addEventListener('contextmenu', listenFunc, true);
    });
    onBeforeUnmount(() => {
      document.removeEventListener('click', listenFunc);
      // document.removeEventListener('contextmenu', listenFunc);
    });

    return () => [
    <Teleport to="body">
      {props.visible && <div class={['casecader-wrapper flex text-3 transition-all', showMenu.value ? 'opacity-100' : 'opacity-0']} style={{ ...position, maxHeight: (showMenu.value ? maxHeight.value : 0) + 'px' }}>
        <ul class={{ '!overflow-hidden': !showMenu.value, hidden: !options.value.length }}>
        {renderOptions()}
        </ul>
        {!!subOtions?.value?.length && <ul style={{ maxHeight: maxHeight.value + 'px' }}>
        {renderSubOptions()}
          </ul>
        }
      </div>}
    </Teleport>
    ];
  }
});
