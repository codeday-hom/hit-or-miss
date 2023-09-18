import React, {useState, useEffect} from 'react';
import {WsMessageTypes} from "../constants/wsMessageTypes";
import useGameWebSocket from "../hooks/useGameWebSocket"

export default function CategoryPicker({ gameId, clientUsername, currentPlayer, onCategoryResultChange }) {
    const categories = ["Sports", "Music", "Science", "Art", "History"].sort(() => Math.random() - 0.5);
    let categoryIndex = 0;

    const [currentCategory, setCurrentCategory] = useState(null);
    const [selectedCategory, setSelectedCategory] = useState(null);

    const { sendMessage } = useGameWebSocket(gameId, message => {
        if (message.type === WsMessageTypes.CATEGORY_CHOSEN) {
            setSelectedCategory(message.data);
            onCategoryResultChange(true);
        }
    });

    const selectCategory = () => {
        sendMessage(JSON.stringify({type: WsMessageTypes.CATEGORY_SELECTED, data: currentCategory}));
    };

    const fetchNextCategory = () => {
        categoryIndex = (categoryIndex + 1) % categories.length;
        setCurrentCategory(categories.at(categoryIndex));
    }

    const skipCategory = () => {
        fetchNextCategory();
    }

    const categoryPicker = () => {
        // If I am picking, and I haven't chosen yet, then I can see these controls.
        // If someone else is picking, I don't see them.
        // If I already selected a category, I don't see them.
        if ((currentPlayer === clientUsername) && !selectedCategory) {
            if (currentCategory) {
                return <div>
                    <p>Category: {currentCategory}</p>
                    <button onClick={selectCategory}>Select</button>
                    <button onClick={skipCategory}>Skip</button>
                </div>

            } else {
                return <button onClick={fetchNextCategory}>Start Picking</button>
            }
        } else {
            return null
        }
    }

    return (
        <div>
            <h2>Category Picker</h2>
            {categoryPicker()}
            {selectedCategory ? <p>Category chosen is: {selectedCategory}</p> : null}
        </div>
    );
}
