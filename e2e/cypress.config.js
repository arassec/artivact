const {defineConfig} = require('cypress');

module.exports = defineConfig({
    e2e: {
        setupNodeEvents(on, config) {
            // implement node event listeners here
        },
        env: {
            'locales': ['en', 'de']
        },
        trashAssetsBeforeRuns: true,
        watchForFileChanges: false,
        defaultCommandTimeout: 5000
    },
    viewportWidth: 1280,
    viewportHeight: 960,
});
