var { app, BrowserWindow } = require("electron");
// const java = require('java');
var path = require("path");
var url = require("url");
var childProcess = require('child_process');

require('find-java-home')(function (err, home) {
  if (err) return console.log('java home error:' + err);
  console.log('find home success :' + home);
})


//var javaLangSystem = java.import('java.lang.System');

//javaLangSystem.out.printlnSync('Hello World');
//require('electron-reload')(__dirname);
var options = { maxBuffer: 1024 * 1024 * 100, encoding: 'utf8', timeout: 5000 };
var child = childProcess.exec('java -jar Main.jar &', options, function (error, stdout, stderr) {
  if (error) {
    console.log(error.stack);
    console.log('ERROR :' + error);
    console.log('Error Code: ' + error.code);
    console.log('Error Signal: ' + error.signal);
  }
  console.log('Results: \n' + stdout);

  if (stderr.length) {
    console.log('Errors: ' + stderr);
  }
});
let win;

function createWindow() {
  win = new BrowserWindow({ width: 1000, height: 800 });

  //to remove menu 
  win.setMenu(null);

  // load the dist folder from Angular
  win.loadURL(
    url.format({
      pathname: path.join(__dirname, `/dist/index.html`),
      protocol: "file:",
      slashes: true,
    })
  );

  //added this line to open developer tools for debugging
  win.webContents.openDevTools();

  // The following is optional and will open the DevTools:
  // win.webContents.openDevTools()

  win.on("closed", () => {
    win = null;
  });
}

app.on("ready", createWindow);

// on macOS, closing the window doesn't quit the app
app.on("window-all-closed", () => {
  if (process.platform !== "darwin") {
    app.quit();
  }
});

// initialize the app's main window
app.on("activate", () => {
  if (win === null) {
    createWindow();
  }
});