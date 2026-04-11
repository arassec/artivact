import * as electron from 'electron';
import {app, BrowserWindow, ipcMain, dialog} from 'electron';
import path from 'node:path';
import os from 'node:os';
import fs from 'node:fs';
import {fileURLToPath} from 'node:url';
import * as http from 'http';
import * as child_process from 'node:child_process';

// needed in case process is undefined under Linux
const platform = process.platform || os.platform()

const currentDir = fileURLToPath(new URL('.', import.meta.url))

let mainWindow

// Artivact: +++
let splashWindow
let projectSelectWindow

// Disable the application menu, even for new window instances!
electron.Menu.setApplicationMenu(null);

let backendChildProcess;
let backendPort = 8080;

function timer(ms) {
  return new Promise(res => setTimeout(res, ms));
}

// Path to the JSON file storing previously opened project directories.
const projectListFile = path.join(app.getPath('userData'), 'project-directories.json');

function loadProjectList() {
  try {
    if (fs.existsSync(projectListFile)) {
      const data = fs.readFileSync(projectListFile, 'utf-8');
      return JSON.parse(data);
    }
  } catch (e) {
    console.error('Failed to load project list:', e.message);
  }
  return [];
}

function saveProjectList(projects) {
  try {
    fs.writeFileSync(projectListFile, JSON.stringify(projects, null, 2), 'utf-8');
  } catch (e) {
    console.error('Failed to save project list:', e.message);
  }
}

// Can be set during startup as command-line parameter, e.g. --artivact.project.root=C:\test-project
let projectRoot = app.commandLine.getSwitchValue('artivact.project.root');
if (projectRoot) {
  console.log('Project root set by command-line: ' + projectRoot);
} else {
  projectRoot = null;
}
const defaultProjectRoot = '${user.home}/.avdata';

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

// IPC handlers for the project selection dialog.
ipcMain.handle('get-projects', () => {
  return loadProjectList();
});

ipcMain.handle('add-project', async () => {
  const result = await dialog.showOpenDialog(projectSelectWindow, {
    properties: ['openDirectory'],
  });
  if (!result.canceled && result.filePaths.length > 0) {
    const dirPath = result.filePaths[0];
    const projects = loadProjectList();
    if (!projects.includes(dirPath)) {
      projects.push(dirPath);
      saveProjectList(projects);
    }
    return dirPath;
  }
  return null;
});

ipcMain.handle('remove-project', (event, projectPath) => {
  let projects = loadProjectList();
  projects = projects.filter(p => p !== projectPath);
  saveProjectList(projects);
  return projects;
});

ipcMain.handle('start-project', (event, selectedPath) => {
  projectRoot = selectedPath;
  if (projectSelectWindow) {
    projectSelectWindow.close();
  }
});

ipcMain.handle('get-system-locale', () => {
  return app.getLocale();
});

// Show the project selection dialog and return a Promise that resolves when a project is selected.
function showProjectSelectDialog() {
  return new Promise((resolve) => {
    projectSelectWindow = new BrowserWindow({
      width: 600,
      height: 480,
      icon: path.resolve(currentDir, 'icons/icon.png'),
      resizable: false,
      frame: true,
      autoHideMenuBar: true,
      webPreferences: {
        contextIsolation: true,
        preload: path.resolve(
          currentDir,
          path.join(process.env.QUASAR_ELECTRON_PRELOAD_FOLDER, 'project-select-preload' + process.env.QUASAR_ELECTRON_PRELOAD_EXTENSION)
        )
      }
    });

    projectSelectWindow.removeMenu();

    const selectFile = path.resolve(currentDir, process.env.QUASAR_PUBLIC_FOLDER, 'project-select.html');
    projectSelectWindow.loadFile(selectFile).then(() => console.log('Loaded project-select.html'));

    projectSelectWindow.on('closed', () => {
      projectSelectWindow = null;
      if (projectRoot) {
        resolve(projectRoot);
      } else {
        // User closed the dialog without selecting a project, quit the app.
        app.quit();
      }
    });
  });
}

async function createWindow() {
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

async function startup() {
  // If project root was not set by command-line, show the project selection dialog.
  if (!projectRoot) {
    await showProjectSelectDialog();
  }
  // Use default project root as fallback if still not set.
  if (!projectRoot) {
    projectRoot = defaultProjectRoot;
  }
  await createWindow();
}

app.whenReady().then(startup)

app.on('window-all-closed', () => {
  if (platform !== 'darwin') {
    app.quit()
  }
})

app.on('activate', () => {
  if (mainWindow === null) {
    startup()
  }
})
