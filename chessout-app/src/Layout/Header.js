import { Fragment } from "react";
//import ImageLogo from "../assets/ImageLogo.png";

import classes from "./Header.module.css";

const Header = (props) => {
  return (
    <Fragment>
      <header className={classes.header}>
        <div>
          <button>Chessout</button>
        </div>
        <div>
        
        </div>
        <div>
          <button className={classes.button}>Team</button>
        </div>
        <div>
        <button className={classes.button}>Login</button>
        </div>
      </header>
    </Fragment>
  );
};

export default Header;
