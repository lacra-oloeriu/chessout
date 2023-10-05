import React, { useState, useEffect } from "react";
import {
  Button,
  ButtonGroup,
  Box,
  Flex,
  Spacer,
  useColorMode,
  IconButton,
  Tooltip,
  Image,
} from "@chakra-ui/react";
import { MoonIcon, SunIcon } from "@chakra-ui/icons";
import ImageLogo from "../assets/ImageLogo.png";
import { Link } from "react-router-dom";
import SignInModal from "./SignInModal";

import { getAuth, onAuthStateChanged, signOut } from 'firebase/auth';
import { firebaseApp } from "../config/firebase";

const Navbar = (props) => {
  const { colorMode, toggleColorMode } = useColorMode();
  const bgColor = { light: "gray.200", dark: "gray.700" };

  // State for the sign-in/sign-up modal
  const authInstance = getAuth(firebaseApp);
  const [isSignInModalOpen, setIsSignInModalOpen] = useState(false);

  const openSignInModal = () => {
    setIsSignInModalOpen(true);
  };

  const closeSignInModal = () => {
    setIsSignInModalOpen(false);
  };

  //user data
  const [user, setUser] = useState(null);
  useEffect(() => {
    const auth = getAuth(firebaseApp);

    const unsubscribe = onAuthStateChanged(auth, (user) => {
      setUser(user); // Set the user state based on the authentication status
    });

    return () => unsubscribe(); // Unsubscribe from the listener when the component unmounts
  }, []);

  const handleSignOut = async () => {
    try {
      await signOut(authInstance);
      // After sign-out, the `onAuthStateChanged` listener will update the `user` state, causing the login button to appear again.
    } catch (error) {
      console.error('Error signing out:', error);
    }
  };

  return (
    <Flex
      minWidth="max-content"
      alignItems="center"
      gap="2"
      bg={bgColor[colorMode]}
    >
      <Box boxSize="55px">
        <Image src={ImageLogo} alt="Logo" />
      </Box>
      <Box p="2">
        <Button bg={bgColor[colorMode]}>ChessOut</Button>
      </Box>
      <Spacer />
      <ButtonGroup gap="2">
        <Link to={"/"}>
          <Button bg={bgColor[colorMode]}>Home</Button>
        </Link>
        <Link to={"/about"}>
          <Button bg={bgColor[colorMode]}>About</Button>
        </Link>
        <Link to={"/team"}>
          <Button bg={bgColor[colorMode]}>Team</Button>
        </Link>
        {user ? (
          // User is logged in, display account button
          <Button bg={bgColor[colorMode]} onClick={handleSignOut}>
            Logout
          </Button>
        ) : (
          // User is not logged in, display login button
          <Button bg={bgColor[colorMode]} onClick={() => openSignInModal()}>
            Login
          </Button>
        )}
        <SignInModal
          isOpen={isSignInModalOpen}
          onClose={closeSignInModal}
          authInstance={authInstance}
        />
      </ButtonGroup>
      <Tooltip label="Toggle Dark Mode">
        <IconButton
          aria-label="Toggle light dark mode"
          onClick={toggleColorMode}
          icon={colorMode === "light" ? <MoonIcon /> : <SunIcon />}
        />
      </Tooltip>
    </Flex>
  );
};

export default Navbar;
