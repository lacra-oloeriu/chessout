//import Image from "next/image";
import { Fragment } from "react";

import classes from "./Header.module.css";


export default function Home() {
  return (
    <Fragment>
      <header className={classes.header}>
        <button>Home</button>
        <button>About</button>
      </header>
      <main className="flex min-h-screen flex-col items-center justify-between p-24">
        <div>Hello Chessout</div>
      </main>
    </Fragment>
  );
}
