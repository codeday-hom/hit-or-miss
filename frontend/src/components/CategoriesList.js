export const CategoriesList = [
  "Music genres",
  "Famous artists",
  "Countries that no longer exist",
  "Chemical elements",
  "Martial arts"
]
  .concat(["Asian", "European", "South American", "North American", "African"].map(continentAdjective => continentAdjective + " countries"))
  .concat(["Fizzy", "Alcoholic", "Non-alcoholic"].map(drinkAdjective => drinkAdjective + " drinks"))
  .concat(["fish", "bird", "dinosaur"].map(animalCategory => "Types of " + animalCategory))
  .concat(["Team", "Individual"].map(sportsAdjective => sportsAdjective + " sports"))
