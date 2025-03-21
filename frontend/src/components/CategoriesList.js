export const CategoriesList = [
  "Famous artists",
  "Pop stars",
  "Art styles",
  "Martial arts",
  "Food cuisines",
  "Fields of science",
  "Countries that no longer exist",
  "Chemical elements",
  "Animals with four legs",
  "Items of stationary",
  "Superheroes",
  "Board games",
  "Taylor Swift songs",
  "Vehicles",
  "TV shows",
  "Musical instruments",
  "Economics things",
  "Fruits",
  "Tropical fruits",
  "Vegetables",
  "Green vegetables",
  "Music festivals",
  "Singapore insects",
  "Singapore local food",
  "Indian street food",
  "Delicious desserts",
  "Bakery brands",
  "Ice cream flavours",
  "Disney channel TV shows",
  "Kids' cartoons",
  "MRT stations",
  "Types of vehicles",
  "Currencies",
  "Flowers",
  "Types of jewelery",
  "Precious gems",
  "Colours",
  "Kitchen utensils",
  "Fancy ingredients",
  "Candle scents",
  "Furniture",
  "Types of wood",
  "Airlines",
  "Dog breeds",
  "Quarter-life crisis hobbies",
  "Red flags in a guy",
  "Green flags in a guy",
  "Red flags in a girl",
  "Green flags in a girl",
  "Things to do in Singapore",
  "Winter clothing",
  "Makeup items",
  "Hairstyles",
  "Items of clothing",
  "Authors",
  "Reality TV shows",
  "Talentless celebrities",
  "Bollywood actors",
  "Medical specialisations",
  "Iconic movies",
  "Iconic books",
  "Musicals",
  "Breakfast buffet foods",
  "TV streaming services",
  "Hobbies",
  "Things that Grandmas do",
  "Harry Potter spells",
  "Malls in Singapore",
  "Skin problems",
  "Diseases",
  "Wise sayings and quotes",
  "Subjects in school",
  "Comedy sitcoms",
  "Bones in the body",
  "Gym equipment",
  "Lab equipment",
  "Disney channel actors who later became famous musicians",
  "Things girls do when they like a guy",
  "Things guys do when they like a girl",
  "Different words for money",
  "Insecurities",
  "Rice dishes",
  "Sports that don't require meaningful physical fitness",
  "Historic kingdoms and empires",
  "Sea creatures",
  "Mythical animals",
  "Things in outer space",
  "Religions",
]
  .concat(["Asian", "European", "South American", "North American", "African"].map(continentAdjective => continentAdjective + " countries"))
  .concat(["Asia", "Europe", "South America", "North America", "Africa"].map(continentAdjective => "Capitals of countries in " + continentAdjective))
  .concat(["European", "South East Asian", "African", "Indian"].map(groupOfLanguages => groupOfLanguages + " languages"))
  .concat(["European", "Chinese", "African", "Indian"].map(cultures => cultures + " cultural festivals"))
  .concat(["Indian", "US"].map(countryWithStates => countryWithStates + " states"))
  .concat(["India", "Britain"].map(countryWithCities => "Cities in " + countryWithCities))
  .concat(["Technology", "Car", "Chocolate"].map(withBrand => withBrand + " brands"))
  .concat(["Music", "Movie"].map(withGenre => withGenre + " genres"))
  .concat(["Fizzy", "Alcoholic", "Non-alcoholic"].map(drinkAdjective => drinkAdjective + " drinks"))
  .concat(["fish", "bird", "dinosaur", "tree", "amphibian", "insect"].map(animalCategory => "Types of " + animalCategory))
  .concat(["Team", "Individual", "Olympic", "Extreme", "Water"].map(sportsAdjective => sportsAdjective + " sports"))
  .concat(["the fridge", "the kitchen", "a dorm room"].map(thingPlaces => "Things in " + thingPlaces))
  .concat(["with shells", "related to rain", "that remind you of autumn/fall", "that pop"].map(otherThings => "Things " + otherThings))
  .concat(["Summer", "Skiing"].map(holidayDestination => holidayDestination + " holiday destinations"))
  .concat(["pasta", "cheese"].map(categorizedFood => "Types of " + categorizedFood))
  .concat(["Singapore", "India", "the UK", "Bali"].map(placeWithTouristSpots => "Tourist spots in " + placeWithTouristSpots))
  .concat(["Tennis", "Football", "Cricket"].map(sport => sport + " players"))
  .concat(["drivers", "teams", "Grand Prix countries"].map(f1Things => "F1 " + f1Things))
