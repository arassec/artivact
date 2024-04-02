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
            {text: 'Create', link: '/create/introduction/about'},
            {text: 'Present', link: '/present/about'},
            {text: 'Experience', link: '/experience/about'},
            {text: 'User Manual', link: '/user-manual/about'},
        ],

        sidebar: {
            '/create/': [
                {
                    text: 'Introduction',
                    items: [
                        {text: 'About', link: '/create/introduction/about'},
                        {text: 'Installation', link: '/create/introduction/installation'},
                        {text: 'Configuration', link: '/create/introduction/configuration'},
                    ]
                },
                {
                    text: 'User Manual',
                    items: [
                        {text: 'Manual', link: '/create/manual/manual'},
                        {text: 'Media Creation', link: '/create/manual/media-creation'},
                        {text: 'Peripherals', link: '/create/manual/peripherals'},
                    ]
                },
                {
                    text: '3D Model Creation',
                    items: [
                        {text: 'About', link: '/create/models/about'},
                        {text: 'Capturing Images', link: '/create/models/capturing-images'},
                        {text: 'Artivact Turntable', link: '/create/models/artivact-turntable'},
                        {text: 'Background Removal', link: '/create/models/background-removal'},
                        {text: 'Model Creation', link: '/create/models/model-creation'},
                        {text: 'Model Editing', link: '/create/models/model-editing'},
                    ]
                }
            ],
            '/present/': [
                {
                    text: 'Introduction',
                    items: [
                        {text: 'About', link: '/present/about'},
                        {text: 'Installation', link: '/present/installation'},
                        {text: 'Configuration', link: '/present/configuration'},
                        {text: 'User Manual', link: '/present/manual'},
                    ]
                }
            ],
            '/experience/': [
                {
                    text: 'Introduction',
                    items: [
                        {text: 'About', link: '/experience/about'},
                    ]
                }
            ],
            '/user-manual/': [
                {
                    text: 'Introduction',
                    items: [
                        {text: 'About', link: '/user-manual/about'},
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
                    {text: 'Start', link: '/de/index'},
                    {text: 'Erstelle', link: '/de/create/introduction/about'},
                    {text: 'Präsentiere', link: '/de/present/about'},
                    {text: 'Erlebe', link: '/de/experience/about'},
                    {text: 'Benutzerhandbuch', link: '/de/user-manual/about'},
                ],

                sidebar: {
                    '/de/create/': [
                        {
                            text: 'Einführung',
                            items: [
                                {text: 'Start', link: '/de/create/introduction/about'},
                                {text: 'Installation', link: '/de/create/introduction/installation'},
                                {text: 'Konfiguration', link: '/de/create/introduction/configuration'},
                            ]
                        },
                        {
                            text: 'Benutzerhandbuch',
                            items: [
                                {text: 'Start', link: '/de/create/manual/manual'},
                                {text: 'Gestaltung', link: '/de/create/manual/media-creation'},
                                {text: 'Peripheriegeräte', link: '/de/create/manual/peripherals'},
                            ]
                        },
                        {
                            text: '3D-Modelle Erstellen',
                            items: [
                                {text: 'Start', link: '/de/create/models/about'},
                                {text: 'Fotos Aufnehmen', link: '/de/create/models/capturing-images'},
                                {text: 'Artivact Drehteller', link: '/de/create/models/artivact-turntable'},
                                {text: 'Freistellen', link: '/de/create/models/background-removal'},
                                {text: '3D-Modell Erzeugung', link: '/de/create/models/model-creation'},
                                {text: '3D-Modell Bearbeitung', link: '/de/create/models/model-editing'},
                            ]
                        }
                    ],
                    '/de/present/': [
                        {
                            text: 'Einführung',
                            items: [
                                {text: 'Über', link: '/de/present/about'},
                                {text: 'Installation', link: '/de/present/installation'},
                                {text: 'Konfiguration', link: '/de/present/configuration'},
                                {text: 'Benutzerhandbuch', link: '/de/present/manual'},
                            ]
                        }
                    ],
                    '/de/experience/': [
                        {
                            text: 'Einführung',
                            items: [
                                {text: 'Über', link: '/de/experience/about'},
                            ]
                        }
                    ],
                    '/de/user-manual/': [
                        {
                            text: 'Einführung',
                            items: [
                                {text: 'Über', link: '/de/user-manual/about'},
                            ]
                        }
                    ],
                }
            }
        }
    },

})
