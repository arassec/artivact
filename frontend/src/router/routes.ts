import {RouteRecordRaw} from 'vue-router';
import {api} from 'boot/axios';
import {setCssVar, useQuasar} from 'quasar';
import {useUserdataStore} from 'stores/userdata';
import {Profiles} from 'components/artivact-models';
import {useProfilesStore} from 'stores/profiles';
import {useApplicationSettingsStore} from 'stores/application-settings';
import {useLocaleStore} from 'stores/locale';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('layouts/MainLayout.vue'),
    beforeEnter: (to, from, next) => {

      const quasar = useQuasar();

      const applicationSettingsStore = useApplicationSettingsStore();
      const localeStore = useLocaleStore();
      const profilesStore = useProfilesStore();
      const userdataStore = useUserdataStore();

      api
        .get('/api/configuration/public/settings')
        .then((response) => {

          applicationSettingsStore.setSettings(response.data);

          localeStore.setAvailableLocales(response.data.availableLocales);

          document.title = response.data.applicationTitle;

          const colorTheme = response.data.colorTheme;
          setCssVar('primary', colorTheme.primary);
          setCssVar('secondary', colorTheme.secondary);
          setCssVar('accent', colorTheme.accent);
          setCssVar('dark', colorTheme.dark);
          setCssVar('positive', colorTheme.positive);
          setCssVar('negative', colorTheme.negative);
          setCssVar('info', colorTheme.info);
          setCssVar('warning', colorTheme.warning);

          const profiles: Profiles = response.data;
          profilesStore.setE2eModeEnabled(profiles.e2e)
          profilesStore.setDesktopModeEnabled(profiles.desktop || profiles.e2e);
          profilesStore.setServerModeEnabled(!profiles.desktop || profiles.e2e);

          if (profiles.desktop || profiles.e2e) {
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
      {path: '/administration/configuration/item/:itemId?', component: () => import('pages/ItemEditPage.vue')},
      {path: '/administration/configuration/properties', component: () => import('pages/PropertiesConfigurationPage.vue')},
      {path: '/administration/configuration/tags', component: () => import('pages/TagsConfigurationPage.vue')},
      {path: '/administration/configuration/appearance', component: () => import('pages/AppearanceConfigurationPage.vue')},
      {path: '/administration/configuration/peripherals', component: () => import('pages/PeripheralsConfigurationPage.vue')},
      {path: '/administration/configuration/exchange', component: () => import('pages/ExchangeConfigurationPage.vue')},
      {path: '/administration/configuration/search', component: () => import('pages/SearchConfigurationPage.vue')},
    ],
  },

  // Always leave this as last one,
  // but you can also remove it
  {
    path: '/:catchAll(.*)*',
    component: () => import('pages/ErrorNotFoundPage.vue'),
  },
];

export default routes;
