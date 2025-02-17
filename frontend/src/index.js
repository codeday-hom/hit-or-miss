import React from "react";
import ReactDOM from "react-dom/client";
import {createBrowserRouter, RouterProvider} from "react-router-dom";
import "./index.css";
import Landing from "./Landing";
import Lobby from "./components/Lobby";
import ContextualGame from "./components/ContextualGame";
import reportWebVitals from "./reportWebVitals";
import Kit from "./components/kit/Kit";

const router = createBrowserRouter([
  {
    path: "/",
    element: <Landing/>,
  },
  {
    path: "/lobby/:gameId",
    element: <Lobby/>,
  },
  {
    path: "/game/:gameId/",
    element: <ContextualGame/>,
  },
  {
    path: "/kit",
    element: <Kit/>,
  }
]);

const root = ReactDOM.createRoot(document.getElementById("root"));
root.render(
  <React.StrictMode>
    <RouterProvider router={router}/>
  </React.StrictMode>
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
