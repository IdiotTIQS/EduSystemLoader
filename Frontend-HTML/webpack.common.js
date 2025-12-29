const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');

module.exports = {
  entry: {
    app: './js/app.js',
    common: './js/common.js',
    api: './js/api.js',
    teacher: './js/teacher.js',
    student: './js/student.js'
  },
  output: {
    path: path.resolve(__dirname, 'dist'),
    clean: true,
    filename: './js/[name].js',
  },
  plugins: [
    new HtmlWebpackPlugin({
      template: './index.html',
      filename: 'index.html'
    }),
    new HtmlWebpackPlugin({
      template: './pages/login.html',
      filename: 'pages/login.html'
    }),
    new HtmlWebpackPlugin({
      template: './pages/teacher.html',
      filename: 'pages/teacher.html'
    }),
    new HtmlWebpackPlugin({
      template: './pages/student.html',
      filename: 'pages/student.html'
    }),
    new CopyWebpackPlugin({
      patterns: [
        { from: 'css', to: 'css' },
        { from: 'img', to: 'img' },
        { from: '*.ico', to: '.' },
        { from: '*.png', to: '.' },
        { from: '*.svg', to: '.' },
        { from: 'site.webmanifest', to: '.' },
        { from: 'robots.txt', to: '.' }
      ]
    })
  ],
  module: {
    rules: [
      {
        test: /\.css$/i,
        use: ['style-loader', 'css-loader'],
      },
      {
        test: /\.(png|svg|jpg|jpeg|gif)$/i,
        type: 'asset/resource',
      },
      {
        test: /\.(woff|woff2|eot|ttf|otf)$/i,
        type: 'asset/resource',
      },
    ],
  },
};
