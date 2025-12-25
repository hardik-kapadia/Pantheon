import {app, BrowserWindow} from 'electron';
import path from "node:path";
import {isDev} from "./utils.js";

app.on("ready", () => {
    const mainWindow = new BrowserWindow({
        webPreferences: {
            preload: path.join(
                app.getAppPath(),
                '.',
                "/dist-electron/preload.cjs"
            )
        }
    });

    if (isDev()) {
        mainWindow.loadURL("http://localhost:5123");
    } else {
        mainWindow.loadFile(path.join(app.getAppPath(), "/dist-react/index.html"));
    }

})