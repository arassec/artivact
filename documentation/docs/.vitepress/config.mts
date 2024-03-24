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
            {text: 'On the Desktop', link: '/desktop/about'},
            {text: 'On the Web', link: '/server/about'},
            {text: 'In the Metaverse', link: '/metaverse/about'},
            {text: 'User Manual', link: '/user-manual/about'},
        ],

        sidebar: {
            '/desktop/': [
                {
                    text: 'Introduction',
                    items: [
                        {text: 'About', link: '/desktop/about'},
                        {text: 'Installation', link: '/desktop/installation'},
                        {text: 'Configuration', link: '/desktop/configuration'},
                        {text: 'User Manual', link: '/desktop/manual'},
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
                    {text: 'Auf dem Desktop', link: '/de/desktop/about'},
                    {text: 'Im Web', link: '/de/server/about'},
                    {text: 'Im Metaverse', link: '/de/metaverse/about'},
                    {text: 'Benutzerhandbuch', link: '/de/user-manual/about'},
                ],

                sidebar: {
                    '/de/desktop/': [
                        {
                            text: 'Einführung',
                            items: [
                                {text: 'Über', link: '/de/desktop/about'},
                                {text: 'Installation', link: '/de/desktop/installation'},
                                {text: 'Konfiguration', link: '/de/desktop/configuration'},
                                {text: 'Benutzerhandbuch', link: '/de/desktop/manual'},
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
