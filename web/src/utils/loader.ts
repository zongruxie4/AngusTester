import { utils } from '@xcan-angus/tools';

type callbackFn = () => void;
interface IStateObj {
  linkSrc?: string;
  scriptSrc?: string;
  linkId: string;
  linkLoaded: boolean;
  scriptId: string;
  scriptLoaded: boolean;
  listeners: callbackFn[];
}

interface ILoader {
  loadScript: (callback: callbackFn) => void;
  loadLink: () => void;
}

const createState = (scriptSrc?:string, linkSrc?:string): IStateObj => ({
  scriptSrc,
  linkSrc,
  linkId: utils.uuid('link'),
  linkLoaded: false,
  scriptId: utils.uuid('script'),
  scriptLoaded: false,
  listeners: []
});

const createLoader = (scriptSrc?:string, linkSrc?:string): ILoader => {
  const state: IStateObj = createState(scriptSrc, linkSrc);
  const injectScriptTag = (id:string, src: string, callback: callbackFn) => {
    const scriptTag = document.createElement('script');
    scriptTag.referrerPolicy = 'origin';
    scriptTag.type = 'application/javascript';
    scriptTag.async = true;
    scriptTag.id = id;
    scriptTag.src = src;

    const handler = () => {
      scriptTag.removeEventListener('load', handler);
      callback();
    };
    scriptTag.addEventListener('load', handler);
    if (document.body) {
      document.body.appendChild(scriptTag);
    }
  };

  const injectLinkTag = (id:string, src: string, callback: callbackFn) => {
    const linkTag = document.createElement('link');
    linkTag.rel = 'stylesheet';
    linkTag.type = 'text/css';
    linkTag.id = id;
    linkTag.href = src;

    const handler = () => {
      linkTag.removeEventListener('load', handler);
      callback();
    };
    linkTag.addEventListener('load', handler);
    if (document.body) {
      document.body.appendChild(linkTag);
    }
  };

  const loadScript = (callback: callbackFn) => {
    if (!state.scriptSrc) {
      return;
    }

    if (state.scriptLoaded) {
      callback();
      return;
    }

    state.listeners.push(callback);
    if (!document.getElementById(state.scriptId)) {
      injectScriptTag(state.scriptId, state.scriptSrc, () => {
        state.listeners.forEach((fn) => fn());
        state.scriptLoaded = true;
      });
    }
  };

  const loadLink = () => {
    if (!state.linkSrc || state.linkLoaded) {
      return;
    }

    if (!document.getElementById(state.linkId)) {
      injectLinkTag(state.linkId, state.linkSrc, () => {
        state.linkLoaded = true;
      });
    }
  };

  return {
    loadScript,
    loadLink
  };
};

export { createLoader };
