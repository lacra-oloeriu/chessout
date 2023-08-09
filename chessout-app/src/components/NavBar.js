import React from "react";
import { Button, ButtonGroup } from "@chakra-ui/react";
import {
  Box,
  //BreadcrumbLink,
  //Heading,
  Flex,
  Spacer,
  useColorMode,
  IconButton,
  Tooltip,
  Image,
} from "@chakra-ui/react";
import { Link } from 'react-router-dom';
import { MoonIcon, SunIcon } from "@chakra-ui/icons";
//import { useColorModeColorMode } from "@chakra-ui/core";
import ImageLogo from "../assets/ImageLogo.png";

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
      <Box boxSize="55px">
        <Image borderRadius="full" boxSize="57px"  src={ImageLogo} alt="Logo" />
      </Box>
      <Box p="2">
        <Button  bg={bgColor[colorMode]}>ChessOut</Button>
      </Box>
      <Spacer />
      <ButtonGroup gap="2">
        <Button bg={bgColor[colorMode]}>About </Button>
        <Button bg={bgColor[colorMode]}>Team </Button>
        <Button bg={bgColor[colorMode]}>Login</Button>
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
