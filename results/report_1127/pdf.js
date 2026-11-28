const { chromium } = require('/opt/node-tools/node_modules/playwright');
(async()=>{const b=await chromium.launch();const p=await b.newPage();
await p.goto('file://'+__dirname+'/report.html');await p.waitForLoadState('load');
await p.pdf({path:__dirname+'/report.pdf',format:'A4',margin:{top:'15mm',bottom:'15mm',left:'12mm',right:'12mm'},printBackground:true});await b.close();})();
