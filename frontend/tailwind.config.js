/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        bradesco: {
          magenta: '#f0005c', // Rosa vibrante do topo
          red: '#cc092f',     // Vermelho Bradesco
          darkRed: '#990022', // Vermelho escuro para sombras/hover
          lightBg: '#f8f9fa',
        }
      }
    },
  },
  plugins: [],
}