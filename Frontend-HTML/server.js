const http = require('http');
const fs = require('fs');
const path = require('path');
const url = require('url');

const PORT = 90;

// MIME类型映射
const mimeTypes = {
  '.html': 'text/html',
  '.js': 'text/javascript',
  '.css': 'text/css',
  '.json': 'application/json',
  '.png': 'image/png',
  '.jpg': 'image/jpg',
  '.gif': 'image/gif',
  '.svg': 'image/svg+xml',
  '.wav': 'audio/wav',
  '.mp4': 'video/mp4',
  '.woff': 'application/font-woff',
  '.ttf': 'application/font-ttf',
  '.eot': 'application/vnd.ms-fontobject',
  '.otf': 'application/font-otf',
  '.wasm': 'application/wasm'
};

const server = http.createServer((req, res) => {
  // 解析URL
  const parsedUrl = url.parse(req.url);
  let pathname = parsedUrl.pathname;
  
  // 添加CORS头
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');
  
  // 处理OPTIONS请求
  if (req.method === 'OPTIONS') {
    res.writeHead(200);
    res.end();
    return;
  }
  
  // 处理根路径
  if (pathname === '/') {
    pathname = '/index.html';
  }
  
  // 构建文件路径
  const safePath = path.normalize(path.join(__dirname, pathname));
  
  // 安全检查：确保路径在项目目录内
  if (!safePath.startsWith(__dirname)) {
    console.log(`安全警告：尝试访问项目目录外的文件: ${safePath}`);
    res.writeHead(403, { 'Content-Type': 'text/html' });
    res.end('<h1>403 Forbidden</h1>');
    return;
  }
  
  // 获取文件扩展名
  const ext = path.parse(safePath).ext;
  const contentType = mimeTypes[ext] || 'application/octet-stream';
  
  // 检查文件是否存在
  fs.readFile(safePath, (err, data) => {
    if (err) {
      if (err.code === 'ENOENT') {
        // 文件不存在，记录日志
        console.log(`文件不存在: ${safePath}`);
        
        // 如果是JS或CSS文件，返回空内容而不是404，避免页面错误
        if (ext === '.js' || ext === '.css') {
          res.writeHead(200, { 'Content-Type': contentType });
          res.end('');
        } else {
          res.writeHead(404, { 'Content-Type': 'text/html' });
          res.end(`<h1>404 Not Found</h1><p>文件 ${pathname} 不存在</p>`);
        }
      } else {
        // 服务器错误
        console.error(`服务器错误: ${err.message}`);
        res.writeHead(500, { 'Content-Type': 'text/html' });
        res.end('<h1>500 Internal Server Error</h1>');
      }
    } else {
      // 文件存在，返回内容
      console.log(`成功加载: ${pathname}`);
      res.writeHead(200, { 
        'Content-Type': contentType,
        'Cache-Control': 'no-cache, no-store, must-revalidate',
        'Pragma': 'no-cache',
        'Expires': '0'
      });
      res.end(data);
    }
  });
});

server.listen(PORT, () => {
  console.log(`服务器运行在 http://localhost:${PORT}`);
  console.log('按 Ctrl+C 停止服务器');
});