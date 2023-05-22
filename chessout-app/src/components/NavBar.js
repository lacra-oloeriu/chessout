import React from "react";
import { Box, Heading, Flex, Link,useColorMode , IconButton} from "@chakra-ui/core";

const MenuItems = ({ children }) => (
  <Link mt={{ base: 4, md: 0 }} mr={6} display="block">
    {children}
  </Link>
);

const Navbar = (props) => {
  const { colorMode, toggleColorMode } = useColorMode(); 
  return (
    <Flex
      as="nav"
      align="center"
      justify="space-between"
      wrap="wrap"
      padding="0.3rem"
      bg=
      //color="teal.300"
      //borderBottom="1px solid black"
      //{...props}
      {colorMode === "light" ? "gray.900" : "teal.500"}  
            color=
            {colorMode === "light" ? "teal.300" : "white"}  
            borderBottom="1px solid black"  
            {...props}  
    >
      <Flex align="center" mr={5}>
        <Heading as="h1" size="lg" letterSpacing={"-.1rem"}>
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
      <Box  
                display="block"  
                mt={{ base: 4, md: 0 }}  
            >  
                <IconButton  
                    bg="transparent"  
                    aria-label="toggle color mode"  
                    icon={colorMode === "light" ? "moon" : "sun"}  
                    onClick={toggleColorMode}  
                    color="white"
                 
                />  
            </Box>  
    </Flex>
  );
};

export default Navbar;
