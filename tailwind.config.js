/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './src/main/resources/static/index.html',
    './src/main/resources/static/js/**/*.js',
  ],
  theme: {
    extend: {
      boxShadow: {
        card:
          '0 1px 2px 0 rgb(15 23 42 / 0.06), 0 8px 24px -12px rgb(15 23 42 / 0.2)',
      },
    },
  },
  plugins: [],
};
