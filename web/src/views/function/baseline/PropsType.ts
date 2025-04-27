export type BaselineInfo = {
  establishedFlag: boolean;
  name: string;
  id?: string;
  description?: string;
}

export type IPane = {
  _id: string;// pane唯一标识
  name: string; // pane的tab文案
  value: string;// pane内部的组件标识
  closable?: boolean;// 是否允许关闭，true - 允许关闭，false - 禁止关闭
  forceRender?: boolean;// 被隐藏时是否渲染 DOM 结构，可选
  icon?: string;// tab文案前面的icon，可选
  active?: boolean; // 是否选中，添加不用设置，缓存时用于记录上次激活的tab pane，可选
  // 组件需要的属性
  data?: { [key: string]: any; };
};
