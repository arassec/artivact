import {contextBridge, ipcRenderer} from 'electron';

// Expose a limited API to the project selection dialog renderer process.
contextBridge.exposeInMainWorld('projectSelectAPI', {
  getProjects: () => ipcRenderer.invoke('get-projects'),
  addProject: () => ipcRenderer.invoke('add-project'),
  removeProject: (projectPath) => ipcRenderer.invoke('remove-project', projectPath),
  startProject: (projectPath) => ipcRenderer.invoke('start-project', projectPath),
  getSystemLocale: () => ipcRenderer.invoke('get-system-locale'),
});
