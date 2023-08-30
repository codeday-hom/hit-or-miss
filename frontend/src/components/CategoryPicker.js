import React, {useState} from 'react';
import {WsMessageTypes} from "../constants/wsMessageTypes";
import useGameWebSocket from "../hooks/useGameWebSocket"

export default function CategoryPicker({ gameId }) {
    const categories = ["Sports", "Music", "Science", "Art", "History"].sort(() => Math.random() - 0.5);
    let categoryIndex = 0;

    const [currentCategory, setCurrentCategory] = useState(null);
    const [selectedCategory, setSelectedCategory] = useState(null);

    const { sendMessage } = useGameWebSocket(gameId, message => {
        if (message.type === WsMessageTypes.CATEGORY_CHOSEN) {
            setSelectedCategory(message.data);
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

    return (
        <div>
            <h2>Category Picker</h2>

            {currentCategory ? (
                <div>
                    <p>Category: {currentCategory}</p>
                    <button onClick={selectCategory}>Select</button>
                    <button onClick={skipCategory}>Skip</button>
                </div>
            ) : (
                <button onClick={fetchNextCategory}>Start Picking</button>
            )}

            {selectedCategory ? <p>Category chosen is: {selectedCategory}</p> : null}
        </div>
    );
}