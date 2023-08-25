import React from "react";
import {BsStopwatch} from "react-icons/bs";

export default function Timer(){
    return (
        <div>
            <BsStopwatch/>
            <div className= ".d-flex flex-column">
                <label> hh</label>
                <input value = {0} />
            </div>
        </div>
    )
}