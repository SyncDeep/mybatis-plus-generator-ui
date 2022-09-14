var fs = require('fs');
var exec = require('child_process').exec;
var path = require("path");
var _ = require("lodash");
const colors = require('colors');

let distPath = path.resolve('../main/resources/generator-ui')
let staticPath = path.resolve('./src/static')
let necessaryFiles = ["_nuxt", "index.html"];

fs.rmdir(distPath, function (error) {
    console.log(colors.green('Cleaned static directory'));
    console.log('Start building front-end resources');
    if (!fs.existsSync(distPath)) {
        console.log(colors.yellow("The destination folder does not exist, it was created automatically：" + distPath));
        fs.mkdirSync(distPath);
    }
    exec("nuxt build", function (err, sto) {
        if (err != null) {
            console.error(colors.red('Front-end resource compilation error'));
            console.error(colors.red(err));
        } else {
            console.log(colors.green('Front-end resources compiled successfully'));
            let staticFiles = fs.readdirSync(staticPath);
            staticFiles.forEach(function (filename) {
                necessaryFiles.push(filename);
            });
            console.log(colors.yellow(necessaryFiles));
            let distFiles = fs.readdirSync(distPath);
            //Remove automatically generated unnecessary static files
            distFiles.forEach(function (filename) {
                let flag = _.findIndex(necessaryFiles, function (f) {
                    return f === filename;
                });
                if (flag === -1) {
                    let filePath = path.join(distPath, filename);
                    if (fs.statSync(filePath).isDirectory()) { // recurse
                        deleteFolder(filePath);
                    } else {
                        fs.unlinkSync(filePath);
                    }
                }
            });
            console.log(colors.green('Unnecessary files have been removed'));
        }
    });
})

function deleteFolder(path) {
    var files = [];
    if (fs.existsSync(path)) {
        files = fs.readdirSync(path);
        files.forEach(function (file, index) {
            var curPath = path + "/" + file;
            if (fs.statSync(curPath).isDirectory()) { // recurse
                deleteFolder(curPath);
            } else { // delete file
                fs.unlinkSync(curPath);
            }
        });
        fs.rmdirSync(path);
    }
}