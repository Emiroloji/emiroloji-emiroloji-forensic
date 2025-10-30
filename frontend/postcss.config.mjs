/** @type {import('postcss').Config} */
export default {
  plugins: {
    "@tailwindcss/postcss": {}, // <-- Hata logundaki gibi bunu kullan
    "autoprefixer": {},
  },
}