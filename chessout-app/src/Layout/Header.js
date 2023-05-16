import { Fragment } from "react";
import chessImage from "../assets/chess-out.JPG";
import classes from "./Header.module.css";

const Header = (props) => {
  return (
    <Fragment>
      <header className={classes.header}>
        <button>Home</button>
        <button>About</button>
      </header>
      <h1>Lets play chess out!</h1>
      <div className={classes["main-image"]}>
        <img src={chessImage} alt="Chess out" />
      </div>
    </Fragment>
  );
};

export default Header;
