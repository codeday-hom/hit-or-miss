import React, {useState, useEffect} from 'react';
import {WsMessageTypes} from "../constants/wsMessageTypes";
import useGameWebSocket from "../hooks/useGameWebSocket"


function CategoryPicker(gameId) {
    const [categories, setCategories] = useState([]);
    const [currentCategory, setCurrentCategory] = useState(null);
    const [message, setMessage] = useState("");

    let socket;

    useEffect(() => {
        socket = new WebSocket(`ws://localhost:8080/ws/game/${gameId}`);

        socket.onmessage = (event) => {
            const data = JSON.parse(event.data);
            // if (data.type === "newPicker") {
            //     setMessage(`It's now ${data.data}'s turn to pick a category!`);
            // } else
            if (data.type === WsMessageTypes.CATEGORY_CHOSEN) {
                setMessage(`Category chosen is: ${data.data}`);
            }
        };

        return () => {
            socket.close();
        };
    }, []);

    const {sendCardSelectedMessage, selectedCategory} = useGameWebSocket(gameId);

    const handleClick = () => {
        sendCardSelectedMessage(currentCategory);
    };

    const fetchNextCategory = () => {
        if (categories.length === 0) {
            setCategories(["Sports", "Music", "Science", "Art", "History"].sort(() => Math.random() - 0.5));
        }

        setCurrentCategory(categories.pop());
    }


    const skipCategory = () => {
        fetchNextCategory();
    }

    const sendCategoryToBackend = async (currentCategory) => {
        const url = `http://localhost:8080/api/join-game/${gameId}`;

        fetch(url, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ gameId, currentCategory }),
        })
            .then((response) => response.json())
            .catch((error) => {
                console.log("Error fetching data:", error);
            });
    };


    return (
        <div>
            <h2>Category Picker</h2>

            {currentCategory ? (
                <div>
                    <p>Category: {currentCategory}</p>
                    <button onClick={handleClick}>Select</button>
                    <button onClick={skipCategory}>Skip</button>
                </div>
            ) : (
                <button onClick={fetchNextCategory}>Start Picking</button>
            )}


            {selectedCategory ? <p>Category chosen is: ${selectedCategory}</p> : null}

        </div>
    );
}

export default CategoryPicker;
