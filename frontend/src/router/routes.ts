import {RouteRecordRaw} from 'vue-router';
import {api} from 'boot/axios';
import {useQuasar} from 'quasar';
import {useDesktopStore} from 'stores/desktop';
import {useUserdataStore} from 'stores/userdata';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('layouts/MainLayout.vue'),
    beforeEnter: (to, from, next) => {

      const quasar = useQuasar();
      const desktopStore = useDesktopStore();
      const userdataStore = useUserdataStore();

      api
        .get('/api/configuration/public/desktop-mode')
        .then((response) => {
          const desktopModeEnabled = response.data;
          desktopStore.setEnabled(desktopModeEnabled);
          if (desktopModeEnabled) {
            const postdata = new URLSearchParams();
            postdata.append('username', 'desktop');
            postdata.append('password', '');

            api
              .post('/api/auth/login', postdata)
              .then(() => {
                api
                  .get('/api/configuration/public/user')
                  .then((response) => {
                    userdataStore.setUserdata(response.data);
                    next();
                  })
                  .catch(() => {
                    quasar.notify({
                      color: 'negative',
                      position: 'bottom',
                      message: 'Loading user data failed',
                      icon: 'report_problem',
                    });
                  });
              })
              .catch(() => {
                quasar.notify({
                  color: 'negative',
                  position: 'bottom',
                  message: 'Login failed',
                  icon: 'report_problem',
                });
              });
          } else {
            next();
          }
        })
        .catch(() => {
          quasar.notify({
            color: 'negative',
            position: 'bottom',
            message: 'Loading data failed',
            icon: 'report_problem',
          });
        });
    },
    children: [
      {path: '', component: () => import('pages/EditablePage.vue')},
      {path: '/page/:pageId?', component: () => import('pages/EditablePage.vue')},
      {path: '/user-login', component: () => import('pages/LoginPage.vue')},
      {path: '/item/:itemId?', component: () => import('pages/ItemDetailsPage.vue')},
      {path: '/account', component: () => import('pages/AccountSettingsPage.vue')},
      {path: '/administration/accounts', component: () => import('pages/AccountsConfigurationPage.vue')},
      {path: '/administration/item/import', component: () => import('pages/ItemImportPage.vue')},
      {path: '/administration/configuration/item/:itemId?', component: () => import('pages/ItemEditPage.vue')},
      {path: '/administration/configuration/properties', component: () => import('pages/PropertiesConfigurationPage.vue')},
      {path: '/administration/configuration/tags', component: () => import('pages/TagsConfigurationPage.vue')},
      {path: '/administration/configuration/license', component: () => import('pages/LicenseConfigurationPage.vue')},
      {path: '/administration/configuration/appearance', component: () => import('pages/AppearanceConfigurationPage.vue')},
      {path: '/administration/configuration/peripherals', component: () => import('pages/PeripheralsConfigurationPage.vue')},
      {path: '/administration/configuration/exhibitions', component: () => import('pages/ExhibitionsConfigurationPage.vue')},
    ],
  },

  // Always leave this as last one,
  // but you can also remove it
  {
    path: '/:catchAll(.*)*',
    component: () => import('pages/ErrorNotFound.vue'),
  },
];

export default routes;
