import {route} from 'quasar/wrappers';
import {createMemoryHistory, createRouter, createWebHashHistory, createWebHistory,} from 'vue-router';
import routes from './routes';

/*
 * If not building with SSR mode, you can
 * directly export the Router instantiation;
 *
 * The function below can be async too; either use
 * async/await or return a Promise which resolves
 * with the Router instance.
 */

export default route(function (/* { store, ssrContext } */) {
  const createHistory = process.env.SERVER
    ? createMemoryHistory
    : process.env.VUE_ROUTER_MODE === 'history'
    ? createWebHistory
    : createWebHashHistory;

  return createRouter({
    scrollBehavior(to, from, savedPosition) {
      if (to && to.hash) {
        setTimeout(() => {
          document.getElementById(to.hash.replace('#', '')).scrollIntoView({
            behavior: "instant", block: "start"
          });
        }, 500)
      } else if (savedPosition) {
        setTimeout(() => {
          return savedPosition;
        }, 500)
      } else {
        return {left: 0, top: 0};
      }
    },
    routes,

    // Leave this as is and make changes in quasar.conf.js instead!
    // quasar.conf.js -> build -> vueRouterMode
    // quasar.conf.js -> build -> publicPath
    history: createHistory(process.env.VUE_ROUTER_BASE),
  });
});
