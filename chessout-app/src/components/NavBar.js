import React from "react";
import { Button, ButtonGroup } from "@chakra-ui/react";
import {
  Box,
  Heading,
  Flex,
  Link,
  useColorMode,
  IconButton,
} from "@chakra-ui/react";
//import { Image } from "@chakra-ui/react";
import ImageLogo from "../assets/ImageLogo.png";

const MenuItems = ({ children }) => (
  <Link mt={{ base: 4, md: 0 }} mr={6} display="block">
    {children}
  </Link>
);

const Navbar = (props) => {
  const { colorMode, toggleColorMode } = useColorMode();
  const bgColor = { light: "red.200", dark: "red.800" };
  const textColor = { light: "red.800", dark: "white" };
  return (
    <Flex
      as="nav"
      align="center"
      justify="space-between"
      wrap="wrap"
      padding="0.3rem"
      bg={
        //{...props} //borderBottom="1px solid black" //color="teal.300"
        colorMode === "light" ? "gray.900" : "teal.500"
      }
      color={colorMode === "light" ? "teal.300" : "white"}
      borderBottom="1px solid black"
      {...props}
    >
      <Flex align="center" mr={5}>
        <Heading as="h1" size="lg" letterSpacing={"-.1rem"}>
          <IconButton
            colorScheme="#343438"
            aria-label="Call Segun"
            size="md"
            background="#chakra-placeholder-color._dark"
            icon={<img src={ImageLogo} />}
            onClick={toggleColorMode}
          />
          Chessout
        </Heading>
      </Flex>

      <Box
        display="flex"
        width="auto"
        alignItems="center"
        flexGrow={1}
        color={colorMode === "light" ? "teal.300" : "white"}
      >
        <MenuItems>Team</MenuItems>
        <MenuItems>Login</MenuItems>
      </Box>
      <Box display="block" mt={{ base: 4, md: 0 }}>
        <IconButton
          //bg="transparent"
          //aria-label="toggle color mode"
          icon={colorMode === "light" ? "moon" : "sun"}
          onClick={toggleColorMode}
          //color="white"
          color={textColor[colorMode]}
          bg={bgColor[colorMode]}
        />
      </Box>
    </Flex>
  );
};

export default Navbar;
