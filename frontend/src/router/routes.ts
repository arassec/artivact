import {RouteRecordRaw} from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('layouts/MainLayout.vue'),
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
