/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        'brand-dark': '#003B71',   // Bleu foncé du logo
        'brand-orange': '#F78F1E', // Orange du logo
        'brand-light': '#00ADEF',  // Bleu clair (antenne)
      }
    },
  },
  plugins: [],
}
