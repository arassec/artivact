import {defineConfig} from 'vitepress'

// https://vitepress.dev/reference/site-config
export default defineConfig({
    base: '/artivact/',
    title: 'Artivact Documentation',
    description: 'Documentation of the Artivact application.',
    head: [
        ['link', { rel: 'icon', href: '/artivact/favicon.ico', sizes: 'any' }],
    ],
    themeConfig: {
        // https://vitepress.dev/reference/default-theme-config
        siteTitle: 'Artivact v##VERSION##',

        nav: [
            {text: 'Home', link: '/'},
            {text: 'Quick Start', link: '/quick-start/local-installation/introduction'},
            {text: 'User Manual', link: '/user-manual/introduction/about'},
        ],

        sidebar: {
            '/quick-start/': [
                {
                    text: 'Local Installation',
                    items: [
                        {text: 'Introduction', link: '/quick-start/local-installation/introduction'},
                        {text: 'Installation', link: '/quick-start/local-installation/installation'},
                        {text: 'Configuration', link: '/quick-start/local-installation/configuration'},
                    ]
                },
                {
                    text: 'Server Installation',
                    items: [
                        {text: 'Introduction', link: '/quick-start/server-installation/introduction'},
                        {text: 'Installation', link: '/quick-start/server-installation/installation'},
                        {text: 'Configuration', link: '/quick-start/server-installation/configuration'},
                    ]
                },
            ],
            '/user-manual/': [
                {
                    text: 'Introduction',
                    items: [
                        {text: 'About', link: '/user-manual/introduction/about'},
                        {text: 'Changelog', link: '/user-manual/introduction/changelog'},
                        {text: 'License', link: '/user-manual/introduction/license'},
                    ]
                },
                {
                    text: 'Item Management',
                    items: [
                        {text: 'Introduction', link: '/user-manual/item-management/introduction'},
                        {text: 'Item Details Page', link: '/user-manual/item-management/item-details-page'},
                        {text: 'Item Editor', link: '/user-manual/item-management/item-editor'},
                    ]
                },
                {
                    text: 'Content Management',
                    items: [
                        {text: 'Introduction', link: '/user-manual/content-management/introduction'},
                        {text: 'Menus', link: '/user-manual/content-management/menus'},
                        {text: 'Pages', link: '/user-manual/content-management/pages'},
                        {text: 'Widgets', link: '/user-manual/content-management/widgets'},
                        {text: 'I18N', link: '/user-manual/content-management/internationalization'},
                    ]
                },
                {
                    text: 'System Settings',
                    items: [
                        {text: 'Introduction', link: '/user-manual/settings/introduction'},
                        {text: 'Properties', link: '/user-manual/settings/properties'},
                        {text: 'Tags', link: '/user-manual/settings/tags'},
                        {text: 'Exchange', link: '/user-manual/settings/exchange'},
                        {text: 'Appearance', link: '/user-manual/settings/appearance'},
                        {text: 'Peripherals', link: '/user-manual/settings/peripherals'},
                    ]
                },
                {
                    text: 'Account Management',
                    items: [
                        {text: 'Accounts & Roles', link: '/user-manual/account-management/accounts'},
                    ]
                },
            ],
        },

        socialLinks: [
            {icon: 'github', link: 'https://github.com/arassec/artivact'}
        ],

    },

    locales: {
        root: {
            label: 'English',
            lang: 'en'
        },
        de: {
            label: 'Deutsch',
            lang: 'de',
            description: 'Dokumentation der Artivact-Anwendung.',
            themeConfig: {

                nav: [
                    {text: 'Start', link: '/de/'},
                    {text: 'Schnelleinstieg', link: '/de/quick-start/local-installation/introduction'},
                    {text: 'Benutzerhandbuch', link: '/de/user-manual/introduction/about'},
                ],

                sidebar: {
                    '/de/quick-start/': [
                        {
                            text: 'Lokale Installation',
                            items: [
                                {text: 'Einführung', link: '/de/quick-start/local-installation/introduction'},
                                {text: 'Installation', link: '/de/quick-start/local-installation/installation'},
                                {text: 'Konfiguration', link: '/de/quick-start/local-installation/configuration'},
                            ]
                        },
                        {
                            text: 'Server Installation',
                            items: [
                                {text: 'Einführung', link: '/de/quick-start/server-installation/introduction'},
                                {text: 'Installation', link: '/de/quick-start/server-installation/installation'},
                                {text: 'Konfiguration', link: '/de/quick-start/server-installation/configuration'},
                            ]
                        },
                    ],
                    '/de/user-manual/': [
                        {
                            text: 'Einführung',
                            items: [
                                {text: 'Über Artivact', link: '/de/user-manual/introduction/about'},
                                {text: 'Changelog', link: '/de/user-manual/introduction/changelog'},
                                {text: 'Lizenz', link: '/de/user-manual/introduction/license'},
                            ]
                        },
                        {
                            text: 'Sammlungsstücke Verwalten',
                            items: [
                                {text: 'Einführung', link: '/de/user-manual/item-management/introduction'},
                                {text: 'Objekt-Detailseite', link: '/de/user-manual/item-management/item-details-page'},
                                {text: 'Objekt-Editor', link: '/de/user-manual/item-management/item-editor'},
                            ]
                        },
                        {
                            text: 'Content-Management',
                            items: [
                                {text: 'Einführung', link: '/de/user-manual/content-management/introduction'},
                                {text: 'Menüs', link: '/de/user-manual/content-management/menus'},
                                {text: 'Seiten', link: '/de/user-manual/content-management/pages'},
                                {text: 'Widgets', link: '/de/user-manual/content-management/widgets'},
                                {text: 'I18N', link: '/de/user-manual/content-management/internationalization'},
                            ]
                        },
                        {
                            text: 'Einstellungen',
                            items: [
                                {text: 'Einführung', link: '/de/user-manual/settings/introduction'},
                                {text: 'Eigenschaften', link: '/de/user-manual/settings/properties'},
                                {text: 'Tags', link: '/de/user-manual/settings/tags'},
                                {text: 'Austausch', link: '/de/user-manual/settings/exchange'},
                                {text: 'Darstellung', link: '/de/user-manual/settings/appearance'},
                                {text: 'Peripheriegeräte', link: '/de/user-manual/settings/peripherals'},
                            ]
                        },
                        {
                            text: 'Account-Management',
                            items: [
                                {text: 'Accounts & Rollen', link: '/de/user-manual/account-management/accounts'},
                            ]
                        },
                    ],
                }
            }
        }
    },
})
