export type IPane = {
    [key: string]: any;
} & {
    tab: string;// pane 的tab文案
    key: string;// pane 的key，唯一标识
    closable: boolean;// 是否允许关闭，true - 允许关闭，false - 禁止关闭
    forceRender?: boolean;// 被隐藏时是否渲染 DOM 结构，可选
    icon?: string;// tab文案前面的icon，可选
    active?: boolean; // 是否选中，添加不用设置，缓存时用于记录上次激活的tab pane，可选
}

// 接口所有权限
export const API_AUTH_CODE = [
  'MODIFY', 'DEBUG', 'SHARE', 'VIEW', 'DELETE', 'TEST', 'GRANT', 'RELEASE', 'EXPORT'
];
