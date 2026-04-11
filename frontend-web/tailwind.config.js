/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        'brand-dark': '#034d92',   // Nouveau bleu premium
        'brand-orange': '#f97316', // Nouvel orange premium
        'brand-light': '#00ADEF',  // Bleu clair (antenne)
      }
    },
  },
  plugins: [],
}
