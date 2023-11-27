import {app, BrowserWindow} from 'electron';
import path from 'path';
import os from 'os';
import * as http from 'http';

// needed in case process is undefined under Linux
const platform = process.platform || os.platform();

let mainWindow;

// Artivact: +++
let kill = require('tree-kill');
let backendChildProcess;
let backendPort = 8080;

function timer(ms) {
  return new Promise(res => setTimeout(res, ms));
}

function startBackend() {
  backendPort = 51232;
  backendChildProcess = require('child_process').spawn('bin/java', [
    '-Dserver.port=' + backendPort,
    '-Dspring.profiles.active=desktop',
    '-jar',
    path.join(
      process.resourcesPath,
      'artivact-server.jar'
    ),
    '',
  ]);
}

async function callBackend() {
  return new Promise(function (resolve) {
    http.get('http://localhost:' + backendPort + '/api/configuration/public/locale', function () {
      console.log('Backend ready!');
      resolve(true);
    }).on('error', function (error) {
      console.log('Backend not available... (' + error.message + ')');
      resolve(false);
    })
  })
}

async function waitForBackend() {
  for (let i = 0; i < 30; i++) {
    await timer(1000);
    let ready = await callBackend();
    if (ready) {
      break;
    }
  }
}
// Artivact: ---

function createWindow() {
  /**
   * Initial window options
   */
  mainWindow = new BrowserWindow({
    icon: path.resolve(__dirname, 'icons/icon.png'), // tray icon
    width: 1024,
    height: 768,
    useContentSize: true,
    // Artivact: +++
    autoHideMenuBar: true,
    show: false,
    // Artivact: ---
    webPreferences: {
      contextIsolation: true,
      // More info: https://v2.quasar.dev/quasar-cli-vite/developing-electron-apps/electron-preload-script
      preload: path.resolve(__dirname, process.env.QUASAR_ELECTRON_PRELOAD),
    },
  });

  // Artivact: +++
  let splash = new BrowserWindow({
    width: 550,
    height: 200,
    transparent: true,
    frame: false,
    alwaysOnTop: true,
    focusable: false
  });
  const splashFile = path.resolve(__dirname, process.env.QUASAR_PUBLIC_FOLDER, 'splash.html')
  splash.loadFile(splashFile).then(() => console.log('Loaded splash.html'));
  splash.center();

  if (!process.env.DEBUGGING) {
    startBackend();
  }

  waitForBackend().then(() => {
    mainWindow.loadURL(process.env.APP_URL);
    if (process.env.DEBUGGING) {
      // if on DEV or Production with debug enabled
      mainWindow.webContents.openDevTools();
    } else {
      // we're on production; no access to devtools pls
      mainWindow.webContents.on('devtools-opened', () => {
        mainWindow.webContents.closeDevTools();
      });
    }
  });
  // Artivact: ---

  mainWindow.on('ready-to-show', () => {
    // Artivact: +++
    splash.close();
    // Artivact: ---
    mainWindow.show();
  })

  mainWindow.on('closed', () => {
    // Artivact: +++
    if (!process.env.DEBUGGING && backendChildProcess) {
      kill(backendChildProcess.pid);
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
