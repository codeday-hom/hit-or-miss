import React, {useState} from 'react';
import {CategoriesList} from "../CategoriesList";

export default function StandaloneCategoryPicker() {
  const categories = CategoriesList.sort(() => Math.random() - 0.5);
  let categoryIndex = 0;

  const [currentCategory, setCurrentCategory] = useState(null);

  const fetchNextCategory = () => {
    categoryIndex = (categoryIndex + 1) % categories.length;
    setCurrentCategory(categories.at(categoryIndex));
  }

  const skipCategory = () => {
    fetchNextCategory();
  }

  return (
    <div>
      <h2>Pick a category</h2>
      <div>
        {currentCategory && <p>Category: {currentCategory}</p>}
        <button onClick={skipCategory}>Draw a card</button>
      </div>
    </div>
  )
}
