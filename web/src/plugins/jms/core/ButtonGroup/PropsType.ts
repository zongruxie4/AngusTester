export type ButtonGroupMenuKey = 'select'|'test'|'debug'|'export'|'import'|'codeView'|'UIView'|'authority'|'follow'|'cancelFollow'|'favourite'|'cancelFavourite'|'refresh'|'save'

export type ButtonGroupMenuItem = {
    name: string;
    icon: string;
    key: ButtonGroupMenuKey;
}
