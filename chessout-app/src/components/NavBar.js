import React from "react";
import { Button, ButtonGroup } from "@chakra-ui/react";
import {
  Box,
  //Heading,
  Flex,
  Spacer,
  useColorMode,
  IconButton,
} from "@chakra-ui/react";
//import { useColorModeColorMode } from "@chakra-ui/core";
//import ImageLogo from "../assets/ImageLogo.png";

const Navbar = (props) => {
  const { colorMode, toggleColorMode } = useColorMode();
  const bgColor = { light: "gray.200", dark: "gray.700" };
  //const textColor = { light: "gray.800", dark: "gray.700" };
  return (
    <Flex
      minWidth="max-content"
      alignItems="center"
      gap="2"
      bg={bgColor[colorMode]}
    >
      <Box p="2">
        <Button bg={bgColor[colorMode]}>ChessOut</Button>
      </Box>
      <Spacer />
      <ButtonGroup gap="2">
        <Button bg={bgColor[colorMode]}>Team </Button>
        <Button bg={bgColor[colorMode]}>Login</Button>
      </ButtonGroup>

      <IconButton
        bg="transparent"
        aria-label="toggle color mode"
        onClick={toggleColorMode}
        
        //color={textColor[colorMode]}
        //bg={bgColor[colorMode]}
        color='white'
      />
    </Flex>
  );
};

export default Navbar;
