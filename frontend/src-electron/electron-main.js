import * as electron from 'electron';
import { app, BrowserWindow } from 'electron';
import path from 'node:path';
import os from 'node:os';
import { fileURLToPath } from 'node:url';
import * as http from 'http';
import * as child_process from 'node:child_process';

// needed in case process is undefined under Linux
const platform = process.platform || os.platform()

const currentDir = fileURLToPath(new URL('.', import.meta.url))

let mainWindow

// Artivact: +++
let splashWindow

// Disable the application menu, even for new window instances!
electron.Menu.setApplicationMenu(null);

let backendChildProcess;
let backendPort = 8080;

function timer(ms) {
  return new Promise(res => setTimeout(res, ms));
}

// Can be set during startup as command-line parameter, e.g. --artivact.project.root=C:\test-project
let projectRoot = app.commandLine.getSwitchValue('artivact.project.root');
if (projectRoot) {
  console.log('Project root set by command-line: ' + projectRoot);
} else {
  projectRoot = '${user.home}/.avdata'
}

function startBackend() {
  backendPort = 51232;
  backendChildProcess = child_process.spawn('bin/java', [
    '-Dserver.port=' + backendPort,
    '-Dspring.profiles.active=desktop',
    '-Dartivact.project.root=' + projectRoot,
    '-Dspring.datasource.url=jdbc:h2:file:' + projectRoot + '/dbdata/artivact;AUTO_SERVER=true',
    '-Dspring.datasource.username=artivact',
    '-Dspring.datasource.password=artivact',
    '-Dspring.datasource.driver-class-name=org.h2.Driver',
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

async function createWindow () {
  /**
   * Initial window options
   */
  mainWindow = new BrowserWindow({
    icon: path.resolve(currentDir, 'icons/icon.png'), // tray icon
    width: 1440,
    height: 1024,
    useContentSize: true,
    // Artivact: +++
    autoHideMenuBar: true,
    show: false,
    // Artivact: ---
    webPreferences: {
      contextIsolation: true,
      // More info: https://v2.quasar.dev/quasar-cli-vite/developing-electron-apps/electron-preload-script
      preload: path.resolve(
        currentDir,
        path.join(process.env.QUASAR_ELECTRON_PRELOAD_FOLDER, 'electron-preload' + process.env.QUASAR_ELECTRON_PRELOAD_EXTENSION)
      )
    }
  });

  // Artivact: +++
  mainWindow.removeMenu()

  splashWindow = new BrowserWindow({
    width: 500,
    height: 500,
    transparent: true,
    frame: false,
    alwaysOnTop: true,
    focusable: false,
  });
  const splashFile = path.resolve(currentDir, process.env.QUASAR_PUBLIC_FOLDER, 'splash.html')
  splashWindow.loadFile(splashFile).then(() => console.log('Loaded splash.html'));
  splashWindow.center();

  // Clean up resources when splash window is closed
  splashWindow.on('closed', () => {
    splashWindow.removeAllListeners()
    splashWindow = null
  })

  if (!process.env.DEBUGGING) {
    startBackend();
  } else {
    // Wait for splash window to show up...
    await timer(3000);
  }

  waitForBackend().then(() => {
    if (!process.env.DEBUGGING) {
      mainWindow.loadURL('http://localhost:' + backendPort + '/');
      // we're on production; no access to devtools pls
      mainWindow.webContents.on('devtools-opened', () => {
        mainWindow.webContents.closeDevTools();
      });
    } else {
      mainWindow.loadURL(process.env.APP_URL);
    }
  });

  mainWindow.once('ready-to-show', () => {
    splashWindow.close();
    mainWindow.show();
  })
  // Artivact: ---

  mainWindow.on('closed', () => {
    // Artivact: +++
    if (!process.env.DEBUGGING && backendChildProcess) {
      backendChildProcess.kill()
    }
    // Artivact: ---
    mainWindow = null
  })
}

app.whenReady().then(createWindow)

app.on('window-all-closed', () => {
  if (platform !== 'darwin') {
    app.quit()
  }
})

app.on('activate', () => {
  if (mainWindow === null) {
    createWindow()
  }
})
