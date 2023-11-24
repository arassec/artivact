import { app, BrowserWindow } from 'electron';
import path from 'path';
import os from 'os';

// needed in case process is undefined under Linux
const platform = process.platform || os.platform();

let mainWindow;

// Artivact: Used to kill the backend on application close.
let kill = require('tree-kill');
// Artivact: ---

function createWindow() {
  /**
   * Initial window options
   */
  mainWindow = new BrowserWindow({
    icon: path.resolve(__dirname, 'icons/icon.png'), // tray icon
    width: 1000,
    height: 600,
    useContentSize: true,
    webPreferences: {
      contextIsolation: true,
      // More info: https://v2.quasar.dev/quasar-cli-vite/developing-electron-apps/electron-preload-script
      preload: path.resolve(__dirname, process.env.QUASAR_ELECTRON_PRELOAD),
    },
  });

  mainWindow.loadURL(process.env.APP_URL);

  if (process.env.DEBUGGING) {
    // if on DEV or Production with debug enabled
    mainWindow.webContents.openDevTools();
  } else {
    // we're on production; no access to devtools pls
    mainWindow.webContents.on('devtools-opened', () => {
      mainWindow.webContents.closeDevTools();
    });

    // Artivact: Start Spring-Boot backend
    // TODO: Copy JAR during build and rename to artivact-vault.jar!
    var jarPath = path.join(
      process.resourcesPath,
      'artivact-vault-1.0.0-SNAPSHOT.jar'
    );
    var child = require('child_process').spawn('java', [
      '-Dserver.port=51232',
      '-Dspring.profiles.active=desktop',
      '-jar',
      jarPath,
      '',
    ]);
    // Artivact: ---
  }

  mainWindow.on('closed', () => {
    // Artivact: Stop Spring-Boot backend
    if (!process.env.DEBUGGING) {
      kill(child.pid);
    }
    // Artivact: ---
    mainWindow = null;
  });
}

app.whenReady().then(createWindow);

app.on('window-all-closed', () => {
  if (platform !== 'darwin') {
    app.quit();
  }
});

app.on('activate', () => {
  if (mainWindow === null) {
    createWindow();
  }
});
