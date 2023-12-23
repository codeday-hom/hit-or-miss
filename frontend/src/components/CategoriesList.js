export const CategoriesList = [
  "Famous artists",
  "Famous chefs",
  "Pop stars",
  "Art styles",
  "Martial arts",
  "Food cuisines",
  "Fields of science",
  "Famous landmarks",
  "Countries that no longer exist",
  "Chemical elements",
  "Animals with four legs",
  "Items of stationary",
  "Superheroes",
  "Board games",
  "Taylor swift songs",
  "Vehicles",
  "TV shows",
  "Musical instruments",
  "Types of insects",
  "Economics things",
  "Fruits",
  "Vegetables",
  "Green vegetables",
  "Music festivals",
]
  .concat(["Technology", "Car"].map(withBrand => withBrand + " brands"))
  .concat(["Music", "Movie"].map(withGenre => withGenre + " genres"))
  .concat(["Asian", "European", "South American", "North American", "African"].map(continentAdjective => continentAdjective + " countries"))
  .concat(["European", "South East Asian", "African", "Indian"].map(groupOfLanguages => groupOfLanguages + " languages"))
  .concat(["European", "Chinese", "African", "Indian"].map(cultures => cultures + " cultural festivals"))
  .concat(["Fizzy", "Alcoholic", "Non-alcoholic"].map(drinkAdjective => drinkAdjective + " drinks"))
  .concat(["fish", "bird", "dinosaur"].map(animalCategory => "Types of " + animalCategory))
  .concat(["Team", "Individual", "Olympic"].map(sportsAdjective => sportsAdjective + " sports"))
  .concat(["the fridge", "the kitchen", "a dorm room"].map(thingPlaces => "Things in " + thingPlaces))
  .concat(["with shells", "related to rain", "that remind you of autumn/fall"].map(otherThings => "Things " + otherThings))
