import {defineConfig} from 'vitepress'

// https://vitepress.dev/reference/site-config
export default defineConfig({
    base: '/artivact/',
    title: 'Artivact Documentation',
    description: 'Documentation of the Artivact project.',
    themeConfig: {
        // https://vitepress.dev/reference/default-theme-config
        logo: '/logo.png',
        siteTitle: 'Artivact v##VERSION##',

        nav: [
            {text: 'Home', link: '/'},
            {text: 'Quick Start', link: '/quick-start/local-installation/introduction'},
            {text: 'User Manual', link: '/user-manual/introduction/about'},
            {text: 'Tutorials', link: '/tutorials/artivact-as-scanner/introduction'},
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
                {
                    text: 'Metaverse Installation',
                    items: [
                        {text: 'Introduction', link: '/quick-start/metaverse-installation/introduction'},
                        {text: 'Installation', link: '/quick-start/metaverse-installation/installation'},
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
                    text: 'Content Management',
                    items: [
                        {text: 'Introduction', link: '/user-manual/content-management/introduction'},
                        {text: 'Menus', link: '/user-manual/content-management/menus'},
                        {text: 'Pages', link: '/user-manual/content-management/pages'},
                    ]
                },
            ],
            '/tutorials/': [
                {
                    text: 'Artivact as 3D-Scanner',
                    items: [
                        {text: 'Introduction', link: '/tutorials/artivact-as-scanner/introduction'},
                        {text: 'Capturing Images', link: '/tutorials/artivact-as-scanner/capturing-images'},
                        {text: 'Artivact Turntable', link: '/tutorials/artivact-as-scanner/artivact-turntable'},
                        {text: 'Background Removal', link: '/tutorials/artivact-as-scanner/background-removal'},
                        {text: 'Model Creation', link: '/tutorials/artivact-as-scanner/model-creation'},
                        {text: 'Model Editing', link: '/tutorials/artivact-as-scanner/model-editing'},
                    ]
                }
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
                    {text: 'Tutorials', link: '/de/tutorials/artivact-as-scanner/introduction'},
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
                        {
                            text: 'Metaverse Installation',
                            items: [
                                {text: 'Einführung', link: '/de/quick-start/metaverse-installation/introduction'},
                                {text: 'Installation', link: '/de/quick-start/metaverse-installation/installation'},
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
                            text: 'Content-Management',
                            items: [
                                {text: 'Einführung', link: '/de/user-manual/content-management/introduction'},
                                {text: 'Menus', link: '/de/user-manual/content-management/menus'},
                                {text: 'Seiten', link: '/de/user-manual/content-management/pages'},
                            ]
                        },
                    ],
                    '/de/tutorials/': [
                        {
                            text: 'Artivact als 3D-Scanner',
                            items: [
                                {text: 'Einführung', link: '/de/tutorials/artivact-as-scanner/introduction'},
                                {text: 'Fotos Aufnehmen', link: '/de/tutorials/artivact-as-scanner/capturing-images'},
                                {text: 'Artivact Drehteller', link: '/de/tutorials/artivact-as-scanner/artivact-turntable'},
                                {text: 'Fotos Freistellen', link: '/de/tutorials/artivact-as-scanner/background-removal'},
                                {text: '3D-Modell Erzeugung', link: '/de/tutorials/artivact-as-scanner/model-creation'},
                                {text: '3D-Modell Bearbeitung', link: '/de/tutorials/artivact-as-scanner/model-editing'},
                            ]
                        }
                    ],
                }
            }
        }
    },

})
