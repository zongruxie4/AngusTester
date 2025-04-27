export type ButtonGroupMenuKey = 'select'|'test'|'debug'|'export'|'import'|'codeView'|'UIView'|'authority'|'followFlag'|'cancelFollowFlag'|'favouriteFlag'|'cancelFavouriteFlag'|'refresh'|'save'

export type ButtonGroupMenuItem = {
    name: string;
    icon: string;
    key: ButtonGroupMenuKey;
}
