export type ButtonGroupMenuKey = 'select'|'test'|'debug'|'export'|'import'|'codeView'|'pageView'|'permission'|'follow'|'cancelFollow'|'favourite'|'cancelFavourite'|'refresh'|'save'

export type ButtonGroupMenuItem = {
    name: string;
    icon: string;
    key: ButtonGroupMenuKey;
}
