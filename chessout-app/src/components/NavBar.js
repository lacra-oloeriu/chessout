import React from "react";
import { Button, ButtonGroup } from "@chakra-ui/react";
import {
  Box,
  Heading,
  Flex,
  Spacer,
  useColorMode,
  IconButton,
} from "@chakra-ui/react";
import ImageLogo from "../assets/ImageLogo.png";

const Navbar = (props) => {
  const { colorMode, toggleColorMode } = useColorMode();
  const bgColor = { light: "red.200", dark: "red.800" };
  //const textColor = { light: "red.800", dark: "white" };
  return (
    <Flex minWidth="max-content" alignItems="center" gap="2">
      <Box p="4" >
        <Heading size="lg">
          <IconButton
            colorScheme="md"
            aria-label="Call Segun"
            size="sm"
            background="#chakra-placeholder-color._dark"
            icon={<img src={ImageLogo} alt="Logo" />}
            onClick={toggleColorMode}
          /> ChessOut
        </Heading>
      </Box>
      <Spacer />
      <ButtonGroup gap="2">
        <Button colorScheme="red">Team</Button>
        <Button colorScheme="red">Log in</Button>
      </ButtonGroup>

      <Box display="block" mt={{ base: 4, md: 0 }}>
        <IconButton
          //bg="transparent"
          aria-label="toggle color mode"
          icon={colorMode === "light" ? "moon" : "sun"}
          onClick={toggleColorMode}
          color="white"
          bg={bgColor[colorMode]}
        />
      </Box>
    </Flex>
  );
};

export default Navbar;
