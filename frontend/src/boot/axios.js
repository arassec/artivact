import { boot } from 'quasar/wrappers';
import axios from 'axios';
import { useLocaleStore } from 'stores/locale';

const api = axios.create({ baseURL: '/' });

export default boot(({ app }) => {
  api.interceptors.request.use((request) => {
    const localeStore = useLocaleStore();
    if (localeStore.selectedLocale !== null) {
      request.headers.set('Accept-Language', localeStore.selectedLocale);
    }
    return request;
  });

  app.config.globalProperties.$api = api;
  // ^ ^ ^ this will allow you to use this.$api (for Vue Options API form)
  //       so you can easily perform requests against your app's API
});

export { axios, api };
