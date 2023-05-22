import { Fragment } from "react";
import LogoImage from "../assets/ImageLogo.png";
import classes from "./Header.module.css";

const Header = (props) => {
  return (
    <Fragment>
      <header className={classes.header}>
        <button>Chessout</button>
        <div className={classes["main-image"]}>
          <img src={LogoImage} />
        </div>
        <button>Team</button>
        <button>Login</button>
      </header>
    </Fragment>
  );
};

export default Header;
