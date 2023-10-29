import React, {useState} from 'react';
import {WsMessageType} from "../websockets/WsMessageType";
import {CategoriesList} from "./CategoriesList";

export default function CategoryPicker({sendWebSocketMessage}) {
  const categories = CategoriesList.sort(() => Math.random() - 0.5);
  let categoryIndex = 0;

  const [currentCategory, setCurrentCategory] = useState(null);
  const [selectedCategory, setSelectedCategory] = useState(null);

  const selectCategory = () => {
    setSelectedCategory(currentCategory)
    sendWebSocketMessage(JSON.stringify({type: WsMessageType.CATEGORY_SELECTED, data: {category: currentCategory}}));
  };

  const fetchNextCategory = () => {
    categoryIndex = (categoryIndex + 1) % categories.length;
    setCurrentCategory(categories.at(categoryIndex));
  }

  const skipCategory = () => {
    fetchNextCategory();
  }

  if (selectedCategory) {
    return null
  }

  if (currentCategory) {
    return (
      <div>
        <h2>Pick a category</h2>
        <div>
          <p>Category: {currentCategory}</p>
          <button onClick={selectCategory}>Select</button>
          <button onClick={skipCategory}>Skip</button>
        </div>
      </div>
    )
  } else {
    return (
      <div>
        <button onClick={fetchNextCategory}>Pick a category</button>
      </div>
    )
  }
}
