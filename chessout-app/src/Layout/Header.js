import { Fragment } from "react";
import ImageLogo from '../assets/ImageLogo.png'

import classes from "./Header.module.css";

const Header = (props) => {
  return (
    <Fragment>
      <header className={classes.header}>
        <button>Chessout</button>
        <img src={ImageLogo} />
        <div className={classes["main-image"]}>
         
        </div>
        <button>Team</button>
        <button>Login</button>
      </header>
    </Fragment>
  );
};

export default Header;
