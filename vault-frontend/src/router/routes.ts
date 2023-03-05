import {RouteRecordRaw} from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('layouts/MainLayout.vue'),
    children: [
      {path: '', component: () => import('pages/IndexPage.vue')},
      {path: '/user-login', component: () => import('pages/LoginPage.vue')},
      {path: '/artivact/:artivactId?', component: () => import('pages/ArtivactDetailsPage.vue')},
      {path: '/account', component: () => import('pages/AccountSettingsPage.vue')},
      {path: '/administration/accounts', component: () => import('pages/AccountsConfigurationPage.vue')},
      {path: '/administration/filesystem/scan', component: () => import('pages/FilesystemScanConfigurationPage.vue')},
      {path: '/administration/configuration/artivact/:artivactId?', component: () => import('pages/ArtivactEditPage.vue')},
      {path: '/administration/configuration/properties/edit', component: () => import('pages/PropertiesConfigurationEditPage.vue')},
      {path: '/administration/configuration/tags/edit', component: () => import('pages/TagsConfigurationEditPage.vue')},
      {path: '/administration/configuration/license/edit', component: () => import('pages/LicenseConfigurationEditPage.vue')},
      {path: '/administration/configuration/menu/edit', component: () => import('pages/MenuConfigurationPage.vue')},
      //{path: 'about', component: () => import('pages/IndexPage.vue')},
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
