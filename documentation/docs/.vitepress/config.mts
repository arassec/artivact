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
            {text: 'Create', link: '/desktop/introduction/about'},
            {text: 'Present', link: '/server/about'},
            {text: 'Experience', link: '/metaverse/about'},
            {text: 'User Manual', link: '/user-manual/about'},
        ],

        sidebar: {
            '/desktop/': [
                {
                    text: 'Introduction',
                    items: [
                        {text: 'About', link: '/desktop/introduction/about'},
                        {text: 'Installation', link: '/desktop/introduction/installation'},
                        {text: 'Configuration', link: '/desktop/introduction/configuration'},
                    ]
                },
                {
                    text: 'User Manual',
                    items: [
                        {text: 'Manual', link: '/desktop/manual/manual'},
                        {text: 'Media Creation', link: '/desktop/manual/media-creation'},
                        {text: 'Peripherals', link: '/desktop/manual/peripherals'},
                    ]
                },
                {
                    text: '3D Model Creation',
                    items: [
                        {text: 'About', link: '/desktop/models/about'},
                        {text: 'Capturing Images', link: '/desktop/models/capturing-images'},
                        {text: 'Artivact Turntable', link: '/desktop/models/artivact-turntable'},
                        {text: 'Background Removal', link: '/desktop/models/background-removal'},
                        {text: 'Model Creation', link: '/desktop/models/model-creation'},
                        {text: 'Model Editing', link: '/desktop/models/model-editing'},
                    ]
                }
            ],
            '/server/': [
                {
                    text: 'Introduction',
                    items: [
                        {text: 'About', link: '/server/about'},
                        {text: 'Installation', link: '/server/installation'},
                        {text: 'Configuration', link: '/server/configuration'},
                        {text: 'User Manual', link: '/server/manual'},
                    ]
                }
            ],
            '/metaverse/': [
                {
                    text: 'Introduction',
                    items: [
                        {text: 'About', link: '/metaverse/about'},
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
                    {text: 'Erstelle', link: '/de/desktop/introduction/about'},
                    {text: 'Präsentiere', link: '/de/server/about'},
                    {text: 'Erlebe', link: '/de/metaverse/about'},
                    {text: 'Benutzerhandbuch', link: '/de/user-manual/about'},
                ],

                sidebar: {
                    '/de/desktop/': [
                        {
                            text: 'Einführung',
                            items: [
                                {text: 'Start', link: '/de/desktop/introduction/about'},
                                {text: 'Installation', link: '/de/desktop/introduction/installation'},
                                {text: 'Konfiguration', link: '/de/desktop/introduction/configuration'},
                            ]
                        },
                        {
                            text: 'Benutzerhandbuch',
                            items: [
                                {text: 'Start', link: '/de/desktop/manual/manual'},
                                {text: 'Gestaltung', link: '/de/desktop/manual/media-creation'},
                                {text: 'Peripheriegeräte', link: '/de/desktop/manual/peripherals'},
                            ]
                        },
                        {
                            text: '3D-Modelle Erstellen',
                            items: [
                                {text: 'Start', link: '/de/desktop/models/about'},
                                {text: 'Fotos Aufnehmen', link: '/de/desktop/models/capturing-images'},
                                {text: 'Artivact Drehteller', link: '/de/desktop/models/artivact-turntable'},
                                {text: 'Freistellen', link: '/de/desktop/models/background-removal'},
                                {text: '3D-Modell Erzeugung', link: '/de/desktop/models/model-creation'},
                                {text: '3D-Modell Bearbeitung', link: '/de/desktop/models/model-editing'},
                            ]
                        }
                    ],
                    '/de/server/': [
                        {
                            text: 'Einführung',
                            items: [
                                {text: 'Über', link: '/de/server/about'},
                                {text: 'Installation', link: '/de/server/installation'},
                                {text: 'Konfiguration', link: '/de/server/configuration'},
                                {text: 'Benutzerhandbuch', link: '/de/server/manual'},
                            ]
                        }
                    ],
                    '/de/metaverse/': [
                        {
                            text: 'Einführung',
                            items: [
                                {text: 'Über', link: '/de/metaverse/about'},
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
