import React,{useState} from "react";
import Timer from "./Timer"

export default function CountdownTimer(){
    const [seconds, setSeconds] = useState(0);
    const [milliseconds,setMilliseconds] = useState(0);
    const [isRunning, setIsRunning] = useState(null);
   useEffect(() => {
       let interval;
       if(isRunning){
           interval = setInterval (() => {
               if (milliseconds> 0){
                   setMilliseconds((milliseconds) => milliseconds - 1)
               }else if(seconds > 0){
                    setSeconds((seconds) => seconds - 1);
                    setMilliseconds(99);
                }
           }, 10);
       }
       return () => clearInterval(interval);
   }, [milliseconds, seconds, isRunning]);

    return (
        <div>
            <h1 className="title "> hi </h1>
            <Timer />
        </div>
    );
}