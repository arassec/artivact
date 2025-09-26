# Configuring the Local Application

## The Project Directory

Artivact stores all project data in the filesystem.
The default project directory is the directory ```.avdata``` in the user's home directory.

In order to change the project directory, you can start the application with an additional parameter.

Example for Windows:

```
Artivact.exe --artivact.project.root=C:\Artivact-Projects\sample-project
```

Example for Linux:

```
./Artivact --artivact.project.root=/opt/Artivact-Projects/sample-project
```

## Third Party Software

All used third party applications are configured within the running application.
In order to use the tools, they have to already be installed.

They can then be configured by navigating to ``Settings`` -> ``Peripherals``.