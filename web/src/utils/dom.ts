import { debounce } from 'throttle-debounce';
import { localStore } from '@xcan-angus/infra';

class Scroll {
    private element: HTMLElement;
    private storageKey: string;
    private addFlag : boolean;// 是否已经启动监听滚动事件
    constructor (element: HTMLElement, storageKey: string) {
      this.element = element;
      this.storageKey = storageKey;
      this.addFlag = false;
    }

    bootstrap ():void {
      const cacheTop = localStore.get(this.storageKey);
      if (cacheTop) {
        setTimeout(() => {
          this.element.scrollTo(0, +cacheTop);
        }, 2000);
      }
      // 启动监听事件
      if (this.addFlag) {
        return;
      }
      this.add();
    }

    handler (e:Event):void {
      const scrollTop = (e.target as HTMLElement).scrollTop + '';
      localStore.set(this.storageKey, scrollTop);
    }

    add ():void {
      this.element.addEventListener('scroll', debounce(50, (e) => { this.handler(e); }));
      this.addFlag = true;
    }

    remove ():void {
      this.element.removeEventListener('scroll', this.handler);
      this.addFlag = false;
    }
}

export { Scroll };
